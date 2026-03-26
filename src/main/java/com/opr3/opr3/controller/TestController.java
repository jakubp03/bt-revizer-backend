package com.opr3.opr3.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opr3.opr3.dto.SubmitTestRequest;
import com.opr3.opr3.dto.TestResultResponse;
import com.opr3.opr3.service.TestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    private final TestService testService;

    @PostMapping("/submitTest")
    public ResponseEntity<TestResultResponse> submitTest(@RequestBody SubmitTestRequest request) {
        log.info("submitting test {}", request.getTestId());

        TestResultResponse result = testService.processTest(request);
        return ResponseEntity.ok(result);
    }
}
