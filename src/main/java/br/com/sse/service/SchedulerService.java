package br.com.sse.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class SchedulerService {

    private final SseEmitterService sseEmitterService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public SchedulerService(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @Scheduled(fixedRate = 60000) // A cada 1 minuto
    public void sendPeriodicEvent() {
        String time = dateFormat.format(new Date());
        sseEmitterService.sendEventToAll("Evento automático enviado às " + time);
    }
}
