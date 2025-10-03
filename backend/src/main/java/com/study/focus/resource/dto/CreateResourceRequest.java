package com.study.focus.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateResourceRequest {
    @NotBlank(message = "제목은 필수 입력 값입니다.")
    @Size(max = 100)
    private String title;

    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;

    List<MultipartFile> files;
}
