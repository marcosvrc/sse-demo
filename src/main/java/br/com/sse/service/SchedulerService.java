package br.com.sse.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class SchedulerService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final SseEmitterService sseEmitterService;

    public SchedulerService(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @Scheduled(fixedRate = 60000) // A cada 1 minuto
    public void sendPeriodicEvent() {
        sseEmitterService.sendEventToAll("Evento automático enviado às " + LocalTime.now().format(TIME_FORMAT));
    }

    @Scheduled(fixedRateString = "${sse.heartbeat-interval-ms:20000}")
    public void sendHeartbeat() {
        sseEmitterService.sendHeartbeat();
    }
}
