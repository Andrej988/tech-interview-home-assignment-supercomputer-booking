# Technical Interview - Home Assignment: Supercomputer Booking
![Build & Test](https://github.com/Andrej988/tech-interview-home-assignment-supercomputer-booking/actions/workflows/build_and_test.yml/badge.svg)

## Instructions
COMPANY_NAME (redacted) has a new cloud computing product: a supercomputer. Many customers are interested in it and want to use it. Sadly, our supercomputer cannot be virtualised, so we cannot divide and share it with multiple customers at once. Instead, the customers have to use it one after another. 

Your goal is to design and implement a small web backend application allowing to book the supercomputer. Below is a list of use cases that should be supported:
- Booking the supercomputer given a time interval and customer identity
- Checking if the supercomputer can be booked in the given time interval
- Cancelling an existing booking
- Viewing active bookings for the given customer identity 

Your application should use modern Java and the Spring Framework. Expect big interest for the supercomputer! Multiple customers might book at the same time. Do not worry about the persistence for the time being and assume you can keep everything in memory.

## Additional Clarification
- We can assume we are operating in the same TZ and can therefore ignore it (use local dates).
- We expect a lot of concurrent requests

## Design Decissions
- Modern Java & Spring:
  - Tech stack used: Spring Boot 3.0.x (Spring 6.0.x under the hood) & Java 17 (latest LTS)
  - Reactive paradigm (Spring Webflux / Functional Endpoints)
- Persistence:
  - In-memory persistence using H2 database
  - Spring Data R2DBC (Rective Relational Database Connectivity)
- Handling high traffic:
  - Reactive stack: Spring Webflux / Functional Endpoints
    - Event loop concurrency model
    - Non-blocking supports Reactive Streams back pressure, runs on Netty.
- Concurrent booking requests (same or overlapping time frame)
  - Transaction Isolation (Serializable isolation level)
    - Highest level of transaction isolation
    - Prevents dirty reads, non-repeatable reads and phantom reads
    - Results in performance decrease due to isolation
- Request/Response data:
  - Usage of DTOs (Java records)
  - Mapping between entity and DTOs: Mapstructs
- Lombok for reducing boilerplate code (getters/setters, ...)
