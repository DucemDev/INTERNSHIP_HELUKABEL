package com.helu.internship.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UnderServedSummaryResponse {
    private List<UnderServedSegmentResponse> industries;
    private List<UnderServedSegmentResponse> regions;
    private List<UnderServedSegmentResponse> products;
    private List<UnderServedSegmentResponse> sources;
}
