package com.prepforge.model;

import java.util.*;

/**
 * Complete Java Topic → Subtopic taxonomy.
 * 
 * Frontend shows only the top-level topic names.
 * Subtopics are used INTERNALLY to guide Gemini prompt generation
 * so that questions cover every important concept within each topic,
 * instead of repeatedly asking generic questions.
 * 
 * Structure: Java → Topic → Subtopics/Concepts
 */
public final class JavaTopics {

    private JavaTopics() {}

    /**
     * Internal map: Topic Name → List of subtopics/concepts.
     * Used to build comprehensive Gemini prompts.
     */
    public static final Map<String, List<String>> TOPIC_SUBTOPICS = new LinkedHashMap<>();

    static {
        TOPIC_SUBTOPICS.put("Core Java", List.of(
                "Static block vs Instance initialization block vs Constructor execution order",
                "Object creation and memory layout (Heap vs Stack reference)",
                "Primitive data types, type casting (widening vs narrowing), compound assignment traps (e.g. byte b += 1)",
                "Operator precedence, short-circuit evaluation side-effects, shift operators (<<, >>, >>>)",
                "Method overloading resolution (widening vs autoboxing vs varargs precedence)",
                "Wrapper classes caching (IntegerCache -128 to 127) and reference == comparisons",
                "String constant pool, String immutability, intern() mechanics, StringBuilder vs StringBuffer",
                "static vs instance members, static method hiding, blank final variables",
                "Object class contracts (equals(), hashCode(), clone(), toString())",
                "Access modifiers across packages and subclass boundaries (protected visibility)",
                "Anonymous inner classes, local inner classes, effectively final variable captures",
                "JVM architecture (Stack, Heap, Metaspace, ClassLoader, GC lifecycle)",
                "main() method nuances, packages, imports, naming conventions"
        ));

        TOPIC_SUBTOPICS.put("Data Types & Variables", List.of(
                "Primitive data types", "Reference data types",
                "Type casting", "Widening", "Narrowing",
                "Local variables", "Instance variables", "Static variables",
                "final variables", "Default values", "Scope and lifetime"
        ));

        TOPIC_SUBTOPICS.put("Operators", List.of(
                "Arithmetic operators", "Relational operators", "Logical operators",
                "Bitwise operators", "Assignment operators", "Unary operators",
                "Ternary operator", "Shift operators",
                "== vs .equals()", "Short-circuit operators"
        ));

        TOPIC_SUBTOPICS.put("Classes & Objects", List.of(
                "Class declaration", "Object creation", "Fields", "Methods",
                "Instance members", "Static members", "Static block",
                "Instance initialization block", "Method declaration",
                "Method invocation", "this keyword", "super keyword"
        ));

        TOPIC_SUBTOPICS.put("Constructors", List.of(
                "Default constructor", "No-argument constructor",
                "Parameterized constructor", "Constructor overloading",
                "Constructor chaining", "this()", "super()",
                "Constructor inheritance rules", "Constructor execution order"
        ));

        TOPIC_SUBTOPICS.put("OOP", List.of(
                // Encapsulation
                "Encapsulation", "Getters/setters", "Data hiding", "Access control",
                // Inheritance
                "Single inheritance", "Multilevel inheritance", "Hierarchical inheritance",
                "Multiple inheritance through interfaces", "super keyword", "Method inheritance",
                // Polymorphism
                "Compile-time polymorphism", "Method overloading",
                "Runtime polymorphism", "Method overriding", "Dynamic method dispatch",
                // Upcasting & Downcasting
                "Implicit upcasting", "Explicit downcasting",
                "Parent → child reference", "instanceof", "ClassCastException",
                // Abstraction
                "Abstract classes", "Abstract methods", "Interfaces",
                "Default methods", "Static interface methods", "Functional interfaces"
        ));

        TOPIC_SUBTOPICS.put("Access Modifiers", List.of(
                "private", "default (package-private)", "protected", "public",
                "Same class access", "Same package access",
                "Subclass access", "Different package access"
        ));

        TOPIC_SUBTOPICS.put("String Handling", List.of(
                "String class", "String pool", "String immutability",
                "String literals", "new String()", "== vs .equals() for Strings",
                ".equalsIgnoreCase()", "intern()", "String concatenation",
                "StringBuilder", "StringBuffer",
                "String vs StringBuilder vs StringBuffer", "Common String methods"
        ));

        TOPIC_SUBTOPICS.put("Wrapper Classes", List.of(
                "Autoboxing", "Unboxing", "Primitive → wrapper conversion",
                "Wrapper → primitive conversion",
                "Integer, Double, Character, Boolean etc.",
                "parseInt()", "valueOf()", "Wrapper caching (-128 to 127)"
        ));

        TOPIC_SUBTOPICS.put("Arrays", List.of(
                "One-dimensional arrays", "Multidimensional arrays",
                "Array initialization", "Array traversal", "Array of objects",
                "Jagged arrays", "Arrays utility class",
                "Arrays.sort()", "Arrays.copyOf()", "Arrays.equals()"
        ));

        TOPIC_SUBTOPICS.put("Exception Handling", List.of(
                "Exception hierarchy", "Checked exceptions", "Unchecked exceptions",
                "try block", "catch block", "finally block",
                "throw keyword", "throws keyword", "Multiple catch blocks",
                "Nested try", "Custom exceptions", "Exception propagation",
                "Try-with-resources", "Suppressed exceptions"
        ));

        TOPIC_SUBTOPICS.put("Collections", List.of(
                "Collection hierarchy", "List interface", "Set interface",
                "Queue interface", "Map interface",
                "ArrayList", "LinkedList", "Vector", "Stack",
                "HashSet", "LinkedHashSet", "TreeSet",
                "PriorityQueue", "HashMap", "LinkedHashMap", "TreeMap", "Hashtable",
                "Iterator", "ListIterator",
                "Comparable", "Comparator",
                "Fail-fast vs fail-safe", "Synchronized collections"
        ));

        TOPIC_SUBTOPICS.put("Generics", List.of(
                "Generic classes", "Generic methods", "Generic interfaces",
                "Type parameters", "Wildcards",
                "Unbounded wildcard <?>",
                "<? extends T> (upper bounded)",
                "<? super T> (lower bounded / PECS)",
                "Type safety", "Type erasure"
        ));

        TOPIC_SUBTOPICS.put("Java 8 / Functional Programming", List.of(
                "Lambda expressions", "Functional interfaces",
                "Predicate", "Consumer", "Supplier", "Function",
                "Method references", "Constructor references",
                "Stream API", "filter()", "map()", "flatMap()",
                "sorted()", "distinct()", "limit()", "skip()",
                "forEach()", "collect()", "reduce()", "count()",
                "Intermediate operations", "Terminal operations", "Lazy evaluation"
        ));

        TOPIC_SUBTOPICS.put("Multithreading", List.of(
                "Thread concept", "Process vs thread",
                "Thread creation (Thread class, Runnable)",
                "Thread lifecycle", "sleep()", "join()", "yield()",
                "Thread priority", "Synchronization", "synchronized keyword",
                "Race condition", "Deadlock",
                "Inter-thread communication", "wait()", "notify()", "notifyAll()",
                "Executor framework"
        ));

        TOPIC_SUBTOPICS.put("static & final", List.of(
                "Static variable", "Static method", "Static block",
                "Static nested class", "Static vs instance",
                "Final variable", "Final method", "Final class",
                "Blank final variable"
        ));

        TOPIC_SUBTOPICS.put("Object Class", List.of(
                "toString()", "equals()", "hashCode()",
                "getClass()", "clone()",
                "Object reference behavior",
                "== vs equals()", "equals()/hashCode() contract"
        ));

        TOPIC_SUBTOPICS.put("Interfaces & Abstract Classes", List.of(
                "Interface declaration", "Implementation",
                "Multiple interfaces", "Default methods",
                "Static methods", "Private interface methods (Java 9+)",
                "Abstract class", "Abstract method",
                "Interface vs abstract class", "Functional interface"
        ));

        TOPIC_SUBTOPICS.put("Inner Classes", List.of(
                "Member inner class", "Static nested class",
                "Local inner class", "Anonymous inner class",
                "Accessing outer class members"
        ));

        TOPIC_SUBTOPICS.put("Memory & JVM Concepts", List.of(
                "Stack memory", "Heap memory", "Method area",
                "Objects in memory", "References",
                "Garbage collection", "Garbage collector types",
                "String pool internals", "Stack vs heap",
                "Memory leaks", "Object lifecycle"
        ));
    }

    /**
     * The 20 top-level topic names shown on the frontend.
     */
    public static final List<String> ALL_TOPICS = List.copyOf(TOPIC_SUBTOPICS.keySet());

    /**
     * Default pre-selected topics on the home page.
     */
    public static final List<String> DEFAULT_TOPICS = List.of(
            "Core Java",
            "OOP",
            "Collections",
            "Exception Handling",
            "Java 8 / Functional Programming",
            "Multithreading"
    );

    /**
     * Returns the subtopics for a given topic name.
     * Uses case-insensitive fuzzy matching so that frontend topic names
     * and Gemini-returned topic names both resolve correctly.
     */
    public static List<String> getSubtopics(String topicName) {
        if (topicName == null) return Collections.emptyList();

        // Exact match first
        List<String> exact = TOPIC_SUBTOPICS.get(topicName);
        if (exact != null) return exact;

        // Fuzzy match
        String lower = topicName.toLowerCase();
        for (Map.Entry<String, List<String>> entry : TOPIC_SUBTOPICS.entrySet()) {
            if (entry.getKey().toLowerCase().contains(lower) ||
                lower.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns a formatted subtopic string for use in Gemini prompts.
     * Example: "OOP: Encapsulation, Getters/setters, Data hiding, Single inheritance, ..."
     */
    public static String getSubtopicPromptBlock(List<String> selectedTopics) {
        StringBuilder sb = new StringBuilder();
        for (String topic : selectedTopics) {
            List<String> subs = getSubtopics(topic);
            if (!subs.isEmpty()) {
                sb.append("  • ").append(topic).append(": ")
                  .append(String.join(", ", subs)).append("\n");
            } else {
                sb.append("  • ").append(topic).append("\n");
            }
        }
        return sb.toString();
    }
}
