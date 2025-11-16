package com.study.focus.chat.repository;

import com.study.focus.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    void deleteAllByStudy_Id(Long studyId);

    /**
     * 특정 스터디의 최근 메시지 N개 (createdAt 내림차순)
     */
    @Query("""
        select m
        from ChatMessage m
        join fetch m.author a
        join fetch a.user u
        where m.study.id = :studyId
          and a.status = com.study.focus.study.domain.StudyMemberStatus.JOINED
        order by m.createdAt desc, m.id desc
    """)
    List<ChatMessage> findRecentMessages(Long studyId, Pageable pageable);

    /**
     * 무한 스크롤 커서기반 (createdAt 내림차순)
     */
    @Query("""
    select m
    from ChatMessage m
    join fetch m.author a
    join fetch a.user u
    where m.study.id = :studyId
      and a.status = com.study.focus.study.domain.StudyMemberStatus.JOINED
      and (m.createdAt < :cursorCreatedAt
           or (m.createdAt = :cursorCreatedAt and m.id < :cursorId))
    order by m.createdAt desc, m.id desc
""")
    List<ChatMessage> findMessagesBefore(Long studyId,
                                         LocalDateTime cursorCreatedAt,
                                         Long cursorId,
                                         Pageable pageable);


}
