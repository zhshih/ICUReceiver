package com.example.ICUReceiver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ICUSignalDto {
    private int nationalId;
    private double heartbeat;
    private double pulse;
    private LocalDateTime timestamp;
    private List<Double> ecgList;
}
