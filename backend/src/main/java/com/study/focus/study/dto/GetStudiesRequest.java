package com.study.focus.study.dto;

import com.study.focus.study.domain.StudySortType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetStudiesRequest {
    private Integer page = 1;
    private Integer limit = 10;

    public int getPageOrDefault() {
        return (page != null && page > 0) ? page - 1 : 0;
    }

    public int getLimitOrDefault() {
        return (limit != null && limit > 0) ? limit : 10;
    }
}
