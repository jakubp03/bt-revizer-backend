package com.opr3.opr3.repository.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.OptionOrderItem;

@Repository
public interface OptionOrderItemRepository extends JpaRepository<OptionOrderItem, Long> {

    List<OptionOrderItem> findByQuestionIdOrderByCorrectPositionAsc(Long questionId);

    void deleteByQuestionId(Long questionId);
}
