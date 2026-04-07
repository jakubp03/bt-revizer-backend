package com.opr3.opr3.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opr3.opr3.dto.IdBasedAnswerSubmission;
import com.opr3.opr3.dto.MatchBasedAnswerSubmission;
import com.opr3.opr3.dto.QuizResponse;
import com.opr3.opr3.dto.QuizResultResponse;
import com.opr3.opr3.dto.SubmitQuizRequest;
import com.opr3.opr3.dto.TextBasedAnswerSubmission;
import com.opr3.opr3.entity.Quiz;
import com.opr3.opr3.entity.User;
import com.opr3.opr3.entity.attempt.IdBasedAttemptAnswer;
import com.opr3.opr3.entity.attempt.MatchBasedAttemptAnswer;
import com.opr3.opr3.entity.attempt.MatchedPairEntry;
import com.opr3.opr3.entity.attempt.QuizAttempt;
import com.opr3.opr3.entity.attempt.TextBasedAttemptAnswer;
import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.enums.QuestionType;
import com.opr3.opr3.exception.InvalidRequestException;
import com.opr3.opr3.exception.ResourceNotFoundException;
import com.opr3.opr3.repository.QuizRepository;
import com.opr3.opr3.repository.attempt.QuizAttemptRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizGradingService quizGradingService;
    private final AuthUtilService authUtilService;

    @Transactional
    public QuizResultResponse processQuiz(SubmitQuizRequest request) {
        User user = authUtilService.getAuthenticatedUser();

        // 1. Validate the submitted quiz
        Quiz quiz = validateQuiz(request);

        // 2. Get previous attempt score BEFORE saving the new one
        Optional<QuizAttempt> previousAttempt = quizAttemptRepository
                .findTopByQuizIdAndUserUidAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(
                        quiz.getId(), user.getUid());

        // 3. Build the attempt entity
        int maxScore = quiz.getQuestions().stream()
                .mapToInt(Question::getPoints)
                .sum();

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .submittedAt(LocalDateTime.now())
                .maxScore(maxScore)
                .timeSpent(request.getTimeSpent())
                .build();

        // 4. Build AttemptAnswer entities from submitted answers
        Map<Long, Question> questionMap = quiz.getIdQuestionMap();

        for (IdBasedAnswerSubmission sub : safeList(request.getIdBasedAnswers())) {
            Question question = questionMap.get(sub.getQuestionId());

            IdBasedAttemptAnswer answer = IdBasedAttemptAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedOptionIds(sub.getSelectedOptionIds() != null
                            ? new ArrayList<>(sub.getSelectedOptionIds())
                            : new ArrayList<>())
                    .build();

            attempt.getAnswers().add(answer);
        }

        for (TextBasedAnswerSubmission sub : safeList(request.getTextBasedAnswers())) {
            Question question = questionMap.get(sub.getQuestionId());

            TextBasedAttemptAnswer answer = TextBasedAttemptAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .submittedAnswer(sub.getSubmittedAnswer())
                    .isAnswerCorrect(sub.getUserMarkedCorrect())
                    .build();

            attempt.getAnswers().add(answer);
        }

        for (MatchBasedAnswerSubmission sub : safeList(request.getMatchBasedAttemptAnswer())) {
            Question question = questionMap.get(sub.getQuestionId());

            List<MatchedPairEntry> pairs = sub.getMatchedPairs() != null
                    ? sub.getMatchedPairs().stream()
                            .map(p -> new MatchedPairEntry(p.getLeftId(), p.getRightId()))
                            .toList()
                    : new ArrayList<>();

            MatchBasedAttemptAnswer answer = MatchBasedAttemptAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .matchedPairs(pairs)
                    .build();

            attempt.getAnswers().add(answer);
        }

        // 5. Grade the quiz
        double scorePercentage = quizGradingService.gradeQuiz(attempt);

        // 6. Save the attempt
        quizAttemptRepository.save(attempt);

        // 7. Compute average score across ALL attempts of this quiz
        Double averageScore = quizAttemptRepository.findAverageScorePercentageByQuizId(quiz.getId());

        // 8. Compute previous attempt score percentage
        Double previousScorePercentage = previousAttempt.map(QuizAttempt::getScorePercentage).orElse(null);

        log.info("Quiz submitted: quizId={}, attemptId={}, score={}%, previous={}%, avg={}%",
                quiz.getId(), attempt.getId(), scorePercentage,
                previousScorePercentage, averageScore);

        return QuizResultResponse.builder()
                .attemptId(attempt.getId())
                .scorePercentage(scorePercentage)
                .previousAttemptScorePercentage(previousScorePercentage)
                .averageScorePercentage(averageScore != null ? averageScore : scorePercentage)
                .build();
    }

    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

    private Quiz validateQuiz(SubmitQuizRequest request) {
        if (request.getQuizId() == null) {
            throw new InvalidRequestException("Quiz ID is required");
        }

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz not found with ID: " + request.getQuizId()));

        Map<Long, Question> questionMap = quiz.getIdQuestionMap();
        Set<Long> seen = new HashSet<>();

        for (IdBasedAnswerSubmission answer : safeList(request.getIdBasedAnswers())) {
            validateCommonAnswer(answer.getQuestionId(), questionMap, seen);
            validateIdBasedAnswer(answer, questionMap.get(answer.getQuestionId()));
        }

        for (TextBasedAnswerSubmission answer : safeList(request.getTextBasedAnswers())) {
            validateCommonAnswer(answer.getQuestionId(), questionMap, seen);
            validateTextBasedAnswer(answer, questionMap.get(answer.getQuestionId()));
        }

        for (MatchBasedAnswerSubmission answer : safeList(request.getMatchBasedAttemptAnswer())) {
            validateCommonAnswer(answer.getQuestionId(), questionMap, seen);
            validateMatchBasedAnswer(answer, questionMap.get(answer.getQuestionId()));
        }

        return quiz;
    }

    private void validateCommonAnswer(Long questionId, Map<Long, Question> questionMap, Set<Long> seen) {
        if (questionId == null) {
            throw new InvalidRequestException("Question ID is required for each answer");
        }
        if (!questionMap.containsKey(questionId)) {
            throw new InvalidRequestException(
                    "Question ID " + questionId + " does not belong to this quiz");
        }
        if (!seen.add(questionId)) {
            throw new InvalidRequestException(
                    "Duplicate answer for question ID: " + questionId);
        }
    }

    private void validateIdBasedAnswer(IdBasedAnswerSubmission answer, Question question) {
        QuestionType type = question.getType();
        boolean hasOptions = answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().isEmpty();

        switch (type) {
            case SINGLE_CHOICE, TRUE_FALSE -> {
                if (!hasOptions) {
                    throw new InvalidRequestException(
                            "Question " + question.getId() + " (" + type + ") requires a selected option");
                }
                if (answer.getSelectedOptionIds().size() != 1) {
                    throw new InvalidRequestException(
                            "Question " + question.getId() + " (" + type + ") requires exactly one selected option");
                }
            }
            case MULTIPLE_CHOICE -> {
                if (!hasOptions) {
                    throw new InvalidRequestException(
                            "Question " + question.getId()
                                    + " (MULTIPLE_CHOICE) requires at least one selected option");
                }
            }
            case ORDERING -> {
                if (!hasOptions) {
                    throw new InvalidRequestException(
                            "Question " + question.getId() + " (ORDERING) requires ordered option IDs");
                }
            }
            case FLASHCARD -> {
                // flashcard is self-evaluated; selectedOptionIds may or may not be present
            }
            default -> throw new InvalidRequestException(
                    "Question " + question.getId() + " (" + type + ") should not be in idBasedAnswers");
        }
    }

    private void validateTextBasedAnswer(TextBasedAnswerSubmission answer, Question question) {
        if (question.getType() != QuestionType.TEXT_INPUT) {
            throw new InvalidRequestException(
                    "Question " + question.getId() + " (" + question.getType() + ") should not be in textBasedAnswers");
        }
        if (answer.getSubmittedAnswer() == null || answer.getSubmittedAnswer().isBlank()) {
            throw new InvalidRequestException(
                    "Question " + question.getId() + " (TEXT_INPUT) requires a text answer");
        }
    }

    private void validateMatchBasedAnswer(MatchBasedAnswerSubmission answer, Question question) {
        if (question.getType() != QuestionType.MATCHING) {
            throw new InvalidRequestException(
                    "Question " + question.getId() + " (" + question.getType()
                            + ") should not be in matchBasedAnswers");
        }
        if (answer.getMatchedPairs() == null || answer.getMatchedPairs().isEmpty()) {
            throw new InvalidRequestException(
                    "Question " + question.getId() + " (MATCHING) requires matched pairs");
        }
    }

    public List<QuizResponse> tempMethod() {
        User user = authUtilService.getAuthenticatedUser();
        return quizRepository.findByAuthorUidOrderByCreatedAtDesc(user.getUid()).stream()
                .map(QuizResponse::from)
                .toList();
    }
}
