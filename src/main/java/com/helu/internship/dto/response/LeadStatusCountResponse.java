package com.helu.internship.dto.response;

public class LeadStatusCountResponse {

    private String status;
    private Long total;

    public LeadStatusCountResponse(String status, Long total) {
        this.status = status;
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public Long getTotal() {
        return total;
    }
}