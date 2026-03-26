package com.opr3.opr3.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opr3.opr3.entity.attempt.AttemptAnswer;
import com.opr3.opr3.entity.attempt.TestAttempt;
import com.opr3.opr3.entity.question.OptionChoice;
import com.opr3.opr3.entity.question.OptionOrderItem;
import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.entity.question.TextAnswerConfig;
import com.opr3.opr3.enums.TextReviewType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestGradingService {

    /**
     * Grades the test attempt by evaluating each answer against the correct
     * answers.
     * Sets the score on the attempt and returns the score percentage.
     *
     * @return score percentage (0.0 - 100.0)
     */
    public double gradeTest(TestAttempt attempt) {
        double totalScore = 0;

        for (AttemptAnswer answer : attempt.getAnswers()) {
            Question question = answer.getQuestion();
            int pointsAwarded = switch (question.getType()) {
                case SINGLE_CHOICE, TRUE_FALSE -> gradeSingleChoice(answer, question);
                case MULTIPLE_CHOICE -> gradeMultipleChoice(answer, question);
                case TEXT_INPUT -> gradeTextInput(answer, question);
                case MATCHING -> gradeMatching(answer, question);
                case ORDERING -> gradeOrdering(answer, question);
                case FLASHCARD -> gradeFlashcard(answer, question);
            };

            answer.setPointsAwarded(pointsAwarded);
            answer.setIsCorrect(pointsAwarded == question.getPoints());
            totalScore += pointsAwarded;
        }

        attempt.setScore(totalScore);
        return attempt.getMaxScore() > 0 ? (totalScore / attempt.getMaxScore()) * 100.0 : 0.0;
    }

    /**
     * SINGLE_CHOICE / TRUE_FALSE: exactly one selected option, 1 point if it's
     * the correct one.
     */
    private int gradeSingleChoice(AttemptAnswer answer, Question question) {
        if (answer.getSelectedOptionIds().isEmpty()) {
            return 0;
        }
        String selectedId = answer.getSelectedOptionIds().get(0);
        return question.getChoiceOptions().stream()
                .filter(opt -> String.valueOf(opt.getId()).equals(selectedId) && opt.isCorrect())
                .findFirst()
                .map(opt -> 1)
                .orElse(0);
    }

    /**
     * MULTIPLE_CHOICE: 1 point for each correctly selected correct option.
     */
    private int gradeMultipleChoice(AttemptAnswer answer, Question question) {
        Set<String> selectedIds = new HashSet<>(answer.getSelectedOptionIds());
        int points = 0;
        for (OptionChoice choice : question.getChoiceOptions()) {
            if (choice.isCorrect() && selectedIds.contains(String.valueOf(choice.getId()))) {
                points++;
            }
        }
        return points;
    }

    /**
     * TEXT_INPUT:
     * - MANUAL review: the user self-evaluates before submission;
     * selectedOptionIds contains "true" if they judged their answer correct.
     * - AUTOMATIC review: case-insensitive comparison against the stored correct
     * answer.
     */
    private int gradeTextInput(AttemptAnswer answer, Question question) {
        TextAnswerConfig config = question.getTextConfig();
        if (config == null) {
            return 0;
        }

        if (config.getReview() == TextReviewType.MANUAL) {
            return answer.getSelectedOptionIds().contains("true") ? 1 : 0;
        }

        // AUTOMATIC review
        String correctAnswer = config.getCorrectAnswer();
        String userAnswer = answer.getTextAnswer();
        if (correctAnswer == null || userAnswer == null) {
            return 0;
        }
        return correctAnswer.trim().equalsIgnoreCase(userAnswer.trim()) ? 1 : 0;
    }

    /**
     * MATCHING: selectedOptionIds contains entries formatted as
     * "leftPairId:rightPairId".
     * A match is correct when both IDs refer to the same OptionMatchPair
     * (i.e. the student paired the left side with its own right side).
     */
    private int gradeMatching(AttemptAnswer answer, Question question) {
        int points = 0;
        for (String pair : answer.getSelectedOptionIds()) {
            String[] parts = pair.split(":");
            if (parts.length == 2 && parts[0].equals(parts[1])) {
                points++;
            }
        }
        return points;
    }

    /**
     * ORDERING: selectedOptionIds contains order-item IDs in the sequence the
     * student placed them (index 0 = position 0).
     * 1 point for each item whose list index matches its correctPosition.
     */
    private int gradeOrdering(AttemptAnswer answer, Question question) {
        Map<String, Integer> correctPositions = question.getOrderItems().stream()
                .collect(Collectors.toMap(
                        item -> String.valueOf(item.getId()),
                        OptionOrderItem::getCorrectPosition));

        int points = 0;
        for (int i = 0; i < answer.getSelectedOptionIds().size(); i++) {
            Integer correctPos = correctPositions.get(answer.getSelectedOptionIds().get(i));
            if (correctPos != null && correctPos == i) {
                points++;
            }
        }
        return points;
    }

    /**
     * FLASHCARD: the user self-evaluates recall; selectedOptionIds contains
     * "true" if they recalled correctly.
     */
    private int gradeFlashcard(AttemptAnswer answer, Question question) {
        return answer.getSelectedOptionIds().contains("true") ? 1 : 0;
    }
}
