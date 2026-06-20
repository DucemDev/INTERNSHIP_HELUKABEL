package com.helu.internship.dto.response;

public interface LostBySourceProjection {

    String getSourceId();

    String getSourceName();

    Long getLostLead();

    Double getLostRate();
}