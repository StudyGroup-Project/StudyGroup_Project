package com.study.focus.study.repository;

import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {
    @Query("""
  select sp
  from Bookmark b
  join b.study s
  join StudyProfile sp on sp.study = s
  where b.user.id = :userId
  order by b.createdAt desc
  """)
    Page<StudyProfile> findBookmarkedStudyProfiles(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    select sp
    from StudyMember sm
    join sm.study s
    join StudyProfile sp on sp.study = s
    where sm.user.id = :userId and
    sm.status = com.study.focus.study.domain.StudyMemberStatus.JOINED
    order by sm.createdAt desc
""")
    Page<StudyProfile> findJoinedStudyProfiles(@Param("userId") Long userId, Pageable pageable);
}