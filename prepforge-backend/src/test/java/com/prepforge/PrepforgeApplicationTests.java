package com.prepforge;

import com.prepforge.dto.*;
import com.prepforge.repository.QuestionRepository;
import com.prepforge.repository.TestAttemptRepository;
import com.prepforge.repository.TestRepository;
import com.prepforge.repository.TopicRepository;
import com.prepforge.service.HealthService;
import com.prepforge.service.QuestionService;
import com.prepforge.service.ScoringService;
import com.prepforge.service.TestService;
import com.prepforge.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
		MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class,
		MongoRepositoriesAutoConfiguration.class
})
class PrepforgeApplicationTests {

	@MockBean
	private MongoTemplate mongoTemplate;

	@MockBean
	private TopicRepository topicRepository;

	@MockBean
	private QuestionRepository questionRepository;

	@MockBean
	private TestRepository testRepository;

	@MockBean
	private TestAttemptRepository testAttemptRepository;

	@Autowired
	private TopicService topicService;

	@Autowired
	private HealthService healthService;

	@Autowired
	private TestService testService;

	@Autowired
	private QuestionService questionService;

	@Autowired
	private ScoringService scoringService;

	@Test
	void contextLoads() {
		assertNotNull(topicService);
		assertNotNull(healthService);
		assertNotNull(testService);
		assertNotNull(scoringService);
	}

	@Test
	void testTopicCatalogContainsJavaBackendTopics() {
		when(topicRepository.findAll()).thenReturn(Collections.emptyList());
		List<TopicDto> topics = topicService.getAllTopics();
		assertFalse(topics.isEmpty());
		assertTrue(topics.stream().anyMatch(t -> t.getName().contains("Core Java")));
		assertTrue(topics.stream().anyMatch(t -> t.getName().contains("Spring Boot")));
		assertTrue(topics.stream().anyMatch(t -> t.getName().contains("SQL")));
		assertTrue(topics.stream().anyMatch(t -> t.getName().contains("Collections")));
		assertTrue(topics.stream().anyMatch(t -> t.getName().contains("Spring Security")));
		assertTrue(topics.stream().anyMatch(t -> t.getName().contains("Kafka")));
		assertTrue(topics.stream().anyMatch(t -> t.getName().contains("Redis")));
	}

	@Test
	void testHealthServiceReportsUp() {
		HealthStatusDto status = healthService.getHealthStatus();
		assertEquals("UP", status.getStatus());
		assertEquals("production-ready", status.getEnvironment());
	}

	@Test
	void testPromptInterpretation() {
		String prompt = "I have 1.5 years of Java experience and I'm preparing for a backend developer interview. Give me medium to hard questions focused on Collections, Multithreading, Java 8 Streams and OOP. Include output-based and tricky interview questions.";
		PromptInterpretationRequest req = PromptInterpretationRequest.builder().prompt(prompt).build();
		PromptInterpretationResponse resp = testService.interpretUserPrompt(req);

		assertNotNull(resp);
		assertEquals("1-2 years", resp.getExperienceLevel());
		assertEquals("Mixed", resp.getDifficulty());
		assertTrue(resp.getTopics().contains("Java Collections Framework"));
		assertTrue(resp.getTopics().contains("Multithreading & Concurrency"));
		assertTrue(resp.getQuestionTypes().contains("Output-based"));
		assertTrue(resp.getQuestionTypes().contains("Interview trick questions"));
	}

	@Test
	void testTestGenerationAndAuthoritativeScoring() {
		TestConfigRequest config = TestConfigRequest.builder()
				.anonymousSessionId("anon_test_user")
				.topics(List.of("Core Java", "Java Collections Framework", "SQL & Query Optimization"))
				.experienceLevel("1-2 years")
				.difficulty("Medium")
				.questionTypes(List.of("Conceptual MCQ", "Output-based"))
				.questionCount(5)
				.timeLimitMinutes(15)
				.build();

		TestDetailDto testDetail = testService.generateFullTest(config);
		assertNotNull(testDetail);
		assertNotNull(testDetail.getTestId());
		assertFalse(testDetail.getQuestions().isEmpty());

		// Verify answers are withheld from candidate during test taking
		assertNull(testDetail.getQuestions().get(0).getCorrectAnswer());
		assertNull(testDetail.getQuestions().get(0).getExplanation());
		assertEquals(4, testDetail.getQuestions().get(0).getOptions().size());

		// Submit test
		String firstQId = testDetail.getQuestions().get(0).getId();
		TestSubmissionRequest submission = TestSubmissionRequest.builder()
				.anonymousSessionId("anon_test_user")
				.attemptId("att_test_123")
				.answers(Map.of(firstQId, "false true"))
				.timeTakenSeconds(120)
				.build();

		TestResultDto result = testService.submitTest(testDetail.getTestId(), submission);
		assertNotNull(result);
		assertEquals("att_test_123", result.getAttemptId());
		assertTrue(result.getTotalQuestions() > 0);
		assertNotNull(result.getFeedbackMessage());
		assertNotNull(result.getQuestions());
		assertNotNull(result.getQuestions().get(0).getCorrectAnswer());
		assertNotNull(result.getQuestions().get(0).getExplanation());
	}
}
