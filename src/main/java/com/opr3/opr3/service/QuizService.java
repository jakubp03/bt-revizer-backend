package com.opr3.opr3.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opr3.opr3.dto.AnswerSubmission;
import com.opr3.opr3.dto.QuizResponse;
import com.opr3.opr3.dto.QuizResultResponse;
import com.opr3.opr3.dto.SubmitQuizRequest;
import com.opr3.opr3.entity.Quiz;
import com.opr3.opr3.entity.User;
import com.opr3.opr3.entity.attempt.AttemptAnswer;
import com.opr3.opr3.entity.attempt.QuizAttempt;
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

        for (AnswerSubmission answerSubmission : request.getAnswers()) {
            Question question = questionMap.get(answerSubmission.getQuestionId());

            AttemptAnswer attemptAnswer = AttemptAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedOptionIds(answerSubmission.getSelectedOptionIds() != null
                            ? new ArrayList<>(answerSubmission.getSelectedOptionIds())
                            : new ArrayList<>())
                    .textAnswer(answerSubmission.getTextAnswer())
                    .build();

            attempt.getAnswers().add(attemptAnswer);
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

    private Quiz validateQuiz(SubmitQuizRequest request) {
        if (request.getQuizId() == null) {
            throw new InvalidRequestException("Quiz ID is required");
        }

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz not found with ID: " + request.getQuizId()));

        if (request.getAnswers() == null) {
            throw new InvalidRequestException("Answers list cannot be null");
        }

        Map<Long, Question> questionMap = quiz.getIdQuestionMap();

        Set<Long> seen = new HashSet<>();
        for (AnswerSubmission answer : request.getAnswers()) {
            if (answer.getQuestionId() == null) {
                throw new InvalidRequestException("Question ID is required for each answer");
            }

            if (!questionMap.containsKey(answer.getQuestionId())) {
                throw new InvalidRequestException(
                        "Question ID " + answer.getQuestionId() + " does not belong to this quiz");
            }

            if (!seen.add(answer.getQuestionId())) {
                throw new InvalidRequestException(
                        "Duplicate answer for question ID: " + answer.getQuestionId());
            }

            validateAnswerStructure(answer, questionMap.get(answer.getQuestionId()));
        }

        return quiz;
    }

    private void validateAnswerStructure(AnswerSubmission answer, Question question) {
        QuestionType type = question.getType();
        boolean hasOptions = answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().isEmpty();
        boolean hasText = answer.getTextAnswer() != null && !answer.getTextAnswer().isBlank();

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
            case TEXT_INPUT -> {
                if (!hasText) {
                    throw new InvalidRequestException(
                            "Question " + question.getId() + " (TEXT_INPUT) requires a text answer");
                }
            }
            case MATCHING -> {
                if (!hasOptions) {
                    throw new InvalidRequestException(
                            "Question " + question.getId() + " (MATCHING) requires selected option pairs");
                }
            }
            case ORDERING -> {
                if (!hasOptions) {
                    throw new InvalidRequestException(
                            "Question " + question.getId() + " (ORDERING) requires ordered option IDs");
                }
            }
            case FLASHCARD -> {
                if (!hasText && !hasOptions) {
                    throw new InvalidRequestException(
                            "Question " + question.getId() + " (FLASHCARD) requires an answer");
                }
            }
        }
    }

    public List<QuizResponse> tempMethod() {
        User user = authUtilService.getAuthenticatedUser();
        return quizRepository.findByAuthorUidOrderByCreatedAtDesc(user.getUid()).stream()
                .map(QuizResponse::from)
                .toList();
    }
}
