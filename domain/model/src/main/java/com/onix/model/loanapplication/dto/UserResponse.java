package com.onix.model.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserResponse {

    int httpCode;
    String httpMessage;
    String timestamp;
    String userMessage;
    UserDTO data;

}