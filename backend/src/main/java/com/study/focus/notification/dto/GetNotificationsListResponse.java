package com.study.focus.notification.dto;

import com.study.focus.notification.domain.AudienceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GetNotificationsListResponse {
    Long id;
    String title;
    AudienceType audienceType;
}
