package com.opr3.opr3.repository.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.question.OptionMatchPair;

@Repository
public interface OptionMatchPairRepository extends JpaRepository<OptionMatchPair, Long> {

    List<OptionMatchPair> findByQuestionIdOrderByPairOrderAsc(Long questionId);

    void deleteByQuestionId(Long questionId);
}
