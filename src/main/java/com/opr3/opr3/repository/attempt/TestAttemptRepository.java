package com.opr3.opr3.repository.attempt;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.attempt.TestAttempt;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    List<TestAttempt> findByTestIdOrderByStartedAtDesc(Long testId);

    List<TestAttempt> findByUserUidOrderByStartedAtDesc(String userUid);

    Optional<TestAttempt> findByIdAndUserUid(Long id, String userUid);

    long countByTestIdAndSubmittedAtIsNotNull(Long testId);
}
