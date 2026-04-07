package com.opr3.opr3.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.opr3.opr3.entity.Quiz;
import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.enums.QuestionType;
import com.opr3.opr3.enums.QuizGradingMethod;
import com.opr3.opr3.enums.TextReviewType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {

        private Long id;
        private String title;
        private String description;
        private Integer timeLimit;
        private boolean shuffleQuestions;
        private QuizGradingMethod gradingMethod;
        private int totalPoints;
        private int questionCount;
        private Set<CategoryInfo> categories;
        private List<QuestionInfo> questions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CategoryInfo {
                private Long id;
                private String name;
                private String color;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class QuestionInfo {
                private Long id;
                private int questionOrder;
                private QuestionType type;
                private String questionText;
                private String imagePath;
                private int points;
                // SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE
                private List<ChoiceOptionInfo> choiceOptions;
                // MATCHING
                private List<MatchPairInfo> matchPairs;
                // ORDERING
                private List<OrderItemInfo> orderItems;
                // TEXT_INPUT
                private TextConfigInfo textConfig;
                // FLASHCARD
                private FlashcardInfo flashcard;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ChoiceOptionInfo {
                private Long id;
                private String text;
                private boolean isCorrect;
                private int optionOrder;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MatchPairInfo {
                private Long id;
                private String leftSide;
                private String rightSide;
                private int pairOrder;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OrderItemInfo {
                private Long id;
                private String text;
                private int correctPosition;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TextConfigInfo {
                private String correctAnswer;
                private TextReviewType review;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FlashcardInfo {
                private String backText;
        }

        public static QuizResponse from(Quiz quiz) {
                return QuizResponse.builder()
                                .id(quiz.getId())
                                .title(quiz.getTitle())
                                .description(quiz.getDescription())
                                .timeLimit(quiz.getTimeLimit())
                                .shuffleQuestions(quiz.isShuffleQuestions())
                                .gradingMethod(quiz.getGradingMethod())
                                .totalPoints(quiz.getQuestions().stream().mapToInt(Question::getPoints).sum())
                                .questionCount(quiz.getQuestions().size())
                                .categories(quiz.getCategories().stream()
                                                .map(c -> CategoryInfo.builder()
                                                                .id(c.getId())
                                                                .name(c.getName())
                                                                .color(c.getColor())
                                                                .build())
                                                .collect(Collectors.toSet()))
                                .questions(quiz.getQuestions().stream()
                                                .sorted((a, b) -> Integer.compare(a.getQuestionOrder(),
                                                                b.getQuestionOrder()))
                                                .map(QuizResponse::mapQuestion)
                                                .collect(Collectors.toList()))
                                .createdAt(quiz.getCreatedAt())
                                .updatedAt(quiz.getUpdatedAt())
                                .build();
        }

        private static QuestionInfo mapQuestion(Question q) {
                return QuestionInfo.builder()
                                .id(q.getId())
                                .questionOrder(q.getQuestionOrder())
                                .type(q.getType())
                                .questionText(q.getQuestionText())
                                .imagePath(q.getImagePath())
                                .points(q.getPoints())
                                .choiceOptions(q.getChoiceOptions().stream()
                                                .sorted((a, b) -> Integer.compare(a.getOptionOrder(),
                                                                b.getOptionOrder()))
                                                .map(o -> ChoiceOptionInfo.builder()
                                                                .id(o.getId())
                                                                .text(o.getText())
                                                                .isCorrect(o.isCorrect())
                                                                .optionOrder(o.getOptionOrder())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .matchPairs(q.getMatchPairs().stream()
                                                .sorted((a, b) -> Integer.compare(a.getPairOrder(), b.getPairOrder()))
                                                .map(p -> MatchPairInfo.builder()
                                                                .id(p.getId())
                                                                .leftSide(p.getLeftSide())
                                                                .rightSide(p.getRightSide())
                                                                .pairOrder(p.getPairOrder())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .orderItems(q.getOrderItems().stream()
                                                .sorted((a, b) -> Integer.compare(a.getCorrectPosition(),
                                                                b.getCorrectPosition()))
                                                .map(i -> OrderItemInfo.builder()
                                                                .id(i.getId())
                                                                .text(i.getText())
                                                                .correctPosition(i.getCorrectPosition())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .textConfig(q.getTextConfig() != null
                                                ? TextConfigInfo.builder()
                                                                .correctAnswer(q.getTextConfig().getCorrectAnswer())
                                                                .review(q.getTextConfig().getTextReviewType())
                                                                .build()
                                                : null)
                                .flashcard(q.getFlashcardConfig() != null
                                                ? FlashcardInfo.builder()
                                                                .backText(q.getFlashcardConfig().getBackText())
                                                                .build()
                                                : null)
                                .build();
        }
}
