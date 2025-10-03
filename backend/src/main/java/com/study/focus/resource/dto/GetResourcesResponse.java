package com.study.focus.resource.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class GetResourcesResponse {

    private Long resourceId;
    private String title;
    private String userName;
    private LocalDateTime createdAt;


}
