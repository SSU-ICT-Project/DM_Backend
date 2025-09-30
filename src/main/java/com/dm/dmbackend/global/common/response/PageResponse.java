package com.dm.dmbackend.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    private List<T> content;
    private PageMeta pageMeta;

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                PageMeta.of(page)
        );
    }

    public static <T> PageResponse<T> of(List<T> content) {
        return new PageResponse<>(
                content,
                PageMeta.empty() // PageMeta에 "빈" 값 넣는 팩토리 메서드 필요
        );
    }
}
