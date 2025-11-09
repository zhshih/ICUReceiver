package com.example.ICUReceiver.repository;

import com.example.ICUReceiver.model.ICUSignal;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ICURepository extends JpaRepository<ICUSignal, Integer> {
    @Timed(
            value = "repository.icuSignal.query",
            extraTags = {"method", "findTop50ByNationalId"},
            description = "Time to find top 50 ICUSignal entries by national ID"
    )
    List<ICUSignal> findTop50ByNationalIdOrderByTimestampDesc(int nationalId);

    @Timed(
            value = "repository.icuSignal.query",
            extraTags = {"method", "findByNationalId"},
            description = "Time to find ICUSignal entries by national ID with pagination"
    )
    Page<ICUSignal> findByNationalIdOrderByTimestampDesc(int nationalId, Pageable pageable);

    @Timed(
            value = "repository.icuSignal.query",
            extraTags = {"method", "findByNationalIdAndTimestampBetween"},
            description = "Time to find ICUSignal entries by national ID and timestamp range"
    )
    Page<ICUSignal> findByNationalIdAndTimestampBetweenOrderByTimestampDesc(
            int nationalId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}
