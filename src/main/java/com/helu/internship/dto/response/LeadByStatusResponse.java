package com.helu.internship.dto.response;

public class LeadByStatusResponse {

    private String leadId;
    private String fullName;
    private String account;
    private String status;
    private String region;
    private String industryType;
    private String customerGroup;

    public LeadByStatusResponse(String leadId, String fullName, String account,
                                String status, String region,
                                String industryType, String customerGroup) {
        this.leadId = leadId;
        this.fullName = fullName;
        this.account = account;
        this.status = status;
        this.region = region;
        this.industryType = industryType;
        this.customerGroup = customerGroup;
    }

    public String getLeadId() { return leadId; }
    public String getFullName() { return fullName; }
    public String getAccount() { return account; }
    public String getStatus() { return status; }
    public String getRegion() { return region; }
    public String getIndustryType() { return industryType; }
    public String getCustomerGroup() { return customerGroup; }
}