package com.prepforge.repository;

import com.prepforge.entity.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends MongoRepository<Topic, String> {
    Optional<Topic> findBySlug(String slug);
    List<Topic> findByCategoryIgnoreCase(String category);
    List<Topic> findByPopularTrue();
}
