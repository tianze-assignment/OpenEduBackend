package com.wudaokou.backend.question;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserQuestionRepository extends JpaRepository<UserQuestion, Integer> {
    Optional<UserQuestion> findByUserQuestionId(UserQuestionId userQuestionId);
}