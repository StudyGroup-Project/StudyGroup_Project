package com.study.focus.application.dto;


import com.study.focus.application.domain.Application;
import com.study.focus.application.domain.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HandleApplicationRequest {

    private ApplicationStatus status;

}
