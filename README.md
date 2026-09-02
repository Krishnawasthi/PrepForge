# PrepForge — AI-Powered Java Backend Interview Preparation Platform

> **Practice smarter. Prepare better.**

PrepForge is a production-quality, public web platform where developers can create and take personalized technical interview tests tailored to their exact experience level (0–5+ YOE), Java core concepts, Spring Boot, Concurrency, and JPA/Hibernate depth.

---

## 🚀 Key Features

- **Zero-Login & Anonymous Session Flow**: Immediate public access without login walls, passwords, or emails.
- **AI-Powered Natural Language Builder**: Describe test goals in plain English (100–150 words) with real-time interpretation.
- **19 Curated Java Backend Assessment Tracks**:
  - Core Java & Language Internals (JVM, GC, Memory, Immutability)
  - Java Collections Framework (`HashMap`, `ConcurrentHashMap`, treeification)
  - Java 8+ & Modern Java (Lambdas, Records, Pattern Matching)
  - Streams API (Pipelines, Collectors, Parallel Streams)
  - Multithreading & Concurrency (`volatile`, Atomic, Locks, `CompletableFuture`)
  - Exception Handling & Best Practices (Clean code, Effective Java idioms)
  - JVM & Performance Tuning (G1, ZGC, heap dumps, JIT)
  - Spring Boot (Auto-Configuration, Custom Starters, Actuator)
  - Spring Framework Core (IoC, DI, Bean lifecycle, Spring AOP)
  - Spring Security & JWT (SecurityFilterChain, RBAC)
  - Spring Cloud & Microservices (Eureka, Gateway, Resilience4j)
  - RESTful API Design (RFC HTTP semantics, Idempotency)
  - JPA & Hibernate ORM (Entity mappings, N+1 query problem, Caching)
  - SQL & Query Optimization (B-Tree indexing, execution plans)
  - DBMS & Database Transactions (ACID, isolation levels, HikariCP)
  - Kafka & Messaging in Java (Topics, partitions, consumer groups)
  - Redis & Backend Caching (Cache-aside, TTL jitter, distributed locks)
- **Interactive Examination Interface**:
  - 4-option randomized answer cards (A, B, C, D)
  - Syntax-highlighted code blocks
  - Real-time countdown timer & urgency warnings
  - Pro keyboard navigation (`A`–`D`, `←`/`→`, `M` for review)
  - Question matrix navigator
- **Authoritative Backend Scoring & Explanations**:
  - Authoritative calculation with topic accuracy & difficulty breakdowns
  - Optimistic, encouraging feedback
  - Detailed 4-option analysis: why correct is right, why each option is incorrect, and actionable interview tips
- **Anonymous Practice Dashboard**: Device-based tracking of completed tests, average score, best score, and one-click "Practice Weak Areas".
- **Optional Gemini LLM Integration**: Connect your Google Gemini API Key for on-demand live question generation, with high-yield dynamic question engine fallback.

---

## 🛠 Tech Stack

- **Frontend**: Next.js 14+ (App Router), React, TypeScript, Tailwind CSS, Lucide React.
- **Backend**: Java 22, Spring Boot 3.3.3, Spring Web, Spring Data MongoDB, Spring Validation, Micrometer Actuator.
- **Database**: MongoDB Atlas with automated TTL indexes for anonymous session data retention.
- **AI Engine**: Google Gemini API (Gemini 1.5/2.0 Flash) + Dynamic Java Parametric Engine.

---

## ⚡ Quickstart

### 1. Spring Boot Backend
```powershell
cd prepforge-backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-22"
.\mvnw.cmd spring-boot:run
# Backend running at http://localhost:8080
```

### 2. Next.js Frontend
```powershell
cd prepforge-frontend
npm install
npm run dev
# Frontend running at http://localhost:3000
```
