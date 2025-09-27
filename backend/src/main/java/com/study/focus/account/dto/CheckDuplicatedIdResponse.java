package com.study.focus.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckDuplicatedIdResponse {
    private boolean available;
}
