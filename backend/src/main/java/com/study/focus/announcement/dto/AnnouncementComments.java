package com.study.focus.announcement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Builder
@Getter
public class AnnouncementComments {

    private Long commentId;
    private String userName;
    private String userProfileImageUrl;
    private String content;
    private LocalDateTime createdAt;

}
