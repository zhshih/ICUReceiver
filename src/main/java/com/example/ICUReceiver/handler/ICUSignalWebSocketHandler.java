package com.example.ICUReceiver.handler;

import com.example.ICUReceiver.model.ICUSignal;
import com.example.ICUReceiver.dto.ICUSignalDto;
import com.example.ICUReceiver.repository.ICURepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class ICUSignalWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessionList =
            new CopyOnWriteArrayList<>();

    @Autowired
    private ICURepository icuRepository;

    public void afterConnectionEstablished(WebSocketSession session) {
        sessionList.add(session);
        log.info("Connection established: {}", session.getId());
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionList.remove(session);
        log.info("Connection closed: {} with status {}", session.getId(), status);
    }

    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.info("Handle message payload: {}", payload);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ICUSignalDto icuSignalDto = objectMapper.readValue(
                    payload, ICUSignalDto.class);
            ICUSignal icuSignal = ICUSignal.builder()
                    .nationalId(icuSignalDto.getNationalId())
                    .heartbeat(icuSignalDto.getHeartbeat())
                    .pulse(icuSignalDto.getPulse())
                    .timestamp(icuSignalDto.getTimestamp())
                    .ecgList(icuSignalDto.getEcgList())
                    .build();

            icuRepository.save(icuSignal);

            log.info("ICUSignal saved successfully: {}", icuSignal);
        } catch (Exception e) {
            log.error("Parse payload failed where exception: {}", e.getMessage(), e);
            // Optionally send error response via WebSocket
            try {
                session.sendMessage(new TextMessage("Error: Invalid payload format"));
            } catch (IOException ioException) {
                log.error("Failed to send error message via WebSocket: {}", ioException.getMessage(), ioException);
            }
        }
    }

    List<WebSocketSession> getSessionList() {
        return sessionList;
    }
}
