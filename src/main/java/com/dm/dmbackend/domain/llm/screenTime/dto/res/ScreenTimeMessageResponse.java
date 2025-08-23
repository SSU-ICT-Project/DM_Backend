package com.dm.dmbackend.domain.llm.screenTime.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenTimeMessageResponse {
    private Long id;
    private String message;
}
