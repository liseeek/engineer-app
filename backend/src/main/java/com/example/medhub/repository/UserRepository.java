package com.example.medhub.repository;

import com.example.medhub.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends BaseUserRepository<User> {

    @Query("SELECT COUNT(u) FROM User u WHERE u.email = :email")
    long countUsersWithEmail(@Param("email") String email);

    @Override
    default boolean existsByEmail(String email) {
        return countUsersWithEmail(email) > 0;
    }
}
