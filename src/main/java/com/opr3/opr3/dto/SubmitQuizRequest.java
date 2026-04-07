package com.opr3.opr3.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizRequest {

    private Long quizId;

    private List<IdBasedAnswerSubmission> idBasedAnswers;

    private List<TextBasedAnswerSubmission> textBasedAnswers;

    private List<MatchBasedAnswerSubmission> matchBasedAttemptAnswer;

    private Integer timeSpent;
}
