package com.opr3.opr3.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import com.opr3.opr3.entity.Category;
import com.opr3.opr3.entity.Quiz;
import com.opr3.opr3.entity.User;
import com.opr3.opr3.entity.question.OptionChoice;
import com.opr3.opr3.entity.question.OptionFlashcard;
import com.opr3.opr3.entity.question.OptionMatchPair;
import com.opr3.opr3.entity.question.OptionOrderItem;
import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.entity.question.TextAnswerConfig;
import com.opr3.opr3.enums.QuestionType;
import com.opr3.opr3.enums.QuizGradingMethod;
import com.opr3.opr3.enums.TextReviewType;
import com.opr3.opr3.repository.CategoryRepository;
import com.opr3.opr3.repository.QuizRepository;
import com.opr3.opr3.repository.UserRepository;

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

        @Bean
        CommandLineRunner commandLineRunner(ApplicationContext ctx) {
                return args -> {
                        initializeMinimalQuizData();
                        userRepository.findUserByEmail("test1@email.com")
                                        .ifPresent(this::initializeMockQuizData);
                        userRepository.findUserByEmail("test1@email.com")
                                        .ifPresent(this::initializeTextReviewQuizData);
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

                // --- Q1: SINGLE_CHOICE (1 correct → 1 point) ---
                Question q1 = Question.builder()
                                .questionOrder(1)
                                .type(QuestionType.SINGLE_CHOICE)
                                .questionText("What is the capital of France?")
                                .points(1)
                                .build();
                q1.setChoiceOptions(List.of(
                                OptionChoice.builder().question(q1).text("Paris").isCorrect(true).optionOrder(1)
                                                .build(),
                                OptionChoice.builder().question(q1).text("London").isCorrect(false).optionOrder(2)
                                                .build(),
                                OptionChoice.builder().question(q1).text("Berlin").isCorrect(false).optionOrder(3)
                                                .build(),
                                OptionChoice.builder().question(q1).text("Madrid").isCorrect(false).optionOrder(4)
                                                .build()));

                // --- Q2: MULTIPLE_CHOICE (3 correct items → 3 points) ---
                Question q2 = Question.builder()
                                .questionOrder(2)
                                .type(QuestionType.MULTIPLE_CHOICE)
                                .questionText("Which of the following are programming languages?")
                                .points(3)
                                .build();
                q2.setChoiceOptions(List.of(
                                OptionChoice.builder().question(q2).text("Java").isCorrect(true).optionOrder(1).build(),
                                OptionChoice.builder().question(q2).text("Python").isCorrect(true).optionOrder(2)
                                                .build(),
                                OptionChoice.builder().question(q2).text("HTML").isCorrect(false).optionOrder(3)
                                                .build(),
                                OptionChoice.builder().question(q2).text("JavaScript").isCorrect(true).optionOrder(4)
                                                .build()));

                // --- Q3: TRUE_FALSE (1 point) ---
                Question q3 = Question.builder()
                                .questionOrder(3)
                                .type(QuestionType.TRUE_FALSE)
                                .questionText("The Earth is the third planet from the Sun.")
                                .points(1)
                                .build();
                q3.setChoiceOptions(List.of(
                                OptionChoice.builder().question(q3).text("True").isCorrect(true).optionOrder(1).build(),
                                OptionChoice.builder().question(q3).text("False").isCorrect(false).optionOrder(2)
                                                .build()));

                // --- Q4: TEXT_INPUT with MANUAL review (1 point) ---
                Question q4 = Question.builder()
                                .questionOrder(4)
                                .type(QuestionType.TEXT_INPUT)
                                .questionText("What does HTTP stand for?")
                                .points(1)
                                .build();
                TextAnswerConfig textConfig = TextAnswerConfig.builder()
                                .question(q4)
                                .correctAnswer("HyperText Transfer Protocol")
                                .textReviewType(TextReviewType.MANUAL)
                                .build();
                q4.setTextConfig(textConfig);

                // --- Q5: MATCHING (3 pairs → 3 points) ---
                Question q5 = Question.builder()
                                .questionOrder(5)
                                .type(QuestionType.MATCHING)
                                .questionText("Match each country to its capital city.")
                                .points(3)
                                .build();
                q5.setMatchPairs(List.of(
                                OptionMatchPair.builder().question(q5).leftSide("France").rightSide("Paris")
                                                .pairOrder(1).build(),
                                OptionMatchPair.builder().question(q5).leftSide("Germany").rightSide("Berlin")
                                                .pairOrder(2).build(),
                                OptionMatchPair.builder().question(q5).leftSide("Japan").rightSide("Tokyo").pairOrder(3)
                                                .build()));

                // --- Q6: ORDERING (4 items → 4 points) ---
                Question q6 = Question.builder()
                                .questionOrder(6)
                                .type(QuestionType.ORDERING)
                                .questionText("Order the planets from closest to furthest from the Sun.")
                                .points(4)
                                .build();
                q6.setOrderItems(List.of(
                                OptionOrderItem.builder().question(q6).text("Mercury").correctPosition(1).build(),
                                OptionOrderItem.builder().question(q6).text("Venus").correctPosition(2).build(),
                                OptionOrderItem.builder().question(q6).text("Earth").correctPosition(3).build(),
                                OptionOrderItem.builder().question(q6).text("Mars").correctPosition(4).build()));

                // --- Q7: FLASHCARD (1 point) ---
                Question q7 = Question.builder()
                                .questionOrder(7)
                                .type(QuestionType.FLASHCARD)
                                .questionText("Time complexity of Binary Search?")
                                .points(1)
                                .build();
                OptionFlashcard flashcard = OptionFlashcard.builder()
                                .question(q7)
                                .backText("O(log n)")
                                .build();
                q7.setFlashcardConfig(flashcard);

                List<Question> questions = new ArrayList<>(List.of(q1, q2, q3, q4, q5, q6, q7));

                Quiz quiz = Quiz.builder()
                                .author(author)
                                .title("Mock Grading Quiz")
                                .description("One question of every type — use this to test grading logic.")
                                .gradingMethod(QuizGradingMethod.OnePointPerAnswer)
                                .categories(Set.of(category))
                                .questions(questions)
                                .build();

                questions.forEach(q -> q.setQuiz(quiz));

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
                Question autoQ = Question.builder()
                                .questionOrder(1)
                                .type(QuestionType.TEXT_INPUT)
                                .questionText("What is the chemical symbol for water?")
                                .points(1)
                                .build();
                TextAnswerConfig autoConfig = TextAnswerConfig.builder()
                                .question(autoQ)
                                .correctAnswer("H2O")
                                .textReviewType(TextReviewType.AUTOMATIC)
                                .build();
                autoQ.setTextConfig(autoConfig);

                Quiz autoQuiz = Quiz.builder()
                                .author(author)
                                .title("Text Input Auto Review Quiz")
                                .description("Single text input question graded automatically.")
                                .gradingMethod(QuizGradingMethod.OnePointPerAnswer)
                                .questions(new ArrayList<>(List.of(autoQ)))
                                .build();
                autoQ.setQuiz(autoQuiz);
                quizRepository.save(autoQuiz);

                // --- Quiz 2: TEXT_INPUT with MANUAL review ---
                Question manualQ = Question.builder()
                                .questionOrder(1)
                                .type(QuestionType.TEXT_INPUT)
                                .questionText("Describe the main purpose of the HTTP protocol.")
                                .points(1)
                                .build();
                TextAnswerConfig manualConfig = TextAnswerConfig.builder()
                                .question(manualQ)
                                .correctAnswer("HTTP is used to transfer data on the web.")
                                .textReviewType(TextReviewType.MANUAL)
                                .build();
                manualQ.setTextConfig(manualConfig);

                Quiz manualQuiz = Quiz.builder()
                                .author(author)
                                .title("Text Input Manual Review Quiz")
                                .description("Single text input question requiring manual grading.")
                                .gradingMethod(QuizGradingMethod.OnePointPerAnswer)
                                .questions(new ArrayList<>(List.of(manualQ)))
                                .build();
                manualQ.setQuiz(manualQuiz);
                quizRepository.save(manualQuiz);

                log.info("Text review quizzes created (auto + manual, 1 question each)");
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
