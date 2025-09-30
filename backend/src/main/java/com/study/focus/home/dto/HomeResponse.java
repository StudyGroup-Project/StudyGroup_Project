package com.study.focus.home.dto;

import com.study.focus.account.domain.UserProfile;
import com.study.focus.common.dto.StudyDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HomeResponse {

    private UserDto user;
    private List<StudyDto> topStudies;

    @Getter
    @AllArgsConstructor
    public static class UserDto {
        private String nickname;
        private String profileImageUrl;
        private String province;
        private String district;

        public UserDto(UserProfile profile, String profileImageUrl) {
            this.nickname = profile.getNickname();
            this.profileImageUrl = profileImageUrl;
            this.province = profile.getAddress() != null ? profile.getAddress().getProvince() : null;
            this.district = profile.getAddress() != null ? profile.getAddress().getDistrict() : null;
        }
    }
}
