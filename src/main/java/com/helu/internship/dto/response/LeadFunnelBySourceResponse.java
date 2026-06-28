package com.helu.internship.dto.response;

public interface LeadFunnelBySourceResponse {
    String getSourceName();

    Long getNewLead();

    Long getContactedLead();

    Long getQualifiedLead();

    Long getProposalSentLead();

    Long getNegotiationLead();
}
