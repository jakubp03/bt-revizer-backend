package com.opr3.opr3.config;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.opr3.opr3.dto.quiz.CreateQuizRequest.ChoiceOptionRequest;
import com.opr3.opr3.dto.quiz.CreateQuizRequest.MatchPairRequest;
import com.opr3.opr3.dto.quiz.CreateQuizRequest.OrderItemRequest;
import com.opr3.opr3.dto.quiz.CreateQuizRequest.QuestionRequest;
import com.opr3.opr3.dto.quiz.CreateQuizRequest.TextConfigRequest;
import com.opr3.opr3.entity.Category;
import com.opr3.opr3.entity.Quiz;
import com.opr3.opr3.entity.User;
import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.enums.QuestionType;
import com.opr3.opr3.enums.TextReviewType;
import com.opr3.opr3.repository.CategoryRepository;
import com.opr3.opr3.repository.QuizRepository;
import com.opr3.opr3.repository.UserRepository;
import com.opr3.opr3.service.quiz.QuestionFactory;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class Config {

        private static final Logger log = LoggerFactory.getLogger(Config.class);

        @Value("${spring.profiles.active}")
        private String activeSpringProfile;
        @Value("${app.docker-profile}")
        private String activeDockerProfile;
        private final UserRepository userRepository;
        private final QuizRepository quizRepository;
        private final CategoryRepository categoryRepository;
        private final QuestionFactory questionFactory;

        @Bean
        CommandLineRunner commandLineRunner(ApplicationContext ctx) {
                return args -> {
                        initializeMinimalQuizData();
                        userRepository.findUserByEmail("test1@email.com")
                                        .ifPresent(this::initializeMockQuizData);
                        userRepository.findUserByEmail("test1@email.com")
                                        .ifPresent(this::initializeTextReviewQuizData);
                        userRepository.findUserByEmail("test1@email.com")
                                        .ifPresent(this::initializeTimerTestQuizData);
                };
        }

        @Transactional
        public void initializeMinimalQuizData() {
                if (activeDockerProfile.equals("docker_dev") || activeSpringProfile.equals("dev")) {

                        Optional<User> testUserOneOptional = userRepository.findUserByEmail("test1@email.com");
                        Optional<User> testUserTwoOptional = userRepository.findUserByEmail("test2@email.com");

                        if (!testUserOneOptional.isPresent()) {
                                User testUser = new User("Test User One", this.passwordEncoder().encode("testuserone"),
                                                "test1@email.com");
                                userRepository.save(testUser);
                                log.info("adding missing test user 1");
                        }

                        if (!testUserTwoOptional.isPresent()) {
                                User testUser2 = new User("Test User Two", this.passwordEncoder().encode("testusertwo"),
                                                "test2@email.com");
                                userRepository.save(testUser2);
                                log.info("adding missing test user 2");
                        }
                }
        }

        @Transactional
        public void initializeMockQuizData(User author) {
                boolean alreadyExists = quizRepository.findByAuthorUidOrderByCreatedAtDesc(author.getUid())
                                .stream().anyMatch(t -> "Mock Grading Quiz".equals(t.getTitle()));
                if (alreadyExists) {
                        log.info("Mock grading quiz already exists, skipping");
                        return;
                }

                Category category = categoryRepository.save(Category.builder()
                                .user(author)
                                .name("Mock Quiz Category")
                                .description("Category for grading logic testing")
                                .color("#6366F1")
                                .build());

                Quiz quiz = Quiz.builder()
                                .author(author)
                                .title("Mock Grading Quiz")
                                .description("One question of every type — use this to test grading logic.")
                                .icon("🧪")
                                .categories(Set.of(category))
                                .build();

                List<QuestionRequest> questionRequests = List.of(
                                QuestionRequest.builder()
                                                .type(QuestionType.SINGLE_CHOICE)
                                                .questionText("What is the capital of France?")
                                                .points(1)
                                                .choiceOptions(List.of(
                                                                ChoiceOptionRequest.builder().text("Paris")
                                                                                .isCorrect(true).build(),
                                                                ChoiceOptionRequest.builder().text("London")
                                                                                .isCorrect(false).build(),
                                                                ChoiceOptionRequest.builder().text("Berlin")
                                                                                .isCorrect(false).build(),
                                                                ChoiceOptionRequest.builder().text("Madrid")
                                                                                .isCorrect(false).build()))
                                                .build(),
                                QuestionRequest.builder()
                                                .type(QuestionType.MULTIPLE_CHOICE)
                                                .questionText("Which of the following are programming languages?")
                                                .points(3)
                                                .choiceOptions(List.of(
                                                                ChoiceOptionRequest.builder().text("Java")
                                                                                .isCorrect(true).build(),
                                                                ChoiceOptionRequest.builder().text("Python")
                                                                                .isCorrect(true).build(),
                                                                ChoiceOptionRequest.builder().text("HTML")
                                                                                .isCorrect(false).build(),
                                                                ChoiceOptionRequest.builder().text("JavaScript")
                                                                                .isCorrect(true).build()))
                                                .build(),
                                QuestionRequest.builder()
                                                .type(QuestionType.TRUE_FALSE)
                                                .questionText("The Earth is the third planet from the Sun.")
                                                .points(1)
                                                .correctAnswer(true)
                                                .build(),
                                QuestionRequest.builder()
                                                .type(QuestionType.TEXT_INPUT)
                                                .questionText("What does HTTP stand for?")
                                                .points(1)
                                                .textConfig(TextConfigRequest.builder()
                                                                .correctAnswer("HyperText Transfer Protocol")
                                                                .reviewType(TextReviewType.MANUAL)
                                                                .build())
                                                .build(),
                                QuestionRequest.builder()
                                                .type(QuestionType.MATCHING)
                                                .questionText("Match each country to its capital city.")
                                                .points(3)
                                                .matchPairs(List.of(
                                                                MatchPairRequest.builder().leftSide("France")
                                                                                .rightSide("Paris").build(),
                                                                MatchPairRequest.builder().leftSide("Germany")
                                                                                .rightSide("Berlin").build(),
                                                                MatchPairRequest.builder().leftSide("Japan")
                                                                                .rightSide("Tokyo").build()))
                                                .build(),
                                QuestionRequest.builder()
                                                .type(QuestionType.ORDERING)
                                                .questionText("Order the planets from closest to furthest from the Sun.")
                                                .points(4)
                                                .orderItems(List.of(
                                                                OrderItemRequest.builder().text("Mercury").build(),
                                                                OrderItemRequest.builder().text("Venus").build(),
                                                                OrderItemRequest.builder().text("Earth").build(),
                                                                OrderItemRequest.builder().text("Mars").build()))
                                                .build(),
                                QuestionRequest.builder()
                                                .type(QuestionType.FLASHCARD)
                                                .questionText("Time complexity of Binary Search?")
                                                .points(1)
                                                .flashcardBackText("O(log n)")
                                                .build());

                List<Question> questions = IntStream.range(0, questionRequests.size())
                                .mapToObj(i -> questionFactory.create(questionRequests.get(i), i + 1, quiz))
                                .toList();
                quiz.setQuestions(questions);

                quizRepository.save(quiz);
                log.info("Mock grading quiz created (7 questions, 11 total points)");
        }

        @Transactional
        public void initializeTextReviewQuizData(User author) {
                boolean alreadyExists = quizRepository.findByAuthorUidOrderByCreatedAtDesc(author.getUid())
                                .stream().anyMatch(t -> "Text Input Auto Review Quiz".equals(t.getTitle()));
                if (alreadyExists) {
                        log.info("Text review quizzes already exist, skipping");
                        return;
                }

                // --- Quiz 1: TEXT_INPUT with AUTOMATIC review ---
                Quiz autoQuiz = Quiz.builder()
                                .author(author)
                                .title("Text Input Auto Review Quiz")
                                .description("Single text input question graded automatically.")
                                .build();
                QuestionRequest autoRequest = QuestionRequest.builder()
                                .type(QuestionType.TEXT_INPUT)
                                .questionText("What is the chemical symbol for water?")
                                .points(1)
                                .textConfig(TextConfigRequest.builder()
                                                .correctAnswer("H2O")
                                                .reviewType(TextReviewType.AUTOMATIC)
                                                .build())
                                .build();
                autoQuiz.setQuestions(List.of(questionFactory.create(autoRequest, 1, autoQuiz)));
                quizRepository.save(autoQuiz);

                // --- Quiz 2: TEXT_INPUT with MANUAL review ---
                Quiz manualQuiz = Quiz.builder()
                                .author(author)
                                .title("Text Input Manual Review Quiz")
                                .description("Single text input question requiring manual grading.")
                                .build();
                QuestionRequest manualRequest = QuestionRequest.builder()
                                .type(QuestionType.TEXT_INPUT)
                                .questionText("Describe the main purpose of the HTTP protocol.")
                                .points(1)
                                .textConfig(TextConfigRequest.builder()
                                                .correctAnswer("HTTP is used to transfer data on the web.")
                                                .reviewType(TextReviewType.MANUAL)
                                                .build())
                                .build();
                manualQuiz.setQuestions(List.of(questionFactory.create(manualRequest, 1, manualQuiz)));
                quizRepository.save(manualQuiz);

                log.info("Text review quizzes created (auto + manual, 1 question each)");
        }

        @Transactional
        public void initializeTimerTestQuizData(User author) {
                boolean alreadyExists = quizRepository.findByAuthorUidOrderByCreatedAtDesc(author.getUid())
                                .stream().anyMatch(t -> "Timer Test Quiz".equals(t.getTitle()));
                if (alreadyExists) {
                        log.info("Timer test quiz already exists, skipping");
                        return;
                }

                Quiz quiz = Quiz.builder()
                                .author(author)
                                .title("Timer Test Quiz")
                                .description("One true/false question with a 10-second timer — use this to test timer behaviour.")
                                .icon("⏱️")
                                .timeLimit(10)
                                .build();
                QuestionRequest request = QuestionRequest.builder()
                                .type(QuestionType.TRUE_FALSE)
                                .questionText("Is Java a statically typed language?")
                                .points(1)
                                .correctAnswer(true)
                                .build();
                quiz.setQuestions(List.of(questionFactory.create(request, 1, quiz)));

                quizRepository.save(quiz);
                log.info("Timer test quiz created (1 true/false question, 10s limit)");
        }

        @Bean
        public UserDetailsService userDetailsService() {
                return username -> userRepository.findUserByEmail(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

}
