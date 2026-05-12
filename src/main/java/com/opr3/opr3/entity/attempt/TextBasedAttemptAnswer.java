package com.opr3.opr3.entity.attempt;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
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
@DiscriminatorValue("TEXT_BASED")
public class TextBasedAttemptAnswer extends AttemptAnswer {
    @Column(columnDefinition = "TEXT")
    private String submittedAnswer;

    @Column(name = "user_marked_correct")
    private Boolean userMarkedCorrect;
}