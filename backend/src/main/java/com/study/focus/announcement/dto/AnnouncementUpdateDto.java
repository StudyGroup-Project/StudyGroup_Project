package com.study.focus.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementUpdateDto {

    @NotBlank
    private String title;
    @NotBlank
    private String content;

    // 새파일 첨부
    private List<MultipartFile> files;
    // 삭제할 파일 ID 목록
    private List<Long> deleteFileIds;


}
