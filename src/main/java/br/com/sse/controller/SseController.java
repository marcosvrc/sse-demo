package br.com.sse.controller;

import br.com.sse.service.SseEmitterService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    static final int MAX_MESSAGE_LENGTH = 500;

    private final SseEmitterService sseEmitterService;

    public SseController(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return sseEmitterService.subscribe();
    }

    // A validação dos parâmetros é feita pelo Spring MVC e retorna 400 em caso de violação.
    @PostMapping("/publish")
    public ResponseEntity<String> publish(
            @RequestParam @NotBlank @Size(max = MAX_MESSAGE_LENGTH) String message) {
        sseEmitterService.sendEventToAll(message.strip());
        return ResponseEntity.ok("Evento publicado com sucesso");
    }
}
