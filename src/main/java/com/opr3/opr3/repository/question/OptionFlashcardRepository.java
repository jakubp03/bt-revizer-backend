package com.opr3.opr3.repository.question;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.OptionFlashcard;

@Repository
public interface OptionFlashcardRepository extends JpaRepository<OptionFlashcard, Long> {

    Optional<OptionFlashcard> findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
