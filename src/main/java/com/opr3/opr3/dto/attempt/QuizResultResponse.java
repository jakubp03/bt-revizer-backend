package com.opr3.opr3.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {

    private Long attemptId;

    private double scorePercentage;

    private Double previousAttemptScorePercentage;

    private double averageScorePercentage;
}
