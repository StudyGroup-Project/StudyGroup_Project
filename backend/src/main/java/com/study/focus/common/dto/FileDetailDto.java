package com.study.focus.common.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDetailDto {

    private  String originalFileName;
    private String key;
    private String  contentType;
    private long fileSize;



}
