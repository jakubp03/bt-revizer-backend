package com.opr3.opr3.entity.attempt;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@DiscriminatorValue("MATCH_BASED")
public class MatchBasedAttemptAnswer extends AttemptAnswer {
    @ElementCollection
    @CollectionTable(name = "attempt_answer_matched_pair")
    private List<MatchedPairEntry> matchedPairs;
}
