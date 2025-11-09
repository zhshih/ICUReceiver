package com.example.ICUReceiver.mapper;

import com.example.ICUReceiver.dto.ICUSignalDto;
import com.example.ICUReceiver.model.ICUSignal;
import org.springframework.stereotype.Component;

@Component
public class ICUSignalMapper {

    public ICUSignal toEntity(ICUSignalDto dto) {
        return new ICUSignal(
                0,
                dto.getNationalId(),
                dto.getHeartbeat(),
                dto.getPulse(),
                dto.getTimestamp(),
                dto.getEcgList()
        );
    }

    public ICUSignalDto toDto(ICUSignal entity) {
        return new ICUSignalDto(
                entity.getNationalId(),
                entity.getHeartbeat(),
                entity.getPulse(),
                entity.getTimestamp(),
                entity.getEcgList()
        );
    }
}
