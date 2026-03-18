package com.opr3.opr3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.Category;
import com.opr3.opr3.entity.Test;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    List<Test> findByAuthorUidOrderByCreatedAtDesc(String authorUid);

    Optional<Test> findByIdAndAuthorUid(Long id, String authorUid);

    List<Test> findDistinctByCategoriesIn(List<Category> categories);
}
