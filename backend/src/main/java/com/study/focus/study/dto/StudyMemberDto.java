package com.study.focus.study.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudyMemberDto {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private LocalDateTime lastLoginAt;
}
