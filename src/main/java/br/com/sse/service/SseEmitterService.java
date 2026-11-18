package br.com.sse.service;

import br.com.sse.model.SseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterService {
    private static final Logger logger = LoggerFactory.getLogger(SseEmitterService.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final long emitterTimeoutMs;

    public SseEmitterService(@Value("${sse.emitter-timeout-ms:1800000}") long emitterTimeoutMs) {
        this.emitterTimeoutMs = emitterTimeoutMs;
    }

    public SseEmitter subscribe() {
        // Timeout finito: o navegador (EventSource) reconecta sozinho quando a conexão expira.
        SseEmitter emitter = new SseEmitter(emitterTimeoutMs);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            logger.debug("Emitter finalizado, total: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            logger.debug("Emitter expirou");
            emitter.complete();
        });
        emitter.onError(e -> {
            // O Spring finaliza o emitter após este callback; basta removê-lo.
            emitters.remove(emitter);
            logger.debug("Erro no emitter (cliente provavelmente desconectou): {}", e.getMessage());
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Conexão SSE estabelecida com sucesso!"));
        } catch (Exception e) {
            logger.warn("Falha ao enviar evento inicial: {}", e.getMessage());
            emitter.completeWithError(e);
            return emitter;
        }

        emitters.add(emitter);
        logger.info("Emitter adicionado, total: {}", emitters.size());
        return emitter;
    }

    public void sendEventToAll(String message) {
        SseEvent event = new SseEvent(UUID.randomUUID().toString(), message);
        logger.info("Enviando evento para {} cliente(s): {}", emitters.size(), event);
        broadcast(SseEmitter.event().id(event.id()).name("message").data(event));
    }

    /**
     * Envia um comentário SSE (ignorado pelo EventSource) para manter a conexão ativa
     * através de proxies e detectar clientes desconectados rapidamente.
     */
    public void sendHeartbeat() {
        broadcast(SseEmitter.event().comment("heartbeat"));
    }

    public int getActiveConnections() {
        return emitters.size();
    }

    private void broadcast(SseEmitter.SseEventBuilder event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (Exception e) {
                // IOException (cliente desconectou) ou IllegalStateException (emitter já finalizado)
                logger.debug("Removendo emitter inativo: {}", e.getMessage());
                emitters.remove(emitter);
            }
        }
    }
}
