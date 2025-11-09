package com.example.ICUReceiver.controller;

import com.example.ICUReceiver.dto.ApiResponse;
import com.example.ICUReceiver.dto.ICUSignalDto;
import com.example.ICUReceiver.mapper.ICUSignalMapper;
import com.example.ICUReceiver.model.ICUSignal;
import com.example.ICUReceiver.service.ICUService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for handling ICU (Intensive Care Unit) signal data operations.
 * <p>
 * This controller provides endpoints for uploading ICU signal data, retrieving data
 * within a specified time range, and fetching the latest ICU signal records for a given patient.
 * </p>
 *
 * <p>Base URL: <b>/api/v1/icu</b></p>
 *
 * <p>All responses are wrapped in an {@link ApiResponse} object.</p>
 *
 */
@RestController
@RequestMapping("/api/v1/icu")
@Slf4j
public class ICUController {

    @Autowired
    private ICUService service;

    @Autowired
    private ICUSignalMapper mapper;

    /**
     * Receives and stores ICU signal data from a client.
     * <p>
     * This endpoint accepts a JSON payload representing an ICU signal and saves it
     * to the database after mapping it to an {@link ICUSignal} entity.
     * </p>
     *
     * @param dto the ICU signal data transfer object containing signal information.
     * @return a {@link ResponseEntity} containing a success message and a null body
     *         wrapped inside an {@link ApiResponse}.
     *
     * <p><b>Example request:</b></p>
     * <pre>
     * POST /api/v1/icu/upload
     * {
     *   "nationalId": 123456,
     *   "timestamp": "2025-11-09T14:30:00",
     *   "heartbeat": 85,
     *   "pulse": 97
     * }
     * </pre>
     *
     * <p><b>Response:</b></p>
     * <pre>
     * {
     *   "message": "Data received successfully",
     *   "data": null
     * }
     * </pre>
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Void>> receive(@RequestBody ICUSignalDto dto) {
        ICUSignal entity = mapper.toEntity(dto);
        service.saveSignal(entity);
        log.info("Received dto {}", dto);
        return ResponseEntity.ok(ApiResponse.success("Data received successfully", null));
    }

    /**
     * Retrieves ICU signal data for a specific patient within a given time range.
     * <p>
     * This endpoint supports pagination and filters ICU signal records by patient national ID
     * and a start-end timestamp range.
     * </p>
     *
     * @param nationalId the patient’s national ID used to filter the ICU signals.
     * @param start      the start timestamp for the query (inclusive).
     * @param end        the end timestamp for the query (inclusive).
     * @param page       the page index (zero-based) for paginated results. Defaults to 0.
     * @param size       the page size (number of records per page). Defaults to 50.
     * @return a paginated {@link ResponseEntity} containing ICU signal DTOs wrapped in an {@link ApiResponse}.
     *
     * <p><b>Example request:</b></p>
     * <pre>
     * GET /api/v1/icu/range/123456?start=2025-11-09T00:00:00&end=2025-11-09T23:59:59&page=0&size=10
     * </pre>
     *
     * <p><b>Response:</b></p>
     * <pre>
     * {
     *   "message": "Fetched ICU signals in time range",
     *   "data": {
     *     "content": [
     *       {"timestamp": "2025-11-09T14:30:00", "heartbeat": 85, "pulse": 97},
     *       ...,
     *     ],
     *     "pageable": {...},
     *     "totalElements": 100
     *   }
     * }
     * </pre>
     */
    @GetMapping("/range/{nationalId}")
    public ResponseEntity<ApiResponse<Page<ICUSignalDto>>> findByTimeRange(
            @PathVariable int nationalId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Page<ICUSignal> result = service.findByTimeRange(
                nationalId,start, end, page, size);
        Page<ICUSignalDto> dtoPage = result.map(mapper::toDto);
        return ResponseEntity.ok(ApiResponse.success(
                "Fetched ICU signals in time range", dtoPage));
    }

    /**
     * Retrieves the latest ICU signal data for a given patient.
     * <p>
     * This endpoint returns the most recent ICU signal records for the specified national ID,
     * ordered by timestamp, and supports pagination.
     * </p>
     *
     * @param nationalId the patient’s national ID used to identify ICU signal records.
     * @param page       the page index (zero-based) for pagination. Defaults to 0.
     * @param size       the number of records per page. Defaults to 50.
     * @return a paginated {@link ResponseEntity} containing the latest ICU signal DTOs
     *         wrapped in an {@link ApiResponse}.
     *
     * <p><b>Example request:</b></p>
     * <pre>
     * GET /api/v1/icu/latest/123456?page=0&size=20
     * </pre>
     *
     * <p><b>Response:</b></p>
     * <pre>
     * {
     *   "message": "Fetched latest ICU signals",
     *   "data": {
     *     "content": [
     *       {"timestamp": "2025-11-09T14:45:00", "heartbeat": 87, "pulse": 96},
     *       ...,
     *     ]
     *   }
     * }
     * </pre>
     */
    @GetMapping("/latest/{nationalId}")
    public ResponseEntity<ApiResponse<Page<ICUSignalDto>>> findLatest(
            @PathVariable int nationalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Page<ICUSignal> result = service.findLatest(nationalId, page, size);
        Page<ICUSignalDto> dtoPage = result.map(mapper::toDto);
        return ResponseEntity.ok(ApiResponse.success(
                "Fetched latest ICU signals", dtoPage));
    }
}
