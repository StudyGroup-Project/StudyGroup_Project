package com.study.focus.resource.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResourceRequest {
    @NotBlank(message = "제목은 필수 입력 값입니다.")
    @Size(max = 100)
    private String title;

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    private String content;

    // 새파일 첨부
    private List<MultipartFile> files;
    // 삭제할 파일 ID 목록
    private List<Long> deleteFileIds;

}
