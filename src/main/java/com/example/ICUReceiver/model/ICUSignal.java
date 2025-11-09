package com.example.ICUReceiver.model;

import com.example.ICUReceiver.dto.ICUSignalDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ICUSignal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int nationalId;
    private double heartbeat;
    private double pulse;
    private LocalDateTime timestamp;

    @ElementCollection
    List<Double> ecgList;

    public static ICUSignal fromPayload(ICUSignalDto signalDto) {
        return ICUSignal.builder()
                .nationalId(signalDto.getNationalId())
                .heartbeat(signalDto.getHeartbeat())
                .pulse(signalDto.getPulse())
                .ecgList(signalDto.getEcgList())
                .timestamp(signalDto.getTimestamp())
                .build();
    }
}
