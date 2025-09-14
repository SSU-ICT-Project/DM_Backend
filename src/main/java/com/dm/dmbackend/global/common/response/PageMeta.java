package com.dm.dmbackend.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldNameConstants
public class PageMeta {
    private final int page;           // 0-based index
    private final int size;
    private final int totalPages;
    private final long totalElements;
    private final boolean first;
    private final boolean last;
    private final boolean hasNext;

    public static PageMeta of(Page<?> p) {
        return new PageMeta(
                p.getNumber(),
                p.getSize(),
                p.getTotalPages(),
                p.getTotalElements(),
                p.isFirst(),
                p.isLast(),
                p.hasNext()
        );
    }
}
