package com.dm.DM_Backend.domain.llm.thesis.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThesisResponse {
    private Long id;
    private String thesisPdfUrl;
}
