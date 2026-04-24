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
            double pointsAwarded = switch (question.getType()) {
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
     * SINGLE_CHOICE / TRUE_FALSE: full question points if the selected option is
     * correct, 0 otherwise.
     */
    private double gradeSingleChoice(IdBasedAttemptAnswer answer, Question question) {
        if (answer.getSelectedOptionIds().isEmpty()) {
            return 0;
        }
        Long selectedId = answer.getSelectedOptionIds().get(0);
        boolean correct = question.getChoiceOptions().stream()
                .anyMatch(opt -> opt.getId().equals(selectedId) && opt.isCorrect());
        return correct ? question.getPoints() : 0;
    }

    /**
     * MULTIPLE_CHOICE: weighted — (correct selected / total correct) × question
     * points.
     */
    private double gradeMultipleChoice(IdBasedAttemptAnswer answer, Question question) {
        long totalCorrect = question.getChoiceOptions().stream().filter(OptionChoice::isCorrect).count();
        if (totalCorrect == 0)
            return 0;
        Set<Long> selectedIds = new HashSet<>(answer.getSelectedOptionIds());
        long correctSelected = question.getChoiceOptions().stream()
                .filter(opt -> opt.isCorrect() && selectedIds.contains(opt.getId()))
                .count();
        return ((double) correctSelected / totalCorrect) * question.getPoints();
    }

    /**
     * TEXT_INPUT:
     * - MANUAL review: the user self-evaluates; full points or 0.
     * - AUTOMATIC review: LLM comparison; full points or 0.
     */
    private double gradeTextInput(TextBasedAttemptAnswer answer, Question question) {
        TextAnswerConfig config = question.getTextConfig();
        if (config == null) {
            return 0;
        }

        if (config.getTextReviewType() == TextReviewType.MANUAL) {
            return Boolean.TRUE.equals(answer.getIsAnswerCorrect()) ? question.getPoints() : 0;
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
                answer.getQuestion().getQuestionText(), correctAnswer, userAnswer) ? question.getPoints() : 0;
    }

    /**
     * MATCHING: weighted — (correct pairs / total pairs) × question points.
     * A pair is correct when leftId equals rightId (same OptionMatchPair row).
     */
    private double gradeMatching(MatchBasedAttemptAnswer answer, Question question) {
        int totalPairs = answer.getMatchedPairs().size();
        if (totalPairs == 0)
            return 0;
        long correctPairs = answer.getMatchedPairs().stream()
                .filter(pair -> pair.getLeftId().equals(pair.getRightId()))
                .count();
        return ((double) correctPairs / totalPairs) * question.getPoints();
    }

    /**
     * ORDERING: weighted — (items in correct position / total items) × question
     * points.
     */
    private double gradeOrdering(IdBasedAttemptAnswer answer, Question question) {
        int totalItems = question.getOrderItems().size();
        if (totalItems == 0)
            return 0;
        Map<Long, Integer> correctPositions = question.getOrderItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getId(),
                        OptionOrderItem::getCorrectPosition));
        long correctCount = 0;
        for (int i = 0; i < answer.getSelectedOptionIds().size(); i++) {
            Integer correctPos = correctPositions.get(answer.getSelectedOptionIds().get(i));
            if (correctPos != null && correctPos == i + 1) {
                correctCount++;
            }
        }
        return ((double) correctCount / totalItems) * question.getPoints();
    }

    /**
     * FLASHCARD: full question points if the user marked recall, 0 otherwise.
     */
    private double gradeFlashcard(IdBasedAttemptAnswer answer, Question question) {
        return !answer.getSelectedOptionIds().isEmpty() ? question.getPoints() : 0;
    }
}
