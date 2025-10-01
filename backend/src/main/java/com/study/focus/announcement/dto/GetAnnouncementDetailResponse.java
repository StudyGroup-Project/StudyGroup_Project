package com.study.focus.announcement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Setter
@Getter
public class GetAnnouncementDetailResponse {
    Long announcementId;
    Long studyId;
    String title;
    String content;
    LocalDateTime updatedAt;
    String userName;
    String userProfileImageUrl;
    List<AnnouncementFiles> files;
    List<AnnouncementComments> comments;

}
