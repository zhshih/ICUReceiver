package com.example.ICUReceiver.mapper;

import com.example.ICUReceiver.dto.ICUSignalDto;
import com.example.ICUReceiver.model.ICUSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ICUSignalMapperTest {

    private ICUSignalMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ICUSignalMapper();
    }

    @Test
    void testToEntity() {
        ICUSignalDto dto = new ICUSignalDto(123, 80.0, 70.0,
                LocalDateTime.of(2025, 11, 9, 10, 0), Arrays.asList(0.1, 0.2, 0.3));

        ICUSignal entity = mapper.toEntity(dto);

        assertEquals(0, entity.getId()); // because you hardcoded 0
        assertEquals(dto.getNationalId(), entity.getNationalId());
        assertEquals(dto.getHeartbeat(), entity.getHeartbeat());
        assertEquals(dto.getPulse(), entity.getPulse());
        assertEquals(dto.getTimestamp(), entity.getTimestamp());
        assertEquals(dto.getEcgList(), entity.getEcgList());
    }

    @Test
    void testToDto() {
        ICUSignal entity = new ICUSignal(1, 123, 80.0, 70.0,
                LocalDateTime.of(2025, 11, 9, 10, 0), Arrays.asList(0.1, 0.2, 0.3));

        ICUSignalDto dto = mapper.toDto(entity);

        assertEquals(entity.getNationalId(), dto.getNationalId());
        assertEquals(entity.getHeartbeat(), dto.getHeartbeat());
        assertEquals(entity.getPulse(), dto.getPulse());
        assertEquals(entity.getTimestamp(), dto.getTimestamp());
        assertEquals(entity.getEcgList(), dto.getEcgList());
    }
}
