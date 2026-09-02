package com.prepforge.service;

import com.prepforge.entity.Question;
import com.prepforge.repository.QuestionRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private static final Logger log = LoggerFactory.getLogger(QuestionBankService.class);
    private final QuestionRepository questionRepository;
    private List<Question> curatedBankCache = new ArrayList<>();

    public QuestionBankService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @PostConstruct
    public void seedInitialQuestions() {
        try {
            log.info("Refreshing comprehensive Java Backend question bank in database...");
            curatedBankCache = getCuratedQuestionBank();
            questionRepository.deleteAll();
            questionRepository.saveAll(curatedBankCache);
            log.info("Successfully seeded {} curated Java Backend questions.", curatedBankCache.size());
        } catch (Exception e) {
            log.warn("Question bank database seeding skipped (will use in-memory bank): {}", e.getMessage());
            curatedBankCache = getCuratedQuestionBank();
        }
    }

    /**
     * Generates dynamic, strictly non-repetitive questions for the specified topics.
     */
    public List<Question> generateDynamicJavaQuestions(List<String> topics, String experienceLevel, String difficulty, int targetCount) {
        List<Question> dynamicList = new ArrayList<>();
        Random random = ThreadLocalRandom.current();
        Set<String> generatedQuestions = new HashSet<>();

        List<String> effectiveTopics = (topics != null && !topics.isEmpty())
                ? topics
                : List.of("Core Java", "Spring Boot", "Multithreading & Concurrency");

        int attempts = 0;
        int maxAttempts = targetCount * 10;

        while (dynamicList.size() < targetCount && attempts < maxAttempts) {
            attempts++;
            String topic = effectiveTopics.get(dynamicList.size() % effectiveTopics.size());
            int variationIndex = attempts % 20;

            Question q = createDiverseParametricQuestion(topic, experienceLevel, difficulty, variationIndex, random);
            if (q != null && generatedQuestions.add(q.getQuestion().trim().toLowerCase())) {
                dynamicList.add(q);
            }
        }

        // Guaranteed fallback if still under target count: synthesize unique algorithmic / interview scenarios
        int counter = 1;
        while (dynamicList.size() < targetCount) {
            String topic = effectiveTopics.get(dynamicList.size() % effectiveTopics.size());
            Question extra = createAlgorithmicOutputQuestion(topic, experienceLevel, difficulty, counter++, random);
            if (generatedQuestions.add(extra.getQuestion().trim().toLowerCase())) {
                dynamicList.add(extra);
            }
        }

        return dynamicList;
    }

    public Question createAlgorithmicOutputQuestion(String topic, String diff, String exp) {
        return createAlgorithmicOutputQuestion(topic, exp, diff, new Random().nextInt(1000), new Random());
    }

    public Question createDiverseParametricQuestion(String topic, String diff, String exp) {
        return createDiverseParametricQuestion(topic, exp, diff, new Random().nextInt(20), new Random());
    }

    private Question createAlgorithmicOutputQuestion(String topic, String exp, String diff, int index, Random random) {
        String uid = "algo_" + System.currentTimeMillis() + "_" + index;
        int a = random.nextInt(2, 9);
        int b = a * random.nextInt(2, 5);
        int c = b + random.nextInt(1, 4);

        if (index % 3 == 0) {
            int shift = random.nextInt(1, 4);
            int shiftedVal = a << shift;
            return createQuestion(uid,
                    "What is the output of evaluating `(" + a + " << " + shift + ") ^ " + b + "` in Java?",
                    List.of(String.valueOf(shiftedVal ^ b), String.valueOf(shiftedVal | b), String.valueOf(shiftedVal & b), String.valueOf(a ^ b)),
                    String.valueOf(shiftedVal ^ b),
                    "Left shift (" + a + " << " + shift + ") shifts bits left by " + shift + " positions, producing " + shiftedVal + ". Then bitwise XOR (^) with " + b + " produces " + (shiftedVal ^ b) + ".",
                    Map.of("A", "Correct calculation of bitwise shift and XOR.",
                            "B", "Incorrect. Bitwise OR (|) was evaluated instead of XOR (^).",
                            "C", "Incorrect. Bitwise AND (&) was evaluated instead of XOR (^).",
                            "D", "Incorrect. The bitwise shift was omitted."),
                    topic, "java-syntax", diff, exp, "Output-based",
                    "Bitwise operations frequently appear in senior Java coding rounds testing precision under pressure.");
        } else if (index % 3 == 1) {
            int start = random.nextInt(1, 5);
            int limit = random.nextInt(5, 10);
            int sum = 0;
            for (int k = start; k < start + limit; k++) if (k % 2 == 0) sum += k;

            return createQuestion(uid,
                    "What is the result of the following Java Stream pipeline?\n\n```java\nint sum = IntStream.range(" + start + ", " + (start + limit) + ")\n    .filter(n -> n % 2 == 0)\n    .sum();\nSystem.out.println(sum);\n```",
                    List.of(String.valueOf(sum), String.valueOf(sum + start), String.valueOf(sum * 2), "Compilation Error"),
                    String.valueOf(sum),
                    "IntStream.range creates a half-open interval [" + start + ", " + (start + limit) + "). The filter retains even numbers, and sum() aggregates them to " + sum + ".",
                    Map.of("A", "Correct. Evaluates even numbers in the specified half-open interval.",
                            "B", "Incorrect. Range in Java is half-open (exclusive of upper bound).",
                            "C", "Incorrect calculation of stream reduction.",
                            "D", "Incorrect. IntStream syntax is valid Java 8+."),
                    topic, "stream-operations", diff, exp, "Output-based",
                    "Remember that IntStream.range(a, b) is exclusive of b, while IntStream.rangeClosed(a, b) is inclusive of b.");
        } else {
            int capacity = (1 << random.nextInt(4, 7));
            int expectedThreshold = (int) (capacity * 0.75f);
            return createQuestion(uid,
                    "A `HashMap` is instantiated with `new HashMap<>(" + capacity + ")`. At what entry count will the table resize by default?",
                    List.of(String.valueOf(expectedThreshold) + " entries (based on 0.75 default load factor)",
                            String.valueOf(capacity) + " entries (when capacity is 100% full)",
                            String.valueOf(capacity * 2) + " entries",
                            "16 entries (default fixed threshold)"),
                    String.valueOf(expectedThreshold) + " entries (based on 0.75 default load factor)",
                    "The resize threshold is calculated as capacity (" + capacity + ") * load factor (0.75) = " + expectedThreshold + ". When map size exceeds this threshold, the bucket array doubles in size.",
                    Map.of("A", "Correct. Threshold = capacity * default load factor (0.75).",
                            "B", "Incorrect. Resizing occurs before 100% capacity to prevent excessive clustering.",
                            "C", "Incorrect. Doubling occurs after reaching the threshold, not at double capacity.",
                            "D", "Incorrect. Initial capacity was explicitly set to " + capacity + "."),
                    topic, "hashmap-internals", diff, exp, "Conceptual MCQ",
                    "Always mention that initial capacity should be sized as (expectedEntries / 0.75) + 1 to avoid runtime rehashing in production.");
        }
    }

    private Question createDiverseParametricQuestion(String topic, String exp, String diff, int variant, Random random) {
        String uid = "dyn_" + topic.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis() + "_" + random.nextInt(10000);

        if (topic.contains("Collections") || topic.contains("Collection")) {
            switch (variant % 5) {
                case 0:
                    return createQuestion(uid,
                            "In a high-throughput Java backend application, which collection provides thread-safe access with bucket-level CAS/synchronized locking rather than global table synchronization?",
                            List.of("ConcurrentHashMap", "Collections.synchronizedMap(new HashMap<>())", "Hashtable", "TreeMap"),
                            "ConcurrentHashMap",
                            "ConcurrentHashMap uses lock striping / synchronized tree bins on individual buckets (CAS + synchronized per bucket) to allow concurrent reads and writes without locking the entire map.",
                            Map.of("A", "Correct. ConcurrentHashMap locks only specific bucket bins during writes.",
                                    "B", "Incorrect. synchronizedMap locks the entire map instance on every read/write.",
                                    "C", "Incorrect. Hashtable synchronizes at the method level globally.",
                                    "D", "Incorrect. TreeMap is not thread-safe."),
                            "Java Collections Framework", "hashmap-internals", diff, exp, "Conceptual MCQ",
                            "Always mention how ConcurrentHashMap in Java 8+ replaced Segment locks with CAS and synchronized bucket heads.");
                case 1:
                    return createQuestion(uid,
                            "What is the time complexity of `ArrayList.remove(0)` versus `LinkedList.removeFirst()` in Java?",
                            List.of("O(N) for ArrayList vs O(1) for LinkedList", "O(1) for ArrayList vs O(N) for LinkedList", "O(1) for both", "O(N) for both"),
                            "O(N) for ArrayList vs O(1) for LinkedList",
                            "ArrayList must shift all N-1 subsequent elements to the left via System.arraycopy (O(N)). LinkedList simply updates its head node pointer (O(1)).",
                            Map.of("A", "Correct. ArrayList requires shifting remaining elements; LinkedList only unlinks pointers.",
                                    "B", "Incorrect. ArrayList cannot shift elements in O(1).",
                                    "C", "Incorrect. ArrayList is not O(1) for index 0 deletion.",
                                    "D", "Incorrect. LinkedList deletion of head is O(1)."),
                            "Java Collections Framework", "list-set-implementations", diff, exp, "Conceptual MCQ",
                            "Remember that ArrayList has better CPU cache locality despite O(N) shifts for small collections.");
                case 2:
                    return createQuestion(uid,
                            "Which Java Collection guarantees that elements are maintained in their exact insertion order without sorting by natural comparison?",
                            List.of("LinkedHashMap", "TreeMap", "HashMap", "ConcurrentSkipListMap"),
                            "LinkedHashMap",
                            "LinkedHashMap maintains a doubly-linked list running through all of its entries, preserving insertion order (or access order for LRU caches).",
                            Map.of("A", "Correct. LinkedHashMap preserves insertion or LRU access order.",
                                    "B", "Incorrect. TreeMap sorts keys by natural order or Comparator.",
                                    "C", "Incorrect. HashMap makes no guarantees regarding iteration order.",
                                    "D", "Incorrect. ConcurrentSkipListMap maintains natural sorted order."),
                            "Java Collections Framework", "hashmap-internals", diff, exp, "Conceptual MCQ",
                            "LinkedHashMap is commonly used to implement LRU caches by overriding removeEldestEntry().");
                case 3:
                    return createQuestion(uid,
                            "Under what condition does `CopyOnWriteArrayList` deliver superior performance compared to `Collections.synchronizedList()`?",
                            List.of("When read operations vastly outnumber write operations", "When write/insert operations vastly outnumber read operations", "When the list size exceeds 10 million items", "When frequent sort operations are executed"),
                            "When read operations vastly outnumber write operations",
                            "CopyOnWriteArrayList allows read operations to proceed without locks on a snapshot array. Writes create a fresh copy of the array, making writes expensive but reads lock-free.",
                            Map.of("A", "Correct. Read-heavy scenarios (e.g. event listeners) benefit from lock-free reads.",
                                    "B", "Incorrect. Write-heavy workloads cause massive GC and memory copying overhead.",
                                    "C", "Incorrect. Copying 10M items on write would cause severe GC pauses.",
                                    "D", "Incorrect. Sorting triggers multiple mutations."),
                            "Java Collections Framework", "list-set-implementations", diff, exp, "Scenario-based",
                            "Use CopyOnWriteArrayList for subscriber/listener registries where subscriptions change rarely.");
                default:
                    return createQuestion(uid,
                            "What happens when two distinct objects produce the exact same `hashCode()` in Java's `HashMap`?",
                            List.of("They are placed in the same bucket and checked via equals()", "The second object overwrites the first immediately", "A HashCollisionException is thrown", "The HashMap automatically doubles its table capacity"),
                            "They are placed in the same bucket and checked via equals()",
                            "Hash collisions are resolved by chaining elements in the same bucket. When searching, HashMap traverses the bucket and calls equals() to locate the matching key.",
                            Map.of("A", "Correct. Collisions reside in the same bucket chained as a list or tree.",
                                    "B", "Incorrect. Overwriting only occurs if equals() also returns true.",
                                    "C", "Incorrect. Collisions are standard and expected; no exception is thrown.",
                                    "D", "Incorrect. Resizing is governed by total count and load factor, not single collisions."),
                            "Java Collections Framework", "hashmap-internals", diff, exp, "Conceptual MCQ",
                            "Remind the interviewer that poorly distributed hashCode() degrades lookup from O(1) to O(log N) or O(N).");
            }
        } else if (topic.contains("Multithreading") || topic.contains("Concurrency")) {
            switch (variant % 5) {
                case 0:
                    int core = random.nextInt(2, 6);
                    int max = core * 2;
                    return createQuestion(uid,
                            "Consider a `ThreadPoolExecutor` configured with corePoolSize=" + core + ", maxPoolSize=" + max + ", and an unbounded `LinkedBlockingQueue`. When " + (max + 10) + " tasks are submitted concurrently, how many active worker threads are created?",
                            List.of(core + " threads (only corePoolSize is created because the work queue is unbounded)",
                                    max + " threads (maxPoolSize is reached)",
                                    (max + 10) + " threads",
                                    "An RejectedExecutionException is thrown"),
                            core + " threads (only corePoolSize is created because the work queue is unbounded)",
                            "ThreadPoolExecutor only scales threads beyond corePoolSize when the work queue is completely full. Because LinkedBlockingQueue is unbounded by default, tasks queue indefinitely and maxPoolSize is never utilized.",
                            Map.of("A", "Correct. Threads beyond corePoolSize are only spawned if the queue rejects task insertion.",
                                    "B", "Incorrect. maxPoolSize is never reached with an unbounded queue.",
                                    "C", "Incorrect. The executor never spawns more than maxPoolSize threads.",
                                    "D", "Incorrect. Unbounded queues do not reject tasks unless memory is exhausted."),
                            "Multithreading & Concurrency", "executors-futures", diff, exp, "Scenario-based",
                            "Classic senior trap: Always bound your queues in production to avoid OutOfMemoryError.");
                case 1:
                    return createQuestion(uid,
                            "What is the difference between `volatile` and `AtomicInteger` in Java concurrency?",
                            List.of("volatile guarantees memory visibility only; AtomicInteger guarantees visibility AND atomic read-modify-write operations (CAS)",
                                    "volatile is synchronized while AtomicInteger is lock-free",
                                    "volatile is atomic for compound operations like count++ while AtomicInteger is not",
                                    "They are completely identical in function and byte-code"),
                            "volatile guarantees memory visibility only; AtomicInteger guarantees visibility AND atomic read-modify-write operations (CAS)",
                            "volatile only guarantees that reads and writes are visible across CPU caches and prevents instruction reordering. Compound operations like count++ (read, increment, write) are NOT atomic on volatile variables. AtomicInteger uses hardware CAS (Compare-And-Swap) for atomicity.",
                            Map.of("A", "Correct. volatile provides visibility; AtomicInteger provides visibility + atomicity via CAS.",
                                    "B", "Incorrect. volatile does not use monitor synchronization.",
                                    "C", "Incorrect. count++ is not atomic with volatile.",
                                    "D", "Incorrect. They have fundamentally different atomicity guarantees."),
                            "Multithreading & Concurrency", "locks-volatiles", diff, exp, "Conceptual MCQ",
                            "Emphasize that count++ on a volatile int produces race conditions under concurrent writes.");
                case 2:
                    return createQuestion(uid,
                            "What problem does `ReentrantLock.tryLock(timeout, unit)` solve that intrinsic `synchronized` blocks cannot?",
                            List.of("Deadlock avoidance via timed non-blocking lock acquisition attempts",
                                    "Automatic garbage collection of thread monitors",
                                    "Guaranteeing zero memory cache invalidations",
                                    "Enforcing thread priority scheduling"),
                            "Deadlock avoidance via timed non-blocking lock acquisition attempts",
                            "Intrinsic synchronized blocks block indefinitely if a monitor is held, risking unrecoverable deadlocks. ReentrantLock allows tryLock() with timeouts, letting a thread back off and release held locks if it cannot acquire the next lock.",
                            Map.of("A", "Correct. Timed tryLock() allows breaking deadlock cycles through cooperative backoff.",
                                    "B", "Incorrect. JVM garbage collection does not depend on tryLock.",
                                    "C", "Incorrect. Locks always coordinate CPU memory barriers.",
                                    "D", "Incorrect. Java locks do not override OS thread priority."),
                            "Multithreading & Concurrency", "locks-volatiles", diff, exp, "Best-practice",
                            "Explain how tryLock() enables the lock-ordering back-off pattern to prevent deadlocks in distributed systems.");
                case 3:
                    return createQuestion(uid,
                            "In `CompletableFuture`, what is the key difference between `thenApply()` and `thenCompose()`?",
                            List.of("thenApply maps a value T to U; thenCompose flattens a nested CompletableFuture (similar to flatMap)",
                                    "thenApply runs asynchronously on a new thread while thenCompose is synchronous",
                                    "thenCompose catches exceptions while thenApply cannot",
                                    "thenApply is deprecated in favor of thenCompose"),
                            "thenApply maps a value T to U; thenCompose flattens a nested CompletableFuture (similar to flatMap)",
                            "thenApply(fn) takes a function T -> U and returns CompletableFuture<U>. thenCompose(fn) takes a function T -> CompletableFuture<U> and flattens it, preventing nested CompletableFuture<CompletableFuture<U>>.",
                            Map.of("A", "Correct. thenApply is equivalent to map(), whereas thenCompose is equivalent to flatMap().",
                                    "B", "Incorrect. Asynchronous execution is controlled by the Async variants (thenApplyAsync).",
                                    "C", "Incorrect. Exception handling is handled by exceptionally() or handle().",
                                    "D", "Incorrect. Neither method is deprecated."),
                            "Multithreading & Concurrency", "executors-futures", diff, exp, "Conceptual MCQ",
                            "Compare thenCompose to Mono.flatMap() in Spring WebFlux or Optional.flatMap().");
                default:
                    return createQuestion(uid,
                            "Why is it essential to call `ThreadLocal.remove()` in applications using pooled worker threads (such as Tomcat or ExecutorService)?",
                            List.of("To prevent memory leaks and state pollution across different HTTP requests reusing the same thread",
                                    "To prevent ClassNotFoundException on application stop",
                                    "To notify the Garbage Collector to shut down the thread pool",
                                    "Because ThreadLocal throws an IllegalStateException if accessed twice"),
                            "To prevent memory leaks and state pollution across different HTTP requests reusing the same thread",
                            "Web servers reuse worker threads. If a ThreadLocal value is not removed after request processing, the thread retains strong references to objects in its ThreadLocalMap, causing both memory leaks and security/data leak issues when the thread serves the next user.",
                            Map.of("A", "Correct. Failing to remove ThreadLocal values causes memory leaks and cross-request state pollution.",
                                    "B", "Incorrect. Classloader leaks can occur, but ClassNotFoundException is not the direct result.",
                                    "C", "Incorrect. ThreadLocal has no control over thread pool shutdown.",
                                    "D", "Incorrect. ThreadLocal can be read multiple times safely."),
                            "Multithreading & Concurrency", "thread-lifecycle", diff, exp, "Best-practice",
                            "Always place threadLocal.remove() inside a finally block at the end of filter/interceptor chains.");
            }
        } else if (topic.contains("Spring Boot") || topic.contains("Spring")) {
            switch (variant % 5) {
                case 0:
                    return createQuestion(uid,
                            "How does Spring Boot determine which `@Configuration` classes to load during auto-configuration?",
                            List.of("By reading META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports and evaluating @Conditional annotations",
                                    "By scanning the entire disk for classes ending in 'Config'",
                                    "By checking the pom.xml at runtime via Maven plugins",
                                    "By inspecting the active OS environment variables for configuration flags"),
                            "By reading META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports and evaluating @Conditional annotations",
                            "Spring Boot 3 uses META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports (or spring.factories in Boot 2.x). Classes listed there are evaluated against conditional annotations (@ConditionalOnClass, @ConditionalOnMissingBean).",
                            Map.of("A", "Correct. AutoConfiguration.imports lists candidate configs, filtered by @Conditional annotations.",
                                    "B", "Incorrect. Spring Boot does not do arbitrary disk scans.",
                                    "C", "Incorrect. pom.xml is not parsed by the running JVM at runtime.",
                                    "D", "Incorrect. Auto-configuration relies on classpath detection and bean presence."),
                            "Spring Boot", "auto-configuration", diff, exp, "Conceptual MCQ",
                            "Mention how @ConditionalOnMissingBean allows developers to override default Spring Boot beans cleanly.");
                case 1:
                    return createQuestion(uid,
                            "What is the default bean scope in the Spring ApplicationContext, and how does it behave in a multi-threaded web application?",
                            List.of("Singleton scope; a single shared instance is used across all concurrent request threads",
                                    "Prototype scope; a new instance is created for every HTTP request",
                                    "Request scope; each thread gets an isolated bean copy automatically",
                                    "Session scope; beans are tied to the HTTP session"),
                            "Singleton scope; a single shared instance is used across all concurrent request threads",
                            "Spring beans are Singletons by default. Since multiple threads concurrently execute methods on the same singleton instance, any mutable instance state is vulnerable to race conditions.",
                            Map.of("A", "Correct. Singleton beans are shared across all threads; they should remain strictly stateless.",
                                    "B", "Incorrect. Prototype scope must be explicitly declared with @Scope(\"prototype\").",
                                    "C", "Incorrect. Request scope requires @RequestScope.",
                                    "D", "Incorrect. Session scope requires @SessionScope."),
                            "Spring Framework Core", "ioc-di", diff, exp, "Conceptual MCQ",
                            "Emphasize that enterprise Spring services should always be stateless to ensure thread safety.");
                case 2:
                    return createQuestion(uid,
                            "In Spring AOP, when does calling a method annotated with `@Transactional` FAIL to trigger a transaction?",
                            List.of("When the method is invoked internally by another method in the same class (self-invocation)",
                                    "When the method returns void",
                                    "When the method is called from an asynchronous thread",
                                    "When the class implements an interface"),
                            "When the method is invoked internally by another method in the same class (self-invocation)",
                            "Spring AOP creates dynamic proxies around beans. Calling this.annotatedMethod() bypasses the proxy interceptor and invokes the target instance method directly, bypassing transactional interceptors.",
                            Map.of("A", "Correct. Self-invocation bypasses the Spring AOP proxy wrapper.",
                                    "B", "Incorrect. Methods returning void can be fully transactional.",
                                    "C", "Incorrect. Calling a separate bean method from an async thread still goes through its proxy.",
                                    "D", "Incorrect. Implementing interfaces allows standard JDK dynamic proxies."),
                            "Spring Framework Core", "spring-aop", diff, exp, "Scenario-based",
                            "To fix self-invocation: extract the method into a separate service bean or inject the self-bean lazily.");
                case 3:
                    return createQuestion(uid,
                            "Which Spring Boot Actuator endpoint provides detailed production metrics like JVM memory, garbage collection pauses, and HTTP request throughput?",
                            List.of("/actuator/metrics", "/actuator/health", "/actuator/info", "/actuator/beans"),
                            "/actuator/metrics",
                            "/actuator/metrics exposes Micrometer metrics including jvm.memory.used, jvm.gc.pause, and http.server.requests. /actuator/health only indicates component UP/DOWN status.",
                            Map.of("A", "Correct. /actuator/metrics provides detailed performance counters and gauges.",
                                    "B", "Incorrect. /actuator/health provides health check status indicator.",
                                    "C", "Incorrect. /actuator/info exposes arbitrary application build details.",
                                    "D", "Incorrect. /actuator/beans lists all configured ApplicationContext beans."),
                            "Spring Boot", "actuator-metrics", diff, exp, "Conceptual MCQ",
                            "Mention Prometheus integration: /actuator/prometheus formats these metrics for scraping.");
                default:
                    return createQuestion(uid,
                            "What is the difference between `@RestController` and `@Controller` in Spring Boot?",
                            List.of("@RestController is a convenience annotation combining @Controller and @ResponseBody",
                                    "@RestController supports XML while @Controller only supports JSON",
                                    "@RestController is for asynchronous methods only",
                                    "@RestController disables Spring Security filters automatically"),
                            "@RestController is a convenience annotation combining @Controller and @ResponseBody",
                            "@RestController annotates classes where every method returns domain objects directly written into the HTTP response body as JSON/XML via HttpMessageConverter, rather than rendering an HTML view template.",
                            Map.of("A", "Correct. @RestController = @Controller + @ResponseBody on every handler method.",
                                    "B", "Incorrect. Both can support XML or JSON based on HttpMessageConverters.",
                                    "C", "Incorrect. Asynchronous execution requires @Async or reactive types.",
                                    "D", "Incorrect. Security applies identically to both."),
                            "RESTful API Design", "http-semantics", diff, exp, "Conceptual MCQ",
                            "Explain how Jackson's MappingJackson2HttpMessageConverter serializes Java DTOs into JSON.");
            }
        } else if (topic.contains("Java 8") || topic.contains("Streams")) {
            switch (variant % 3) {
                case 0:
                    return createQuestion(uid,
                            "In the Java Streams API, what is the fundamental difference between intermediate and terminal operations?",
                            List.of("Intermediate operations are lazy and return a new Stream; terminal operations trigger execution and close the stream",
                                    "Intermediate operations execute immediately; terminal operations execute on a separate thread",
                                    "Intermediate operations modify the underlying collection in-place",
                                    "Terminal operations can be chained together indefinitely"),
                            "Intermediate operations are lazy and return a new Stream; terminal operations trigger execution and close the stream",
                            "Intermediate operations (filter, map, sorted) are evaluated lazily only when a terminal operation (collect, count, forEach) is called. Once a terminal operation finishes, the stream pipeline is consumed and cannot be reused.",
                            Map.of("A", "Correct. Intermediate operations are lazy transformations; terminal operations produce a result or side effect.",
                                    "B", "Incorrect. Intermediate operations are strictly lazy and do not execute eagerly.",
                                    "C", "Incorrect. Streams never mutate their source data structures.",
                                    "D", "Incorrect. A stream pipeline can only have exactly one terminal operation."),
                            "Streams API", "stream-operations", diff, exp, "Conceptual MCQ",
                            "Demonstrate that attempting to consume a stream twice results in IllegalStateException: stream has already been operated upon or closed.");
                case 1:
                    return createQuestion(uid,
                            "When should `parallelStream()` be avoided in high-throughput enterprise backend services?",
                            List.of("When tasks perform blocking I/O (database, HTTP calls) or when request threads are already pooled",
                                    "When processing more than 100 elements in memory",
                                    "When the CPU has more than 4 cores",
                                    "When filtering immutable strings"),
                            "When tasks perform blocking I/O (database, HTTP calls) or when request threads are already pooled",
                            "parallelStream() shares the common ForkJoinPool.commonPool() across the entire JVM. Blocking I/O inside parallel streams starves worker threads, crippling all parallel streams across the application.",
                            Map.of("A", "Correct. Blocking I/O saturates the common ForkJoinPool, impacting the entire JVM.",
                                    "B", "Incorrect. In-memory CPU-bound computations on large collections are the primary use-case.",
                                    "C", "Incorrect. Multiple cores are beneficial for parallel computation.",
                                    "D", "Incorrect. String filtering is purely CPU-bound."),
                            "Streams API", "parallel-streams", diff, exp, "Best-practice",
                            "Never use parallelStream() for database calls or REST API requests in Spring Boot.");
                default:
                    return createQuestion(uid,
                            "What is the difference between `Optional.orElse()` and `Optional.orElseGet()` in Java 8+?",
                            List.of("orElse() evaluates its default argument eagerly even when value is present; orElseGet() takes a Supplier evaluated lazily",
                                    "orElse() is thread-safe while orElseGet() is not",
                                    "orElse() returns null if the value is missing",
                                    "They behave identically with zero performance differences"),
                            "orElse() evaluates its default argument eagerly even when value is present; orElseGet() takes a Supplier evaluated lazily",
                            "orElse(expensiveCall()) executes expensiveCall() every time regardless of whether the Optional has a value. orElseGet(() -> expensiveCall()) only invokes the lambda if the Optional is empty.",
                            Map.of("A", "Correct. orElse is eager; orElseGet is lazy via Supplier.",
                                    "B", "Incorrect. Thread safety is unaffected.",
                                    "C", "Incorrect. orElse returns the default argument provided.",
                                    "D", "Incorrect. Eager evaluation can cause unnecessary database or network calls."),
                            "Java 8+ & Modern Java", "optional-api", diff, exp, "Code analysis",
                            "Always prefer orElseGet() when the fallback involves computing an object or database lookup.");
            }
        } else {
            // General Core Java & JVM
            switch (variant % 3) {
                case 0:
                    int val1 = 100;
                    int val2 = 200;
                    return createQuestion(uid,
                            "What does the following Java code print?\n\n```java\nInteger a = " + val1 + ", b = " + val1 + ";\nInteger c = " + val2 + ", d = " + val2 + ";\nSystem.out.println((a == b) + \" \" + (c == d));\n```",
                            List.of("true false", "true true", "false false", "false true"),
                            "true false",
                            "Java caches Integer objects between -128 and 127 (IntegerCache). Values in this range share the same cached reference, so (a == b) is true. Values outside this range (like 200) create separate heap instances, so (c == d) is false.",
                            Map.of("A", "Correct. a and b are cached by IntegerCache; c and d are separate heap objects.",
                                    "B", "Incorrect. 200 exceeds the default IntegerCache upper bound of 127.",
                                    "C", "Incorrect. 100 is within the -128 to 127 cached range.",
                                    "D", "Incorrect. c == d cannot be true while a == b is false."),
                            "Core Java", "jvm-internals", diff, exp, "Output-based",
                            "Always use equals() for object wrapper equality comparisons, never ==.");
                case 1:
                    return createQuestion(uid,
                            "What is the key advantage of `ZGC` (Z Garbage Collector) introduced in modern Java (JDK 17/21)?",
                            List.of("Sub-millisecond maximum pause times regardless of heap size (from 8MB to 16TB)",
                                    "Zero CPU overhead during collection cycles",
                                    "It eliminates the need to allocate Java heap memory",
                                    "It automatically fixes OutOfMemoryErrors at runtime"),
                            "Sub-millisecond maximum pause times regardless of heap size (from 8MB to 16TB)",
                            "ZGC performs all heavy GC work concurrently (concurrent marking, relocation, reference processing). Its maximum stop-the-world pause times are consistently below 1 millisecond.",
                            Map.of("A", "Correct. ZGC delivers sub-millisecond pauses across massive heap sizes.",
                                    "B", "Incorrect. Concurrent GC threads consume modest CPU cycles.",
                                    "C", "Incorrect. All Java objects reside on the heap.",
                                    "D", "Incorrect. ZGC cannot prevent OOM if heap memory is exhausted."),
                            "JVM & Performance Tuning", "gc-algorithms", diff, exp, "Conceptual MCQ",
                            "Highlighting Generational ZGC in Java 21 demonstrates up-to-date knowledge of modern Java runtime internals.");
                default:
                    return createQuestion(uid,
                            "What happens when an exception is thrown in a `try` block, and the `finally` block also executes a `return` statement?",
                            List.of("The exception is suppressed and silently swallowed; the finally return value is returned to the caller",
                                    "The exception is thrown and the finally return statement is ignored",
                                    "A MultipleReturnException is thrown at runtime",
                                    "The code will fail to compile with an unreachable statement error"),
                            "The exception is suppressed and silently swallowed; the finally return value is returned to the caller",
                            "A return statement inside a finally block overrides any unhandled exception or previous return in the try/catch block, causing the exception to be silently discarded. This is considered an anti-pattern.",
                            Map.of("A", "Correct. Returning from a finally block discards and swallows active exceptions.",
                                    "B", "Incorrect. The finally return takes precedence over thrown exceptions.",
                                    "C", "Incorrect. Java does not have a MultipleReturnException.",
                                    "D", "Incorrect. The code compiles without error but violates clean coding practices."),
                            "Exception Handling & Best Practices", "exception-hierarchy", diff, exp, "Interview trick questions",
                            "Never place return or throw statements inside finally blocks; it hides severe application bugs.");
            }
        }
    }

    public List<Question> getCuratedQuestionBank() {
        List<Question> list = new ArrayList<>();

        // 1. Core Java - String Pool
        list.add(createQuestion(
                "q_java_01",
                "What is the output of the following Java snippet?\n\n```java\nString s1 = \"prepforge\";\nString s2 = new String(\"prepforge\");\nString s3 = s2.intern();\nSystem.out.println((s1 == s2) + \" \" + (s1 == s3));\n```",
                List.of("false true", "true true", "false false", "true false"),
                "false true",
                "s1 points to the string literal in the String Pool. s2 is created in heap memory, so (s1 == s2) evaluates to false. Calling s2.intern() returns the canonical reference from the String Pool, identical to s1, making (s1 == s3) true.",
                Map.of("A", "Correct. s2 is on the heap, but s2.intern() returns the pooled reference matching s1.",
                        "B", "Incorrect. new String() always creates a separate heap instance.",
                        "C", "Incorrect. s1 == s3 is true due to string pooling.",
                        "D", "Incorrect. s1 == s2 cannot be true."),
                "Core Java", "jvm-internals", "Medium", "1-2 years", "Output-based",
                "Interviewers love asking about String Constant Pool and intern() to test understanding of heap allocation versus string pooling."
        ));

        // 2. Core Java - Equals & HashCode
        list.add(createQuestion(
                "q_java_02",
                "Why is it strongly recommended to override `hashCode()` whenever `equals()` is overridden in Java?",
                List.of("To fulfill the general contract so that equal objects produce equal hash codes in hash-based collections",
                        "To prevent compilation errors when defining custom classes",
                        "To ensure objects are placed into the exact same LinkedList bucket inside HashMap",
                        "To improve the garbage collection cycle performance of the object"),
                "To fulfill the general contract so that equal objects produce equal hash codes in hash-based collections",
                "According to the Java Object contract: if two objects are equal according to equals(Object), calling hashCode() on each must produce the same integer result. If violated, HashMap cannot reliably retrieve objects.",
                Map.of("A", "Correct. The Object contract guarantees that equal objects must have identical hash codes.",
                        "B", "Incorrect. Overriding equals() without hashCode() compiles without error.",
                        "C", "Incorrect. The goal of hashing is even distribution across buckets.",
                        "D", "Incorrect. hashCode() has no relation to garbage collection cycles."),
                "Core Java", "java-syntax", "Medium", "1-2 years", "Conceptual MCQ",
                "Always mention that violating the equals/hashCode contract causes HashMap.get() to return null even when a matching key exists."
        ));

        // 3. Collections - HashMap Treeify
        list.add(createQuestion(
                "q_col_01",
                "In Java 8+, what condition causes a `HashMap` bucket to transition from a LinkedList to a Balanced Red-Black Tree (Treeify)?",
                List.of("When bucket entries reach TREEIFY_THRESHOLD (8) AND table capacity is at least MIN_TREEIFY_CAPACITY (64)",
                        "When the total number of elements in the HashMap exceeds 64",
                        "Whenever any single hash collision occurs",
                        "When the load factor exceeds 0.75"),
                "When bucket entries reach TREEIFY_THRESHOLD (8) AND table capacity is at least MIN_TREEIFY_CAPACITY (64)",
                "When a bucket has 8 elements and the table capacity is at least 64, the bucket transforms into a Red-Black Tree, improving worst-case search time complexity from O(n) to O(log n). If table capacity is less than 64, it resizes the table instead.",
                Map.of("A", "Correct. Both conditions must be met: bucket length >= 8 AND array capacity >= 64.",
                        "B", "Incorrect. Exceeding 64 elements triggers table resizing based on load factor.",
                        "C", "Incorrect. Collisions initially chain into a linked list.",
                        "D", "Incorrect. Load factor 0.75 determines when table capacity doubles."),
                "Java Collections Framework", "hashmap-internals", "Hard", "2-3 years", "Conceptual MCQ",
                "Highlighting the worst-case time complexity transition from O(N) to O(log N) demonstrates senior-level knowledge of Java internals."
        ));

        // 4. Multithreading - volatile semantics
        list.add(createQuestion(
                "q_multi_01",
                "What memory guarantee is provided by the `volatile` keyword in Java?",
                List.of("Guarantees visibility across threads and establishes a happens-before relationship, but does NOT guarantee atomicity for compound operations",
                        "Guarantees atomicity for all operations including increments like count++",
                        "Acquires an exclusive monitor lock on the object",
                        "Prevents the thread from being preempted by the OS scheduler"),
                "Guarantees visibility across threads and establishes a happens-before relationship, but does NOT guarantee atomicity for compound operations",
                "volatile ensures reads and writes go directly to main memory and prevents compiler/CPU instruction reordering. However, compound read-modify-write operations like count++ are not atomic.",
                Map.of("A", "Correct. volatile provides visibility and memory ordering, but not atomicity for compound operations.",
                        "B", "Incorrect. count++ requires synchronization or AtomicInteger for atomicity.",
                        "C", "Incorrect. volatile does not use monitor locks.",
                        "D", "Incorrect. volatile has no control over OS thread scheduling."),
                "Multithreading & Concurrency", "locks-volatiles", "Medium", "1-2 years", "Conceptual MCQ",
                "A classic interview question: explain why two threads incrementing a volatile int 10,000 times result in a value less than 20,000."
        ));

        // 5. Spring Boot - Auto-configuration
        list.add(createQuestion(
                "q_spring_01",
                "How does Spring Boot's `@ConditionalOnMissingBean` annotation assist in writing robust microservice libraries?",
                List.of("It provides a default bean implementation while allowing application developers to override it by defining their own bean",
                        "It prevents circular dependencies between beans",
                        "It forces Spring to initialize the bean as a prototype",
                        "It validates that required environment variables are non-null"),
                "It provides a default bean implementation while allowing application developers to override it by defining their own bean",
                "@ConditionalOnMissingBean tells Spring Boot to register the autoconfigured bean ONLY if the user has not already defined a bean of that type, enabling seamless custom overriding.",
                Map.of("A", "Correct. Auto-configuration uses @ConditionalOnMissingBean to let user configurations take precedence.",
                        "B", "Incorrect. Circular dependencies are handled via design refactoring or @Lazy.",
                        "C", "Incorrect. Scope is defined by @Scope.",
                        "D", "Incorrect. Configuration properties validation uses @Validated."),
                "Spring Boot", "auto-configuration", "Medium", "1-2 years", "Best-practice",
                "Mention that custom Spring Boot starters use @ConditionalOnMissingBean so developers can easily customize behavior."
        ));

        // 6. Spring Security - FilterChain
        list.add(createQuestion(
                "q_sec_01",
                "In Spring Security 6+ (Spring Boot 3+), how is HTTP security configured without extending deprecated adapter classes?",
                List.of("By registering a `@Bean` returning a `SecurityFilterChain`",
                        "By extending `WebSecurityConfigurerAdapter`",
                        "By configuring web.xml with security constraints",
                        "By defining security policies inside application.properties only"),
                "By registering a `@Bean` returning a `SecurityFilterChain`",
                "Spring Security 5.7+ deprecated WebSecurityConfigurerAdapter. In Spring Boot 3, security is configured component-style by defining a SecurityFilterChain @Bean taking HttpSecurity.",
                Map.of("A", "Correct. The modern standard is a @Bean returning SecurityFilterChain.",
                        "B", "Incorrect. WebSecurityConfigurerAdapter was deprecated and completely removed in Spring Security 6.",
                        "C", "Incorrect. web.xml is obsolete in modern Spring Boot applications.",
                        "D", "Incorrect. Complete security policies cannot be configured solely via properties."),
                "Spring Security & JWT", "filter-chain", "Medium", "1-2 years", "Best-practice",
                "Highlighting the shift from inheritance (WebSecurityConfigurerAdapter) to composition (SecurityFilterChain bean) shows current knowledge."
        ));

        return list;
    }

    private Question createQuestion(
            String id,
            String question,
            List<String> options,
            String correctAnswer,
            String explanation,
            Map<String, String> optionExplanations,
            String topic,
            String subTopic,
            String difficulty,
            String experienceLevel,
            String questionType,
            String interviewTip
    ) {
        return Question.builder()
                .id(id)
                .question(question)
                .options(options)
                .correctAnswer(correctAnswer)
                .explanation(explanation)
                .optionExplanations(optionExplanations)
                .topic(topic)
                .subTopic(subTopic)
                .difficulty(difficulty)
                .experienceLevel(experienceLevel)
                .questionType(questionType)
                .interviewTip(interviewTip)
                .build();
    }
}
