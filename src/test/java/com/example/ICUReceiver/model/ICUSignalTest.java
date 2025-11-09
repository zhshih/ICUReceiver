package com.example.ICUReceiver.model;

import com.example.ICUReceiver.dto.ICUSignalDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ICUSignalTest {

    @Test
    void testFromPayload() {
        ICUSignalDto dto = new ICUSignalDto();
        dto.setNationalId(123);
        dto.setHeartbeat(80.0);
        dto.setPulse(70.0);
        dto.setEcgList(Arrays.asList(0.1, 0.2, 0.3));
        dto.setTimestamp(LocalDateTime.of(2025, 11, 9, 10, 0));

        ICUSignal signal = ICUSignal.fromPayload(dto);

        assertEquals(123, signal.getNationalId());
        assertEquals(80.0, signal.getHeartbeat());
        assertEquals(70.0, signal.getPulse());
        assertEquals(Arrays.asList(0.1, 0.2, 0.3), signal.getEcgList());
        assertEquals(LocalDateTime.of(2025, 11, 9, 10, 0), signal.getTimestamp());
    }
}
