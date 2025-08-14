package com.dm.DM_Backend.global.common.response;

import lombok.Getter;

import java.util.List;

@Getter
public class DMPage<T> {
    private List<T> contents;

    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private long totalCount;

    public static <T> DMPage<T> of(org.springframework.data.domain.Page<T> pagedContents) {
        DMPage<T> converted = new DMPage<>();
        converted.contents = pagedContents.getContent();
        converted.pageNumber = pagedContents.getNumber();
        converted.pageSize = pagedContents.getSize();
        converted.totalPages = pagedContents.getTotalPages();
        converted.totalCount = pagedContents.getTotalElements();
        return converted;
    }
}
