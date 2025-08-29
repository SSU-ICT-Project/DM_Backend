package com.dm.dmbackend.domain.schedule.schedule.entity;

import lombok.Data;
import lombok.Getter;

@Data
public class SchedulePage {
    // 기본 page, size
    private int page = 0;
    private int size = 31;
    @Getter
    private static final int maxPageSize = 31;
}
