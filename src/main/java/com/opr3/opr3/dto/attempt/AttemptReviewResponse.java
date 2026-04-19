package com.opr3.opr3.dto.attempt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.opr3.opr3.entity.attempt.AttemptAnswer;
import com.opr3.opr3.entity.attempt.IdBasedAttemptAnswer;
import com.opr3.opr3.entity.attempt.MatchBasedAttemptAnswer;
import com.opr3.opr3.entity.attempt.MatchedPairEntry;
import com.opr3.opr3.entity.attempt.QuizAttempt;
import com.opr3.opr3.entity.attempt.TextBasedAttemptAnswer;
import com.opr3.opr3.entity.question.OptionMatchPair;
import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.enums.QuestionType;
import com.opr3.opr3.enums.TextReviewType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptReviewResponse {

    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private double scorePercentage;
    private double score;
    private int maxScore;
    private Integer timeSpent;
    private LocalDateTime submittedAt;
    private List<QuestionReview> questions;

    // --- inner DTOs ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionReview {
        private Long questionId;
        private int questionOrder;
        private QuestionType type;
        private String questionText;
        private String imagePath;
        private int points;
        private int pointsAwarded;
        private boolean correct;
        // SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE
        private List<ChoiceOptionReview> choiceOptions;
        // MATCHING
        private List<MatchPairReview> matchPairs;
        // ORDERING
        private List<OrderItemReview> orderItems;
        // TEXT_INPUT
        private TextAnswerReview textAnswer;
        // FLASHCARD
        private FlashcardReview flashcard;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoiceOptionReview {
        private Long id;
        private String text;
        private int optionOrder;
        private boolean correct;
        private boolean selected;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchPairReview {
        private Long id;
        private String leftSide;
        private String correctRightSide;
        private Long userRightId;
        private String userRightSide;
        private boolean correct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemReview {
        private Long id;
        private String text;
        private int correctPosition;
        private int userPosition;
        private boolean correct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextAnswerReview {
        private TextReviewType reviewType;
        private String correctAnswer;
        private String userAnswer;
        private boolean correct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashcardReview {
        private String backText;
        private boolean markedCorrect;
    }

    // --- factory method ---

    public static AttemptReviewResponse from(QuizAttempt attempt) {
        Map<Long, AttemptAnswer> answerByQuestionId = attempt.getAnswers().stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        List<QuestionReview> questionReviews = attempt.getQuiz().getQuestions().stream()
                .sorted((a, b) -> Integer.compare(a.getQuestionOrder(), b.getQuestionOrder()))
                .map(q -> mapQuestion(q, answerByQuestionId.get(q.getId())))
                .collect(Collectors.toList());

        return AttemptReviewResponse.builder()
                .attemptId(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .scorePercentage(attempt.getScorePercentage())
                .score(attempt.getScore())
                .maxScore(attempt.getMaxScore())
                .timeSpent(attempt.getTimeSpent())
                .submittedAt(attempt.getSubmittedAt())
                .questions(questionReviews)
                .build();
    }

    private static QuestionReview mapQuestion(Question q, AttemptAnswer answer) {
        int pointsAwarded = answer != null && answer.getPointsAwarded() != null ? answer.getPointsAwarded() : 0;
        boolean isCorrect = answer != null && Boolean.TRUE.equals(answer.getIsAnswerCorrect());

        QuestionReview.QuestionReviewBuilder builder = QuestionReview.builder()
                .questionId(q.getId())
                .questionOrder(q.getQuestionOrder())
                .type(q.getType())
                .questionText(q.getQuestionText())
                .imagePath(q.getImagePath())
                .points(q.getPoints())
                .pointsAwarded(pointsAwarded)
                .correct(isCorrect);

        switch (q.getType()) {
            case SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE -> builder.choiceOptions(
                    mapChoiceOptions(q, answer));
            case MATCHING -> builder.matchPairs(
                    mapMatchPairs(q, answer));
            case ORDERING -> builder.orderItems(
                    mapOrderItems(q, answer));
            case TEXT_INPUT -> builder.textAnswer(
                    mapTextAnswer(q, answer));
            case FLASHCARD -> builder.flashcard(
                    mapFlashcard(q, answer));
        }

        return builder.build();
    }

    private static List<ChoiceOptionReview> mapChoiceOptions(Question q, AttemptAnswer answer) {
        List<Long> selectedIds = List.of();
        if (answer instanceof IdBasedAttemptAnswer idAnswer && idAnswer.getSelectedOptionIds() != null) {
            selectedIds = idAnswer.getSelectedOptionIds();
        }
        List<Long> finalSelectedIds = selectedIds;

        return q.getChoiceOptions().stream()
                .sorted((a, b) -> Integer.compare(a.getOptionOrder(), b.getOptionOrder()))
                .map(o -> ChoiceOptionReview.builder()
                        .id(o.getId())
                        .text(o.getText())
                        .optionOrder(o.getOptionOrder())
                        .correct(o.isCorrect())
                        .selected(finalSelectedIds.contains(o.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private static List<MatchPairReview> mapMatchPairs(Question q, AttemptAnswer answer) {
        // Build a map of leftId -> user's rightId from the attempt
        Map<Long, Long> userPairMap = Map.of();
        if (answer instanceof MatchBasedAttemptAnswer matchAnswer && matchAnswer.getMatchedPairs() != null) {
            userPairMap = matchAnswer.getMatchedPairs().stream()
                    .collect(Collectors.toMap(MatchedPairEntry::getLeftId, MatchedPairEntry::getRightId));
        }

        // Build a map of matchPair id -> matchPair for resolving right-side text by id
        Map<Long, OptionMatchPair> pairById = q.getMatchPairs().stream()
                .collect(Collectors.toMap(OptionMatchPair::getId, p -> p));

        Map<Long, Long> finalUserPairMap = userPairMap;

        return q.getMatchPairs().stream()
                .sorted((a, b) -> Integer.compare(a.getPairOrder(), b.getPairOrder()))
                .map(p -> {
                    Long userRightId = finalUserPairMap.get(p.getId());
                    String userRightSide = null;
                    if (userRightId != null && pairById.containsKey(userRightId)) {
                        userRightSide = pairById.get(userRightId).getRightSide();
                    }
                    return MatchPairReview.builder()
                            .id(p.getId())
                            .leftSide(p.getLeftSide())
                            .correctRightSide(p.getRightSide())
                            .userRightId(userRightId)
                            .userRightSide(userRightSide)
                            .correct(p.getId().equals(userRightId))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static List<OrderItemReview> mapOrderItems(Question q, AttemptAnswer answer) {
        List<Long> userOrder = List.of();
        if (answer instanceof IdBasedAttemptAnswer idAnswer && idAnswer.getSelectedOptionIds() != null) {
            userOrder = idAnswer.getSelectedOptionIds();
        }

        // Build a lookup: itemId -> user-submitted position (0-based index in the list)
        Map<Long, Integer> userPositionMap = new java.util.HashMap<>();
        for (int i = 0; i < userOrder.size(); i++) {
            userPositionMap.put(userOrder.get(i), i + 1);
        }

        return q.getOrderItems().stream()
                .sorted((a, b) -> Integer.compare(a.getCorrectPosition(), b.getCorrectPosition()))
                .map(item -> {
                    int userPos = userPositionMap.getOrDefault(item.getId(), -1);
                    return OrderItemReview.builder()
                            .id(item.getId())
                            .text(item.getText())
                            .correctPosition(item.getCorrectPosition())
                            .userPosition(userPos)
                            .correct(item.getCorrectPosition() == userPos)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static TextAnswerReview mapTextAnswer(Question q, AttemptAnswer answer) {
        String userAnswer = null;
        if (answer instanceof TextBasedAttemptAnswer textAnswer) {
            userAnswer = textAnswer.getSubmittedAnswer();
        }
        boolean isCorrect = answer != null && Boolean.TRUE.equals(answer.getIsAnswerCorrect());

        return TextAnswerReview.builder()
                .reviewType(q.getTextConfig() != null ? q.getTextConfig().getTextReviewType() : null)
                .correctAnswer(q.getTextConfig() != null ? q.getTextConfig().getCorrectAnswer() : null)
                .userAnswer(userAnswer)
                .correct(isCorrect)
                .build();
    }

    private static FlashcardReview mapFlashcard(Question q, AttemptAnswer answer) {
        boolean markedCorrect = answer != null && Boolean.TRUE.equals(answer.getIsAnswerCorrect());

        return FlashcardReview.builder()
                .backText(q.getFlashcardConfig() != null ? q.getFlashcardConfig().getBackText() : null)
                .markedCorrect(markedCorrect)
                .build();
    }
}
