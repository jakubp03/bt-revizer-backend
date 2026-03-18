package com.opr3.opr3.repository.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.QuestionMatchPair;

@Repository
public interface QuestionMatchPairRepository extends JpaRepository<QuestionMatchPair, Long> {

    List<QuestionMatchPair> findByQuestionIdOrderByPairOrderAsc(Long questionId);

    void deleteByQuestionId(Long questionId);
}
