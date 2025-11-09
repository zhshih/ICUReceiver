package com.example.ICUReceiver.service;

import com.example.ICUReceiver.model.ICUSignal;
import com.example.ICUReceiver.repository.ICURepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ICUServiceTest {

    @Mock
    private ICURepository repository;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter saveCounter;

    @Mock
    private Counter queryCounter;

    @Mock
    private Timer queryTimer;

    private ICUService icuService;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("icu.signals.saved.total")).thenReturn(saveCounter);
        when(meterRegistry.counter("icu.signals.query.total")).thenReturn(queryCounter);
        when(meterRegistry.timer("icu.signals.query.duration")).thenReturn(queryTimer);
        icuService = new ICUService(repository, meterRegistry);
    }

    @Test
    void testSaveSignal_callsRepositoryAndIncrementsCounter() {
        ICUSignal signal = new ICUSignal();
        signal.setNationalId(1);

        icuService.saveSignal(signal);

        verify(repository, times(1)).save(signal);
        verify(saveCounter, times(1)).increment();
        assertNotNull(signal.getTimestamp()); // timestamp should be set
    }

    @Test
    void testFindByTimeRange_callsRepositoryAndRecordsTimer() {
        int nationalId = 1;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        Page<ICUSignal> pageResult = new PageImpl<>(List.of(new ICUSignal()));

        when(repository.findByNationalIdAndTimestampBetweenOrderByTimestampDesc(
                eq(nationalId), eq(start), eq(end), any(Pageable.class)))
                .thenReturn(pageResult);

        Page<ICUSignal> result = icuService.findByTimeRange(nationalId, start, end, 0, 10);

        assertEquals(1, result.getContent().size());
        verify(queryCounter, times(1)).increment();
        verify(queryTimer, times(1)).record(anyLong(), eq(TimeUnit.NANOSECONDS));
    }

    @Test
    void testFindLatest_callsRepositoryAndRecordsTimer() {
        int nationalId = 1;
        Page<ICUSignal> pageResult = new PageImpl<>(List.of(new ICUSignal()));

        when(repository.findByNationalIdOrderByTimestampDesc(eq(nationalId), any(Pageable.class)))
                .thenReturn(pageResult);

        Page<ICUSignal> result = icuService.findLatest(nationalId, 0, 5);

        assertEquals(1, result.getContent().size());
        verify(queryCounter, times(1)).increment();
        verify(queryTimer, times(1)).record(anyLong(), eq(TimeUnit.NANOSECONDS));
    }

    @Test
    void testFallbackSaveSignal_logsError() {
        ICUSignal signal = new ICUSignal();
        Throwable ex = new RuntimeException("fail");

        // just call fallback, check no exception thrown
        icuService.fallbackSaveSignal(signal, ex);
    }

    @Test
    void testFallbackFindByTimeRange_returnsEmptyPage() {
        Page<ICUSignal> result = icuService.fallbackFindByTimeRange(1,
                LocalDateTime.now(), LocalDateTime.now(), 0, 10, new RuntimeException());
        assertTrue(result.isEmpty());
    }

    @Test
    void testFallbackFindLatest_returnsEmptyPage() {
        Page<ICUSignal> result = icuService.fallbackFindLatest(1, 0, 10, new RuntimeException());
        assertTrue(result.isEmpty());
    }
}
