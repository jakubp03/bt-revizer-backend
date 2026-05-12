package com.opr3.opr3.dto.attempt;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizStatsAnswer {

    private Integer[] attemptTimes;
    private double[] scorePercentages;
    private Double previousAttemptScorePercentage;
    private List<AttemptAnswerInfo> questionAttempts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptAnswerInfo {
        private Long questionId;
        private Integer averageQuestionAttemptTime;
        private double averageQuestionScorePercentage;
    }
}
