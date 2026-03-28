package com.opr3.opr3.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opr3.opr3.dto.QuizResponse;
import com.opr3.opr3.dto.QuizResultResponse;
import com.opr3.opr3.dto.SubmitQuizRequest;
import com.opr3.opr3.service.QuizService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);

    private final QuizService quizService;

    @PostMapping("/submitQuiz")
    public ResponseEntity<QuizResultResponse> submitQuiz(@RequestBody SubmitQuizRequest request) {

        QuizResultResponse result = quizService.processQuiz(request);
        log.info("[200] submitting Quiz {}", request.getQuizId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/allQuizzes")
    public List<QuizResponse> getAllQuizzesTemp() {
        log.info("[200] returning all user quizzes");
        return quizService.tempMethod();
    }

}
