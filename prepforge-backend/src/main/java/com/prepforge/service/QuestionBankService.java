package com.prepforge.service;

import com.prepforge.entity.Question;
import com.prepforge.repository.QuestionRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class QuestionBankService {

    private static final Logger log = LoggerFactory.getLogger(QuestionBankService.class);
    private final QuestionRepository questionRepository;

    public QuestionBankService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @PostConstruct
    public void seedInitialQuestions() {
        try {
            log.info("Refreshing comprehensive Java Backend question bank in database...");
            questionRepository.deleteAll();
            List<Question> questions = getCuratedQuestionBank();
            questionRepository.saveAll(questions);
            log.info("Successfully seeded {} curated Java Backend questions.", questions.size());
        } catch (Exception e) {
            log.warn("Question bank database seeding skipped (will use in-memory bank): {}", e.getMessage());
        }
    }

    /**
     * Generates dynamic, non-repetitive parametric questions on the fly for any topic and target count.
     */
    public List<Question> generateDynamicJavaQuestions(List<String> topics, String experienceLevel, String difficulty, int targetCount) {
        List<Question> dynamicList = new ArrayList<>();
        Random random = ThreadLocalRandom.current();

        for (int i = 0; i < targetCount; i++) {
            String topic = (topics != null && !topics.isEmpty())
                    ? topics.get(i % topics.size())
                    : "Core Java";

            dynamicList.add(createParametricQuestion(topic, experienceLevel, difficulty, i, random));
        }

        return dynamicList;
    }

    private Question createParametricQuestion(String topic, String exp, String diff, int index, Random random) {
        int a = random.nextInt(2, 10);
        int b = random.nextInt(10, 50);
        String uid = "dyn_" + topic.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis() + "_" + index;

        if (topic.contains("Collections") || topic.contains("Collection")) {
            return createQuestion(
                    uid,
                    "In a high-throughput Java backend application, which collection provides thread-safe access with segment/bucket level locking rather than global table synchronization?",
                    List.of(
                            "ConcurrentHashMap",
                            "Collections.synchronizedMap(new HashMap<>())",
                            "Hashtable",
                            "TreeMap"
                    ),
                    "ConcurrentHashMap",
                    "ConcurrentHashMap uses lock striping / synchronized tree bins on individual buckets (CAS + synchronized per bucket) to allow concurrent reads and writes without locking the entire map, unlike Hashtable or synchronizedMap which use a single mutual exclusion monitor lock.",
                    Map.of(
                            "A", "Correct. ConcurrentHashMap allows concurrent reads without locking and locks only specific buckets/bins during writes.",
                            "B", "Incorrect. Collections.synchronizedMap synchronizes every read/write operation on the same single mutex lock.",
                            "C", "Incorrect. Hashtable synchronizes every method at the method level, creating a severe concurrency bottleneck.",
                            "D", "Incorrect. TreeMap is neither thread-safe nor hash-based."
                    ),
                    "Java Collections Framework", "hashmap-internals", diff, exp, "Conceptual MCQ",
                    "Always mention how ConcurrentHashMap in Java 8+ replaced Segment locks with CAS and synchronized nodes on individual bucket heads."
            );
        } else if (topic.contains("Multithreading") || topic.contains("Concurrency")) {
            int poolSize = a * 2;
            return createQuestion(
                    uid,
                    "Consider a `ThreadPoolExecutor` configured with corePoolSize=" + a + ", maxPoolSize=" + poolSize + ", and an unbounded `LinkedBlockingQueue`. When " + (poolSize + 5) + " tasks are submitted concurrently, how many active worker threads are created?",
                    List.of(
                            String.valueOf(a) + " threads (only corePoolSize is created because the work queue is unbounded)",
                            String.valueOf(poolSize) + " threads (maxPoolSize is reached)",
                            String.valueOf(poolSize + 5) + " threads",
                            "An IllegalArgumentException is thrown"
                    ),
                    String.valueOf(a) + " threads (only corePoolSize is created because the work queue is unbounded)",
                    "ThreadPoolExecutor only scales threads beyond corePoolSize when the work queue is full. With an unbounded LinkedBlockingQueue, the queue never fills up, so threads beyond corePoolSize (" + a + ") will never be created.",
                    Map.of(
                            "A", "Correct. Threads beyond corePoolSize are only spawned if the queue rejects task insertion due to reaching capacity.",
                            "B", "Incorrect. maxPoolSize is ignored when using an unbounded work queue.",
                            "C", "Incorrect. The executor never creates more than maxPoolSize threads under any circumstance.",
                            "D", "Incorrect. An unbounded queue is valid syntax and will not throw exceptions."
                    ),
                    "Multithreading & Concurrency", "executors-futures", diff, exp, "Scenario-based",
                    "This is a classic senior Java concurrency question. Remind interviewers that using unbounded queues with maxPoolSize can lead to unexpected OOM without increasing thread counts."
            );
        } else if (topic.contains("Spring Boot") || topic.contains("Spring")) {
            return createQuestion(
                    uid,
                    "What is the default scope of a Spring Bean defined via `@Component` or `@Service` in Spring Boot, and is it thread-safe by default?",
                    List.of(
                            "Singleton scope; it is NOT inherently thread-safe if it maintains mutable state.",
                            "Prototype scope; a new instance is created on each injection.",
                            "Request scope; tied to the HTTP request lifecycle.",
                            "Singleton scope; Spring automatically synchronizes all method calls."
                    ),
                    "Singleton scope; it is NOT inherently thread-safe if it maintains mutable state.",
                    "Spring beans are Singletons by default (one instance per ApplicationContext). Because multiple HTTP requests execute concurrently on different worker threads accessing the same singleton bean instance, maintaining mutable instance fields causes race conditions.",
                    Map.of(
                            "A", "Correct. Spring Singletons are shared across all threads in the JVM. Statelessness or proper thread-safety mechanisms must be used.",
                            "B", "Incorrect. Prototype scope must be explicitly declared with @Scope('prototype').",
                            "C", "Incorrect. Request scope is only for web-aware contexts with @RequestScope.",
                            "D", "Incorrect. Spring does NOT automatically synchronize bean method executions."
                    ),
                    "Spring Boot", "auto-configuration", diff, exp, "Conceptual MCQ",
                    "State that enterprise Spring services should always remain stateless to prevent concurrency bugs under concurrent user load."
            );
        } else if (topic.contains("JPA") || topic.contains("Hibernate")) {
            return createQuestion(
                    uid,
                    "How do you resolve the `N+1 Query Problem` when fetching a `@OneToMany` collection of child entities in Spring Data JPA?",
                    List.of(
                            "Use `@Query(\"SELECT p FROM Parent p JOIN FETCH p.children\")` or `@EntityGraph(attributePaths = {\"children\"})`",
                            "Change FetchType to `EAGER` on the @OneToMany annotation",
                            "Set `spring.jpa.show-sql=false` in application.properties",
                            "Use `findAllById()` inside a for-each loop"
                    ),
                    "Use `@Query(\"SELECT p FROM Parent p JOIN FETCH p.children\")` or `@EntityGraph(attributePaths = {\"children\"})`",
                    "Setting FetchType.EAGER does NOT solve N+1 queries in JPQL/Criteria queries (it actually triggers N immediate secondary select queries). JOIN FETCH or @EntityGraph forces Hibernate to perform an SQL LEFT OUTER JOIN in a single query.",
                    Map.of(
                            "A", "Correct. JOIN FETCH or @EntityGraph forces a single SQL JOIN query, loading parents and children together.",
                            "B", "Incorrect. FetchType.EAGER often exacerbates the N+1 problem by firing N extra SELECT queries eagerly.",
                            "C", "Incorrect. Hiding SQL logs does not fix the underlying performance issue.",
                            "D", "Incorrect. Looping queries in Java code causes the exact N+1 query antipattern."
                    ),
                    "Hibernate ORM & Performance", "n-plus-one", diff, exp, "Best-practice",
                    "Emphasize that FetchType.LAZY + JOIN FETCH / EntityGraph is the recommended production standard for all @OneToMany relationships."
            );
        } else if (topic.contains("SQL") || topic.contains("DBMS")) {
            return createQuestion(
                    uid,
                    "In SQL, which index structure is most optimal for range queries (e.g. `WHERE created_at BETWEEN ? AND ?`) and sorting operations?",
                    List.of(
                            "B-Tree (Balanced Tree) Index",
                            "Hash Index",
                            "Bitmap Index",
                            "Spatial Index"
                    ),
                    "B-Tree (Balanced Tree) Index",
                    "B-Tree indexes store keys in sorted sequential order with pointers to data rows, making range scans (BETWEEN, <, >) and ORDER BY operations logarithmic in lookup and linear in traversal. Hash indexes only support exact equality (=).",
                    Map.of(
                            "A", "Correct. B-Tree structures maintain sorted ordering which is optimal for range filtering and ordering.",
                            "B", "Incorrect. Hash indexes only support O(1) exact match lookups and cannot perform range scans.",
                            "C", "Incorrect. Bitmap indexes are used in OLAP systems with low-cardinality columns.",
                            "D", "Incorrect. Spatial indexes are used for geospatial coordinate data (R-Tree)."
                    ),
                    "SQL & Query Optimization", "query-optimization", diff, exp, "Conceptual MCQ",
                    "Explain that B+ Trees keep all data pointers in leaf nodes connected via a doubly linked list, enabling rapid range scans."
            );
        } else if (topic.contains("Kafka") || topic.contains("Redis")) {
            return createQuestion(
                    uid,
                    "When designing a distributed caching layer with Redis in a Java Spring Boot application, what strategy best mitigates the `Cache Stampede` (Thundering Herd) problem?",
                    List.of(
                            "Using distributed mutex locks (e.g., Redisson) or pre-computing cache keys with randomized TTL jitter",
                            "Disabling TTL so cache keys never expire",
                            "Increasing the maximum heap size of the Spring Boot application",
                            "Switching to in-memory ConcurrentHashMap on each instance"
                    ),
                    "Using distributed mutex locks (e.g., Redisson) or pre-computing cache keys with randomized TTL jitter",
                    "Cache stampede occurs when a high-traffic cache key expires simultaneously, causing hundreds of concurrent threads to hit the backend database at once. Mutex locking ensures only one thread regenerates the cache while others wait, and TTL jitter prevents synchronized expiration.",
                    Map.of(
                            "A", "Correct. Distributed mutexes and randomized TTL jitter prevent thousands of threads from overwhelming the DB simultaneously.",
                            "B", "Incorrect. Never expiring cache keys leads to stale data and memory exhaustion.",
                            "C", "Incorrect. Java heap size has no effect on external Redis cache expiration spikes.",
                            "D", "Incorrect. Local maps lack cross-instance distributed consistency."
                    ),
                    "Redis & Backend Caching", "cache-patterns", diff, exp, "Scenario-based",
                    "Mentioning TTL jitter (+/- 5% random offset) demonstrates real-world production experience with distributed high-load caching."
            );
        } else {
            // Core Java snippet
            return createQuestion(
                    uid,
                    "What will be the output of the following Java code?\n\n```java\nint x = " + a + ";\nint y = " + b + ";\nSystem.out.println(x > y ? \"Alpha\" : (y % x == 0 ? \"Beta\" : \"Gamma\"));\n```",
                    List.of(
                            (b % a == 0) ? "Beta" : "Gamma",
                            (b % a == 0) ? "Gamma" : "Beta",
                            "Alpha",
                            "Compilation Error"
                    ),
                    (b % a == 0) ? "Beta" : "Gamma",
                    "Since " + a + " is not greater than " + b + ", the ternary operator evaluates the false branch. Because " + b + " % " + a + " evaluates to " + (b % a) + ", the resulting output is " + ((b % a == 0) ? "Beta" : "Gamma") + ".",
                    Map.of(
                            "A", "Correct based on the evaluation of the ternary operator logic.",
                            "B", "Incorrect calculation of modulo condition.",
                            "C", "Incorrect because " + a + " is less than " + b + ".",
                            "D", "Incorrect. Nested ternary operators are valid Java expressions."
                    ),
                    "Core Java", "java-syntax", diff, exp, "Output-based",
                    "Test your ability to trace conditional ternary expressions quickly under interview pressure."
            );
        }
    }

    public List<Question> getCuratedQuestionBank() {
        List<Question> list = new ArrayList<>();

        // 1. Core Java - String Pool
        list.add(createQuestion(
                "q_java_01",
                "What is the output of the following Java snippet?\n\n```java\nString s1 = \"prepforge\";\nString s2 = new String(\"prepforge\");\nString s3 = s2.intern();\nSystem.out.println((s1 == s2) + \" \" + (s1 == s3));\n```",
                List.of("true true", "false true", "false false", "true false"),
                "false true",
                "s1 points to the string literal in the String Pool. s2 is created in heap memory, so (s1 == s2) evaluates to false. Calling s2.intern() returns the canonical representation from the String Pool, which is the exact same reference as s1, making (s1 == s3) evaluate to true.",
                Map.of(
                        "A", "Incorrect because 'new String()' explicitly creates a new object on the heap, so s1 and s2 refer to distinct heap memory addresses.",
                        "B", "Correct. 's1 == s2' is false because s2 is a separate heap instance, but 's1 == s3' is true because s2.intern() returns the String Pool reference identical to s1.",
                        "C", "Incorrect because 's2.intern()' returns the interned reference in the String Constant Pool where s1 resides.",
                        "D", "Incorrect because 's1 == s2' cannot be true due to object reference inequality."
                ),
                "Core Java", "jvm-internals", "Medium", "1-2 years", "Output-based",
                "Interviewers love asking about String Constant Pool and intern() to test your understanding of heap memory allocation versus string pooling."
        ));

        // 2. Core Java - Equals & HashCode
        list.add(createQuestion(
                "q_java_02",
                "Why is it strongly recommended to override `hashCode()` whenever `equals()` is overridden in Java?",
                List.of(
                        "To prevent compilation errors when defining custom classes.",
                        "To fulfill the general contract so that equal objects produce equal hash codes in hash-based collections like HashMap and HashSet.",
                        "To ensure objects are placed into the exact same LinkedList bucket inside HashMap.",
                        "To improve the garbage collection cycle performance of the object."
                ),
                "To fulfill the general contract so that equal objects produce equal hash codes in hash-based collections like HashMap and HashSet.",
                "According to the Java Object contract: if two objects are equal according to equals(Object), calling hashCode() on each must produce the same integer result. If violated, hash-based collections cannot reliably locate, store, or retrieve objects.",
                Map.of(
                        "A", "Incorrect. Overriding equals() without hashCode() compiles without error, though it generates a compiler/linter warning.",
                        "B", "Correct. The Object contract guarantees that two equal objects must have identical hash codes to function properly in Hash-based data structures.",
                        "C", "Incorrect. The goal of hashing is even distribution across buckets, not clustering into the same bucket.",
                        "D", "Incorrect. hashCode() has no direct relation to garbage collection cycles."
                ),
                "Core Java", "java-syntax", "Medium", "1-2 years", "Conceptual MCQ",
                "Always mention that violating the equals/hashCode contract causes HashMap.get() to return null even when a matching key exists."
        ));

        // 3. Core Java - Pass-by-value
        list.add(createQuestion(
                "q_java_03",
                "How does Java pass object references into methods when calling `modify(MyObject obj)`?",
                List.of(
                        "Strictly pass-by-value; a copy of the reference address is passed by value.",
                        "Strictly pass-by-reference; the original memory variable itself is passed.",
                        "Pass-by-reference for objects, and pass-by-value for primitive types.",
                        "Pass-by-value for immutable objects and pass-by-reference for mutable objects."
                ),
                "Strictly pass-by-value; a copy of the reference address is passed by value.",
                "Java is ALWAYS strictly pass-by-value. When an object reference is passed, a copy of the reference pointer is passed by value. Reassigning the parameter inside the method does not affect the caller's reference.",
                Map.of(
                        "A", "Correct. Java is purely pass-by-value. For objects, the value being copied is the object's reference address.",
                        "B", "Incorrect. Java does not have C++ style pass-by-reference.",
                        "C", "Incorrect. Both primitives and object references are passed by value in Java.",
                        "D", "Incorrect. Immutability does not change Java's parameter passing semantics."
                ),
                "Core Java", "java-syntax", "Easy", "0-1 years", "Conceptual MCQ",
                "Be ready to demonstrate that `obj = new MyObject()` inside a helper method leaves the caller's variable unchanged."
        ));

        // 4. Collections - HashMap Treeify
        list.add(createQuestion(
                "q_col_01",
                "In Java 8+, what condition causes a `HashMap` bucket to transition from a LinkedList to a Balanced Red-Black Tree (Treeify)?",
                List.of(
                        "When the number of total elements in the HashMap exceeds 64.",
                        "When the number of entries in a single bucket reaches TREEIFY_THRESHOLD (8) AND total table capacity is at least MIN_TREEIFY_CAPACITY (64).",
                        "Whenever a single hash collision occurs.",
                        "When the load factor exceeds 0.75."
                ),
                "When the number of entries in a single bucket reaches TREEIFY_THRESHOLD (8) AND total table capacity is at least MIN_TREEIFY_CAPACITY (64).",
                "In Java 8, when a bucket has 8 elements and the table capacity is at least 64, the bucket is transformed into a TreeNode (Red-Black Tree), improving worst-case search time complexity from O(n) to O(log n). If table capacity is less than 64, it resizes the table instead.",
                Map.of(
                        "A", "Incorrect. Exceeding 64 elements alone triggers table resizing based on load factor, not necessarily treeification.",
                        "B", "Correct. Both conditions must be met: bucket length >= 8 AND array capacity >= 64.",
                        "C", "Incorrect. Hash collisions are initially handled by chaining in a linked list.",
                        "D", "Incorrect. Load factor 0.75 determines when the overall table capacity doubles."
                ),
                "Java Collections Framework", "hashmap-internals", "Hard", "2-3 years", "Code analysis",
                "Highlighting the worst-case time complexity transition from O(N) to O(log N) demonstrates senior-level knowledge of Java internals."
        ));

        // 5. Collections - Fail-Fast vs Fail-Safe
        list.add(createQuestion(
                "q_col_02",
                "What exception is thrown when an `ArrayList` is structurally modified while iterating through it using a standard for-each loop?",
                List.of(
                        "ConcurrentModificationException",
                        "IllegalStateException",
                        "IndexOutOfBoundsException",
                        "NoSuchElementException"
                ),
                "ConcurrentModificationException",
                "ArrayList iterators are fail-fast. They check the internal `modCount` against expectedModCount on every next() call. If modified during iteration without using Iterator.remove(), a ConcurrentModificationException is immediately thrown.",
                Map.of(
                        "A", "Correct. ArrayList uses a fail-fast iterator that throws ConcurrentModificationException upon structural modification.",
                        "B", "Incorrect. IllegalStateException is thrown if Iterator.remove() is called before next().",
                        "C", "Incorrect. Array bounds are not violated.",
                        "D", "Incorrect. NoSuchElementException is thrown when next() is called past the end of the collection."
                ),
                "Java Collections Framework", "list-set-implementations", "Easy", "0-1 years", "Conceptual MCQ",
                "Explain the difference between fail-fast (ArrayList, HashMap) and fail-safe (CopyOnWriteArrayList, ConcurrentHashMap) collections."
        ));

        // 6. Multithreading - Volatile vs Atomic
        list.add(createQuestion(
                "q_thread_01",
                "What is the primary difference between the `volatile` keyword and `AtomicInteger` in Java concurrency?",
                List.of(
                        "volatile guarantees atomicity of compound operations (like count++), whereas AtomicInteger only guarantees visibility.",
                        "volatile guarantees visibility across CPU caches using memory barriers, but does NOT guarantee compound operation atomicity. AtomicInteger provides both visibility and atomic CAS (Compare-And-Swap) operations.",
                        "volatile locks the monitor of the object, while AtomicInteger is non-blocking.",
                        "volatile is deprecated in modern Java in favor of AtomicInteger."
                ),
                "volatile guarantees visibility across CPU caches using memory barriers, but does NOT guarantee compound operation atomicity. AtomicInteger provides both visibility and atomic CAS (Compare-And-Swap) operations.",
                "volatile ensures that reads and writes go directly to main memory (visibility and instruction reordering prevention), but operations like i++ (read-modify-write) are not atomic. AtomicInteger uses low-level hardware CAS instructions to provide atomic updates.",
                Map.of(
                        "A", "Incorrect. volatile does NOT guarantee atomicity of compound operations like count++.",
                        "B", "Correct. volatile provides memory visibility without mutual exclusion; AtomicInteger provides atomic lock-free updates via CAS.",
                        "C", "Incorrect. volatile is completely non-blocking and does not acquire monitor locks.",
                        "D", "Incorrect. volatile is actively used throughout modern concurrency frameworks."
                ),
                "Multithreading & Concurrency", "locks-volatiles", "Hard", "2-3 years", "Conceptual MCQ",
                "Explain the three steps of 'count++' (read, modify, write) to clearly demonstrate why volatile alone fails under concurrent writes."
        ));

        // 7. Multithreading - CompletableFuture
        list.add(createQuestion(
                "q_thread_02",
                "In Java Concurrency, which method on `CompletableFuture` executes a subsequent asynchronous stage only when BOTH supplied stages complete successfully?",
                List.of(
                        "thenCombineAsync()",
                        "applyToEitherAsync()",
                        "thenAcceptBoth()",
                        "allOf()"
                ),
                "thenCombineAsync()",
                "thenCombine / thenCombineAsync takes another CompletableFuture and a BiFunction, executing when both upstream futures complete and transforming both results into a single result.",
                Map.of(
                        "A", "Correct. thenCombine executes a function with results of two completed futures.",
                        "B", "Incorrect. applyToEither executes when either one of the two completes first.",
                        "C", "Incorrect. thenAcceptBoth consumes both results but returns CompletableFuture<Void> without producing a return value.",
                        "D", "Incorrect. allOf takes a varargs array of futures and returns CompletableFuture<Void>."
                ),
                "Multithreading & Concurrency", "executors-futures", "Medium", "2-3 years", "Conceptual MCQ",
                "CompletableFuture pipelines are frequently tested in modern Java backend and reactive programming interviews."
        ));

        // 8. Java 8 - Streams Laziness
        list.add(createQuestion(
                "q_stream_01",
                "Consider the following code. How many times will `peek()` print to the console?\n\n```java\nStream.of(\"one\", \"two\", \"three\", \"four\")\n    .filter(e -> e.length() > 3)\n    .peek(e -> System.out.println(\"Filtered: \" + e));\n```",
                List.of("2 times", "4 times", "0 times", "Throws an IllegalStateException"),
                "0 times",
                "Java Streams are lazy. Intermediate operations like filter() and peek() are not executed unless a terminal operation (like collect(), forEach(), count()) is invoked on the stream pipeline.",
                Map.of(
                        "A", "Incorrect. Although 2 elements have length > 3, the stream is never evaluated.",
                        "B", "Incorrect. Streams do not eagerly process elements.",
                        "C", "Correct. Because there is no terminal operation attached to the pipeline, intermediate operations are never triggered due to lazy evaluation.",
                        "D", "Incorrect. The syntax is completely valid and creates an unconsumed Stream instance without throwing exceptions."
                ),
                "Streams API", "stream-operations", "Medium", "1-2 years", "Interview trick questions",
                "Stream laziness is one of the top 3 Java 8 trick questions asked in technical rounds. Always look for the terminal operation."
        ));

        // 9. Java 8 - FlatMap
        list.add(createQuestion(
                "q_stream_02",
                "What is the difference between `map()` and `flatMap()` in Java Streams?",
                List.of(
                        "map transforms each element into another object (1-to-1), whereas flatMap transforms each element into a Stream and flattens multiple streams into a single Stream (1-to-N).",
                        "map is for primitive types and flatMap is for reference types.",
                        "flatMap executes in parallel whereas map executes sequentially.",
                        "map modifies the underlying collection in place, whereas flatMap creates a copy."
                ),
                "map transforms each element into another object (1-to-1), whereas flatMap transforms each element into a Stream and flattens multiple streams into a single Stream (1-to-N).",
                "map() produces one output element for each input element. flatMap() takes a function that returns a stream of elements and flattens the resulting streams of streams into a single contiguous stream.",
                Map.of(
                        "A", "Correct. map transforms Stream<T> to Stream<R>; flatMap transforms Stream<List<T>> or Stream<Stream<T>> to Stream<T>.",
                        "B", "Incorrect. Map and flatMap both operate on object and primitive streams.",
                        "C", "Incorrect. Both operations follow the stream's sequential/parallel mode.",
                        "D", "Incorrect. Streams never modify their underlying source collections."
                ),
                "Streams API", "stream-operations", "Easy", "0-1 years", "Conceptual MCQ",
                "Give a quick example like flattening a `List<Order>` containing multiple `List<Item>` into a single `Stream<Item>`."
        ));

        // 10. Spring Boot - Transactional Proxy Bypass
        list.add(createQuestion(
                "q_spring_01",
                "What happens when a method annotated with `@Transactional` calls another `@Transactional(propagation = Propagation.REQUIRES_NEW)` method within the same Spring Bean class?",
                List.of(
                        "A new independent physical database transaction is suspended and started as expected.",
                        "The inner method runs within the existing transaction because Spring's standard CGLIB proxy is bypassed on self-invocation (this-call).",
                        "Spring throws an UnsupportedOperationException at runtime.",
                        "The existing transaction is immediately committed before the inner method runs."
                ),
                "The inner method runs within the existing transaction because Spring's standard CGLIB proxy is bypassed on self-invocation (this-call).",
                "Spring AOP works via dynamic proxies. When a method invokes another method within the same instance using 'this', the proxy is bypassed, meaning annotations like @Transactional, @Async, and @Cacheable on the internal method are ignored.",
                Map.of(
                        "A", "Incorrect. Propagation.REQUIRES_NEW requires intercepting through the proxy, which does not happen during internal self-invocation.",
                        "B", "Correct. Internal self-invocation bypasses the Spring proxy, causing the inner method to run without its configured transactional advice.",
                        "C", "Incorrect. No exception is thrown; it silently executes within the caller's context.",
                        "D", "Incorrect. The outer transaction remains active and uncommitted."
                ),
                "Spring Boot", "auto-configuration", "Hard", "3-5 years", "Code analysis",
                "Mentioning Spring AOP proxy interception and suggesting self-injection or extracting to a separate service shows deep Spring architecture mastery."
        ));

        // 11. Spring Security - FilterChain
        list.add(createQuestion(
                "q_sec_01",
                "In Spring Security 6+ (Spring Boot 3+), how is the security filter chain configured instead of extending the deprecated `WebSecurityConfigurerAdapter`?",
                List.of(
                        "By declaring a `@Bean` method that returns a `SecurityFilterChain` taking `HttpSecurity` as a parameter.",
                        "By implementing the `SecurityFilter` interface directly on @Controller classes.",
                        "By configuring security XML elements inside application.yml.",
                        "By annotating the main application class with `@EnableWebSecurityAdapter`."
                ),
                "By declaring a `@Bean` method that returns a `SecurityFilterChain` taking `HttpSecurity` as a parameter.",
                "Spring Security 5.7+ deprecated WebSecurityConfigurerAdapter in favor of component-based security configuration using a @Bean of type SecurityFilterChain, enabling cleaner lambda-based DSL configuration.",
                Map.of(
                        "A", "Correct. Standard Spring Boot 3+ security declares `@Bean public SecurityFilterChain filterChain(HttpSecurity http) throws Exception`.",
                        "B", "Incorrect. Security is handled by the DelegatingFilterProxy before reaching controllers.",
                        "C", "Incorrect. Modern Spring Security uses programmatic Java DSL configuration.",
                        "D", "Incorrect. WebSecurityConfigurerAdapter and associated adapters are removed."
                ),
                "Spring Security & JWT", "filter-chain", "Medium", "2-3 years", "Best-practice",
                "Highlighting Spring Boot 3 / Spring Security 6 lambda DSL syntax (`http.authorizeHttpRequests(auth -> auth...)`) demonstrates up-to-date modern Java expertise."
        ));

        // 12. REST APIs - 201 Created
        list.add(createQuestion(
                "q_rest_01",
                "Which HTTP status code should be returned by a REST API when a `POST /orders` request successfully creates a new resource?",
                List.of(
                        "200 OK with the resource representation.",
                        "201 Created with a `Location` header pointing to the new resource URI.",
                        "204 No Content.",
                        "202 Accepted."
                ),
                "201 Created with a `Location` header pointing to the new resource URI.",
                "RFC 9110 specifies that 201 Created indicates the request has succeeded and led to the creation of one or more new resources, typically accompanied by a Location header referencing the created entity.",
                Map.of(
                        "A", "Incorrect. 200 OK is general success, whereas 201 Created is the standard RFC status for resource creation.",
                        "B", "Correct. 201 Created with Location header is the standard RESTful specification.",
                        "C", "Incorrect. 204 No Content is typically used for successful DELETE or PUT operations with no response body.",
                        "D", "Incorrect. 202 Accepted means the request has been accepted for asynchronous batch processing, not yet completed."
                ),
                "RESTful API Design", "http-semantics", "Easy", "0-1 years", "Best-practice",
                "Always mention returning both the 201 status code and the `Location: /orders/{id}` response header in REST design interviews."
        ));

        // 13. SQL - WHERE vs HAVING
        list.add(createQuestion(
                "q_sql_01",
                "What is the difference between `WHERE` and `HAVING` clauses in standard SQL?",
                List.of(
                        "WHERE filters aggregate function values, whereas HAVING filters individual table rows.",
                        "WHERE filters individual rows before aggregation occurs, whereas HAVING filters grouped rows after the GROUP BY aggregation.",
                        "HAVING can only be used with subqueries.",
                        "WHERE and HAVING are completely interchangeable in modern SQL engines."
                ),
                "WHERE filters individual rows before aggregation occurs, whereas HAVING filters grouped rows after the GROUP BY aggregation.",
                "The WHERE clause filters rows before any grouping or aggregate functions (SUM, COUNT, AVG) are computed. The HAVING clause applies conditions on the aggregated groups created by GROUP BY.",
                Map.of(
                        "A", "Incorrect. WHERE cannot contain aggregate functions like SUM() or AVG().",
                        "B", "Correct. Execution order: FROM -> WHERE -> GROUP BY -> HAVING -> SELECT -> ORDER BY.",
                        "C", "Incorrect. HAVING is standard group filtering and does not require subqueries.",
                        "D", "Incorrect. They serve fundamentally distinct phases in SQL query execution."
                ),
                "SQL & Query Optimization", "joins-subqueries", "Easy", "0-1 years", "Conceptual MCQ",
                "Explaining the SQL logical query processing order (FROM -> WHERE -> GROUP BY -> HAVING -> SELECT) leaves a memorable impression on interviewers."
        ));

        // 14. DBMS - Isolation Levels
        list.add(createQuestion(
                "q_sql_02",
                "In relational databases, which transaction isolation level prevents `Dirty Reads` and `Non-Repeatable Reads`, but may still permit `Phantom Reads` under standard ANSI SQL?",
                List.of(
                        "Read Uncommitted",
                        "Read Committed",
                        "Repeatable Read",
                        "Serializable"
                ),
                "Repeatable Read",
                "Under ANSI SQL-92: Repeatable Read guarantees that any row read within a transaction cannot be modified by other transactions, preventing dirty and non-repeatable reads. Phantom reads (new rows inserted by another transaction that match a search condition) can theoretically still occur.",
                Map.of(
                        "A", "Incorrect. Read Uncommitted permits dirty reads.",
                        "B", "Incorrect. Read Committed prevents dirty reads but allows non-repeatable reads.",
                        "C", "Correct. Repeatable Read locks the read rows, preventing non-repeatable reads while standard ANSI definition permits phantom inserts.",
                        "D", "Incorrect. Serializable prevents all concurrency anomalies including phantom reads."
                ),
                "DBMS & Database Transactions", "isolation-levels", "Hard", "3-5 years", "Conceptual MCQ",
                "Note that MySQL InnoDB uses Next-Key locking to prevent phantom reads even in Repeatable Read mode, which is a great extra interview nugget."
        ));

        // 15. Kafka - Partitions & Ordering
        list.add(createQuestion(
                "q_kafka_01",
                "How does Apache Kafka guarantee strict ordering of messages?",
                List.of(
                        "Across all partitions globally within a topic.",
                        "Only within a single partition of a topic for messages that share the same partition key.",
                        "By using a global distributed lock across consumer groups.",
                        "Through automatic timestamp reordering at the consumer side."
                ),
                "Only within a single partition of a topic for messages that share the same partition key.",
                "Kafka only guarantees strict message order within a single partition. If total ordering is required across related messages, they must be published with the same message key so that Kafka's default murmur2 partitioner routes them to the same partition.",
                Map.of(
                        "A", "Incorrect. Total ordering across multiple partitions is not supported by Kafka's distributed architecture.",
                        "B", "Correct. Message ordering is strictly guaranteed within an individual partition.",
                        "C", "Incorrect. Kafka achieves high throughput by avoiding global locks.",
                        "D", "Incorrect. Consumers read messages sequentially as stored in log offsets."
                ),
                "Kafka & Messaging in Java", "kafka-architecture", "Medium", "2-3 years", "Conceptual MCQ",
                "Mention that having too few partitions can become a scalability bottleneck even though it simplifies ordering."
        ));

        // 16. Redis - Cache-Aside Pattern
        list.add(createQuestion(
                "q_redis_01",
                "In the `Cache-Aside` (Lazy Loading) pattern, what are the steps taken by a Spring Boot application when updating a database record?",
                List.of(
                        "Update the database record first, and then evict/delete the corresponding key from the Redis cache.",
                        "Update Redis cache only and rely on a background batch job to write to the database.",
                        "Delete the database record and write new data into Redis with a 24-hour TTL.",
                        "Acquire a global database lock and update Redis and DB in a two-phase commit."
                ),
                "Update the database record first, and then evict/delete the corresponding key from the Redis cache.",
                "In Cache-Aside, on write/update, the application updates the authoritative database and evicts (invalidates) the cache entry. The next read operation will experience a cache miss, fetch the fresh data from the database, and repopulate Redis.",
                Map.of(
                        "A", "Correct. DB update followed by cache eviction is the industry standard Cache-Aside pattern.",
                        "B", "Incorrect. Writing to cache and syncing to DB later is the Write-Behind (Write-Back) pattern.",
                        "C", "Incorrect. Deleting the DB record destroys authoritative persistence.",
                        "D", "Incorrect. 2PC is not standard for Redis caching and introduces severe latency."
                ),
                "Redis & Backend Caching", "cache-patterns", "Medium", "1-2 years", "Best-practice",
                "Always recommend cache eviction over cache update on writes to prevent race conditions from concurrent updates."
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
