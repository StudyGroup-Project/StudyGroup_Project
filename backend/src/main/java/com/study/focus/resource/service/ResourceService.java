package com.study.focus.resource.service;

import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.service.AnnouncementService;
import com.study.focus.resource.dto.GetResourcesResponse;
import com.study.focus.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final AnnouncementService announcementService;
    // 자료 목록 가져오기
    public List<GetResourcesResponse> getResources(Long studyId, Long UserId) {

        return  null;
        // TODO: 자료 목록 조회
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