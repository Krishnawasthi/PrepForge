package com.prepforge.repository;

import com.prepforge.entity.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
    List<Question> findByTopicIn(List<String> topics);
    List<Question> findByTopicAndDifficulty(String topic, String difficulty);
    List<Question> findByIdIn(List<String> ids);
}
