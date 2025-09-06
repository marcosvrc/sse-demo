package br.com.sse.service;

import br.com.sse.model.SseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterService {
    private static final Logger logger = LoggerFactory.getLogger(SseEmitterService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitter.onCompletion(() -> {
            logger.info("Emitter completed");
            this.emitters.remove(emitter);
        });
        
        emitter.onTimeout(() -> {
            logger.info("Emitter timed out");
            emitter.complete();
            this.emitters.remove(emitter);
        });
        
        emitter.onError(e -> {
            logger.error("Error in SSE", e);
            emitter.completeWithError(e);
            this.emitters.remove(emitter);
        });

        try {
            // Enviar um evento inicial para estabelecer a conexão
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Conexão SSE estabelecida com sucesso!"));
        } catch (IOException e) {
            logger.error("Error sending initial event", e);
            emitter.completeWithError(e);
            return emitter;
        }

        this.emitters.add(emitter);
        logger.info("Emitter added, total emitters: {}", this.emitters.size());
        return emitter;
    }

    public void sendEventToAll(String message) {
        SseEvent event = new SseEvent(UUID.randomUUID().toString(), message);
        logger.info("Sending event to all clients: {}", event);
        
        List<SseEmitter> deadEmitters = new ArrayList<>();
        
        this.emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(event.getId())
                        .name("message")
                        .data(event));
            } catch (IOException e) {
                logger.error("Error sending event to emitter", e);
                deadEmitters.add(emitter);
            }
        });
        
        this.emitters.removeAll(deadEmitters);
    }
}
