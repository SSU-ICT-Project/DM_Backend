package com.dm.dmbackend.domain.llm.thesis.dto.req;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThesisDeleteRequest {
    @Column(columnDefinition = "jsonb") // PostgreSQL의 jsonb 타입 사용
    private List<Long> thesisIdList;
}
