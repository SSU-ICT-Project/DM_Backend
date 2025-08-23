package com.dm.dmbackend.domain.llm.screenTime.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenTimeReviewRequest {
    private String screenTimeData;
}
