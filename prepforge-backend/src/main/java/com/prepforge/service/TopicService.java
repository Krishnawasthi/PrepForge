package com.prepforge.service;

import com.prepforge.dto.SubTopicDto;
import com.prepforge.dto.TopicDto;
import com.prepforge.entity.Topic;
import com.prepforge.repository.TopicRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private static final Logger log = LoggerFactory.getLogger(TopicService.class);
    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @PostConstruct
    public void seedTopicsIfEmpty() {
        try {
            log.info("Refreshing Java Backend technical topics catalog in database...");
            topicRepository.deleteAll();
            List<Topic> initialTopics = getDefaultTopicDefinitions();
            topicRepository.saveAll(initialTopics);
            log.info("Successfully seeded {} Java Backend interview topics.", initialTopics.size());
        } catch (Exception e) {
            log.warn("Database topic seeding skipped (using in-memory Java Backend catalog): {}", e.getMessage());
        }
    }

    @Cacheable("topics")
    public List<TopicDto> getAllTopics() {
        try {
            List<Topic> topics = topicRepository.findAll();
            if (!topics.isEmpty()) {
                return topics.stream().map(this::mapToDto).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch topics from repository, falling back to static catalog: {}", e.getMessage());
        }
        return getDefaultTopicDefinitions().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public Optional<TopicDto> getTopicBySlug(String slug) {
        return getAllTopics().stream()
                .filter(t -> t.getSlug().equalsIgnoreCase(slug) || t.getName().equalsIgnoreCase(slug))
                .findFirst();
    }

    public List<TopicDto> getTopicsByCategory(String category) {
        return getAllTopics().stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    private TopicDto mapToDto(Topic topic) {
        List<SubTopicDto> subDtos = topic.getSubTopics() != null
                ? topic.getSubTopics().stream()
                .map(s -> SubTopicDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .estimatedQuestionCount(50)
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        return TopicDto.builder()
                .id(topic.getId())
                .name(topic.getName())
                .slug(topic.getSlug())
                .category(topic.getCategory())
                .description(topic.getDescription())
                .icon(topic.getIcon())
                .badgeColor(topic.getBadgeColor())
                .popular(topic.isPopular())
                .subTopics(subDtos)
                .build();
    }

    public List<Topic> getDefaultTopicDefinitions() {
        List<Topic> topics = new ArrayList<>();

        // 1. Core Java
        topics.add(createTopic("core-java", "Core Java", "Java Core",
                "Fundamental Java principles, memory management, garbage collection, and data types.",
                "coffee", "amber", true,
                List.of(
                        new Topic.SubTopic("jvm-internals", "JVM Internals & Memory", "ClassLoaders, Heap, Stack, Metaspace, GC algorithms"),
                        new Topic.SubTopic("java-syntax", "Syntax & Language Features", "Pass-by-value, Autoboxing, Enums, Generics"),
                        new Topic.SubTopic("cloning-immutability", "Immutability & Object Cloning", "Deep vs Shallow copy, Immutable classes")
                )));

        // 2. OOP & Design Patterns
        topics.add(createTopic("oop", "Object-Oriented Programming (OOP) & Patterns", "Java Core",
                "SOLID principles, inheritance, encapsulation, polymorphism, and GoF design patterns in Java.",
                "layers", "blue", true,
                List.of(
                        new Topic.SubTopic("solid-principles", "SOLID Principles in Java", "Single Responsibility, Open/Closed, Liskov, Interface Segregation, Dependency Inversion"),
                        new Topic.SubTopic("polymorphism-inheritance", "Polymorphism & Dynamic Dispatch", "Method overloading, overriding, covariant return types"),
                        new Topic.SubTopic("design-patterns", "GoF Design Patterns", "Singleton, Factory, Builder, Strategy, Observer, Decorator")
                )));

        // 3. Java Collections Framework
        topics.add(createTopic("collections", "Java Collections Framework", "Java Core",
                "Deep dive into List, Set, Map implementations, internal hashing, and concurrent collections.",
                "boxes", "orange", true,
                List.of(
                        new Topic.SubTopic("hashmap-internals", "HashMap & ConcurrentHashMap", "Buckets, rehashing, treeification, thread-safety"),
                        new Topic.SubTopic("list-set-implementations", "ArrayList, LinkedList, HashSet, TreeSet", "Time complexities, ordering, duplicates"),
                        new Topic.SubTopic("comparable-comparator", "Comparable & Comparator", "Sorting mechanisms and custom contracts")
                )));

        // 4. Java 8+ & Modern Java
        topics.add(createTopic("java-8", "Java 8+ & Modern Java", "Java Core",
                "Lambdas, Functional Interfaces, Optional, Default Methods, Records, and Pattern Matching.",
                "zap", "yellow", true,
                List.of(
                        new Topic.SubTopic("functional-interfaces", "Functional Interfaces & Lambdas", "Predicate, Function, Consumer, Supplier"),
                        new Topic.SubTopic("optional-api", "Optional API", "Avoiding NullPointerExceptions effectively"),
                        new Topic.SubTopic("modern-java", "Java 17/21 Records & Pattern Matching", "Records, Sealed classes, Switch expressions, Virtual Threads")
                )));

        // 5. Streams API
        topics.add(createTopic("streams", "Streams API", "Java Core",
                "Functional data processing pipelines, intermediate vs terminal operations, parallel streams.",
                "workflow", "teal", true,
                List.of(
                        new Topic.SubTopic("stream-operations", "Map, Filter, FlatMap, Reduce", "Stream pipeline mechanics and laziness"),
                        new Topic.SubTopic("collectors", "Collectors & GroupingBy", "Custom collectors, partitioning, counting"),
                        new Topic.SubTopic("parallel-streams", "Parallel Streams & ForkJoin", "Performance trade-offs and thread safety")
                )));

        // 6. Multithreading & Concurrency
        topics.add(createTopic("multithreading", "Multithreading & Concurrency", "Java Core",
                "Threads, Synchronization, Locks, volatile, ExecutorService, and java.util.concurrent.",
                "cpu", "red", true,
                List.of(
                        new Topic.SubTopic("thread-lifecycle", "Thread Lifecycle & State", "Creation, synchronization, wait/notify"),
                        new Topic.SubTopic("locks-volatiles", "Volatile, Atomic & Locks", "Memory visibility, ReentrantLock, CAS"),
                        new Topic.SubTopic("executors-futures", "ExecutorService & CompletableFuture", "Thread pools, asynchronous composition")
                )));

        // 7. Exception Handling & Best Practices
        topics.add(createTopic("exception-handling", "Exception Handling & Best Practices", "Java Core",
                "Checked vs Unchecked exceptions, try-with-resources, Effective Java idioms, and clean code.",
                "alert-triangle", "pink", false,
                List.of(
                        new Topic.SubTopic("exception-hierarchy", "Throwable Hierarchy", "Checked vs Unchecked, Error vs Exception"),
                        new Topic.SubTopic("try-resources", "Try-with-resources & AutoCloseable", "Resource management and suppression"),
                        new Topic.SubTopic("effective-java", "Effective Java Best Practices", "Clean coding standards, defensive copying")
                )));

        // 8. JVM & Performance Tuning
        topics.add(createTopic("jvm", "JVM & Performance Tuning", "Java Core",
                "Garbage Collection algorithms (G1, ZGC), memory profiling, JIT compilation, and JVM flags.",
                "server", "slate", false,
                List.of(
                        new Topic.SubTopic("gc-algorithms", "Garbage Collectors (G1, Parallel, ZGC)", "Generational GC, stop-the-world pauses"),
                        new Topic.SubTopic("memory-leaks", "Memory Leak Detection & Heap Dumps", "Out of Memory errors, profiler analysis"),
                        new Topic.SubTopic("jit-bytecode", "JIT Compiler & Class Loading", "Bytecode execution, tiered compilation")
                )));

        // 9. Spring Boot
        topics.add(createTopic("spring-boot", "Spring Boot", "Spring Ecosystem",
                "Auto-configuration, Custom Starters, Actuator, Profiles, embedded servers, and packaging.",
                "leaf", "emerald", true,
                List.of(
                        new Topic.SubTopic("auto-configuration", "Auto-Configuration Mechanics", "@EnableAutoConfiguration, Conditionals"),
                        new Topic.SubTopic("actuator-metrics", "Actuator, Health & Metrics", "Production monitoring and readiness probes"),
                        new Topic.SubTopic("spring-profiles", "Profiles & Externalized Config", "Environment-specific configurations")
                )));

        // 10. Spring Framework Core
        topics.add(createTopic("spring", "Spring Framework Core", "Spring Ecosystem",
                "Inversion of Control (IoC), Dependency Injection (DI), Bean Lifecycles, and Spring AOP.",
                "shield", "green", true,
                List.of(
                        new Topic.SubTopic("ioc-di", "IoC Container & Dependency Injection", "Bean scopes, wiring, @Autowired vs Constructor"),
                        new Topic.SubTopic("bean-lifecycle", "Bean Lifecycle", "InitializingBean, @PostConstruct, BeanPostProcessor"),
                        new Topic.SubTopic("spring-aop", "Spring AOP & Proxy Patterns", "Aspects, Pointcuts, Advices, CGLIB vs JDK Dynamic Proxies")
                )));

        // 11. Spring Security & JWT
        topics.add(createTopic("spring-security", "Spring Security & JWT", "Spring Ecosystem",
                "SecurityFilterChain, JWT authentication, RBAC, method-level security, and CORS/CSRF.",
                "lock", "amber", true,
                List.of(
                        new Topic.SubTopic("filter-chain", "Security Filter Chain Architecture", "AuthenticationManager, UserDetailsService"),
                        new Topic.SubTopic("jwt-oauth2", "JWT & OAuth2 Resource Server", "Token verification, claims extraction, stateless sessions"),
                        new Topic.SubTopic("rbac-method-security", "RBAC & @PreAuthorize", "Role-based access control, method interception")
                )));

        // 12. Spring Cloud & Microservices
        topics.add(createTopic("spring-cloud", "Spring Cloud & Microservices", "Spring Ecosystem",
                "Service discovery, API Gateway, Circuit Breakers (Resilience4j), and distributed tracing.",
                "network", "violet", true,
                List.of(
                        new Topic.SubTopic("api-gateway-discovery", "Eureka & Spring Cloud Gateway", "Routing, filtering, load balancing"),
                        new Topic.SubTopic("resilience4j-circuit", "Resilience4j Circuit Breaker", "Fallback mechanisms, rate limiting, retry"),
                        new Topic.SubTopic("distributed-tracing", "Micrometer Tracing & Sleuth", "Trace IDs, Span IDs, OpenTelemetry integration")
                )));

        // 13. REST APIs
        topics.add(createTopic("rest-apis", "RESTful API Design", "Web & APIs",
                "HTTP methods, status codes, REST constraints, idempotency, versioning, and OpenAPI.",
                "globe", "cyan", true,
                List.of(
                        new Topic.SubTopic("http-semantics", "HTTP Verbs & Status Codes", "Idempotency, caching, safe methods"),
                        new Topic.SubTopic("api-versioning", "API Versioning & URI Design", "URI vs Header versioning, resource modeling"),
                        new Topic.SubTopic("error-contracts", "Error Handling & Problem Details", "RFC 7807, standard response envelopes")
                )));

        return topics;
    }

    private Topic createTopic(String slug, String name, String category, String description,
                               String icon, String badgeColor, boolean popular, List<Topic.SubTopic> subTopics) {
        return Topic.builder()
                .id(slug)
                .slug(slug)
                .name(name)
                .category(category)
                .description(description)
                .icon(icon)
                .badgeColor(badgeColor)
                .popular(popular)
                .subTopics(subTopics)
                .build();
    }
}
