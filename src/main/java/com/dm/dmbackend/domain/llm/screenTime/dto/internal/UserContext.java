package com.dm.dmbackend.domain.llm.screenTime.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserContext {
    private final String motivationPrompt;
    private final String userData;
    private final String goalSummary;
}

