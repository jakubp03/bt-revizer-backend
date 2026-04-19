package com.opr3.opr3.dto.quiz;

import java.util.Set;
import java.util.stream.Collectors;

import com.opr3.opr3.dto.category.CategoryInfo;
import com.opr3.opr3.entity.Quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizBasicResponse {
        private Long id;
        private String title;
        private String icon;
        private Boolean timeLimit;
        private Set<CategoryInfo> categories;
        private int questionCount;

        public static QuizBasicResponse from(Quiz quiz) {
                return QuizBasicResponse.builder()
                                .id(quiz.getId())
                                .title(quiz.getTitle())
                                .icon(quiz.getIcon())
                                .timeLimit(quiz.getTimeLimit() != null)
                                .categories(quiz.getCategories().stream()
                                                .map(c -> CategoryInfo.builder()
                                                                .id(c.getId())
                                                                .build())
                                                .collect(Collectors.toSet()))
                                .questionCount(quiz.getQuestions().size())
                                .build();
        }
}
