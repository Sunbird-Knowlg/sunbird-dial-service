# Play Framework and Apache Pekko Upgrade

## Overview

This document describes the upgrade of sunbird-dial-service from Play Framework 2.4.6 with Akka 2.4.x to Play Framework 3.0.5 with Apache Pekko 1.0.3.

## Why This Upgrade

1. License Compliance: Akka changed from Apache 2.0 to Business Source License 1.1, requiring commercial licenses for production use. Apache Pekko maintains Apache 2.0 license.
2. Security: Play 2.4.6 and Akka 2.4.x no longer receive security updates. Multiple known CVEs exist.
3. Modernization: Access to latest features and performance improvements.

## Technology Stack Changes

- Play Framework: 2.4.6 to 3.0.5
- Actor Framework: Akka 2.4.x to Apache Pekko 1.0.3
- Scala: 2.11.12 to 2.13.12
- Guice: Added 5.1.0
- SLF4J: Added 2.0.9
- Logback: 1.2.0 to 1.4.14
- Jackson: 2.13.5 to 2.14.3
- Netty: 4.1.68.Final to 4.1.93.Final

## Key Changes

### Dependencies

All Maven POM files updated with new versions. Play Framework groupId changed from com.typesafe.play to org.playframework. Scala library exclusions added to prevent version conflicts between Scala 2.11 and 2.13.

### Source Code

Akka imports migrated to Pekko across all Java files:
- akka.actor to org.apache.pekko.actor
- akka.util to org.apache.pekko.util
- akka.dispatch to org.apache.pekko.dispatch

Files modified:
- app/managers/DialcodeManager.java
- app/elasticsearch/ElasticSearchUtil.java
- app/elasticsearch/SearchProcessor.java

### Configuration

application.conf files updated with Pekko namespaces:
- akka to pekko
- Actor system configurations migrated
- Configuration references changed

Files modified:
- conf/application.conf
- devops/roles/dial-service-deploy/templates/dial-service.conf.j2

## Build Instructions

Note: Due to Maven Play plugin limitations with Play 3.0, manual steps may be required or migration to SBT build system.

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

## Files Modified

- pom.xml
- app/managers/DialcodeManager.java
- app/elasticsearch/ElasticSearchUtil.java
- app/elasticsearch/SearchProcessor.java
- conf/application.conf
- devops/roles/dial-service-deploy/templates/dial-service.conf.j2

## Known Issues

Maven Play Plugin: The play2-maven-plugin does not have native support for Play 3.0. Consider migrating to SBT build system for better Play 3.0 support.

Scala 2.11/2.13 Conflict: Verify dependency tree to ensure no Scala 2.11 artifacts are present. Run mvn dependency:tree and add exclusions for any scala-library or scala-reflect with version 2.11.

## Dependency Tree Check

Run the following command to verify all dependencies are using Scala 2.13:
```
mvn dependency:tree | grep scala
```

Ensure no Scala 2.11 dependencies are present.

## Additional Notes

This upgrade represents a significant version jump from Play 2.4.6 to 3.0.5, spanning multiple major versions. The main complexities come from:
- Play Framework API changes across major versions
- Maven plugin limitations for Play 3.0
- Scala binary compatibility (2.11 to 2.13)

Consider incremental testing and staged rollout for production deployment.
