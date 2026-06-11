package com.helu.internship.dto.response;

import java.time.LocalDate;

public class LeadByStatusResponse {

    private String leadId;
    private String fullName;
    private String account;
    private String status;
    private String region;
    private String industryType;
    private String customerGroup;
    private String email;
    private String phoneNumber;
    private String userName;
    private LocalDate createdDate;

    public LeadByStatusResponse(String leadId, String fullName, String account,
                                String status, String region,
                                String industryType, String customerGroup,
                                String email, String phoneNumber,
                                String userName, LocalDate createdDate) {
        this.leadId = leadId;
        this.fullName = fullName;
        this.account = account;
        this.status = status;
        this.region = region;
        this.industryType = industryType;
        this.customerGroup = customerGroup;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.createdDate = createdDate;
    }

    public String getLeadId() { return leadId; }
    public String getFullName() { return fullName; }
    public String getAccount() { return account; }
    public String getStatus() { return status; }
    public String getRegion() { return region; }
    public String getIndustryType() { return industryType; }
    public String getCustomerGroup() { return customerGroup; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getUserName() { return userName; }
    public LocalDate getCreatedDate() { return createdDate; }
}