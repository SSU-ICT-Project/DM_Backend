package com.dm.dmbackend.domain.llm.digitalDetox.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserContextDto {
    private final String motivationPrompt;
    private final String userData;
    private final String goalSummary;
}

