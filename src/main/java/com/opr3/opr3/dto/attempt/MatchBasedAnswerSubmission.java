package com.opr3.opr3.dto.attempt;

import java.util.List;

import com.opr3.opr3.entity.attempt.MatchedPairEntry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchBasedAnswerSubmission {

    private Long questionId;

    private List<MatchedPairEntry> matchedPairs;
}
