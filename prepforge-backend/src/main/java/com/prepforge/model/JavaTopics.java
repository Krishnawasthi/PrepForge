package com.prepforge.model;

import java.util.List;

public final class JavaTopics {

    private JavaTopics() {}

    public static final List<String> ALL_TOPICS = List.of(
            "Core Java",
            "Object-Oriented Programming (OOP)",
            "Java Collections Framework",
            "Exception Handling",
            "Java 8 & Modern Java",
            "Streams API",
            "Multithreading & Concurrency",
            "JVM Internals & Memory",
            "Strings & Immutability",
            "Generics",
            "Interfaces & Abstract Classes",
            "Inheritance & Polymorphism",
            "Constructors & Object Lifecycle",
            "Encapsulation & Access Modifiers"
    );

    public static final List<String> DEFAULT_TOPICS = List.of(
            "Core Java",
            "Object-Oriented Programming (OOP)",
            "Java Collections Framework",
            "Exception Handling",
            "Streams API",
            "Multithreading & Concurrency"
    );
}
