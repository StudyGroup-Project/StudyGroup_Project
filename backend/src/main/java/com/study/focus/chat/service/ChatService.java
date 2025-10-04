package com.study.focus.chat.service;

import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.chat.domain.ChatMessage;
import com.study.focus.chat.dto.ChatMessageResponse;
import com.study.focus.chat.repository.ChatMessageRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.StudyErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyMemberStatus;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.common.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserProfileRepository userProfileRepository;
    private final S3Uploader s3Uploader;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 메시지 전송 (저장 + DTO 반환)
     */
    @Transactional
    public void handleMessage(Long studyId, Long userId, String content) {
        // 1. 멤버 확인
        log.info("study id: {}, user id: {}", studyId, userId);
        StudyMember member = studyMemberRepository
                .findByStudyIdAndUserIdAndStatus(studyId, userId, StudyMemberStatus.JOINED)
                .orElseThrow(() -> new BusinessException(StudyErrorCode.MEMBER_NOT_FOUND));

        // 2. 프로필 정보
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.PROFILE_NOT_FOUND));

        // 3. 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .study(member.getStudy())
                .author(member)
                .content(content)
                .build();
        chatMessageRepository.save(message);

        String profileImageUrl = profile.getProfileImage() != null
                ? s3Uploader.getUrlFile(profile.getProfileImage().getFileKey())
                : null;

        // 4. 응답 DTO
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(message.getId())
                .userId(userId)
                .nickname(profile.getNickname())
                .profileImageUrl(profileImageUrl)
                .content(content)
                .createdAt(message.getCreatedAt())
                .build();

        log.info("브로드캐스트 전송: {}", response);

        // 5. 브로드캐스트
        messagingTemplate.convertAndSend("/sub/studies/" + studyId, response);
    }

    /**
     * 최근 메시지 조회 (커서 기반)
     */
    @Transactional
    public List<ChatMessageResponse> getRecentMessages(Long studyId, Long lastMessageId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        List<ChatMessage> messages;

        if (lastMessageId == null) {
            // 최근 메시지 N개 (최신순)
            messages = chatMessageRepository.findRecentMessages(studyId, pageable);
        } else {
            // 커서 기준 메시지 찾기
            ChatMessage cursor = chatMessageRepository.findById(lastMessageId)
                    .orElseThrow(() -> new BusinessException(StudyErrorCode.MESSAGE_NOT_FOUND));

            // 커서보다 이전(createdAt 더 과거)이면서, 같은 시각이면 id 더 작은 것
            messages = chatMessageRepository.findMessagesBefore(
                    studyId, cursor.getCreatedAt(), cursor.getId(), pageable
            );
        }

        // DTO 변환
        return messages.stream()
                .map(m -> {
                    UserProfile profile = userProfileRepository.findByUser(m.getAuthor().getUser())
                            .orElseThrow(() -> new BusinessException(UserErrorCode.PROFILE_NOT_FOUND));

                    String profileImageUrl = profile.getProfileImage() != null
                            ? s3Uploader.getUrlFile(profile.getProfileImage().getFileKey())
                            : null;

                    return ChatMessageResponse.builder()
                            .id(m.getId())
                            .userId(profile.getUser().getId())
                            .nickname(profile.getNickname())
                            .profileImageUrl(profileImageUrl)
                            .content(m.getContent())
                            .createdAt(m.getCreatedAt())
                            .build();
                })
                .toList();
    }
}
