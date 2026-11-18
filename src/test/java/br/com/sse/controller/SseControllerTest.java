package br.com.sse.controller;

import br.com.sse.service.SseEmitterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Test
    void subscribeReceivesInitAndPublishedEvent() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/sse/subscribe").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(post("/api/sse/publish").param("message", "olá <b>mundo</b>"))
                .andExpect(status().isOk());

        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(result.getResponse().getContentType()).startsWith(MediaType.TEXT_EVENT_STREAM_VALUE);
        assertThat(body).contains("event:INIT");
        assertThat(body).contains("event:message");
        assertThat(body).contains("\"message\":\"olá <b>mundo</b>\"");
        assertThat(body).containsPattern("\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T[^\"]+Z\"");
    }

    @Test
    void heartbeatIsSentAsComment() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/sse/subscribe"))
                .andExpect(request().asyncStarted())
                .andReturn();

        sseEmitterService.sendHeartbeat();

        assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).contains(":heartbeat");
    }

    @Test
    void publishRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/sse/publish").param("message", "   "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishRejectsTooLongMessage() throws Exception {
        mockMvc.perform(post("/api/sse/publish").param("message", "a".repeat(SseController.MAX_MESSAGE_LENGTH + 1)))
                .andExpect(status().isBadRequest());
    }
}
