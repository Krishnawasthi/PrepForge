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
    private List<Question> curatedBankCache = new ArrayList<>();

    public QuestionBankService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @PostConstruct
    public void seedInitialQuestions() {
        try {
            log.info("Refreshing pure Java interview question bank in database...");
            curatedBankCache = getCuratedQuestionBank();
            questionRepository.deleteAll();
            questionRepository.saveAll(curatedBankCache);
            log.info("Successfully seeded {} curated pure Java questions.", curatedBankCache.size());
        } catch (Exception e) {
            log.warn("Question bank database seeding skipped (using in-memory bank): {}", e.getMessage());
            curatedBankCache = getCuratedQuestionBank();
        }
    }

    /**
     * Generates strictly topic-constrained questions for the user's selected topics.
     * Guaranteed ~45% code review / output prediction questions.
     */
    public List<Question> generateDynamicJavaQuestions(List<String> topics, String experienceLevel, String difficulty, int targetCount) {
        List<Question> resultList = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Random random = ThreadLocalRandom.current();

        List<String> validTopics = (topics != null && !topics.isEmpty())
                ? topics
                : List.of("Core Java", "Java Collections Framework", "Exception Handling", "Streams API");

        // First, draw curated questions matching the selected topics
        for (Question q : curatedBankCache) {
            if (isTopicMatch(q.getTopic(), validTopics)) {
                if (seen.add(normalize(q.getQuestion()))) {
                    resultList.add(q);
                    if (resultList.size() >= targetCount) return resultList;
                }
            }
        }

        // Generate dynamic questions strictly for the selected topics
        int attempts = 0;
        int maxAttempts = targetCount * 20;

        while (resultList.size() < targetCount && attempts < maxAttempts) {
            attempts++;
            String topic = validTopics.get(attempts % validTopics.size());
            int variant = attempts;

            Question q = createTopicSpecificQuestion(topic, experienceLevel, difficulty, variant, random);
            if (q != null && isTopicMatch(q.getTopic(), validTopics)) {
                if (seen.add(normalize(q.getQuestion()))) {
                    resultList.add(q);
                }
            }
        }

        return resultList;
    }

    public Question createAlgorithmicOutputQuestion(String topic, String diff, String exp) {
        return createTopicSpecificQuestion(topic, exp, diff, new Random().nextInt(100), new Random());
    }

    public Question createDiverseParametricQuestion(String topic, String diff, String exp) {
        return createTopicSpecificQuestion(topic, exp, diff, new Random().nextInt(100), new Random());
    }

    private boolean isTopicMatch(String questionTopic, List<String> allowedTopics) {
        if (questionTopic == null) return false;
        for (String allowed : allowedTopics) {
            if (allowed.equalsIgnoreCase(questionTopic) ||
                allowed.toLowerCase().contains(questionTopic.toLowerCase()) ||
                questionTopic.toLowerCase().contains(allowed.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    /**
     * Creates topic-specific questions strictly belonging to the requested topic.
     * ~45% of generated variants are code review / output prediction questions.
     */
    private Question createTopicSpecificQuestion(String topic, String exp, String diff, int variant, Random random) {
        String uid = "dyn_" + topic.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
        String t = topic.toLowerCase();

        // 1. EXCEPTION HANDLING
        if (t.contains("exception")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                return createQuestion(uid,
                        "What is the output of the following Java code?\n\n```java\nclass Test {\n    public static int test() {\n        try {\n            int x = 10 / 0;\n            return 1;\n        } catch (ArithmeticException e) {\n            return 2;\n        } finally {\n            return 3;\n        }\n    }\n    public static void main(String[] args) {\n        System.out.println(test());\n    }\n}\n```",
                        List.of("3", "2", "1", "ArithmeticException is thrown"),
                        "3",
                        "The finally block always executes and its return statement overrides any return statement executed in the try or catch block.",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "Which statement is true regarding Java's try-with-resources statement introduced in Java 7?",
                        List.of("Resources are closed in the reverse order of their declaration",
                                "Resources are closed in the exact order of their declaration",
                                "Resources must implement the java.lang.Runnable interface",
                                "The catch block always executes before the AutoCloseable resources are closed"),
                        "Resources are closed in the reverse order of their declaration",
                        "In try-with-resources, resources declared in the try(...) header are closed automatically in reverse order of declaration before any catch/finally blocks execute.",
                        topic, diff);
            }
        }

        // 2. COLLECTIONS FRAMEWORK
        if (t.contains("collection")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                int val1 = random.nextInt(10, 20);
                int val2 = val1 + 5;
                return createQuestion(uid,
                        "What is the output of the following Java code?\n\n```java\nimport java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        Map<String, Integer> map = new HashMap<>();\n        map.put(\"A\", " + val1 + ");\n        map.computeIfPresent(\"A\", (k, v) -> v + 10);\n        map.computeIfAbsent(\"B\", k -> " + val2 + ");\n        map.computeIfPresent(\"C\", (k, v) -> 100);\n        System.out.println(map.get(\"A\") + \" \" + map.get(\"B\") + \" \" + map.get(\"C\"));\n    }\n}\n```",
                        List.of((val1 + 10) + " " + val2 + " null",
                                (val1 + 10) + " " + val2 + " 100",
                                val1 + " " + val2 + " null",
                                "NullPointerException"),
                        (val1 + 10) + " " + val2 + " null",
                        "computeIfPresent modifies 'A' to " + (val1 + 10) + ". computeIfAbsent computes 'B' to " + val2 + ". computeIfPresent does nothing for 'C' because key 'C' is absent, so map.get(\"C\") returns null.",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "How does Java's ConcurrentHashMap achieve thread safety in Java 8+ compared to Hashtable?",
                        List.of("Using bucket-level synchronized blocks and CAS operations rather than a global table lock",
                                "By cloning the entire bucket array on every mutation",
                                "By using ReentrantReadWriteLock globally across all buckets",
                                "By delegating all write operations to a single worker background thread"),
                        "Using bucket-level synchronized blocks and CAS operations rather than a global table lock",
                        "In Java 8+, ConcurrentHashMap uses lock-free Compare-And-Swap (CAS) for node insertion and synchronizes only on the head node of individual hash bins during hash collisions.",
                        topic, diff);
            }
        }

        // 3. STREAMS API
        if (t.contains("stream")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                int start = random.nextInt(1, 4);
                return createQuestion(uid,
                        "What is the output of the following Java Stream code?\n\n```java\nimport java.util.stream.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        int result = IntStream.of(" + start + ", " + (start + 1) + ", " + (start + 2) + ", " + (start + 3) + ")\n            .filter(n -> n % 2 != 0)\n            .map(n -> n * 2)\n            .reduce(0, Integer::sum);\n        System.out.println(result);\n    }\n}\n```",
                        List.of(String.valueOf(computeStreamResult(start)),
                                String.valueOf(computeStreamResult(start) + 2),
                                String.valueOf(computeStreamResult(start) * 2),
                                "0"),
                        String.valueOf(computeStreamResult(start)),
                        "The stream filters odd numbers from the input sequence, multiplies each by 2, and sums them via reduce.",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "Which of the following is a terminal operation in Java Streams that triggers stream pipeline evaluation?",
                        List.of("collect()", "peek()", "map()", "filter()"),
                        "collect()",
                        "Intermediate operations like map(), filter(), and peek() are lazy. Terminal operations like collect(), forEach(), reduce(), and findFirst() trigger pipeline execution.",
                        topic, diff);
            }
        }

        // 4. STRINGS & IMMUTABILITY
        if (t.contains("string")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                return createQuestion(uid,
                        "What is the output of the following Java code?\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        String s1 = \"Java\";\n        String s2 = new String(\"Java\");\n        String s3 = s2.intern();\n        System.out.println((s1 == s2) + \" \" + (s1 == s3) + \" \" + s1.equals(s2));\n    }\n}\n```",
                        List.of("false true true", "true true true", "false false true", "true false true"),
                        "false true true",
                        "s1 is a literal in the String pool. s2 is a separate heap object, so (s1 == s2) is false. s2.intern() returns the pool reference matching s1, so (s1 == s3) is true. equals() compares content, returning true.",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "Why are String objects immutable in Java?",
                        List.of("Security, thread safety, String pool caching, and stable hashCode caching",
                                "Because Java does not allow dynamic memory allocation on the heap",
                                "Because primitive types cannot be wrapped in mutable objects",
                                "To allow multiple inheritance of character arrays"),
                        "Security, thread safety, String pool caching, and stable hashCode caching",
                        "Immutability allows String pooling (saving heap memory), safe parameter passing (e.g. database URLs, network sockets), thread safety without locks, and caching of hashCode.",
                        topic, diff);
            }
        }

        // 5. MULTITHREADING & CONCURRENCY
        if (t.contains("thread") || t.contains("concurrency")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                return createQuestion(uid,
                        "What will happen when the following code is executed?\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        Thread t = new Thread(() -> System.out.print(\"Running \"));\n        t.run();\n        t.run();\n    }\n}\n```",
                        List.of("Prints 'Running Running ' on the main thread without throwing an exception",
                                "Throws IllegalThreadStateException on the second call",
                                "Spawns two new OS threads concurrently",
                                "Compilation Error because run() cannot be called directly"),
                        "Prints 'Running Running ' on the main thread without throwing an exception",
                        "Calling run() directly simply executes the method synchronously on the current (main) thread. Only calling start() initiates a new thread and throws IllegalThreadStateException if called repeatedly.",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "What guarantee does the `volatile` keyword provide in Java?",
                        List.of("Memory visibility across threads and prevention of instruction reordering",
                                "Mutual exclusion lock synchronization like synchronized blocks",
                                "Atomicity for compound operations such as count++",
                                "Automatic garbage collection prioritization for the variable"),
                        "Memory visibility across threads and prevention of instruction reordering",
                        "volatile establishes a happens-before relationship ensuring thread writes are immediately flushed to main memory. It does NOT provide atomicity for compound operations like count++.",
                        topic, diff);
            }
        }

        // 6. OOP / INHERITANCE / POLYMORPHISM
        if (t.contains("oop") || t.contains("inheritance") || t.contains("polymorphism")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                return createQuestion(uid,
                        "What is the output of the following Java code?\n\n```java\nclass Parent {\n    static void print() {\n        System.out.print(\"Parent \");\n    }\n}\nclass Child extends Parent {\n    static void print() {\n        System.out.print(\"Child \");\n    }\n}\npublic class Main {\n    public static void main(String[] args) {\n        Parent p = new Child();\n        p.print();\n    }\n}\n```",
                        List.of("Parent ", "Child ", "Compilation Error", "ClassCastException"),
                        "Parent ",
                        "Static methods are not overridden in Java; they are hidden (method hiding). Static method resolution occurs at compile time based on the reference type (Parent), not the runtime instance (Child).",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "In Java, can an overriding method in a subclass declare a broader access modifier or throw broader checked exceptions?",
                        List.of("Broader access is allowed, but broader checked exceptions are NOT allowed",
                                "Broader checked exceptions are allowed, but broader access is NOT allowed",
                                "Both broader access and broader checked exceptions are allowed",
                                "Neither broader access nor broader checked exceptions are allowed"),
                        "Broader access is allowed, but broader checked exceptions are NOT allowed",
                        "Subclass overriding methods can broaden access (e.g. protected -> public) but cannot throw broader checked exceptions than those declared by the superclass method.",
                        topic, diff);
            }
        }

        // 7. JVM INTERNALS & MEMORY
        if (t.contains("jvm") || t.contains("memory")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                return createQuestion(uid,
                        "What is the output of the following code regarding Java Integer cache?\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        Integer a = 100;\n        Integer b = 100;\n        Integer c = 200;\n        Integer d = 200;\n        System.out.println((a == b) + \" \" + (c == d));\n    }\n}\n```",
                        List.of("true false", "true true", "false false", "false true"),
                        "true false",
                        "Java caches Integer objects between -128 and 127 via the IntegerCache. 100 is cached so (a == b) evaluates to true. 200 is outside the default cache range, creating distinct heap objects so (c == d) evaluates to false.",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "Where are method local variables of primitive types allocated in the Java Virtual Machine?",
                        List.of("On the thread's Stack frame",
                                "In the Metaspace",
                                "In the Eden generation of the Heap",
                                "In the code cache"),
                        "On the thread's Stack frame",
                        "Local variables defined inside methods are allocated directly on the executing thread's JVM Stack frame and destroyed as soon as the method exits.",
                        topic, diff);
            }
        }

        // 8. INTERFACES & ABSTRACT CLASSES
        if (t.contains("interface") || t.contains("abstract")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                return createQuestion(uid,
                        "What is the output of the following Java interface default method code?\n\n```java\ninterface A {\n    default void show() { System.out.print(\"A \"); }\n}\ninterface B {\n    default void show() { System.out.print(\"B \"); }\n}\nclass C implements A, B {\n    public void show() {\n        A.super.show();\n        System.out.print(\"C \");\n    }\n}\npublic class Main {\n    public static void main(String[] args) {\n        new C().show();\n    }\n}\n```",
                        List.of("A C ", "B C ", "Compilation Error: Duplicate default method", "Runtime Ambiguity Exception"),
                        "A C ",
                        "When multiple interfaces provide conflicting default methods, the implementing class must explicitly override the method. C resolves conflict using A.super.show(), producing 'A C '.",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "Which is a fundamental difference between an Abstract Class and an Interface in Java 8+?",
                        List.of("An abstract class can maintain mutable instance state (instance fields); interfaces only allow public static final constants",
                                "Interfaces cannot declare any method implementations",
                                "A class can extend multiple abstract classes",
                                "Abstract classes cannot have constructors"),
                        "An abstract class can maintain mutable instance state (instance fields); interfaces only allow public static final constants",
                        "Even with default and static methods in Java 8+, interfaces cannot declare instance variables (state). Abstract classes can have instance state and constructors.",
                        topic, diff);
            }
        }

        // 9. CONSTRUCTORS & OBJECT LIFECYCLE
        if (t.contains("constructor") || t.contains("lifecycle")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                return createQuestion(uid,
                        "What is the output of the following Java code?\n\n```java\nclass Base {\n    Base() {\n        System.out.print(\"Base \");\n    }\n}\nclass Derived extends Base {\n    Derived() {\n        this(5);\n        System.out.print(\"DerivedDefault \");\n    }\n    Derived(int x) {\n        System.out.print(\"DerivedArg \" + x + \" \");\n    }\n}\npublic class Main {\n    public static void main(String[] args) {\n        new Derived();\n    }\n}\n```",
                        List.of("Base DerivedArg 5 DerivedDefault ",
                                "DerivedDefault DerivedArg 5 Base ",
                                "Base DerivedDefault DerivedArg 5 ",
                                "Compilation Error"),
                        "Base DerivedArg 5 DerivedDefault ",
                        "new Derived() invokes this(5). Derived(5) first invokes super() (printing 'Base '), then prints 'DerivedArg 5 '. Finally, the default constructor completes, printing 'DerivedDefault '.",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "What happens if a Java class defines at least one parameterized constructor but does not declare a default no-arg constructor?",
                        List.of("The Java compiler will NOT generate a default no-arg constructor",
                                "The Java compiler automatically generates a no-arg constructor anyway",
                                "Instantiation via reflection fails with a CompilerError",
                                "The class automatically becomes abstract"),
                        "The Java compiler will NOT generate a default no-arg constructor",
                        "The compiler only provides a default no-argument constructor if NO constructors are explicitly defined in the class.",
                        topic, diff);
            }
        }

        // 10. GENERICS
        if (t.contains("generic")) {
            if (variant % 2 == 0) {
                // Code output question (45%)
                return createQuestion(uid,
                        "What is the output or behavior of the following Java Generics code?\n\n```java\nimport java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        List<String> list1 = new ArrayList<>();\n        List<Integer> list2 = new ArrayList<>();\n        System.out.println(list1.getClass() == list2.getClass());\n    }\n}\n```",
                        List.of("true (because of Java type erasure, both are java.util.ArrayList at runtime)",
                                "false (List<String> and List<Integer> are distinct types)",
                                "Compilation Error: Cannot compare distinct generic types",
                                "Throws ClassCastException at runtime"),
                        "true (because of Java type erasure, both are java.util.ArrayList at runtime)",
                        "Java uses Type Erasure. Generic type parameters are erased at compile time. At runtime, both list1 and list2 are raw ArrayList instances.",
                        topic, diff);
            } else {
                return createQuestion(uid,
                        "What is the difference between `List<? extends Number>` and `List<? super Integer>` (PECS principle)?",
                        List.of("`? extends Number` is a producer (read-only for numbers); `? super Integer` is a consumer (can write Integer values)",
                                "`? extends Number` allows adding new Number instances; `? super Integer` does not",
                                "They are completely interchangeable in method signatures",
                                "`? super` is prohibited in method parameter definitions"),
                        "`? extends Number` is a producer (read-only for numbers); `? super Integer` is a consumer (can write Integer values)",
                        "PECS = Producer Extends, Consumer Super. You can read Numbers from List<? extends Number>, but cannot add elements. You can safely add Integers to List<? super Integer>.",
                        topic, diff);
            }
        }

        // DEFAULT / CORE JAVA
        if (variant % 2 == 0) {
            int a = random.nextInt(3, 8);
            int b = a * 2;
            return createQuestion(uid,
                    "What is the output of evaluating the following Java expression?\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        int a = " + a + ";\n        int b = a++ + ++a * 2;\n        System.out.println(b);\n    }\n}\n```",
                    List.of(String.valueOf(a + (a + 2) * 2),
                            String.valueOf((a + 1) * 3),
                            String.valueOf(a * 3),
                            String.valueOf(b)),
                    String.valueOf(a + (a + 2) * 2),
                    "a++ evaluates to " + a + " and increments a to " + (a + 1) + ". Then ++a increments a to " + (a + 2) + " and evaluates to " + (a + 2) + ". Then " + (a + 2) + " * 2 = " + ((a + 2) * 2) + ". Sum = " + (a + (a + 2) * 2) + ".",
                    topic, diff);
        } else {
            return createQuestion(uid,
                    "In Java, what is the contract between `equals()` and `hashCode()`?",
                    List.of("If two objects are equal according to equals(), they MUST have the same hashCode()",
                            "If two objects have the same hashCode(), they MUST be equal according to equals()",
                            "hashCode() must always return a unique positive integer for every object",
                            "Overriding equals() does not require overriding hashCode()"),
                    "If two objects are equal according to equals(), they MUST have the same hashCode()",
                    "If o1.equals(o2) is true, then o1.hashCode() == o2.hashCode() is strictly mandatory so hash-based collections (HashMap, HashSet) function correctly.",
                    topic, diff);
        }
    }

    private int computeStreamResult(int start) {
        int sum = 0;
        int[] vals = new int[]{start, start + 1, start + 2, start + 3};
        for (int v : vals) {
            if (v % 2 != 0) sum += v * 2;
        }
        return sum;
    }

    private Question createQuestion(
            String id,
            String question,
            List<String> options,
            String correctAnswer,
            String explanation,
            String topic,
            String difficulty
    ) {
        return Question.builder()
                .id(id)
                .question(question)
                .options(options)
                .correctAnswer(correctAnswer)
                .explanation(explanation)
                .topic(topic)
                .difficulty(difficulty)
                .build();
    }

    public List<Question> getCuratedQuestionBank() {
        List<Question> list = new ArrayList<>();

        // 1. Exception Handling
        list.add(createQuestion(
                "q_cur_exc_01",
                "What is the output of the following Java code?\n\n```java\nclass Resource implements AutoCloseable {\n    public void close() throws Exception {\n        throw new Exception(\"Close Exception\");\n    }\n}\npublic class Main {\n    public static void main(String[] args) {\n        try (Resource r = new Resource()) {\n            throw new Exception(\"Try Exception\");\n        } catch (Exception e) {\n            System.out.println(e.getMessage() + \" : \" + e.getSuppressed()[0].getMessage());\n        }\n    }\n}\n```",
                List.of("Try Exception : Close Exception", "Close Exception : Try Exception", "Close Exception", "Compilation Error"),
                "Try Exception : Close Exception",
                "In try-with-resources, the primary exception thrown in the try block takes precedence. The exception thrown by close() is added as a suppressed exception accessible via e.getSuppressed().",
                "Exception Handling", "Medium"
        ));

        // 2. Collections
        list.add(createQuestion(
                "q_cur_col_01",
                "What is the output of the following Java Collections code?\n\n```java\nimport java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        List<String> list = new ArrayList<>(Arrays.asList(\"A\", \"B\", \"C\", \"D\"));\n        for (Iterator<String> it = list.iterator(); it.hasNext(); ) {\n            String s = it.next();\n            if (s.equals(\"B\")) {\n                it.remove();\n            }\n        }\n        System.out.println(String.join(\",\", list));\n    }\n}\n```",
                List.of("A,C,D", "ConcurrentModificationException is thrown", "A,B,C,D", "A,C"),
                "A,C,D",
                "Removing elements via Iterator.remove() safely mutates the underlying list and updates the iterator's expectedModCount, avoiding ConcurrentModificationException.",
                "Java Collections Framework", "Medium"
        ));

        // 3. Streams
        list.add(createQuestion(
                "q_cur_stm_01",
                "What is the output of the following Java code?\n\n```java\nimport java.util.*;\nimport java.util.stream.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        List<String> items = Arrays.asList(\"apple\", \"banana\", \"pear\");\n        long count = items.stream()\n            .peek(s -> System.out.print(s.length() + \" \"))\n            .count();\n        System.out.print(\"Count:\" + count);\n    }\n}\n```",
                List.of("Count:3 (in Java 9+, count() may skip peek() stream stages)",
                        "5 6 4 Count:3",
                        "Compilation Error",
                        "Count:0"),
                "Count:3 (in Java 9+, count() may skip peek() stream stages)",
                "In Java 9+, Stream.count() is optimized to return the known collection size directly without traversing the pipeline, meaning intermediate operations like peek() are omitted.",
                "Streams API", "Hard"
        ));

        // 4. Multithreading
        list.add(createQuestion(
                "q_cur_thr_01",
                "What is the output of the following Java Concurrency code?\n\n```java\nimport java.util.concurrent.atomic.AtomicInteger;\n\npublic class Main {\n    public static void main(String[] args) {\n        AtomicInteger val = new AtomicInteger(10);\n        boolean updated = val.compareAndSet(10, 20);\n        boolean updatedAgain = val.compareAndSet(10, 30);\n        System.out.println(updated + \" \" + updatedAgain + \" \" + val.get());\n    }\n}\n```",
                List.of("true false 20", "true true 30", "false false 10", "true false 10"),
                "true false 20",
                "The first compareAndSet(10, 20) succeeds because the current value is 10, updating it to 20. The second compareAndSet(10, 30) fails because the current value is now 20.",
                "Multithreading & Concurrency", "Medium"
        ));

        // 5. Strings & Immutability
        list.add(createQuestion(
                "q_cur_str_01",
                "What is the output of the following Java String code?\n\n```java\npublic class Main {\n    public static void main(String[] args) {\n        String a = \"Hello\";\n        String b = \"Hello\";\n        String c = new String(\"Hello\");\n        System.out.println((a == b) + \" \" + (a == c));\n    }\n}\n```",
                List.of("true false", "true true", "false false", "false true"),
                "true false",
                "String literals 'Hello' are canonicalized in the String Constant Pool, so (a == b) is true. new String() creates a distinct object on the heap, so (a == c) is false.",
                "Strings & Immutability", "Easy"
        ));

        // 6. OOP & Dynamic Dispatch
        list.add(createQuestion(
                "q_cur_oop_01",
                "What is the output of the following Java inheritance code?\n\n```java\nclass Alpha {\n    int val = 10;\n    void show() { System.out.print(\"A:\" + val + \" \"); }\n}\nclass Beta extends Alpha {\n    int val = 20;\n    void show() { System.out.print(\"B:\" + val + \" \"); }\n}\npublic class Main {\n    public static void main(String[] args) {\n        Alpha obj = new Beta();\n        System.out.print(obj.val + \" \");\n        obj.show();\n    }\n}\n```",
                List.of("10 B:20 ", "20 B:20 ", "10 A:10 ", "20 A:10 "),
                "10 B:20 ",
                "In Java, instance variables are NOT polymorphic and are resolved at compile time using the reference type (Alpha.val = 10). Methods are dynamically dispatched at runtime (Beta.show() = B:20).",
                "Inheritance & Polymorphism", "Hard"
        ));

        return list;
    }
}
