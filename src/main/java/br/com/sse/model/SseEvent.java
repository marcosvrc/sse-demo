package br.com.sse.model;

import java.time.LocalDateTime;

public class SseEvent {
    private String id;
    private String message;
    private LocalDateTime timestamp;

    public SseEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public SseEvent(String id, String message) {
        this.id = id;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SseEvent{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
