---
name: test
description: Testing best practices
---

# Testing

1. Run the project's standard test command (for example Maven `mvn test`).
2. Prefer fixing root causes over silencing failures.
3. When a test is flaky, isolate timing/order dependencies instead of retry loops.
