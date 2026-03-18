package com.opr3.opr3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserUidOrderByNameAsc(String userUid);

    Optional<Category> findByIdAndUserUid(Long id, String userUid);

    boolean existsByNameIgnoreCaseAndUserUid(String name, String userUid);
}
