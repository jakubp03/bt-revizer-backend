package com.opr3.opr3.repository.question;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.Question;
import com.opr3.opr3.enums.QuestionType;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTestIdOrderByQuestionOrderAsc(Long testId);

    Optional<Question> findByIdAndTestAuthorUid(Long questionId, String authorUid);

    List<Question> findByTypeAndTestId(QuestionType type, Long testId);
}
