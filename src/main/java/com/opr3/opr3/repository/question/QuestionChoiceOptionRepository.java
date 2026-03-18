package com.opr3.opr3.repository.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.QuestionChoiceOption;

@Repository
public interface QuestionChoiceOptionRepository extends JpaRepository<QuestionChoiceOption, Long> {

    List<QuestionChoiceOption> findByQuestionIdOrderByOptionOrderAsc(Long questionId);

    List<QuestionChoiceOption> findByQuestionIdAndIsCorrectTrue(Long questionId);

    void deleteByQuestionId(Long questionId);
}
