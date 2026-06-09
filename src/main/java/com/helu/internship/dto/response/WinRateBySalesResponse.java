package com.helu.internship.dto.response;

import java.util.UUID;

public interface WinRateBySalesResponse {
    UUID getUserId();
    String getUserName();
    Long getqualifiedLead();
    Long getWonLead();
    Double getWinRate();
}

