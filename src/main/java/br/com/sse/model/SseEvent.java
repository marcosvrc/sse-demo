package br.com.sse.model;

import java.time.Instant;

/**
 * Evento enviado aos clientes SSE. O timestamp usa {@link Instant} (UTC, ISO-8601)
 * para que o navegador converta corretamente para o fuso local.
 */
public record SseEvent(String id, String message, Instant timestamp) {

    public SseEvent(String id, String message) {
        this(id, message, Instant.now());
    }
}
