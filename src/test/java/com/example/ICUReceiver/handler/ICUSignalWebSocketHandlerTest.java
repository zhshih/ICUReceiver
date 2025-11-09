package com.example.ICUReceiver.handler;

import com.example.ICUReceiver.model.ICUSignal;
import com.example.ICUReceiver.repository.ICURepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ICUSignalWebSocketHandlerTest {

    @InjectMocks
    private ICUSignalWebSocketHandler handler;

    @Mock
    private ICURepository icuRepository;

    @Mock
    private WebSocketSession session;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        lenient().when(session.getId()).thenReturn("123");
        lenient().doNothing().when(session).sendMessage(any());
    }

    @Test
    void testAfterConnectionEstablished() {
        when(session.getId()).thenReturn("123");
        handler.afterConnectionEstablished(session);

        assertTrue(handler.getSessionList().contains(session));
    }

    @Test
    void testAfterConnectionClosed() {
        when(session.getId()).thenReturn("123");
        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        assertFalse(handler.getSessionList().contains(session));
    }

    @Test
    void testHandleTextMessage_validPayload() throws Exception {
        String payload = """
                {
                    "nationalId": 1,
                    "heartbeat": 80.0,
                    "pulse": 70.0,
                    "timestamp": "2025-11-09T10:00:00",
                    "ecgList": [0.1, 0.2]
                }
                """;

        TextMessage message = new TextMessage(payload);

        handler.handleTextMessage(session, message);

        verify(icuRepository, times(1)).save(any(ICUSignal.class));
    }

    @Test
    void testHandleTextMessage_invalidPayload() throws Exception {
        doNothing().when(session).sendMessage(any());

        String payload = "{ invalid json }"; // invalid JSON
        TextMessage message = new TextMessage(payload);

        handler.handleTextMessage(session, message);

        verify(icuRepository, never()).save(any());

        verify(session, times(1)).sendMessage(any());
    }
}
