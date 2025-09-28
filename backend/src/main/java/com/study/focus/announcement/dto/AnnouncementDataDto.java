package com.study.focus.announcement.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class AnnouncementDataDto {

    @NonNull
    private String title;
    @NonNull
    private String content;

    private List<MultipartFile> files;


}
