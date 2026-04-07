package com.opr3.opr3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextBasedAnswerSubmission {
    private Long questionId;

    private String submittedAnswer;

    private Boolean userMarkedCorrect;
}
