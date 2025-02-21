package com.ssafy.exhi.domain.tip.converter;

import com.ssafy.exhi.domain.tip.model.dto.TipRequest;
import com.ssafy.exhi.domain.tip.model.dto.TipResponse;
import com.ssafy.exhi.domain.tip.model.entity.Tip;

import java.util.List;
import java.util.stream.Collectors;

public class TipConverter {
    public static TipResponse.DetailResultDTO toDTO(Tip tip) {
        return TipResponse.DetailResultDTO.builder()
                .tipId(tip.getId())
                .serviceType(tip.getServiceType())
                .tipContent(tip.getTipContent())
                .build();
    }

    public static List<TipResponse.DetailResultDTO> toDTO(List<Tip> tips) {
        return tips.stream().map(TipConverter::toDTO).collect(Collectors.toList());
    }

    public static Tip toEntity(TipRequest.CreateDTO createDTO) {
        return Tip.builder()
                .serviceType(createDTO.getServiceType())
                .tipContent(createDTO.getTipContent())
                .build();
    }
}
