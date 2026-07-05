package com.opr3.opr3.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsResponse {

    private Integer[] attemptTimes;
    private double[] scorePercentages;
    private Double averageScorePercentage;
    private Double medianScorePercentage;
    private Double averageAttemptTime;
    private Double medianAttemptTime;
}
