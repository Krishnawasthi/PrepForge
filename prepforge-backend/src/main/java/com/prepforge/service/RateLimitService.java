package com.prepforge.service;

import com.prepforge.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private static final int MAX_GENERATION_REQUESTS_PER_WINDOW = 20; // 20 tests per 10 minutes per session
    private static final long WINDOW_DURATION_SECONDS = 600; // 10 minutes

    private final Map<String, TokenBucket> sessionBuckets = new ConcurrentHashMap<>();

    public void checkAndConsumeRateLimit(String sessionId, String action) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        TokenBucket bucket = sessionBuckets.computeIfAbsent(sessionId, k -> new TokenBucket(MAX_GENERATION_REQUESTS_PER_WINDOW));

        if (!bucket.tryConsume()) {
            log.warn("Rate limit exceeded for session: {} on action: {}", sessionId, action);
            throw new RateLimitExceededException(
                    "You have reached the test generation limit for this session. Please wait a few moments before creating another test."
            );
        }
    }

    private static class TokenBucket {
        private final int maxTokens;
        private int tokens;
        private Instant lastRefillTimestamp;

        public TokenBucket(int maxTokens) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.lastRefillTimestamp = Instant.now();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            long elapsedSeconds = lastRefillTimestamp.until(now, java.time.temporal.ChronoUnit.SECONDS);
            if (elapsedSeconds > 0) {
                double tokensToAdd = ((double) elapsedSeconds / WINDOW_DURATION_SECONDS) * maxTokens;
                int added = (int) tokensToAdd;
                if (added > 0) {
                    tokens = Math.min(maxTokens, tokens + added);
                    lastRefillTimestamp = now;
                }
            }
        }
    }
}
