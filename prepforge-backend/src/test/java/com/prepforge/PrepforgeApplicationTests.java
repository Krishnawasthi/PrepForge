package com.prepforge;

import com.prepforge.dto.CreatePracticeRequest;
import com.prepforge.dto.PracticeResultDto;
import com.prepforge.dto.PracticeTestDto;
import com.prepforge.dto.SubmitPracticeRequest;
import com.prepforge.repository.QuestionRepository;
import com.prepforge.repository.TestAttemptRepository;
import com.prepforge.repository.TestRepository;
import com.prepforge.service.PracticeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
	private QuestionRepository questionRepository;

	@MockBean
	private TestRepository testRepository;

	@MockBean
	private TestAttemptRepository testAttemptRepository;

	@Autowired
	private PracticeService practiceService;

	@Test
	void contextLoads() {
		assertNotNull(practiceService);
	}

	@Test
	void testTopicsContainsCoreJava() {
		List<String> topics = practiceService.getTopics();
		assertNotNull(topics);
		assertFalse(topics.isEmpty());
		assertTrue(topics.contains("Core Java"));
		assertTrue(topics.contains("Java Collections Framework"));
		assertTrue(topics.contains("Streams API"));
	}

	@Test
	void testCreatePracticeTestAndSubmit() {
		CreatePracticeRequest req = CreatePracticeRequest.builder()
				.topics(List.of("Core Java", "Java Collections Framework"))
				.experienceLevel("Intermediate")
				.questionCount(5)
				.build();

		PracticeTestDto test = practiceService.createPracticeTest(req);
		assertNotNull(test);
		assertNotNull(test.getTestId());
		assertEquals(5, test.getQuestions().size());

		// Verify answers are withheld during test
		assertNull(test.getQuestions().get(0).getCorrectAnswer());
		assertNull(test.getQuestions().get(0).getExplanation());
		assertEquals(4, test.getQuestions().get(0).getOptions().size());

		// Submit test
		String firstQId = test.getQuestions().get(0).getId();
		SubmitPracticeRequest submitReq = SubmitPracticeRequest.builder()
				.answers(Map.of(firstQId, test.getQuestions().get(0).getOptions().get(0)))
				.timeTakenSeconds(60)
				.build();

		PracticeResultDto result = practiceService.submitPracticeTest(test.getTestId(), submitReq);
		assertNotNull(result);
		assertEquals(5, result.getTotalQuestions());
		assertNotNull(result.getQuestions());
		assertNotNull(result.getQuestions().get(0).getCorrectAnswer());
		assertNotNull(result.getQuestions().get(0).getExplanation());
	}
}
