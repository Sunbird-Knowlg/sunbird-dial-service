# Play Framework and Apache Pekko Upgrade (Phase 1 - Play Framework 2.4.6 TO Play Framework 2.8.22)

## Overview

This document describes the upgrade of sunbird-dial-service from Play Framework 2.4.6 with Akka to Play Framework 2.8.22 with Apache Pekko 1.0.3.

## Why This Upgrade

1. License Compliance: Akka changed from Apache 2.0 to Business Source License 1.1, requiring commercial licenses for production use. Apache Pekko maintains Apache 2.0 license.
2. Security: Play 2.4.6 and Akka no longer receive security updates.
3. Modernization: Access to latest features and performance improvements.

## Technology Stack Changes

- Play Framework: 2.4.6 to 2.8.22
- Actor Framework: Akka to Apache Pekko 1.0.3
- Scala: 2.11.12 to 2.13.12
- Jackson: 2.13.5 to 2.14.3
- Guice: 4.2.3
- SLF4J: 2.0.9
- Logback: 1.4.14
- Netty: 4.1.100.Final
- Commons Lang3: 3.12.0

## Key Changes

### Dependencies

All Maven POM files updated with new versions. Play Framework groupId changed from com.typesafe.play to org.playframework. Scala library exclusions added to prevent version conflicts between Scala 2.11 and 2.13.

### Source Code

Akka imports migrated to Pekko across all Java files:
- akka.actor to org.apache.pekko.actor
- akka.pattern to org.apache.pekko.pattern
- akka.routing to org.apache.pekko.routing
- akka.util to org.apache.pekko.util
- akka.testkit to org.apache.pekko.testkit

### Configuration

application.conf files updated with Pekko namespaces:
- akka blocks changed to pekko blocks
- Actor system configurations migrated
- Serialization bindings updated
- Dispatcher references changed

### Play 2.8 API Updates

- StartModule: Implements AbstractModule for Guice-based initialization
- Filters: Updated to return List of EssentialFilter
- Controllers: Updated to accept Http.Request parameter
- Promise API: Converted to CompletionStage
- Header API: Updated to use Optional pattern

### Global.java Removal

The deprecated Global.java class was replaced with modules/StartModule.java for Play 2.8 compatibility. All functionality has been preserved:

Before (Global.java using deprecated GlobalSettings):
- Set system property for Netty
- Initialize telemetry component
- Run health check on startup

After (StartModule.java using Guice):
- Same system property configuration
- Same telemetry initialization
- Same health check execution via eager singleton

No functionality was lost. The StartModule uses Guice dependency injection which is the recommended approach in Play 2.8 and provides better lifecycle management.

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

## Phase 2
Play Framework 2.8.22 To 3.0.5

### Overview

Phase 2 upgrades sunbird-dial-service from Play Framework 2.8.22 to Play Framework 3.0.5, bringing the application to the latest stable version with improved performance and security.

### Technology Stack Changes

- Play Framework: 2.8.22 to 3.0.5
- Scala: 2.13.12 (kept at 2.13.12 for sbt-compiler-maven-plugin compatibility)
- Jackson: 2.11.4 to 2.14.3
- Guice: 5.1.0 to 6.0.0
- Netty: 4.1.93.Final to 4.1.100.Final
- Java Runtime: 11 to 17 (minimum requirement for Play 3.0.5)

### Key Changes

#### Dependencies

- Play Framework groupId changed from `com.typesafe.play` to `org.playframework`
- Updated all Play dependencies:
  - `play_2.13` → version 3.0.5
  - `play-netty-server_2.13` → version 3.0.5
  - `play-specs2_2.13` → version 3.0.5
  - `filters-helpers_2.13` → `play-filters-helpers_2.13` version 3.0.5
  - Added `play-guice_2.13` version 3.0.5 for Guice application loader
- Jackson upgraded to 2.14.3 for compatibility with Play 3.0.5
- Guice upgraded to 6.0.0 for Jakarta EE support
- Guava upgraded to 32.1.3-jre for Guice 6.0.0 compatibility
- Cassandra driver upgraded from 3.1.2 to 3.11.5 for Guava 32.x compatibility
- Added jakarta.inject-api 2.0.1 dependency

#### Jakarta EE Migration

Play 3.0.5 uses Jakarta EE instead of Java EE (javax.* packages). All imports updated:
- `javax.inject.Inject` → `jakarta.inject.Inject`
- All other javax.* references replaced with jakarta.* equivalents

Files updated:
- `app/Filters.java`
- `app/modules/StartModule.java`
- `app/filters/HealthCheckFilter.scala`

#### Build Configuration

- Dockerfile updated to use Java 17 (eclipse-temurin:17-jdk-focal)
- Maven compiler plugin already set to Java 11+ with `<release>11</release>`
- Packaging type changed from `pom` to `play2` to enable distribution package creation
- Scala version kept at 2.13.12 for compatibility with sbt-compiler-maven-plugin 1.0.0
- Added application loader configuration in application.conf for Play 3.0 compatibility

### Build Instructions

Build all modules:
```
mvn clean install -DskipTests
```

Build with test compilation:
```
mvn clean install
```

Compile Scala and Java code:
```
mvn sbt-compiler:compile sbt-compiler:testCompile
```

Run development server:
```
mvn play2:run
```

Create distribution package:
```
mvn play2:dist
```

### Migration Impact

- **Business Logic**: No changes to business logic or functionality
- **API Compatibility**: Maintained, all endpoints remain the same
- **Code Changes**: Minimal - primarily dependency updates and javax to jakarta package migrations
- **Runtime Requirements**: Java 17 or higher now required (was Java 11)
- **License**: Remains Apache 2.0 compliant throughout the stack

### Testing

All existing tests remain compatible. The upgrade maintains backward compatibility for the application's API and functionality.
