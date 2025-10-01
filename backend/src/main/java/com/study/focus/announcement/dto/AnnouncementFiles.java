package com.study.focus.announcement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Builder
@Getter
@AllArgsConstructor
public class AnnouncementFiles {

    private  Long fileId;
    private String fileName;
    private String fileUrl;

}
