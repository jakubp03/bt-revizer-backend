package com.opr3.opr3.repository.attempt;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.attempt.AttemptAnswer;

@Repository
public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {

    List<AttemptAnswer> findByAttemptId(Long attemptId);

    List<AttemptAnswer> findByQuestionId(Long questionId);

    @Query("SELECT COUNT(a) FROM AttemptAnswer a WHERE a.question.id = :questionId AND a.maxPoints IS NOT NULL AND a.pointsAwarded < a.maxPoints")
    long countByQuestionIdAndNotFullScore(Long questionId);
}
