package com.opr3.opr3.dto.attempt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.opr3.opr3.dto.quiz.QuizBasicResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private int totalTimeSpent;
    private int submittedQuizzesCount;
    private Double averageScorePercentage;
    private List<QuizBasicResponse> recentQuizzes;
    private Map<LocalDate, Integer> monthlyActivity;
}
