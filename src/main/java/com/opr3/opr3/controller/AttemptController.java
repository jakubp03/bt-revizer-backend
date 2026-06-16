package com.opr3.opr3.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opr3.opr3.dto.attempt.AttemptBasicResponse;
import com.opr3.opr3.dto.attempt.AttemptReviewResponse;
import com.opr3.opr3.dto.attempt.AttemptSummaryResponse;
import com.opr3.opr3.dto.attempt.QuizResultResponse;
import com.opr3.opr3.dto.attempt.QuizStatsAnswer;
import com.opr3.opr3.dto.attempt.SubmitQuizRequest;
import com.opr3.opr3.service.AttemptService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attempt")
@RequiredArgsConstructor
public class AttemptController {

    private static final Logger log = LoggerFactory.getLogger(AttemptController.class);

    private final AttemptService attemptService;

    @PostMapping("/submitAttempt")
    public ResponseEntity<QuizResultResponse> submitQuiz(@RequestBody SubmitQuizRequest request) {

        QuizResultResponse result = attemptService.processQuiz(request);
        log.info("[200] submitting Quiz {}", request.getQuizId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<AttemptSummaryResponse>> getAllMyAttempts(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AttemptSummaryResponse> result = attemptService.getAllAttempts(pageable);
        log.info("[200] returning all attempts for current user, page {}", pageable.getPageNumber());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/byQuiz/{quizId}/all")
    public ResponseEntity<List<AttemptBasicResponse>> getAllAttemptsByQuizId(@PathVariable Long quizId) {
        List<AttemptBasicResponse> result = attemptService.getAllAttemptsByQuizId(quizId);
        log.info("[200] returning all attempts for quiz {}", quizId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/byQuiz/{quizId}")
    public ResponseEntity<List<AttemptBasicResponse>> getAttemptsByQuizId(@PathVariable Long quizId) {
        List<AttemptBasicResponse> result = attemptService.getAttemptsByQuizId(quizId);
        log.info("[200] returning attempts for quiz {}", quizId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{attemptId}/review")
    public ResponseEntity<AttemptReviewResponse> getAttemptReview(@PathVariable Long attemptId) {
        AttemptReviewResponse result = attemptService.getAttemptReview(attemptId);
        log.info("[200] returning review for attempt {}", attemptId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/statistics/byQuizId/{quizId}")
    public ResponseEntity<QuizStatsAnswer> getQuizStatsByQuizId(@PathVariable Long quizId) {
        QuizStatsAnswer result = attemptService.getQuizStatsByQuizId(quizId);
        log.info("[200] returning stats for quiz {}", quizId);
        return ResponseEntity.ok(result);
    }

}
