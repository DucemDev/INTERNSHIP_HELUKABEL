package com.helu.internship.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BantRequest {
    private Integer budget;
    private Integer authority;
    private Integer need;
    private Integer timeline;
}
