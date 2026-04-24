package com.opr3.opr3.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opr3.opr3.dto.quiz.CreateQuizRequest;
import com.opr3.opr3.dto.quiz.QuizBasicResponse;
import com.opr3.opr3.dto.quiz.QuizDetailedResponse;
import com.opr3.opr3.service.quiz.QuizService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<QuizBasicResponse> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        QuizBasicResponse result = quizService.createQuiz(request);
        log.info("[201] created quiz '{}'", result.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping({ "/allQuizzes", "/allQuizes" })
    public ResponseEntity<List<QuizBasicResponse>> getAllQuizzes() {
        List<QuizBasicResponse> result = quizService.getAllQuizzes();
        log.info("[200] returning all user quizzes");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDetailedResponse> getQuizById(@PathVariable Long id) {
        QuizDetailedResponse result = quizService.getQuizById(id);
        log.info("[200] returning quiz {}", id);
        return ResponseEntity.ok(result);
    }

}