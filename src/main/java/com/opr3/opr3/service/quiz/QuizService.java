package com.opr3.opr3.service.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opr3.opr3.dto.quiz.CreateQuizRequest;
import com.opr3.opr3.dto.quiz.QuizBasicResponse;
import com.opr3.opr3.dto.quiz.QuizDetailedResponse;
import com.opr3.opr3.dto.quiz.QuizDetailedResponse.ChoiceOptionInfo;
import com.opr3.opr3.dto.quiz.QuizDetailedResponse.MatchPairInfo;
import com.opr3.opr3.dto.quiz.QuizDetailedResponse.OrderItemInfo;
import com.opr3.opr3.entity.Category;
import com.opr3.opr3.entity.Quiz;
import com.opr3.opr3.entity.User;
import com.opr3.opr3.entity.attempt.QuizAttempt;
import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.enums.QuestionType;
import com.opr3.opr3.exception.ResourceNotFoundException;
import com.opr3.opr3.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final AuthUtilService authUtilService;
    private final QuestionFactory questionFactory;

    @Transactional
    public QuizBasicResponse createQuiz(CreateQuizRequest request) {
        User user = authUtilService.getAuthenticatedUser();

        Quiz quiz = Quiz.builder()
                .author(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimit(request.getTimeLimit())
                .icon(request.getIcon())
                .build();

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(
                    categoryRepository.findAllById(request.getCategoryIds()));
            quiz.setCategories(categories);
        }

        List<Question> questions = IntStream.range(0, request.getQuestions().size())
                .mapToObj(i -> questionFactory.create(request.getQuestions().get(i), i + 1, quiz))
                .toList();
        quiz.setQuestions(questions);

        Quiz saved = quizRepository.save(quiz);
        return QuizBasicResponse.from(saved);
    }

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
        result = shuffleQuestionOptions(result);
        return result;
    }

    private Double getPreviousAttemptScorePercentage(Long quizId) {
        User user = authUtilService.getAuthenticatedUser();

        Optional<QuizAttempt> previousAttempt = quizAttemptRepository
                .findTopByQuizIdAndUserUidAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(
                        quizId, user.getUid());

        return previousAttempt.map(QuizAttempt::getScorePercentage).orElse(null);
    }

    private QuizDetailedResponse shuffleQuestionOptions(QuizDetailedResponse quizResponse) {
        quizResponse.getQuestions().forEach(q -> {
            if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
                List<ChoiceOptionInfo> shuffled = new ArrayList<>(q.getChoiceOptions());
                Collections.shuffle(shuffled);
                q.setChoiceOptions(shuffled);
            } else if (q.getType() == QuestionType.MATCHING) {
                List<MatchPairInfo> shuffled = new ArrayList<>(q.getMatchPairs());
                Collections.shuffle(shuffled);
                q.setMatchPairs(shuffled);
            } else if (q.getType() == QuestionType.ORDERING) {
                List<OrderItemInfo> shuffled = new ArrayList<>(q.getOrderItems());
                Collections.shuffle(shuffled);
                q.setOrderItems(shuffled);
            }
        });
        return quizResponse;
    }

}
