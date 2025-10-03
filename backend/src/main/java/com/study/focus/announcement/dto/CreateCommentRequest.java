package com.study.focus.announcement.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateCommentRequest {
    String content;
}
