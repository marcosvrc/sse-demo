package br.com.sse.controller;

import br.com.sse.service.SseEmitterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final SseEmitterService sseEmitterService;

    public SseController(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return sseEmitterService.subscribe();
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publish(@RequestParam String message) {
        sseEmitterService.sendEventToAll(message);
        return ResponseEntity.ok("Evento publicado com sucesso");
    }
}
