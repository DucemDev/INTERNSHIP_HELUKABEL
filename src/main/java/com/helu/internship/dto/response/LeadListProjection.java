package com.helu.internship.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LeadListProjection {
    String getLeadId();
    LocalDate getCreatedDate();
    String getFullName();
    String getPhoneNumber();
    String getAccount();
    String getIndustryType();
    String getCustomerGroup();
    String getCustomerRole();
    String getLocation();
    String getRegion();
    String getStatus();
    BigDecimal getCost();
    String getLossReason();
    BigDecimal getBusinessResult();
    String getProductName();
    String getSourceName();
    String getUserName();
}
