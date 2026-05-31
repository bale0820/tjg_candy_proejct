package com.tjg_project.candy.domain.user.repository;

import com.tjg_project.candy.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByEmail(String email);
    Optional<Users> findByEmail(String email);
    Optional<Users> findByUserId(String userId);
    Optional<Users> findByUserIdAndProvider(String userId, String provider);
    @Query("SELECT u FROM Users u WHERE u.name = :name AND u.provider = :provider")
    Optional<Users> findByNameAndProvider(@Param("name") String name,
                                          @Param("provider") String provider);
}
