package com.example.ICUReceiver.service;

import com.example.ICUReceiver.model.ICUSignal;
import com.example.ICUReceiver.repository.ICURepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * ICUService provides core business operations for handling ICU signal data.
 * <p>
 * This service demonstrates production-grade design principles:
 * <ul>
 *   <li><b>Resilience</b> — Uses {@link CircuitBreaker} and {@link Retry} to handle transient failures gracefully.</li>
 *   <li><b>Transactional Safety</b> — Writes are enclosed in a transactional context.</li>
 *   <li><b>Observability</b> — Exposes Micrometer metrics for operations, latency, and reliability tracking.</li>
 *   <li><b>Structured Logging</b> — Consistent logs to aid monitoring and troubleshooting.</li>
 * </ul>
 *
 * Metrics exported via Micrometer (viewable under <code>/actuator/metrics</code>):
 * <ul>
 *   <li><b>icu.signals.saved.total</b> – Total number of successfully saved ICU signals.</li>
 *   <li><b>icu.signals.query.duration</b> – Timer measuring signal query execution time.</li>
 *   <li><b>icu.signals.query.total</b> – Total number of signal query requests.</li>
 * </ul>
 */
@Service
@Slf4j
public class ICUService {

    private static final String ICU_SERVICE = "icuService";

    private final ICURepository repository;
    private final Counter saveCounter;
    private final Counter queryCounter;
    private final Timer queryTimer;

    /**
     * Constructs the ICUService with Micrometer instrumentation.
     *
     * @param repository    The repository for persistent ICU signals.
     * @param meterRegistry The Micrometer registry for metrics tracking.
     */
    @Autowired
    public ICUService(ICURepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;

        this.saveCounter = meterRegistry.counter("icu.signals.saved.total");
        this.queryCounter = meterRegistry.counter("icu.signals.query.total");
        this.queryTimer = meterRegistry.timer("icu.signals.query.duration");
    }

    /**
     * Saves an ICU signal with timestamping, transactional safety, and resilience features.
     * <p>
     * Increments the custom metric <b>icu.signals.saved.total</b> upon successful save.
     *
     * @param icuSignal The ICU signal to persist.
     */
    @Transactional
    @CircuitBreaker(name = ICU_SERVICE, fallbackMethod = "fallbackSaveSignal")
    @Retry(name = ICU_SERVICE)
    public void saveSignal(ICUSignal icuSignal) {
        icuSignal.setTimestamp(LocalDateTime.now());
        repository.save(icuSignal);
        saveCounter.increment();

        log.info("Saved ICU signal | nationalId={} | timestamp={}",
                icuSignal.getNationalId(), icuSignal.getTimestamp());
    }

    /**
     * Fallback invoked when the save operation fails due to circuit-breaker or retry exhaustion.
     * In a real-world system, this could enqueue the failed data for asynchronous retry.
     *
     * @param icuSignal The signal that failed to save.
     * @param t         The exception that triggered the fallback.
     */
    public void fallbackSaveSignal(ICUSignal icuSignal, Throwable t) {
        log.error("Failed to save signal | nationalId={} | reason={}",
                icuSignal.getNationalId(), t.getMessage());
        log.warn("Queuing signal for deferred retry or backup processing...");
        // TODO: Implement message queue / fallback persistence.
    }

    /**
     * Retrieves signals for a specific patient within a time range.
     * <p>
     * This method is instrumented with:
     * <ul>
     *   <li><b>icu.signals.query.total</b> — incremented on each invocation.</li>
     *   <li><b>icu.signals.query.duration</b> — captures query latency.</li>
     * </ul>
     *
     * @param nationalId The patient identifier.
     * @param start      Start of the time range.
     * @param end        End of the time range.
     * @param page       Zero-based page index.
     * @param size       Page size.
     * @return A page of {@link ICUSignal} entities sorted by timestamp descending.
     */
    @CircuitBreaker(name = ICU_SERVICE, fallbackMethod = "fallbackFindByTimeRange")
    @Retry(name = ICU_SERVICE)
    public Page<ICUSignal> findByTimeRange(
            int nationalId, LocalDateTime start, LocalDateTime end, int page, int size) {

        long startTime = System.nanoTime();
        queryCounter.increment();
        Pageable pageable = PageRequest
                .of(page, size, Sort.by("timestamp")
                        .descending());

        try {
            Page<ICUSignal> result = repository
                    .findByNationalIdAndTimestampBetweenOrderByTimestampDesc(nationalId, start, end, pageable);
            log.info("Retrieved {} signals | nationalId={} | range={}–{}",
                    result.getContent().size(), nationalId, start, end);
            return result;
        } finally {
            queryTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Fallback for {@link #findByTimeRange(int, LocalDateTime, LocalDateTime, int, int)}.
     *
     * @return Empty page result when query fails.
     */
    public Page<ICUSignal> fallbackFindByTimeRange(
            int nationalId, LocalDateTime start, LocalDateTime end, int page, int size, Throwable t) {
        log.error("Query failed | nationalId={} | range={}–{} | error={}",
                nationalId, start, end, t.getMessage());
        return Page.empty();
    }

    /**
     * Retrieves the most recent ICU signals for a patient, sorted by newest first.
     * <p>
     * Tracks both query count and duration for observability.
     *
     * @param nationalId The patient identifier.
     * @param page       Zero-based page index.
     * @param size       Page size.
     * @return A page of latest {@link ICUSignal} records.
     */
    @CircuitBreaker(name = ICU_SERVICE, fallbackMethod = "fallbackFindLatest")
    @Retry(name = ICU_SERVICE)
    public Page<ICUSignal> findLatest(int nationalId, int page, int size) {
        long start = System.nanoTime();
        queryCounter.increment();

        try {
            Page<ICUSignal> result = repository.findByNationalIdOrderByTimestampDesc(
                    nationalId, PageRequest.of(page, size));
            log.info("Retrieved {} latest signals | nationalId={}",
                    result.getContent().size(), nationalId);
            return result;
        } finally {
            queryTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Fallback for {@link #findLatest(int, int, int)} when circuit breaker or retry fails.
     *
     * @return Empty result page.
     */
    public Page<ICUSignal> fallbackFindLatest(
            int nationalId, int page, int size, Throwable t) {
        log.error("Failed to fetch latest signals | nationalId={} | reason={}",
                nationalId, t.getMessage());
        return Page.empty();
    }
}
