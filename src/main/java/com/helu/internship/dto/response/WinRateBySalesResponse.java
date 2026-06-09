package com.helu.internship.dto.response;

import java.util.UUID;

public interface WinRateBySalesResponse {
    UUID getUserId();
    Long getqualifiedLead();
    Long getWonLead();
    Double getWinRate();


}
