package com.opr3.opr3.dto.attempt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAttemptTimeInfo {
    private Integer minQuestionAttemptTime;
    private Integer q1QuestionAttemptTime;
    private Integer medQuestionAttemptTime;
    private Integer q3QuestionAttemptTime;
    private Integer maxQuestionAttemptTime;
    private Integer[] outliers;
}
