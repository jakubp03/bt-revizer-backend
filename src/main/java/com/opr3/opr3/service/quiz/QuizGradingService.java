package com.opr3.opr3.service.quiz;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.opr3.opr3.entity.attempt.AttemptAnswer;
import com.opr3.opr3.entity.attempt.IdBasedAttemptAnswer;
import com.opr3.opr3.entity.attempt.MatchBasedAttemptAnswer;
import com.opr3.opr3.entity.attempt.MatchedPairEntry;
import com.opr3.opr3.entity.attempt.QuizAttempt;
import com.opr3.opr3.entity.attempt.TextBasedAttemptAnswer;
import com.opr3.opr3.entity.question.OptionChoice;
import com.opr3.opr3.entity.question.OptionOrderItem;
import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.entity.question.TextAnswerConfig;
import com.opr3.opr3.enums.TextReviewType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizGradingService {

    private static final Logger log = LoggerFactory.getLogger(QuizGradingService.class);

    private final OllamaService ollamaService;

    /**
     * Grades the quiz attempt by evaluating each answer against the correct
     * answers.
     * Sets the score on the attempt and returns the score percentage.
     *
     * @return score percentage (0.0 - 100.0)
     */
    public double gradeQuiz(QuizAttempt attempt) {
        double totalScore = 0;

        for (AttemptAnswer answer : attempt.getAnswers()) {
            Question question = answer.getQuestion();
            int pointsAwarded = switch (question.getType()) {
                case SINGLE_CHOICE, TRUE_FALSE -> gradeSingleChoice((IdBasedAttemptAnswer) answer, question);
                case MULTIPLE_CHOICE -> gradeMultipleChoice((IdBasedAttemptAnswer) answer, question);
                case TEXT_INPUT -> gradeTextInput((TextBasedAttemptAnswer) answer, question);
                case MATCHING -> gradeMatching((MatchBasedAttemptAnswer) answer, question);
                case ORDERING -> gradeOrdering((IdBasedAttemptAnswer) answer, question);
                case FLASHCARD -> gradeFlashcard((IdBasedAttemptAnswer) answer, question);
            };

            answer.setPointsAwarded(pointsAwarded);
            answer.setIsAnswerCorrect(pointsAwarded == question.getPoints());
            totalScore += pointsAwarded;
        }

        attempt.setScore(totalScore);
        return attempt.getMaxScore() > 0 ? (totalScore / attempt.getMaxScore()) * 100.0 : 0.0;
    }

    /**
     * SINGLE_CHOICE / TRUE_FALSE: exactly one selected option, 1 point if it's
     * the correct one.
     */
    private int gradeSingleChoice(IdBasedAttemptAnswer answer, Question question) {
        if (answer.getSelectedOptionIds().isEmpty()) {
            return 0;
        }
        Long selectedId = answer.getSelectedOptionIds().get(0);
        return question.getChoiceOptions().stream()
                .filter(opt -> opt.getId().equals(selectedId) && opt.isCorrect())
                .findFirst()
                .map(opt -> 1)
                .orElse(0);
    }

    /**
     * MULTIPLE_CHOICE: 1 point for each correctly selected correct option.
     */
    private int gradeMultipleChoice(IdBasedAttemptAnswer answer, Question question) {
        Set<Long> selectedIds = new HashSet<>(answer.getSelectedOptionIds());
        int points = 0;
        for (OptionChoice choice : question.getChoiceOptions()) {
            if (choice.isCorrect() && selectedIds.contains(choice.getId())) {
                points++;
            }
        }
        return points;
    }

    /**
     * TEXT_INPUT:
     * - MANUAL review: the user self-evaluates before submission;
     * isAnswerCorrect is already set from the DTO's userMarkedCorrect.
     * - AUTOMATIC review: LLM comparison against the stored correct answer.
     */
    private int gradeTextInput(TextBasedAttemptAnswer answer, Question question) {
        TextAnswerConfig config = question.getTextConfig();
        if (config == null) {
            return 0;
        }

        if (config.getTextReviewType() == TextReviewType.MANUAL) {
            return Boolean.TRUE.equals(answer.getIsAnswerCorrect()) ? 1 : 0;
        }

        // AUTOMATIC review via LLM
        String correctAnswer = config.getCorrectAnswer();
        String userAnswer = answer.getSubmittedAnswer();
        if (correctAnswer == null || userAnswer == null) {
            log.warn("[Grading] TEXT_INPUT questionId={} skipped LLM check — correctAnswer or userAnswer is null",
                    answer.getQuestion().getId());
            return 0;
        }

        log.info("automatic question review initialized");

        return ollamaService.isAnswerCorrect(
                answer.getQuestion().getQuestionText(), correctAnswer, userAnswer) ? 1 : 0;
    }

    /**
     * MATCHING: each MatchedPairEntry has leftId and rightId.
     * A match is correct when both IDs refer to the same OptionMatchPair
     * (i.e. the student paired the left side with its own right side).
     */
    private int gradeMatching(MatchBasedAttemptAnswer answer, Question question) {
        int points = 0;
        for (MatchedPairEntry pair : answer.getMatchedPairs()) {
            if (pair.getLeftId().equals(pair.getRightId())) {
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
    private int gradeOrdering(IdBasedAttemptAnswer answer, Question question) {
        Map<Long, Integer> correctPositions = question.getOrderItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getId(),
                        OptionOrderItem::getCorrectPosition));

        int points = 0;
        for (int i = 0; i < answer.getSelectedOptionIds().size(); i++) {
            Integer correctPos = correctPositions.get(answer.getSelectedOptionIds().get(i));
            if (correctPos != null && correctPos == i + 1) {
                points++;
            }
        }
        return points;
    }

    /**
     * FLASHCARD: the user self-evaluates recall; selectedOptionIds contains
     * the flashcard option ID if they recalled correctly, empty otherwise.
     */
    private int gradeFlashcard(IdBasedAttemptAnswer answer, Question question) {
        return !answer.getSelectedOptionIds().isEmpty() ? 1 : 0;
    }
}
