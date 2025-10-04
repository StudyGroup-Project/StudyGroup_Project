package com.study.focus.chat;

import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.chat.domain.ChatMessage;
import com.study.focus.chat.dto.ChatMessageResponse;
import com.study.focus.chat.repository.ChatMessageRepository;
import com.study.focus.chat.service.ChatService;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.StudyErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyMemberStatus;
import com.study.focus.study.repository.StudyMemberRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    ChatMessageRepository chatMessageRepository;
    @Mock
    StudyMemberRepository studyMemberRepository;
    @Mock
    UserProfileRepository userProfileRepository;
    @Mock
    S3Uploader s3Uploader;
    @Mock
    SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    ChatService chatService;

    // 간단한 픽스처
    private Study study() {
        return Study.builder().id(1L).build();
    }

    private User user() {
        return User.builder().id(1L).build();
    }

    private StudyMember member(Study study, User user) {
        return StudyMember.builder()
                .id(10L)
                .study(study)
                .user(user)
                .status(StudyMemberStatus.JOINED)
                .build();
    }

    private UserProfile profile(User user, String nickname) {
        return UserProfile.builder()
                .user(user)
                .nickname(nickname)
                .build();
    }

    @Test
    @DisplayName("handleMessage: 저장 후 브로드캐스트 전송 성공")
    void handleMessage_success() {
        // given
        var study = study();
        var user = user();
        var member = member(study, user);
        var profile = profile(user, "테스터");

        when(studyMemberRepository.findByStudyIdAndUserIdAndStatus(1L, 1L, StudyMemberStatus.JOINED))
                .thenReturn(Optional.of(member));
        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile));

        // save() 호출 시 JPA가 id/createdAt 채워준 것처럼 반영
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(inv -> {
                    ChatMessage m = inv.getArgument(0);
                    ReflectionTestUtils.setField(m, "id", 100L);
                    ReflectionTestUtils.setField(m, "createdAt", LocalDateTime.now());
                    return m;
                });

        // when
        chatService.handleMessage(1L, 1L, "안녕하세요");

        // then
        ArgumentCaptor<ChatMessageResponse> captor = ArgumentCaptor.forClass(ChatMessageResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/sub/studies/1"), captor.capture());

        ChatMessageResponse sent = captor.getValue();
        assertThat(sent.getId()).isEqualTo(100L);
        assertThat(sent.getNickname()).isEqualTo("테스터");
        assertThat(sent.getContent()).isEqualTo("안녕하세요");
        assertThat(sent.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("handleMessage: 스터디 멤버가 아니면 BusinessException(MEMBER_NOT_FOUND)")
    void handleMessage_memberNotFound() {
        when(studyMemberRepository.findByStudyIdAndUserIdAndStatus(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.handleMessage(1L, 1L, "메시지"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(StudyErrorCode.MEMBER_NOT_FOUND.getMessage());
        verifyNoInteractions(userProfileRepository, chatMessageRepository, messagingTemplate);
    }

    @Test
    @DisplayName("handleMessage: 프로필 없으면 BusinessException(PROFILE_NOT_FOUND)")
    void handleMessage_profileNotFound() {
        var study = study();
        var user = user();
        var member = member(study, user);

        when(studyMemberRepository.findByStudyIdAndUserIdAndStatus(1L, 1L, StudyMemberStatus.JOINED))
                .thenReturn(Optional.of(member));
        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.handleMessage(1L, 1L, "메시지"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.PROFILE_NOT_FOUND.getMessage());

        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, never())
                .convertAndSend(anyString(), any(ChatMessageResponse.class));
    }

    @Test
    @DisplayName("getRecentMessages: lastMessageId 없이 최근 N개 조회 성공")
    void getRecentMessages_initial_success() {
        var study = study();
        var user = user();
        var member = member(study, user);
        var profile = profile(user, "테스터");

        ChatMessage msg = ChatMessage.builder()
                .study(study)
                .author(member)
                .content("테스트 메시지")
                .build();
        // DB가 채우는 필드 시뮬레이션
        ReflectionTestUtils.setField(msg, "id", 200L);
        ReflectionTestUtils.setField(msg, "createdAt", LocalDateTime.now());

        when(chatMessageRepository.findRecentMessages(eq(1L), any()))
                .thenReturn(List.of(msg));
        when(userProfileRepository.findByUser(eq(user)))
                .thenReturn(Optional.of(profile));

        List<ChatMessageResponse> result = chatService.getRecentMessages(1L, null, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(200L);
        assertThat(result.get(0).getNickname()).isEqualTo("테스터");
        assertThat(result.get(0).getContent()).isEqualTo("테스트 메시지");
        assertThat(result.get(0).getCreatedAt()).isNotNull();
    }
}