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
- Jackson: 2.13.5 to 2.11.4
- Netty: 4.1.68.Final to 4.1.93.Final
- Commons Lang3: Added 3.12.0

## Key Changes

### Dependencies

Maven POM file updated with new versions. Scala library exclusions added to prevent version conflicts between Scala 2.11 and 2.13.

### Source Code

Akka imports migrated to Pekko:
- akka.util.Timeout to org.apache.pekko.util.Timeout
- akka.dispatch.Futures to org.apache.pekko.dispatch.Futures
- akka.dispatch.Mapper to org.apache.pekko.dispatch.Mapper

### Play 2.8 API Updates

- Removed Global.java and created modules.StartModule for Guice-based initialization
- Updated Filters.java to return List instead of array
- Converted Promise to CompletionStage for async operations
- Updated controller methods to accept Http.Request parameter
- Updated routes file to pass request parameter
- Fixed header API calls to use Optional pattern

### Configuration

application.conf files updated with Pekko namespaces (akka to pekko blocks).

## Build Instructions

Build all modules:
```
mvn clean install -Dmaven.test.skip=true
```

Build with test compilation:
```
mvn clean install -DskipTests
```

Create distribution package:
```
mvn play2:dist
```

Run development server:
```
mvn play2:run
```

## Migration Impact

Business Logic: No changes to business logic or functionality
API Compatibility: Maintained, as Pekko is API-compatible with Akka 2.6
Code Changes: Primarily package name updates from akka to pekko
License: Now compliant with Apache 2.0 throughout the stack

## Testing Recommendations

1. Execute full unit test suite
2. Run integration tests for actor communication
3. Perform regression testing for all features
4. Conduct performance benchmarking
5. Test under production-like load

## Known Issues

Scala Version Conflicts: If you encounter NoClassDefFoundError for scala.collection.GenMap, verify dependency tree to ensure no Scala 2.11 artifacts are present. Run mvn dependency:tree and add exclusions for any scala-library or scala-reflect with version 2.11.
