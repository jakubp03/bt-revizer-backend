package com.opr3.opr3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.Category;
import com.opr3.opr3.entity.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByAuthorUidOrderByCreatedAtDesc(String authorUid);

    Optional<Quiz> findByIdAndAuthorUid(Long id, String authorUid);

    List<Quiz> findDistinctByCategoriesIn(List<Category> categories);
}
