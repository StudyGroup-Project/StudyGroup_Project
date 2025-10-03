package com.study.focus.study.domain;

import lombok.Getter;

@Getter
public enum StudySortType {
    LATEST,            // 최신순
    TRUST_SCORE_DESC;  // 신뢰 점수 내림차순
}
