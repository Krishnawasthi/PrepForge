package com.prepforge.repository;

import com.prepforge.entity.TestAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestAttemptRepository extends MongoRepository<TestAttempt, String> {
    Optional<TestAttempt> findByAttemptId(String attemptId);
    List<TestAttempt> findByTestId(String testId);
}
