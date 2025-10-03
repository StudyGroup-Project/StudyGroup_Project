package com.study.focus.study.dto;

import com.study.focus.common.domain.Category;
import com.study.focus.study.domain.StudySortType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchStudiesRequest {
    private String keyword;
    private Category category;
    private String province;
    private String district;
    private Integer page = 1;
    private Integer limit = 10;
    private StudySortType sort = StudySortType.LATEST; // enum 그대로 사용

    public int getPageOrDefault() {
        return (page != null && page > 0) ? page - 1 : 0;
    }

    public int getLimitOrDefault() {
        return (limit != null && limit > 0) ? limit : 10;
    }
}
