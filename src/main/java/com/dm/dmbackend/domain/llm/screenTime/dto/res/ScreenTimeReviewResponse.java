package com.dm.dmbackend.domain.llm.screenTime.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenTimeReviewResponse {
    private Long id;
    private String review;
}
