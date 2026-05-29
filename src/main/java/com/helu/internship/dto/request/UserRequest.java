package com.helu.internship.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String userCode;
    private String password;
    private String fullName;
    private String email;
    private Integer roleId;
    private Boolean isActive;
}
