package com.dm.DM_Backend.domain.llm.screenTime.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenTimeReviewResponse {
    private Long id;
    private String review;
}
