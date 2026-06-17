package com.opr3.opr3.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opr3.opr3.dto.attempt.AttemptBasicResponse;
import com.opr3.opr3.dto.attempt.AttemptReviewResponse;
import com.opr3.opr3.dto.attempt.AttemptSummaryResponse;
import com.opr3.opr3.dto.attempt.DashboardResponse;
import com.opr3.opr3.dto.attempt.IdBasedAnswerSubmission;
import com.opr3.opr3.dto.attempt.MatchBasedAnswerSubmission;
import com.opr3.opr3.dto.attempt.QuestionAttemptTimeInfo;
import com.opr3.opr3.dto.attempt.QuizResultResponse;
import com.opr3.opr3.dto.attempt.QuizStatsAnswer;
import com.opr3.opr3.dto.attempt.SubmitQuizRequest;
import com.opr3.opr3.dto.attempt.TextBasedAnswerSubmission;
import com.opr3.opr3.dto.quiz.QuizBasicResponse;
import com.opr3.opr3.entity.Quiz;
import com.opr3.opr3.entity.User;
import com.opr3.opr3.entity.attempt.AttemptAnswer;
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
import com.opr3.opr3.service.auth.AuthUtilService;
import com.opr3.opr3.service.quiz.QuizGradingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttemptService {

        private static final Logger log = LoggerFactory.getLogger(AttemptService.class);

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
                                        .timeSpent(sub.getTimeSpent())
                                        .build();

                        attempt.getAnswers().add(answer);
                }

                for (TextBasedAnswerSubmission sub : safeList(request.getTextBasedAnswers())) {
                        Question question = questionMap.get(sub.getQuestionId());

                        TextBasedAttemptAnswer answer = TextBasedAttemptAnswer.builder()
                                        .attempt(attempt)
                                        .question(question)
                                        .submittedAnswer(sub.getSubmittedAnswer())
                                        .userMarkedCorrect(sub.getUserMarkedCorrect())
                                        .timeSpent(sub.getTimeSpent())
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
                                        .timeSpent(sub.getTimeSpent())
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

        @Transactional(readOnly = true)
        public Page<AttemptSummaryResponse> getAllAttempts(Pageable pageable) {
                User user = authUtilService.getAuthenticatedUser();
                return quizAttemptRepository
                                .findByUserUidAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(user.getUid(), pageable)
                                .map(AttemptSummaryResponse::from);
        }

        @Transactional(readOnly = true)
        public List<AttemptBasicResponse> getAllAttemptsByQuizId(Long quizId) {
                return quizAttemptRepository
                                .findByQuizIdAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(quizId)
                                .stream()
                                .map(AttemptBasicResponse::from)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<AttemptBasicResponse> getAttemptsByQuizId(Long quizId) {
                User user = authUtilService.getAuthenticatedUser();
                return quizAttemptRepository
                                .findByQuizIdAndUserUidAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(quizId,
                                                user.getUid())
                                .stream()
                                .map(AttemptBasicResponse::from)
                                .toList();
        }

        @Transactional(readOnly = true)
        public AttemptReviewResponse getAttemptReview(Long attemptId) {
                User user = authUtilService.getAuthenticatedUser();

                QuizAttempt attempt = quizAttemptRepository.findByIdAndUserUid(attemptId, user.getUid())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Attempt not found with ID: " + attemptId));

                return AttemptReviewResponse.from(attempt);
        }

        @Transactional(readOnly = true)
        public QuizStatsAnswer getQuizStatsByQuizId(Long quizId) {
                User user = authUtilService.getAuthenticatedUser();

                List<QuizAttempt> attempts = quizAttemptRepository
                                .findByQuizIdAndUserUidAndSubmittedAtIsNotNullOrderBySubmittedAtAsc(quizId,
                                                user.getUid());

                Integer[] attemptTimes = attempts.stream()
                                .map(QuizAttempt::getTimeSpent)
                                .toArray(Integer[]::new);

                double[] scorePercentages = attempts.stream()
                                .mapToDouble(QuizAttempt::getScorePercentage)
                                .toArray();

                Double previousAttemptScorePercentage = attempts.size() >= 1
                                ? attempts.get(attempts.size() - 1).getScorePercentage()
                                : null;

                Map<Long, List<AttemptAnswer>> answersByQuestion = attempts.stream()
                                .flatMap(a -> a.getAnswers().stream())
                                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

                List<QuizStatsAnswer.AttemptAnswerInfo> questionAttempts = answersByQuestion.entrySet().stream()
                                .map(entry -> {
                                        List<AttemptAnswer> answers = entry.getValue();

                                        DescriptiveStatistics questionTimeStats = new DescriptiveStatistics();
                                        answers.stream()
                                                        .filter(a -> a.getTimeSpent() != null)
                                                        .mapToInt(AttemptAnswer::getTimeSpent)
                                                        .forEach(questionTimeStats::addValue);

                                        DescriptiveStatistics questionScoreStats = new DescriptiveStatistics();
                                        answers.stream()
                                                        .mapToDouble(AttemptAnswer::getScorePercentage)
                                                        .forEach(questionScoreStats::addValue);

                                        QuestionAttemptTimeInfo questionInfo = calculateQuestionAttemptTimeValues(
                                                        questionTimeStats);

                                        return QuizStatsAnswer.AttemptAnswerInfo.builder()
                                                        .questionId(entry.getKey())
                                                        .minQuestionAttemptTime(
                                                                        questionInfo.getMinQuestionAttemptTime())
                                                        .q1QuestionAttemptTime(questionInfo.getQ1QuestionAttemptTime())
                                                        .medQuestionAttemptTime(
                                                                        questionInfo.getMedQuestionAttemptTime())
                                                        .q3QuestionAttemptTime(questionInfo.getQ3QuestionAttemptTime())
                                                        .maxQuestionAttemptTime(
                                                                        questionInfo.getMaxQuestionAttemptTime())
                                                        .outliers(questionInfo.getOutliers())
                                                        .medQuestionScorePercentage(
                                                                        questionScoreStats.getPercentile(50))
                                                        .build();
                                })
                                .toList();

                return new QuizStatsAnswer(attemptTimes, scorePercentages, previousAttemptScorePercentage,
                                questionAttempts);
        }

        @Transactional(readOnly = true)
        public DashboardResponse getDashboard() {
                User user = authUtilService.getAuthenticatedUser();

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime weekStart = now.minusDays(7);
                LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

                List<QuizAttempt> weekAttempts = quizAttemptRepository
                                .findSubmittedByUserAndDateRange(user.getUid(), weekStart, now);

                int submittedCount = weekAttempts.size();

                int totalTimeSpent = weekAttempts.stream()
                                .map(QuizAttempt::getTimeSpent)
                                .filter(Objects::nonNull)
                                .mapToInt(Integer::intValue)
                                .sum();

                OptionalDouble avgOpt = weekAttempts.stream()
                                .mapToDouble(QuizAttempt::getScorePercentage)
                                .average();
                Double averageScorePercentage = avgOpt.isPresent() ? avgOpt.getAsDouble() : null;

                Set<Long> seenIds = new LinkedHashSet<>();
                List<QuizBasicResponse> recentQuizzes = weekAttempts.stream()
                                .map(QuizAttempt::getQuiz)
                                .filter(q -> seenIds.add(q.getId()))
                                .limit(5)
                                .map(QuizBasicResponse::from)
                                .toList();

                List<QuizAttempt> monthAttempts = quizAttemptRepository
                                .findSubmittedByUserAndDateRange(user.getUid(), monthStart, now);

                Map<LocalDate, Integer> monthlyActivity = monthAttempts.stream()
                                .collect(Collectors.groupingBy(
                                                a -> a.getSubmittedAt().toLocalDate(),
                                                TreeMap::new,
                                                Collectors.collectingAndThen(Collectors.counting(),
                                                                Long::intValue)));

                return DashboardResponse.builder()
                                .totalTimeSpent(totalTimeSpent)
                                .submittedQuizzesCount(submittedCount)
                                .averageScorePercentage(averageScorePercentage)
                                .recentQuizzes(recentQuizzes)
                                .monthlyActivity(monthlyActivity)
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
                                                        "Question " + question.getId() + " (" + type
                                                                        + ") requires a selected option");
                                }
                                if (answer.getSelectedOptionIds().size() != 1) {
                                        throw new InvalidRequestException(
                                                        "Question " + question.getId() + " (" + type
                                                                        + ") requires exactly one selected option");
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
                                                        "Question " + question.getId()
                                                                        + " (ORDERING) requires ordered option IDs");
                                }
                        }
                        case FLASHCARD -> {
                                // flashcard is self-evaluated; selectedOptionIds may or may not be present
                        }
                        default -> throw new InvalidRequestException(
                                        "Question " + question.getId() + " (" + type
                                                        + ") should not be in idBasedAnswers");
                }
        }

        private void validateTextBasedAnswer(TextBasedAnswerSubmission answer, Question question) {
                if (question.getType() != QuestionType.TEXT_INPUT) {
                        throw new InvalidRequestException(
                                        "Question " + question.getId() + " (" + question.getType()
                                                        + ") should not be in textBasedAnswers");
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

        private QuestionAttemptTimeInfo calculateQuestionAttemptTimeValues(DescriptiveStatistics questionTimeStats) {
                if (questionTimeStats.getN() == 0) {
                        return new QuestionAttemptTimeInfo(null, null, null, null, null, null);
                }

                double q1 = questionTimeStats.getPercentile(25);
                double q3 = questionTimeStats.getPercentile(75);
                double median = questionTimeStats.getPercentile(50);
                double iqr = q3 - q1;
                double lowerFence = q1 - 1.5 * iqr;
                double upperFence = q3 + 1.5 * iqr;

                double[] sorted = questionTimeStats.getSortedValues();

                double whiskerLow = Arrays.stream(sorted).filter(v -> v >= lowerFence).findFirst().orElse(q1);
                double whiskerHigh = Arrays.stream(sorted).filter(v -> v <= upperFence).reduce((a, b) -> b).orElse(q3);

                double[] outliers = Arrays.stream(sorted)
                                .filter(v -> v < lowerFence || v > upperFence)
                                .toArray();

                Integer[] outliersInt = Arrays.stream(outliers)
                                .mapToObj(v -> (int) v)
                                .toArray(Integer[]::new);

                return new QuestionAttemptTimeInfo(
                                (int) whiskerLow,
                                (int) q1,
                                (int) median,
                                (int) q3,
                                (int) whiskerHigh,
                                outliersInt);
        }
}
