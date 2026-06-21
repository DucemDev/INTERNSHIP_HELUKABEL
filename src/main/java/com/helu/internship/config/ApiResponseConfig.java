package com.helu.internship.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseConfig<T> {
    private String code;
    private String message;
    private T result;

}
