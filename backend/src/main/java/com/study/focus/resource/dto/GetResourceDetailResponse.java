package com.study.focus.resource.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.swing.text.StringContent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class GetResourceDetailResponse {
    private String title;
    private String content;
    private String author;
    private String profileUrl;
    private LocalDateTime createdAt;
    private List<ResourceDetailFileDto> files;
}
