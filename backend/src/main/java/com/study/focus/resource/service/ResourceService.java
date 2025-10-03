package com.study.focus.resource.service;

import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.UserService;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.service.AnnouncementService;
import com.study.focus.common.service.GroupService;
import com.study.focus.resource.domain.Resource;
import com.study.focus.resource.dto.GetResourcesResponse;
import com.study.focus.resource.repository.ResourceRepository;
import com.study.focus.study.domain.StudyMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final GroupService groupService;
    private final UserService userService;

    // 자료 목록 가져오기
    public List<GetResourcesResponse> getResources(Long studyId, Long userId) {
        //1. 스터디 그룹 멤버 확인
        groupService.memberValidation(studyId, userId);
        //2. 데이터 조회
        List<Resource> groupResource = resourceRepository.findAllByStudy_Id(studyId);
        List<GetResourcesResponse> resourcesResponseList = groupResource.stream()
                .map(r -> {
                            GetMyProfileResponse userProfile = userService.getMyProfile(r.getAuthor().getUser().getId());
                            return GetResourcesResponse.builder().resourceId(r.getId())
                                    .title(r.getTitle())
                                    .userName(userProfile.getNickname())
                                    .createdAt(r.getCreatedAt()).build();
                        }
                ).toList();
        return resourcesResponseList;
    }

    // 자료 생성
    public void createResource(Long studyId) {
        // TODO: 자료 생성
    }

    // 자료 상세 데이터 가져오기
    public void getResourceDetail(Long studyId, Long resourceId) {
        // TODO: 자료 상세 조회
    }

    // 자료 수정
    public void updateResource(Long studyId, Long resourceId) {
        // TODO: 자료 수정
    }

    // 자료 삭제
    public void deleteResource(Long studyId, Long resourceId) {
        // TODO: 자료 삭제
    }
}