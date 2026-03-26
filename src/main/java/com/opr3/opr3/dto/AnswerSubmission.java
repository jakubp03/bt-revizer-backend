package com.opr3.opr3.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSubmission {

    private Long questionId;

    private List<String> selectedOptionIds;

    private String textAnswer;
}
