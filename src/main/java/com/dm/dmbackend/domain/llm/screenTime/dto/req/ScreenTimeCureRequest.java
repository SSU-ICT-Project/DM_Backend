package com.dm.dmbackend.domain.llm.screenTime.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenTimeCureRequest {
    private String userId;
    private String date;
    private List<AppUsage> appUsages;
    private int totalScreenTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppUsage {
        private String packageName;
        private String appName;
        private int usageTimeMinutes;
        private String lastUsed;
    }
}
