package com.study.focus.study.repository;

import com.study.focus.study.domain.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember,Long> {
    Optional<StudyMember> findByStudyIdAndUserId(Long study_id, Long user_id);
    
    Optional<StudyMember> findByStudyIdAndRole(Long studyId, StudyRole role);

    long countByStudyIdAndStatus(Long studyId, StudyMemberStatus status);

    boolean existsByStudyIdAndUserIdAndStatus(Long studyId, Long userId, StudyMemberStatus status);

    Optional<StudyMember> findByStudyIdAndUserIdAndStatus(Long studyId,
                                                            Long userId,
                                                            StudyMemberStatus status);

    /**
     * 방장의 trustScore 기준 내림차순 상위 10개 스터디 프로필 조회
     */
    @Query("""
        SELECT sp
        FROM StudyMember sm
        JOIN sm.user u
        JOIN Study s ON sm.study = s
        JOIN StudyProfile sp ON sp.study = s
        WHERE sm.role = com.study.focus.study.domain.StudyRole.LEADER
          AND sm.status = com.study.focus.study.domain.StudyMemberStatus.JOINED
        ORDER BY u.trustScore DESC
        """)
    List<StudyProfile> findTop10StudyProfiles(Pageable pageable);

    @Query("""
        SELECT u.trustScore
        FROM StudyMember sm
        JOIN sm.user u
        WHERE sm.study.id = :studyId
          AND sm.role = com.study.focus.study.domain.StudyRole.LEADER
          AND sm.status = com.study.focus.study.domain.StudyMemberStatus.JOINED
        """)
    Optional<Long> findLeaderTrustScoreByStudyId(Long studyId);
}