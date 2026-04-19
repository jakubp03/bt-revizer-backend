package com.opr3.opr3.service.quiz;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opr3.opr3.dto.quiz.QuizBasicResponse;
import com.opr3.opr3.dto.quiz.QuizDetailedResponse;
import com.opr3.opr3.entity.Quiz;
import com.opr3.opr3.entity.User;
import com.opr3.opr3.entity.attempt.QuizAttempt;
import com.opr3.opr3.exception.ResourceNotFoundException;
import com.opr3.opr3.repository.QuizRepository;
import com.opr3.opr3.repository.attempt.QuizAttemptRepository;
import com.opr3.opr3.service.auth.AuthUtilService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AuthUtilService authUtilService;

    public List<QuizBasicResponse> getAllQuizzes() {
        User user = authUtilService.getAuthenticatedUser();

        return quizRepository.findByAuthorUidOrderByCreatedAtDesc(user.getUid()).stream()
                .map(QuizBasicResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizDetailedResponse getQuizById(Long quizId) {
        User user = authUtilService.getAuthenticatedUser();

        Quiz quiz = quizRepository.findByIdAndAuthorUid(quizId, user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz not found with ID: " + quizId));

        QuizDetailedResponse result = QuizDetailedResponse.from(quiz);
        result.setPreviousAttemptScorePercentage(getPreviousAttemptScorePercentage(quiz.getId()));
        result.setAverageScorePercentage(quizAttemptRepository.findAverageScorePercentageByQuizId(quiz.getId()));
        return result;
    }

    private Double getPreviousAttemptScorePercentage(Long quizId) {
        User user = authUtilService.getAuthenticatedUser();

        Optional<QuizAttempt> previousAttempt = quizAttemptRepository
                .findTopByQuizIdAndUserUidAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(
                        quizId, user.getUid());

        return previousAttempt.map(QuizAttempt::getScorePercentage).orElse(null);
    }

}
