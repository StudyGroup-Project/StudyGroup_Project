package com.study.focus.common.service;

import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {

    private  final StudyMemberRepository studyMemberRepository;

    //현재 스터디 멤버인지 아닌지 판별
    public StudyMember memberValidation(Long studyId, Long userId) {
        if(studyId == null || userId == null) {throw new BusinessException(CommonErrorCode.INVALID_REQUEST);}
        return studyMemberRepository.findByStudyIdAndUserId(studyId, userId).
                orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }

    //해당 유저가 그룹 유저인지 판별
    public  void isLeader(StudyMember userStudyMember) {
        if(!userStudyMember.getRole().equals(StudyRole.LEADER))
        {
            throw new BusinessException(UserErrorCode.URL_FORBIDDEN);
        }
    }


}
