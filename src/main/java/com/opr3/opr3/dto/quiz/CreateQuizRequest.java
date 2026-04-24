package com.opr3.opr3.dto.quiz;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opr3.opr3.enums.QuestionType;
import com.opr3.opr3.enums.TextReviewType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {

    @NotBlank
    private String title;

    private String description;

    private Integer timeLimit;

    private Set<Long> categoryIds;

    private String icon;

    @NotEmpty
    @Valid
    private List<QuestionRequest> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionRequest {
        @NotNull
        private QuestionType type;

        @NotBlank
        private String questionText;

        private String imagePath;

        @Positive
        private int points;

        // SINGLE_CHOICE, MULTIPLE_CHOICE
        private List<ChoiceOptionRequest> choiceOptions;

        // TRUE_FALSE
        private Boolean correctAnswer;

        // TEXT_INPUT
        private TextConfigRequest textConfig;

        // MATCHING
        private List<MatchPairRequest> matchPairs;

        // ORDERING
        private List<OrderItemRequest> orderItems;

        // FLASHCARD
        private String flashcardBackText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoiceOptionRequest {
        @NotBlank
        private String text;
        @JsonProperty("isCorrect")
        private boolean isCorrect;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextConfigRequest {
        private String correctAnswer;
        @NotNull
        private TextReviewType reviewType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchPairRequest {
        @NotBlank
        private String leftSide;
        @NotBlank
        private String rightSide;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotBlank
        private String text;
    }
}
