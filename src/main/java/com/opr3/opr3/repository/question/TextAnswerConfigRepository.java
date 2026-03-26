package com.opr3.opr3.repository.question;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.TextAnswerConfig;

@Repository
public interface TextAnswerConfigRepository extends JpaRepository<TextAnswerConfig, Long> {

    Optional<TextAnswerConfig> findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
