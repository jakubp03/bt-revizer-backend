package com.opr3.opr3.dto.attempt;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdBasedAnswerSubmission {

    private Long questionId;

    private List<Long> selectedOptionIds;
}
