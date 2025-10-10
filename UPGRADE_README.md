# Play Framework and Apache Pekko Upgrade

## Overview

This document describes the upgrade of sunbird-dial-service from Play Framework 2.4.6 with Akka 2.4.x to Play Framework 2.8.22 with Apache Pekko 1.0.3.

## Why This Upgrade

1. License Compliance: Akka changed from Apache 2.0 to Business Source License 1.1, requiring commercial licenses for production use. Apache Pekko maintains Apache 2.0 license.
2. Security: Play 2.4.6 and Akka 2.4.x no longer receive security updates. Multiple known CVEs exist.
3. Modernization: Access to latest features and performance improvements.

## Technology Stack Changes

- Play Framework: 2.4.6 to 2.8.22
- Actor Framework: Akka 2.4.x to Apache Pekko 1.0.3
- Scala: 2.11.12 to 2.13.12
- Guice: Added 5.1.0
- SLF4J: Added 2.0.9
- Logback: 1.2.0 to 1.4.14
- Jackson: 2.13.5 to 2.14.3
- Netty: 4.1.68.Final to 4.1.93.Final
- Commons Lang3: Added 3.12.0

## Note on Play 3.0

The original plan was to upgrade to Play 3.0.5. However, the play2-maven-plugin does not support Play 3.0. Therefore, this upgrade targets Play 2.8.22, which is the latest 2.x version and fully supports Apache Pekko integration.

## Key Changes

### Dependencies

Maven POM file updated with new versions. Scala library exclusions added to prevent version conflicts.

### Source Code

Akka imports migrated to Pekko across all Java files:
- akka.util to org.apache.pekko.util
- akka.dispatch to org.apache.pekko.dispatch

### API Changes

Play 2.8 requires API migrations:
- Promise to CompletionStage for async operations
- request() method patterns updated
- Filter API updates

### Configuration

Configuration files updated with Pekko namespaces (akka to pekko).

## Build Instructions

Build all modules:
```
mvn clean install -Dmaven.test.skip=true
```

Build with test compilation:
```
mvn clean install -DskipTests
```

## Migration Impact

Business Logic: No changes to business logic or functionality
API Compatibility: Maintained, as Pekko is API-compatible with Akka 2.6
Code Changes: Package name updates from akka to pekko, and Play API migrations
License: Now compliant with Apache 2.0 throughout the stack

## Testing Recommendations

1. Execute full unit test suite
2. Run integration tests
3. Perform regression testing
4. Conduct performance benchmarking
5. Test under production-like load

## Known Issues

Controller Request Parameter: Play 2.8 requires controller methods to accept Http.Request as a parameter. Additional updates may be needed.

Scala Version Conflicts: Verify dependency tree to ensure no Scala 2.11 artifacts present.
