package com.opr3.opr3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.opr3.opr3.entity.Token;
import com.opr3.opr3.entity.User;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {

    @Query(value = """
            select t from Token t inner join User u\s
            on t.user.uid = u.uid\s
            where u.uid = :uid and t.revoked = false\s
            """)
    List<Token> findAllValidTokenByUser(@Param("uid") String uid);

    Optional<Token> findByToken(String token);

    void deleteByUser(User user);

    void deleteByToken(String token);

}
