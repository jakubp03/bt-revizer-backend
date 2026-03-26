package com.opr3.opr3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultResponse {

    private Long attemptId;

    private double scorePercentage;

    private Double previousAttemptScorePercentage;

    private double averageScorePercentage;
}
