package com.opr3.opr3.repository.attempt;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.attempt.TestAttempt;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    List<TestAttempt> findByTestIdOrderByStartedAtDesc(Long testId);

    List<TestAttempt> findByUserUidOrderByStartedAtDesc(String userUid);

    Optional<TestAttempt> findByIdAndUserUid(Long id, String userUid);

    long countByTestIdAndSubmittedAtIsNotNull(Long testId);

    Optional<TestAttempt> findTopByTestIdAndUserUidAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(
            Long testId, String userUid);

    @Query("SELECT AVG(ta.score / ta.maxScore * 100) FROM TestAttempt ta " +
            "WHERE ta.test.id = :testId AND ta.submittedAt IS NOT NULL AND ta.maxScore > 0")
    Double findAverageScorePercentageByTestId(@Param("testId") Long testId);
}
