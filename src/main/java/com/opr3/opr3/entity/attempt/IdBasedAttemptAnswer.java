package com.opr3.opr3.entity.attempt;

import java.util.List;

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
@DiscriminatorValue("ID_BASED")
public class IdBasedAttemptAnswer extends AttemptAnswer {
    @ElementCollection
    private List<Long> selectedOptionIds;
}