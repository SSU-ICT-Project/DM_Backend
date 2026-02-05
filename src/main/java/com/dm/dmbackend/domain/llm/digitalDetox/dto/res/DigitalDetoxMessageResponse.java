package com.dm.dmbackend.domain.llm.digitalDetox.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DigitalDetoxMessageResponse {
    private Long id;
    private String message;
}
