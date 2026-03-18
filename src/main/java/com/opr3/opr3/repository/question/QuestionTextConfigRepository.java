package com.opr3.opr3.repository.question;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.QuestionTextConfig;

@Repository
public interface QuestionTextConfigRepository extends JpaRepository<QuestionTextConfig, Long> {

    Optional<QuestionTextConfig> findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
