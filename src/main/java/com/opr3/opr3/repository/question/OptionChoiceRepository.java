package com.opr3.opr3.repository.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.OptionChoice;

@Repository
public interface OptionChoiceRepository extends JpaRepository<OptionChoice, Long> {

    List<OptionChoice> findByQuestionIdOrderByOptionOrderAsc(Long questionId);

    List<OptionChoice> findByQuestionIdAndIsCorrectTrue(Long questionId);

    void deleteByQuestionId(Long questionId);
}
