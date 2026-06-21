package com.helu.internship.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {
        USER_EXISTED(999,"User da ton tai");
    private int code;
    private String message;
}
