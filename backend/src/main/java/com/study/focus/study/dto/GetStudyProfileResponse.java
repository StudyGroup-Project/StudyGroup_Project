package com.study.focus.study.dto;


import com.study.focus.common.domain.Category;
import com.study.focus.study.domain.RecruitStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetStudyProfileResponse {

    private Long id; // 그룹(스터디) ID
    private String title; // 그룹명
    private int maxMemberCount; // 모집 인원
    private int memberCount; // 현재 인원
    private String bio; // 짧은 소개
    private String description; // 상세 소개
    private List<Category> category;; // 카테고리
    private String province; // 시/도
    private String district; // 시/군/구
    private RecruitStatus recruitStatus; // 모집 상태 (OPEN, CLOSED 등)
    private int trustScore; // 신뢰 점수

    private String applicationStatus; // 지원서 상태 (SUBMITTED, ACCEPTED, REJECTED) - 없으면 null 또는 "NONE"
    private boolean canApply; // 지원 가능 여부 (추방, 마감, 이미 지원 등 모두 포함)
    private boolean leaderCheck; // 리더 인지 확인
    private LeaderProfile leader; // 그룹장 프로필

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LeaderProfile {
        private Long id;
        private String nickname;
        private String profileImageUrl;
    }
}
