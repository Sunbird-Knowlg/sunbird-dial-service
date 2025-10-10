# Migration Quick Reference Guide
## Play Framework 2.4.6 → 2.9.x & Akka → Pekko

This document provides quick reference for common migration patterns and code changes needed.

---

## Table of Contents
1. [Dependency Changes](#dependency-changes)
2. [Package Name Changes](#package-name-changes)
3. [API Migration Patterns](#api-migration-patterns)
4. [Configuration Changes](#configuration-changes)
5. [Common Pitfalls](#common-pitfalls)

---

## Dependency Changes

### Maven Dependencies (pom.xml)

#### Remove Akka Dependencies
```xml
<!-- REMOVE THESE -->
<dependency>
    <groupId>com.typesafe.akka</groupId>
    <artifactId>akka-actor_2.11</artifactId>
    <version>2.4.8</version>
</dependency>
<dependency>
    <groupId>com.typesafe.akka</groupId>
    <artifactId>akka-slf4j_2.11</artifactId>
    <version>2.4.6</version>
</dependency>
```

#### Add Pekko Dependencies
```xml
<!-- ADD THESE -->
<dependency>
    <groupId>org.apache.pekko</groupId>
    <artifactId>pekko-actor_2.13</artifactId>
    <version>1.0.3</version>
</dependency>
<dependency>
    <groupId>org.apache.pekko</groupId>
    <artifactId>pekko-slf4j_2.13</artifactId>
    <version>1.0.3</version>
</dependency>
```

#### Update Play Framework
```xml
<!-- UPDATE FROM -->
<properties>
    <play2.version>2.4.6</play2.version>
    <scala.version>2.11.12</scala.version>
</properties>

<!-- UPDATE TO -->
<properties>
    <play2.version>2.9.5</play2.version>
    <scala.version>2.13.14</scala.version>
</properties>
```

---

## Package Name Changes

### Akka → Pekko Imports

#### File: `app/managers/DialcodeManager.java`
```java
// OLD
import akka.util.Timeout;

// NEW
import org.apache.pekko.util.Timeout;
```

#### File: `app/elasticsearch/ElasticSearchUtil.java`
```java
// OLD
import akka.dispatch.Futures;

// NEW
import org.apache.pekko.dispatch.Futures;
```

#### File: `app/elasticsearch/SearchProcessor.java`
```java
// OLD
import akka.dispatch.Mapper;

// NEW
import org.apache.pekko.dispatch.Mapper;
```

### Automated Find/Replace
```bash
# Find all Akka imports
find . -name "*.java" -o -name "*.scala" | xargs grep -l "import akka"

# Replace akka with pekko (use with caution, review each change)
find . -name "*.java" -o -name "*.scala" | xargs sed -i 's/import akka\./import org.apache.pekko./g'
```

---

## API Migration Patterns

### 1. Promise → CompletionStage

#### Pattern: Async Actions

**BEFORE (Play 2.4):**
```java
import play.libs.F.Promise;
import static play.libs.F.Promise;

public Promise<Result> index() {
    return Promise.promise(() -> {
        // do work
        return ok("Done");
    });
}
```

**AFTER (Play 2.9):**
```java
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public CompletionStage<Result> index() {
    return supplyAsync(() -> {
        // do work
        return ok("Done");
    });
}
```

#### Pattern: Promise Chaining

**BEFORE (Play 2.4):**
```java
public Promise<Result> complexOperation() {
    return serviceCall()
        .map(result -> processResult(result))
        .map(processed -> ok(Json.toJson(processed)))
        .recover(error -> badRequest(error.getMessage()));
}
```

**AFTER (Play 2.9):**
```java
public CompletionStage<Result> complexOperation() {
    return serviceCall()
        .thenApply(result -> processResult(result))
        .thenApply(processed -> ok(Json.toJson(processed)))
        .exceptionally(error -> badRequest(error.getMessage()));
}
```

#### Pattern: Promise Composition

**BEFORE (Play 2.4):**
```java
Promise<Integer> p1 = Promise.promise(() -> 1);
Promise<Integer> p2 = Promise.promise(() -> 2);
Promise<Integer> combined = p1.flatMap(v1 -> 
    p2.map(v2 -> v1 + v2)
);
```

**AFTER (Play 2.9):**
```java
CompletionStage<Integer> p1 = supplyAsync(() -> 1);
CompletionStage<Integer> p2 = supplyAsync(() -> 2);
CompletionStage<Integer> combined = p1.thenCompose(v1 -> 
    p2.thenApply(v2 -> v1 + v2)
);
```

### 2. GlobalSettings → ApplicationLoader

#### Pattern: Application Lifecycle

**BEFORE (Play 2.4) - Global.java:**
```java
import play.GlobalSettings;
import play.Application;

public class Global extends GlobalSettings {
    
    @Override
    public void onStart(Application app) {
        System.out.println("Application started");
        // Initialize services
    }
    
    @Override
    public void onStop(Application app) {
        System.out.println("Application stopped");
        // Cleanup
    }
    
    @Override
    public Action onRequest(Request request, Method actionMethod) {
        // Request interception
        return super.onRequest(request, actionMethod);
    }
}
```

**AFTER (Play 2.9) - CustomApplicationLoader.java:**
```java
import play.ApplicationLoader;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;

public class CustomApplicationLoader extends GuiceApplicationLoader {
    
    @Override
    public GuiceApplicationBuilder builder(ApplicationLoader.Context context) {
        return initialBuilder
            .in(context.environment())
            .loadConfig(context.initialConfiguration())
            .overrides(overrides(context));
    }
}
```

**AFTER (Play 2.9) - Module.java for startup/shutdown:**
```java
import com.google.inject.AbstractModule;
import play.api.inject.ApplicationLifecycle;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

public class StartupModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApplicationStart.class).asEagerSingleton();
    }
}

@Singleton
class ApplicationStart {
    @Inject
    public ApplicationStart(ApplicationLifecycle lifecycle) {
        // Startup logic
        System.out.println("Application started");
        
        lifecycle.addStopHook(() -> {
            // Cleanup logic
            System.out.println("Application stopped");
            return CompletableFuture.completedFuture(null);
        });
    }
}
```

**Configuration (application.conf):**
```hocon
play.modules.enabled += "StartupModule"
play.application.loader = "CustomApplicationLoader"
```

### 3. Filter API Updates

#### Pattern: Request Filtering

**BEFORE (Play 2.4):**
```scala
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class CustomFilter extends Filter {
  def apply(next: RequestHeader => Future[Result])
           (request: RequestHeader): Future[Result] = {
    // Pre-processing
    next(request).map { result =>
      // Post-processing
      result
    }
  }
}
```

**AFTER (Play 2.9):**
```scala
import play.api.mvc._
import scala.concurrent.{Future, ExecutionContext}
import javax.inject.Inject

class CustomFilter @Inject()(implicit ec: ExecutionContext) extends Filter {
  override implicit val mat: Materializer = ???
  
  def apply(next: RequestHeader => Future[Result])
           (request: RequestHeader): Future[Result] = {
    // Pre-processing
    next(request).map { result =>
      // Post-processing
      result
    }
  }
}
```

### 4. JSON Parsing Changes

**BEFORE (Play 2.4):**
```java
JsonNode json = request().body().asJson();
if (json == null) {
    return badRequest("Expecting JSON");
}
```

**AFTER (Play 2.9):**
```java
Optional<JsonNode> jsonOptional = request().body().asJson();
if (!jsonOptional.isPresent()) {
    return badRequest("Expecting JSON");
}
JsonNode json = jsonOptional.get();
```

### 5. Form API Changes

**BEFORE (Play 2.4):**
```java
Form<User> userForm = Form.form(User.class);
Form<User> filledForm = userForm.bindFromRequest();
```

**AFTER (Play 2.9):**
```java
Form<User> userForm = formFactory.form(User.class);
Form<User> filledForm = userForm.bindFromRequest(request);
```

---

## Configuration Changes

### Application Configuration (conf/application.conf)

#### Akka → Pekko Blocks

**BEFORE:**
```hocon
akka {
  loggers = ["akka.event.Logging$DefaultLogger"]
  log-config-on-start = true
  
  actor {
    default-dispatcher {
      fork-join-executor {
        parallelism-min = 8
        parallelism-max = 64
      }
    }
  }
}
```

**AFTER:**
```hocon
pekko {
  loggers = ["org.apache.pekko.event.Logging$DefaultLogger"]
  log-config-on-start = true
  
  actor {
    default-dispatcher {
      fork-join-executor {
        parallelism-min = 8
        parallelism-max = 64
      }
    }
  }
}
```

#### Play Configuration Updates

**BEFORE (Play 2.4):**
```hocon
play.crypto.secret = "changeme"
application.global = Global
```

**AFTER (Play 2.9):**
```hocon
play.http.secret.key = "changeme"
play.application.loader = "CustomApplicationLoader"
```

### Deployment Configuration Template

**File:** `devops/roles/dial-service-deploy/templates/dial-service.conf.j2`

**Find and replace:**
```bash
# Replace akka with pekko
sed -i 's/## Akka/## Pekko/g' dial-service.conf.j2
sed -i 's/akka {/pekko {/g' dial-service.conf.j2
sed -i 's/akka\./pekko./g' dial-service.conf.j2
```

---

## Common Pitfalls

### 1. Binary Compatibility Issues

❌ **Wrong:** Mixing Scala 2.11 and 2.13 dependencies
```xml
<dependency>
    <groupId>org.apache.pekko</groupId>
    <artifactId>pekko-actor_2.11</artifactId> <!-- Wrong! -->
</dependency>
```

✅ **Correct:** Use consistent Scala version
```xml
<dependency>
    <groupId>org.apache.pekko</groupId>
    <artifactId>pekko-actor_2.13</artifactId> <!-- Correct! -->
</dependency>
```

### 2. Transitive Dependencies

❌ **Wrong:** Not excluding Akka from transitive dependencies
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>some-library</artifactId>
    <!-- This might bring in Akka transitively -->
</dependency>
```

✅ **Correct:** Explicitly exclude Akka
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>some-library</artifactId>
    <exclusions>
        <exclusion>
            <groupId>com.typesafe.akka</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 3. Context in Async Operations

❌ **Wrong:** Losing context in Play 2.9
```java
public CompletionStage<Result> action() {
    return supplyAsync(() -> {
        // request() not available here!
        String value = request().getQueryString("param");
        return ok(value);
    });
}
```

✅ **Correct:** Capture context before async
```java
public CompletionStage<Result> action(Http.Request request) {
    String value = request.getQueryString("param");
    return supplyAsync(() -> {
        return ok(value);
    });
}
```

### 4. Execution Context

❌ **Wrong:** Using deprecated execution context
```java
import play.libs.concurrent.Execution;
// Execution.defaultContext() is deprecated
```

✅ **Correct:** Inject execution context
```java
import javax.inject.Inject;
import java.util.concurrent.Executor;

@Inject
private Executor executor;

public CompletionStage<Result> action() {
    return supplyAsync(() -> ok("Done"), executor);
}
```

### 5. Compile-time vs Runtime Issues

⚠️ **Watch out:** Code may compile but fail at runtime
- Always test thoroughly after migration
- Check for ClassNotFoundException
- Verify all Akka references removed from classpath

---

## Testing Strategy

### Unit Tests
```java
// Update test helpers
// BEFORE
import play.test.Helpers;
import play.libs.F.Promise;

// AFTER
import play.test.Helpers;
import java.util.concurrent.CompletionStage;
```

### Integration Tests
```java
// Test async actions
CompletionStage<Result> result = controller.asyncAction(fakeRequest);
Result finalResult = result.toCompletableFuture().get(5, TimeUnit.SECONDS);
assertEquals(OK, finalResult.status());
```

---

## Verification Checklist

After migration, verify:

- [ ] No Akka dependencies in `mvn dependency:tree`
- [ ] All imports use `org.apache.pekko.*`
- [ ] Configuration uses `pekko { }` blocks
- [ ] No `play.libs.F.Promise` imports
- [ ] GlobalSettings removed
- [ ] All tests passing
- [ ] Application starts successfully
- [ ] No ClassNotFoundException in logs
- [ ] All endpoints responding correctly
- [ ] Performance benchmarks maintained

---

## Tools & Scripts

### Check for Akka References
```bash
# Find Akka imports in code
find . -name "*.java" -o -name "*.scala" | xargs grep "import akka" | grep -v "/target/"

# Find Akka in config files
find . -name "*.conf" -o -name "*.hocon" | xargs grep "akka {"

# Check Maven dependencies
mvn dependency:tree | grep akka
```

### Automated Replacement Script
```bash
#!/bin/bash
# Use with EXTREME CAUTION - review all changes

# Replace imports
find ./app -name "*.java" | xargs sed -i 's/import akka\./import org.apache.pekko./g'

# Replace config
find ./conf -name "*.conf" | xargs sed -i 's/akka {/pekko {/g'
find ./conf -name "*.conf" | xargs sed -i 's/akka\./pekko./g'

echo "Replacements complete - REVIEW ALL CHANGES before committing!"
```

---

## Resources

### Documentation
- Play 2.9 Migration Guide: https://www.playframework.com/documentation/2.9.x/Migration29
- Pekko Documentation: https://pekko.apache.org/docs/pekko/current/
- Pekko Migration Guide: https://pekko.apache.org/docs/pekko/current/project/migration-guides.html

### Community
- Play Framework Discussions: https://github.com/playframework/playframework/discussions
- Stack Overflow: Tag `play-framework`, `apache-pekko`

### Tools
- IntelliJ IDEA: Play Framework plugin
- VS Code: Scala Metals plugin
- Maven Helper: Dependency analysis plugin

---

## Quick Command Reference

```bash
# Build with Maven
mvn clean install

# Run tests
mvn test

# Check dependencies
mvn dependency:tree

# Run application
mvn play2:run

# Create distribution
mvn play2:dist

# Check for security vulnerabilities
mvn org.owasp:dependency-check-maven:check
```

---

**Document Version:** 1.0  
**Last Updated:** 2025-10-10  
**For Full Details:** See `PLAY_PEKKO_MIGRATION_REPORT.md`
