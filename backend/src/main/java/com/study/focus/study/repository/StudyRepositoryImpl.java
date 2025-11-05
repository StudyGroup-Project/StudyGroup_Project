package com.study.focus.study.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.focus.account.domain.QUser;
import com.study.focus.common.domain.Category;
import com.study.focus.common.dto.StudyDto;
import com.study.focus.study.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StudyRepositoryImpl implements StudyRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final String DELIMITER = "_"; // CategoryListConverter와 동일한 구분자

    @Override
    public Page<StudyDto> searchStudies(String keyword,
                                        List<Category> categories,
                                        String province,
                                        String district,
                                        Long userId,
                                        StudySortType sortType,
                                        Pageable pageable) {
        QStudy study = QStudy.study;
        QStudyProfile profile = QStudyProfile.studyProfile;
        QStudyMember member = QStudyMember.studyMember;
        QBookmark bookmark = QBookmark.bookmark;
        QUser leaderUser = new QUser("leaderUser");
        QStudyMember leaderMember = new QStudyMember("leaderMember");

        BooleanBuilder where = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            where.and(profile.title.containsIgnoreCase(keyword)
                    .or(profile.bio.containsIgnoreCase(keyword)));
        }

        // 1) DB의 실제 문자열 컬럼을 직접 Path로 잡기 (ListPath 피하기)
        StringPath categoryPath = Expressions.stringPath("category");

        // 2) '_{category}_' 형태로 패딩
        StringExpression padded = Expressions.stringTemplate("concat('_', {0}, '_')", categoryPath);

        // 3) 단일 카테고리 포함 검사: %_IT_% 와 매칭
        if (categories != null && !categories.isEmpty()) {
            String categoryName = categories.get(0).name();
            where.and(padded.like("%_" + categoryName + "_%"));
        }

        if (province != null && !province.isBlank()) {
            where.and(profile.address.province.eq(province));
        }
        if (district != null && !district.isBlank()) {
            where.and(profile.address.district.eq(district));
        }

        // content 조회 (방장 trustScore 기준)
        List<StudyDto> content = queryFactory
                .select(Projections.constructor(StudyDto.class,
                        study.id,
                        profile.title,
                        study.maxMemberCount,
                        member.id.countDistinct().intValue(),
                        bookmark.id.countDistinct(),
                        profile.bio,
                        profile.category,
                        leaderUser.trustScore,
                        JPAExpressions.selectOne()
                                .from(bookmark)
                                .where(bookmark.study.eq(study)
                                        .and(bookmark.user.id.eq(userId)))
                                .exists()
                ))
                .from(study)
                .join(profile).on(profile.study.eq(study))
                .leftJoin(member).on(member.study.eq(study))
                .leftJoin(bookmark).on(bookmark.study.eq(study))
                .join(leaderMember).on(leaderMember.study.eq(study)
                        .and(leaderMember.role.eq(StudyRole.LEADER)))
                .join(leaderMember.user, leaderUser)
                .where(where)
                .groupBy(study.id, profile.title, study.maxMemberCount,
                        profile.bio, profile.category, leaderUser.trustScore)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifier(sortType, study, leaderUser))
                .fetch();

        Long total = queryFactory
                .select(study.countDistinct())
                .from(study)
                .join(profile).on(profile.study.eq(study))
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private OrderSpecifier<?> toOrderSpecifier(StudySortType sortType,
                                               QStudy study,
                                               QUser user) {
        return switch (sortType) {
            case LATEST -> study.createdAt.desc();
            case TRUST_SCORE_DESC -> user.trustScore.desc(); // 방장 trustScore 기준 정렬
        };
    }
}
