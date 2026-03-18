package com.opr3.opr3.repository.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.QuestionOrderItem;

@Repository
public interface QuestionOrderItemRepository extends JpaRepository<QuestionOrderItem, Long> {

    List<QuestionOrderItem> findByQuestionIdOrderByCorrectPositionAsc(Long questionId);

    void deleteByQuestionId(Long questionId);
}
