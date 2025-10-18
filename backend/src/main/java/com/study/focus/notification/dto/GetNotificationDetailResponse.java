package com.study.focus.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GetNotificationDetailResponse {
    String title;
    String description;
    LocalDateTime createAt;
}
