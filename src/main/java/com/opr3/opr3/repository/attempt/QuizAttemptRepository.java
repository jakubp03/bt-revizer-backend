package com.opr3.opr3.repository.attempt;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.attempt.QuizAttempt;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

        List<QuizAttempt> findByQuizIdOrderByStartedAtDesc(Long quizId);

        List<QuizAttempt> findByUserUidOrderByStartedAtDesc(String userUid);

        Optional<QuizAttempt> findByIdAndUserUid(Long id, String userUid);

        long countByQuizIdAndSubmittedAtIsNotNull(Long quizId);

        Optional<QuizAttempt> findTopByQuizIdAndUserUidAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(
                        Long quizId, String userUid);

        @Query("SELECT AVG(ta.score / ta.maxScore * 100) FROM QuizAttempt ta " +
                        "WHERE ta.quiz.id = :quizId AND ta.submittedAt IS NOT NULL AND ta.maxScore > 0")
        Double findAverageScorePercentageByQuizId(@Param("quizId") Long quizId);
}
