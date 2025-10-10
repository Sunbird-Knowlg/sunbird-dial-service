# Play Framework Upgrade & Akka to Pekko Migration - Compatibility Report
## Sunbird DIAL Service

---

## Executive Summary

This report provides a comprehensive analysis for upgrading the Play Framework and migrating from Akka to Apache Pekko in the Sunbird DIAL Service. The migration is necessary due to Akka's license change from open-source (Apache 2.0) to commercial (Business Source License 1.1) starting with version 2.7+.

**Current State:**
- Play Framework: **2.4.6** (Released: July 2016, EOL)
- Scala: **2.11.12**
- Akka: **2.4.8** and **2.4.6** (Pre-license change versions)
- Build Tool: **Maven with play2-maven-plugin 1.0.0-rc5**
- Java: **11** (configured in pom.xml)

**Recommendation:** Upgrade to Play Framework 2.9.x or 3.0.x with Apache Pekko migration.

---

## Table of Contents

1. [Current Architecture Analysis](#1-current-architecture-analysis)
2. [Play Framework Upgrade Options](#2-play-framework-upgrade-options)
3. [Akka to Pekko Migration](#3-akka-to-pekko-migration)
4. [Build System Migration](#4-build-system-migration)
5. [Dependencies Impact Analysis](#5-dependencies-impact-analysis)
6. [Code Impact Assessment](#6-code-impact-assessment)
7. [Configuration Changes Required](#7-configuration-changes-required)
8. [Migration Strategy & Roadmap](#8-migration-strategy--roadmap)
9. [Risks & Challenges](#9-risks--challenges)
10. [Benefits](#10-benefits)
11. [Estimated Effort](#11-estimated-effort)
12. [Recommendations](#12-recommendations)

---

## 1. Current Architecture Analysis

### 1.1 Current Technology Stack

```
┌─────────────────────────────────────────┐
│     Play Framework 2.4.6                │
│     (Scala 2.11.12 + Java 11)          │
├─────────────────────────────────────────┤
│     Akka Actor 2.4.8                   │
│     Akka SLF4J 2.4.6                   │
├─────────────────────────────────────────┤
│     Elasticsearch 7.10.2                │
│     Cassandra Driver 3.1.2              │
│     Redis (Jedis 2.6.2)                │
│     Kafka Client 1.1.0                  │
└─────────────────────────────────────────┘
```

### 1.2 Current Akka Usage

**Minimal Akka Usage Identified:**

1. **File: `app/managers/DialcodeManager.java`**
   - Import: `import akka.util.Timeout;`
   - Usage: Type definition only (appears unused in actual code)

2. **File: `app/elasticsearch/ElasticSearchUtil.java`**
   - Import: `import akka.dispatch.Futures;`
   - Usage: Futures-related operations (scala.concurrent.Future)

3. **File: `app/elasticsearch/SearchProcessor.java`**
   - Import: `import akka.dispatch.Mapper;`
   - Usage: Mapping functions for Future transformations

**Key Finding:** Akka usage is **minimal and limited to utility classes** (Futures, Timeout, Mapper), not core actor systems.

### 1.3 Current Build System

- **Build Tool:** Maven 3.x
- **Play Plugin:** `play2-maven-plugin` version 1.0.0-rc5
- **Scala Compiler Plugin:** `sbt-compiler-maven-plugin` 1.0.0
- **Key Issue:** Maven-based Play 2.4.x setup is **non-standard** (Play officially uses SBT)

---

## 2. Play Framework Upgrade Options

### 2.1 Version Comparison Table

| Version | Release Date | Scala Version | Java Support | Akka/Pekko | Status | Notes |
|---------|-------------|---------------|--------------|------------|--------|-------|
| **2.4.6** (Current) | Jul 2016 | 2.11 | 8 | Akka 2.4 | EOL | Vulnerable, unsupported |
| **2.5.x** | Apr 2017 | 2.11/2.12 | 8 | Akka 2.5 | EOL | Intermediate step |
| **2.6.x** | Sep 2018 | 2.11/2.12 | 8 | Akka 2.5 | EOL | Last 2.x before 2.7 |
| **2.7.x** | Apr 2019 | 2.12/2.13 | 8, 11 | Akka 2.6 | EOL | Still uses Akka |
| **2.8.x** | Feb 2021 | 2.12/2.13 | 8, 11, 17 | Akka 2.6 | Maintenance | Still uses Akka |
| **2.9.x** | May 2024 | 2.13/3 | 11, 17, 21 | **Pekko 1.0** | **Supported** | **First with Pekko** |
| **3.0.x** | Sep 2023 | 2.13/3 | 11, 17, 21 | **Pekko 1.0** | **Supported** | Major rewrite |

### 2.2 Recommended Target Version

**Option A: Play 2.9.x (Recommended)**
- ✅ Supports Pekko 1.0 natively
- ✅ Smooth migration path from 2.8.x
- ✅ Maintains Java support (Java 11/17/21)
- ✅ Scala 2.13 or Scala 3 support
- ✅ Long-term support expected
- ⚠️ Requires incremental upgrades (2.4 → 2.6 → 2.8 → 2.9)

**Option B: Play 3.0.x**
- ✅ Modern architecture with Pekko 1.0
- ✅ Future-proof solution
- ⚠️ Breaking API changes
- ⚠️ Requires significant refactoring
- ⚠️ Steeper learning curve

**Decision:** Target **Play 2.9.x** for safer migration with Pekko support.

### 2.3 Key Play Framework Changes (2.4 → 2.9)

#### Breaking Changes:

1. **Dependency Injection:**
   - Play 2.4: Optional DI with Guice
   - Play 2.9: Required DI (Guice is default)
   - **Impact:** `GlobalSettings` deprecated → Need to migrate to `ApplicationLoader`

2. **Futures API:**
   - Play 2.4: `play.libs.F.Promise`
   - Play 2.9: `java.util.concurrent.CompletionStage`
   - **Impact:** Requires rewriting promise chains

3. **HTTP Context:**
   - Play 2.4: ThreadLocal-based context
   - Play 2.9: Explicit context passing
   - **Impact:** Changes in `Global.java` request handling

4. **Configuration:**
   - Play 2.4: `play.Configuration`
   - Play 2.9: Typesafe Config with new API
   - **Impact:** Configuration access patterns change

5. **WS Client:**
   - API modernization required
   - Async operations use CompletionStage

---

## 3. Akka to Pekko Migration

### 3.1 What is Apache Pekko?

Apache Pekko is a **fork of Akka 2.6.x** maintained by the Apache Software Foundation:
- **License:** Apache License 2.0 (Open Source)
- **Compatibility:** Drop-in replacement for Akka 2.6.x
- **Versions:** Pekko 1.0.x (based on Akka 2.6.x)
- **Governance:** Apache Foundation (vendor-neutral)

### 3.2 Akka vs Pekko Comparison

| Aspect | Akka 2.6.x (Old) | Akka 2.7+ (New) | Pekko 1.0.x |
|--------|------------------|-----------------|-------------|
| **License** | Apache 2.0 | BSL 1.1 (Commercial) | Apache 2.0 |
| **Cost** | Free | Paid (production) | Free |
| **Package Name** | `com.typesafe.akka` | `com.typesafe.akka` | `org.apache.pekko` |
| **Compatibility** | N/A | Breaking changes | Drop-in for Akka 2.6 |
| **Support** | EOL | Lightbend | Apache Foundation |
| **Community** | Large | Lightbend-driven | Growing (Apache) |

### 3.3 Package Name Changes

```diff
- import akka.actor._
+ import org.apache.pekko.actor._

- import akka.dispatch.Futures
+ import org.apache.pekko.dispatch.Futures

- import akka.util.Timeout
+ import org.apache.pekko.util.Timeout

- import akka.dispatch.Mapper
+ import org.apache.pekko.dispatch.Mapper
```

### 3.4 Maven Dependency Changes

```xml
<!-- OLD: Akka -->
<dependency>
    <groupId>com.typesafe.akka</groupId>
    <artifactId>akka-actor_2.11</artifactId>
    <version>2.4.8</version>
</dependency>

<!-- NEW: Pekko -->
<dependency>
    <groupId>org.apache.pekko</groupId>
    <artifactId>pekko-actor_2.13</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 3.5 Configuration Changes

```hocon
# OLD: application.conf
akka {
  loggers = ["akka.event.Logging$DefaultLogger"]
  log-config-on-start = true
}

# NEW: application.conf
pekko {
  loggers = ["org.apache.pekko.event.Logging$DefaultLogger"]
  log-config-on-start = true
}
```

### 3.6 Migration Impact for This Project

**Low Impact:** Since the project uses Akka minimally (only Futures, Timeout, Mapper utilities):

1. **No Actor System:** No custom actors or actor systems defined
2. **No Akka Streams:** Not using Akka Streams
3. **No Akka HTTP:** Using Play's HTTP stack
4. **Limited to Utilities:** Only dispatch utilities used

**Code Changes Required:**
- 3 import statements (DialcodeManager, ElasticSearchUtil, SearchProcessor)
- Configuration file updates (akka → pekko)
- Dependency updates in pom.xml

---

## 4. Build System Migration

### 4.1 Current Build System Issues

**Maven with Play 2.4.6:**
- `play2-maven-plugin` is at RC (Release Candidate) version
- Limited community support
- Plugin development stalled
- Non-standard approach (Play recommends SBT)

### 4.2 Migration Options

#### Option A: Continue with Maven
**Pros:**
- Maintains existing build infrastructure
- Team familiarity with Maven
- No build system learning curve

**Cons:**
- Limited Play 2.8+ support in Maven plugins
- Community moved to SBT/Gradle
- Harder to find documentation/support
- May encounter plugin compatibility issues

#### Option B: Migrate to SBT
**Pros:**
- Official Play Framework build tool
- Better Play plugin support
- Rich ecosystem for Scala/Play
- Better incremental compilation

**Cons:**
- Learning curve for SBT
- Build script rewrite required
- CI/CD pipeline updates needed
- Different dependency management

#### Option C: Migrate to Gradle
**Pros:**
- Modern build tool
- Good Play support via plugins
- Familiar syntax (Groovy/Kotlin DSL)
- Better performance than Maven

**Cons:**
- Less common for Play projects
- Plugin ecosystem smaller than SBT
- Migration effort

### 4.3 Recommendation: Phased Migration

**Phase 1:** Upgrade Play within Maven (if possible up to 2.6/2.7)
**Phase 2:** Evaluate SBT migration for Play 2.8+
**Phase 3:** Complete migration to Play 2.9 + Pekko with SBT

**Alternative:** If Maven plugins support is available, stay with Maven for simplicity.

---

## 5. Dependencies Impact Analysis

### 5.1 Direct Dependencies Requiring Updates

| Current Dependency | Current Version | Target Version | Breaking Changes | Notes |
|-------------------|-----------------|----------------|------------------|-------|
| **Play Framework** | 2.4.6 | 2.9.x | Yes (Major) | Core framework |
| **Scala** | 2.11.12 | 2.13.x | Yes (Minor) | Binary incompatible |
| **Akka Actor** | 2.4.8 | Pekko 1.0.3 | Yes (Package) | Direct replacement |
| **Akka SLF4J** | 2.4.6 | Pekko SLF4J 1.0.3 | Yes (Package) | Direct replacement |
| Jackson | 2.13.5 | 2.15.x+ | Minor | Security updates |
| Elasticsearch | 7.10.2 | 8.x | Moderate | API changes possible |
| Cassandra Driver | 3.1.2 | 4.x | Moderate | API updates |
| Jedis (Redis) | 2.6.2 | 5.x | Major | Consider Lettuce |
| Kafka Client | 1.1.0 | 3.x | Minor | Mostly compatible |

### 5.2 Transitive Dependencies

**Concern:** Play Framework internally depends on Akka/Pekko:
- Play 2.4-2.8: Uses Akka internally
- Play 2.9+: Uses Pekko internally
- Transitive dependency resolution critical

**Action:** Ensure no Akka/Pekko version conflicts in dependency tree.

### 5.3 Plugin Dependencies

```xml
<!-- Current plugins needing updates -->
<plugin>
    <groupId>com.google.code.play2-maven-plugin</groupId>
    <artifactId>play2-maven-plugin</artifactId>
    <version>1.0.0-rc5</version> <!-- Need stable version -->
</plugin>
```

**Issue:** play2-maven-plugin may not support Play 2.8+
**Research Needed:** Check latest Maven plugin versions or plan SBT migration.

---

## 6. Code Impact Assessment

### 6.1 Files Requiring Changes

#### High Priority Changes:

1. **`app/Global.java`** (Critical)
   - Extends `GlobalSettings` (deprecated in Play 2.6+)
   - Uses `play.libs.F.Promise` (removed in Play 2.6+)
   - Request lifecycle hooks need migration
   - **Migration:** Replace with `ApplicationLoader` and DI modules

2. **`app/filters/HealthCheckFilter.scala`**
   - Uses Play 2.4 Filter API
   - Execution context imports
   - **Migration:** Update to Play 2.9 Filter API

3. **`app/managers/DialcodeManager.java`**
   - Import: `import akka.util.Timeout;`
   - **Migration:** Change to `org.apache.pekko.util.Timeout`

4. **`app/elasticsearch/ElasticSearchUtil.java`**
   - Import: `import akka.dispatch.Futures;`
   - Uses: `scala.concurrent.Future` and `Promise`
   - **Migration:** Change to `org.apache.pekko.dispatch.Futures`

5. **`app/elasticsearch/SearchProcessor.java`**
   - Import: `import akka.dispatch.Mapper;`
   - **Migration:** Change to `org.apache.pekko.dispatch.Mapper`

#### Medium Priority Changes:

6. **`conf/application.conf`**
   - Akka configuration blocks
   - **Migration:** Rename `akka` blocks to `pekko`

7. **`devops/roles/dial-service-deploy/templates/dial-service.conf.j2`**
   - Template configuration with Akka settings
   - **Migration:** Update template for Pekko

8. **All Controller Classes**
   - May use Play 2.4 Action API
   - **Migration:** Review and update to Play 2.9 standards

### 6.2 API Migration Examples

#### Example 1: Promise to CompletionStage

```java
// Play 2.4 (OLD)
import play.libs.F.Promise;

public Promise<Result> asyncAction() {
    return Promise.promise(() -> doWork())
                  .map(result -> ok(Json.toJson(result)));
}

// Play 2.9 (NEW)
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public CompletionStage<Result> asyncAction() {
    return supplyAsync(() -> doWork())
                  .thenApply(result -> ok(Json.toJson(result)));
}
```

#### Example 2: GlobalSettings to ApplicationLoader

```java
// Play 2.4 (OLD) - Global.java
public class Global extends GlobalSettings {
    public void onStart(Application app) {
        // initialization
    }
}

// Play 2.9 (NEW) - ApplicationLoader.java
public class CustomApplicationLoader implements ApplicationLoader {
    @Override
    public Application load(Context context) {
        return new Components(context).application();
    }
    
    static class Components extends BuiltInComponentsFromContext {
        Components(ApplicationLoader.Context context) {
            super(context);
            // initialization
        }
    }
}
```

### 6.3 Estimated Lines of Code Changes

| Category | Files Affected | LOC to Change | Complexity |
|----------|----------------|---------------|------------|
| Akka → Pekko imports | 3 | ~10 | Low |
| Global → DI migration | 1 | ~150 | High |
| Promise → CompletionStage | ~5-10 | ~200-400 | Medium |
| Filter updates | 1 | ~30 | Medium |
| Configuration | 2 | ~20 | Low |
| Controller updates | ~10-15 | ~300-500 | Medium |
| **Total Estimated** | **25-30** | **~710-1110** | **Medium-High** |

---

## 7. Configuration Changes Required

### 7.1 Application Configuration

**File:** `conf/application.conf`

```hocon
# Current (Akka)
akka {
  #loggers = ["akka.event.Logging$DefaultLogger"]
  #log-config-on-start = true
}

# New (Pekko)
pekko {
  #loggers = ["org.apache.pekko.event.Logging$DefaultLogger"]
  #log-config-on-start = true
}
```

### 7.2 Deployment Configuration

**File:** `devops/roles/dial-service-deploy/templates/dial-service.conf.j2`

- Update Akka configuration references
- Update documentation URLs
- Verify Play Framework configuration paths

### 7.3 Build Configuration

**File:** `pom.xml`

Major updates needed:
1. Play Framework version
2. Scala version
3. Remove Akka dependencies
4. Add Pekko dependencies
5. Update plugin versions
6. Update transitive dependencies

---

## 8. Migration Strategy & Roadmap

### 8.1 Recommended Migration Path

**Incremental Approach (Lower Risk):**

```
Current State (Play 2.4.6 + Akka 2.4)
    ↓
Step 1: Play 2.5.x + Akka 2.5
    ↓
Step 2: Play 2.6.x + Akka 2.5 (Remove GlobalSettings)
    ↓
Step 3: Play 2.7.x + Akka 2.6 (Update Promise APIs)
    ↓
Step 4: Play 2.8.x + Akka 2.6 (Stabilize on Akka 2.6)
    ↓
Step 5: Play 2.9.x + Pekko 1.0 (Final migration)
```

**Big Bang Approach (Higher Risk, Faster):**

```
Current State (Play 2.4.6 + Akka 2.4)
    ↓
Direct Jump: Play 2.9.x + Pekko 1.0
```

### 8.2 Phased Migration Timeline

#### Phase 1: Preparation & Assessment (2 weeks)
- ✅ Analyze all code dependencies (Complete - This Report)
- Set up development environment
- Create comprehensive test suite
- Document current behavior
- Set up CI/CD for testing

#### Phase 2: Incremental Upgrades (6-8 weeks)
- Week 1-2: Upgrade to Play 2.5.x
- Week 3-4: Upgrade to Play 2.6.x (GlobalSettings removal)
- Week 5-6: Upgrade to Play 2.7.x/2.8.x
- Week 7-8: Testing and stabilization

#### Phase 3: Pekko Migration (2-3 weeks)
- Week 1: Upgrade to Play 2.9.x with Pekko
- Week 2: Update imports and configuration
- Week 3: Integration testing

#### Phase 4: Build System (2-4 weeks, Optional)
- Evaluate SBT migration need
- Create SBT build configuration
- Migrate build scripts
- Update CI/CD pipelines

#### Phase 5: Testing & Validation (2-3 weeks)
- Comprehensive integration testing
- Performance testing
- Load testing
- Security scanning
- Documentation updates

#### Phase 6: Deployment & Monitoring (1-2 weeks)
- Staged deployment
- Production monitoring
- Rollback plan ready
- Team training

**Total Estimated Timeline:** 15-22 weeks (3.5-5 months)

### 8.3 Parallel Track Options

If timeline is critical, consider:
1. Upgrade Play Framework first (stay on old Akka 2.6)
2. Migrate to Pekko in a separate phase
3. This buys time while maintaining security

---

## 9. Risks & Challenges

### 9.1 Technical Risks

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| **Breaking API Changes** | High | High | Incremental upgrades, extensive testing |
| **Dependency Conflicts** | High | Medium | Careful dependency management, exclusions |
| **Performance Regression** | Medium | Low | Benchmark before/after, profiling |
| **Hidden Akka Usage** | Medium | Low | Comprehensive code analysis, dependency tree review |
| **Build System Issues** | High | Medium | Maven plugin research, SBT backup plan |
| **Test Coverage Gaps** | High | Medium | Improve test coverage before migration |
| **Third-party Plugin Compatibility** | Medium | Medium | Research compatible versions early |

### 9.2 Operational Risks

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| **Extended Downtime** | High | Low | Blue-green deployment, rollback plan |
| **Data Migration Issues** | High | Low | No data migration expected (runtime only) |
| **Team Skill Gap** | Medium | Medium | Training, pair programming, documentation |
| **Production Incidents** | High | Low | Thorough testing, phased rollout |
| **Rollback Complexity** | Medium | Low | Maintain parallel versions, feature flags |

### 9.3 Business Risks

- **Timeline Uncertainty:** 3-5 months is significant
- **Resource Allocation:** Requires dedicated team time
- **Feature Development Freeze:** During migration period
- **Budget Impact:** Testing infrastructure, possible consulting

### 9.4 Critical Challenges

1. **GlobalSettings Deprecation:**
   - Current: `Global.java` extends `GlobalSettings`
   - Challenge: Complete rewrite needed
   - Impact: High (affects app initialization)

2. **Promise API Removal:**
   - Current: Uses `play.libs.F.Promise`
   - Challenge: Rewrite all async operations
   - Impact: High (affects all async code)

3. **Maven Plugin Support:**
   - Current: play2-maven-plugin 1.0.0-rc5
   - Challenge: May not support Play 2.8+
   - Impact: High (may force SBT migration)

4. **Binary Compatibility:**
   - Current: Scala 2.11
   - Target: Scala 2.13
   - Challenge: All dependencies must support 2.13
   - Impact: High (dependency resolution)

---

## 10. Benefits

### 10.1 Technical Benefits

✅ **Open Source Compliance:**
- Avoid Akka BSL 1.1 commercial license
- Maintain Apache 2.0 licensing
- No vendor lock-in

✅ **Security:**
- Address CVEs in Play 2.4.6 (EOL since 2016)
- Get security patches from maintained versions
- Comply with security policies

✅ **Modern Platform:**
- Java 17/21 support
- Modern async patterns (CompletionStage)
- Better IDE support
- Improved performance

✅ **Framework Features:**
- HTTP/2 support
- Better JSON handling
- Improved routing
- Enhanced testing tools

✅ **Community & Support:**
- Active Play 2.9+ community
- Apache Pekko backed by Apache Foundation
- Better documentation
- More Stack Overflow answers

### 10.2 Business Benefits

✅ **Cost Savings:**
- Avoid Akka commercial licensing fees
- Reduce security vulnerability costs
- Lower technical debt

✅ **Future-Proofing:**
- Extend application lifespan
- Easier future upgrades
- Access to new features

✅ **Developer Productivity:**
- Modern development patterns
- Better tooling
- Faster build times (with SBT)

✅ **Compliance:**
- Open source compliance
- License audit friendly
- No commercial dependencies

### 10.3 Long-term Strategic Benefits

- **Maintainability:** Modern codebase easier to maintain
- **Talent Acquisition:** Easier to hire with modern stack
- **Innovation:** Access to newer features and patterns
- **Risk Reduction:** Supported frameworks reduce operational risk

---

## 11. Estimated Effort

### 11.1 Development Effort Breakdown

| Phase | Task | Effort (Person-Days) |
|-------|------|---------------------|
| **Phase 1** | Analysis & Planning | 10 |
| | Test Suite Enhancement | 5 |
| | Environment Setup | 3 |
| **Phase 2** | Play 2.4 → 2.5 Upgrade | 8 |
| | Play 2.5 → 2.6 Upgrade | 10 |
| | GlobalSettings Migration | 5 |
| | Play 2.6 → 2.7 Upgrade | 8 |
| | Promise API Migration | 10 |
| | Play 2.7 → 2.8 Upgrade | 5 |
| **Phase 3** | Play 2.8 → 2.9 + Pekko | 8 |
| | Akka → Pekko Code Changes | 3 |
| | Configuration Updates | 2 |
| **Phase 4** | Build System (Optional) | 15 |
| **Phase 5** | Testing & Validation | 15 |
| | Performance Testing | 5 |
| | Security Testing | 3 |
| **Phase 6** | Documentation | 5 |
| | Deployment | 5 |
| | Post-deployment Support | 5 |
| **Total** | | **124-134 person-days** |

### 11.2 Team Composition

**Recommended Team:**
- 2 Senior Backend Developers (Play/Scala experience)
- 1 DevOps Engineer (CI/CD, deployment)
- 1 QA Engineer (testing, automation)
- 1 Technical Lead (oversight, architecture)

**Timeline:** 3-5 months (calendar time)

### 11.3 Resource Requirements

**Infrastructure:**
- Development environments (4-5)
- Test environment (mirrors production)
- Staging environment
- CI/CD pipeline capacity

**Tools:**
- Performance monitoring tools
- Profiling tools
- Testing frameworks
- Build tools (Maven/SBT)

---

## 12. Recommendations

### 12.1 Immediate Actions (Next 2 Weeks)

1. ✅ **Approve This Report** for stakeholder review
2. 🔲 **Allocate Team Resources** (2-3 developers, 1 QA)
3. 🔲 **Set Up Test Environment** for migration experiments
4. 🔲 **Improve Test Coverage** (target: >80%)
5. 🔲 **Research Maven Plugin Support** for Play 2.8+
6. 🔲 **Create Detailed Migration Backlog** (JIRA/GitHub)
7. 🔲 **Establish Success Metrics** (performance baselines)

### 12.2 Short-term Strategy (Next 3 Months)

**Option A: Incremental Migration (Recommended)**
1. Start with Play 2.5 upgrade
2. Proceed incrementally to 2.6, 2.7, 2.8
3. Finish with Play 2.9 + Pekko
4. **Risk:** Lower, **Timeline:** Longer (4-5 months)

**Option B: Direct Jump**
1. Jump directly to Play 2.9 + Pekko
2. Fix all breaking changes in one go
3. **Risk:** Higher, **Timeline:** Shorter (3 months)

**Recommendation:** Choose **Option A** for production systems, **Option B** only if timeline is critical and team is experienced.

### 12.3 Build System Decision

**Decision Point:** After Play 2.6 upgrade
- If Maven plugins work well: Continue with Maven
- If Maven plugins problematic: Migrate to SBT

**Recommendation:** Attempt Maven path first; only switch to SBT if necessary.

### 12.4 Long-term Recommendations

1. **Establish Upgrade Cadence:**
   - Review dependencies quarterly
   - Upgrade minor versions regularly
   - Avoid falling behind again

2. **Improve CI/CD:**
   - Automated dependency scanning
   - Automated security scanning
   - Performance regression testing

3. **Documentation:**
   - Maintain architecture documentation
   - Document upgrade procedures
   - Create runbooks

4. **Training:**
   - Team training on Play 2.9 features
   - Pekko fundamentals
   - Modern async patterns

---

## 13. Conclusion

### Summary

The migration from Play Framework 2.4.6 with Akka to Play Framework 2.9.x with Apache Pekko is:

- ✅ **Necessary:** Due to Akka license changes and Play 2.4 being EOL
- ✅ **Feasible:** Low Akka usage makes Pekko migration straightforward
- ⚠️ **Moderate Complexity:** Play Framework upgrade is the main challenge
- ✅ **Valuable:** Security, compliance, and modernization benefits
- ⏱️ **Time Investment:** 3-5 months with 2-3 developers

### Key Takeaways

1. **Akka → Pekko migration is LOW impact** (only 3 import changes)
2. **Play 2.4 → 2.9 upgrade is HIGH impact** (API changes, deprecations)
3. **Incremental approach recommended** for risk mitigation
4. **Build system may need attention** (Maven plugin support uncertain)
5. **Significant testing required** but benefits outweigh costs

### Decision Matrix

| Factor | Weight | Score (1-5) | Weighted |
|--------|--------|-------------|----------|
| Business Value | 30% | 5 | 1.5 |
| Technical Feasibility | 25% | 4 | 1.0 |
| Risk Level | 20% | 3 | 0.6 |
| Resource Availability | 15% | 3 | 0.45 |
| Timeline Pressure | 10% | 3 | 0.3 |
| **Total** | **100%** | | **3.85/5** |

**Recommendation: PROCEED with migration** using incremental approach.

---

## Appendix A: Reference Links

### Play Framework
- Play 2.9 Documentation: https://www.playframework.com/documentation/2.9.x/Home
- Migration Guides: https://www.playframework.com/documentation/2.9.x/Migration29
- Play Repository: https://github.com/playframework/playframework

### Apache Pekko
- Official Site: https://pekko.apache.org/
- Documentation: https://pekko.apache.org/docs/pekko/current/
- GitHub: https://github.com/apache/incubator-pekko
- Migration Guide: https://pekko.apache.org/docs/pekko/current/project/migration-guides.html

### Akka License Information
- Akka License Change: https://www.lightbend.com/akka/license-faq
- BSL 1.1 License: https://www.lightbend.com/akka/license

### Community Resources
- Play Framework Discussions: https://github.com/playframework/playframework/discussions
- Stack Overflow: [play-framework], [apache-pekko] tags

---

## Appendix B: Code Audit Details

### Files with Direct Akka References

```
app/managers/DialcodeManager.java:3:import akka.util.Timeout;
app/elasticsearch/ElasticSearchUtil.java:6:import akka.dispatch.Futures;
app/elasticsearch/SearchProcessor.java:3:import akka.dispatch.Mapper;
```

### Configuration Files with Akka References

```
conf/application.conf:5:## Akka
conf/application.conf:8:akka {
devops/roles/dial-service-deploy/templates/dial-service.conf.j2:5:## Akka
devops/roles/dial-service-deploy/templates/dial-service.conf.j2:8:akka {
```

### Total Impact
- **Java Files:** 3
- **Configuration Files:** 2
- **Import Statements:** 3
- **Configuration Blocks:** 2

---

## Appendix C: Glossary

- **Akka:** Actor-based concurrency framework (now commercial)
- **Pekko:** Apache's open-source fork of Akka 2.6.x
- **BSL 1.1:** Business Source License (not open source)
- **Play Framework:** Web framework for Scala and Java
- **SBT:** Scala Build Tool (official Play build tool)
- **DI:** Dependency Injection
- **EOL:** End of Life (no longer supported)
- **CVE:** Common Vulnerabilities and Exposures

---

## Document Metadata

- **Report Version:** 1.0
- **Date:** 2025-10-10
- **Author:** AI Migration Analysis
- **Project:** Sunbird DIAL Service
- **Repository:** SNT01/sunbird-dial-service
- **Purpose:** Migration Feasibility & Compatibility Assessment
- **Status:** Draft for Review
- **Next Review Date:** TBD based on stakeholder feedback

---

**END OF REPORT**
