package com.prepforge.repository;

import com.prepforge.entity.TestSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends MongoRepository<TestSession, String> {
    Optional<TestSession> findByTestId(String testId);
    List<TestSession> findByAnonymousSessionIdOrderByCreatedAtDesc(String anonymousSessionId);
}
