package com.example.ICUReceiver.contoller;

import com.example.ICUReceiver.controller.ICUController;
import com.example.ICUReceiver.dto.ApiResponse;
import com.example.ICUReceiver.dto.ICUSignalDto;
import com.example.ICUReceiver.mapper.ICUSignalMapper;
import com.example.ICUReceiver.model.ICUSignal;
import com.example.ICUReceiver.service.ICUService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ICUControllerTest {

    @Mock
    private ICUService service;

    @Mock
    private ICUSignalMapper mapper;

    @InjectMocks
    private ICUController controller;

    @Test
    void testReceive_callsServiceAndReturnsSuccess() {
        ICUSignalDto dto = new ICUSignalDto();
        ICUSignal entity = new ICUSignal();
        when(mapper.toEntity(dto)).thenReturn(entity);

        ResponseEntity<ApiResponse<Void>> response = controller.receive(dto);

        verify(mapper).toEntity(dto);
        verify(service).saveSignal(entity);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Data received successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void testFindByTimeRange_callsServiceAndMapper() {
        int nationalId = 1;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        Page<ICUSignal> servicePage = new PageImpl<>(List.of(new ICUSignal()));

        when(service.findByTimeRange(nationalId, start, end, 0, 50)).thenReturn(servicePage);
        when(mapper.toDto(any(ICUSignal.class))).thenReturn(new ICUSignalDto());

        ResponseEntity<ApiResponse<Page<ICUSignalDto>>> response =
                controller.findByTimeRange(nationalId, start, end, 0, 50);

        verify(service).findByTimeRange(nationalId, start, end, 0, 50);
        verify(mapper, times(servicePage.getContent().size())).toDto(any(ICUSignal.class));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Fetched ICU signals in time range", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(servicePage.getContent().size(), response.getBody().getData().getContent().size());
    }

    @Test
    void testFindLatest_callsServiceAndMapper() {
        int nationalId = 1;
        Page<ICUSignal> servicePage = new PageImpl<>(List.of(new ICUSignal()));
        when(service.findLatest(nationalId, 0, 50)).thenReturn(servicePage);
        when(mapper.toDto(any(ICUSignal.class))).thenReturn(new ICUSignalDto());

        ResponseEntity<ApiResponse<Page<ICUSignalDto>>> response =
                controller.findLatest(nationalId, 0, 50);

        verify(service).findLatest(nationalId, 0, 50);
        verify(mapper, times(servicePage.getContent().size())).toDto(any(ICUSignal.class));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Fetched latest ICU signals", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(servicePage.getContent().size(), response.getBody().getData().getContent().size());
    }
}
