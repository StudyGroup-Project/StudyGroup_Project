package com.study.focus.announcement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GetAnnouncementsResponse {
    private  Long announcementId;
    private  String title;
    private LocalDateTime createdAt;
}
