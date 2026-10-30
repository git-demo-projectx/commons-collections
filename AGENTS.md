# AGENTS.md

This repository is a snapshot of Apache Commons Collections 4 (Apache License 2.0).
The source level is Java 8. The build uses Maven 3 and JDK 17.

## Commands

| Task | Command |
|---|---|
| Download dependencies and compile | `make setup` |
| Checks: Apache RAT, PMD, Checkstyle | `make lint` |
| All tests | `make test` |
| One test class | `mvn -B -ntp -q test -Dtest=ClassName` |

CI runs `make lint`, the tests with JaCoCo coverage, and a SonarQube Cloud analysis.

## Conventions

- Match the code around you. Do not refactor code outside the task.
- Keep the Apache license header on every new file.
- Add or update tests for every behavior change. Do not weaken an assertion to make a test pass.

## SonarQube coding guidelines

MUST FOLLOW: apply the SonarQube coding guidelines below to all code you write or edit in this task. Do not produce code that violates them.

### Rules

1. Java: Only one method invocation is expected when testing runtime exceptions
2. Java: Sections of code should not be commented out
3. Java: Fields in a "Serializable" class should either be transient or serializable
4. Java: Generic wildcard types should not be used in return types
5. Java: Local variables should not shadow class fields
6. Java: Switch cases should end with an unconditional "break" statement
7. Java: Lambdas should be replaced with method references
8. Java: Tests should include assertions
9. Java: JUnit5 inner test classes should be annotated with @Nested
10. Java: Calls to methods should not trigger an exception
