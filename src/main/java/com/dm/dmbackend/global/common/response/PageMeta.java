package com.dm.dmbackend.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldNameConstants
public class PageMeta {
    private int page;           // 0-based index
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private boolean hasNext;

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

    public static PageMeta empty() {
        return new PageMeta(
                0, 0, 0, 0,
                true,  // first
                true,  // last
                false  // hasNext
        );
    }
}
