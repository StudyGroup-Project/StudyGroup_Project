package com.study.focus.resource.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResourceDetailFileDto {
    private Long fileId;
    private String fileName;
    private String fileUrl;
}
