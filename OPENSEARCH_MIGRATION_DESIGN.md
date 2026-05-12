# Design Document: OpenSearch Migration for sunbird-dial-service

## Table of Contents

1. [Abstract](#1-abstract)
2. [Background](#2-background)
3. [Problem Statement](#3-problem-statement)
4. [Goals](#4-goals)
5. [System Context](#5-system-context)
6. [Proposed Solution](#6-proposed-solution)
7. [Detailed Implementation Plan](#7-detailed-implementation-plan)
8. [Testing Strategy](#8-testing-strategy)
9. [Rollout Plan](#9-rollout-plan)

---

## 1. Abstract

This document describes the migration of `sunbird-dial-service` — the DIAL code generation, management, and linking service — from Elasticsearch 7.10.2 to OpenSearch 2.19.5. The migration replaces the Java client library (`elasticsearch-rest-high-level-client` 7.10.2 → `opensearch-rest-high-level-client` 2.19.5) and updates associated Docker and VM setup infrastructure. The change aligns dial-service with the broader platform migration to OpenSearch and unblocks adoption of native vector search capabilities for semantic DIAL code discovery.

---

## 2. Background

### 2.1 Role of sunbird-dial-service in Content Linking

`sunbird-dial-service` operates as the **write and read side** of DIAL code indexing. The service:

1. **Generates** QR codes and DIAL (Dynamic Indexed Addressable Links) identifiers for learning content
2. **Stores** metadata (content associations, contextual attributes, linkages) in two Elasticsearch indices: `dialcode` and `dialcodemetrics`
3. **Serves** DIAL code resolution queries from Portal, Mobile, and upstream services
4. **Indexes** content-to-DIAL code mappings asynchronously via Kafka events

The service is a Play 2.3 application (Scala/Java) running on port 8088. All search operations funnel through `ElasticSearchUtil.java`, a 790-line client abstraction using `RestHighLevelClient` from the Elasticsearch library.

| Class | Index | Operations | Direction |
|---|---|---|---|
| `SearchController` | `dialcode`, `dialcodemetrics` | `INDEX_SEARCH`, `COUNT`, `SEARCH_RESULT_BY_PROPERTY_ID` | Read |
| `SearchProcessor` | `dialcode`, `dialcodemetrics` | `search`, `searchAsync`, `count`, `aggregation` | Read |
| `ElasticSearchUtil` | `dialcode`, `dialcodemetrics` | Full CRUD, bulk index/delete, search, mget, index creation | Read + Write |

### 2.2 Current Technology Stack

- **Search server:** Elasticsearch 7.10.2 (SSPL-licensed, frozen OSS fork)
- **Java client:** `org.elasticsearch.client:elasticsearch-rest-high-level-client:7.10.2`
- **Service runtime:** Play Framework 2.3.0 on Java 8, Scala 2.11.11
- **Client abstraction:** `ElasticSearchUtil.java` (~790 lines) and `SearchProcessor.java` (~450 lines)
- **Indices accessed:** `dialcode`, `dialcodemetrics`
- **Event stream:** Kafka (content-to-dialcode mapping updates)

### 2.3 Why Elasticsearch 7.10.2 is a Dead End

Elasticsearch 7.10.2 is the last Apache 2.0–licensed release. It receives no patches, security updates, or new features. It predates k-NN vector search and lacks support for semantic DIAL code discovery. OpenSearch 2.x provides:

- Native k-NN with HNSW/IVF algorithms (`knn_vector` field type)
- Hybrid BM25 + vector query DSL
- Active security patching
- Apache 2.0 licensing

---

## 3. Problem Statement

The platform is migrating from Elasticsearch 7.10.2 to OpenSearch 2.19.5 across all services to:

1. Unblock semantic search via k-NN vector embeddings
2. Adopt a modern, actively maintained search server with security patches
3. Consolidate on a single search infrastructure (Kubernetes cluster running OpenSearch)

`sunbird-dial-service` currently cannot participate because its Java client library (`elasticsearch-rest-high-level-client`) is bound to the deprecated Elasticsearch package namespace and API.

---

## 4. Goals

- Replace `elasticsearch-rest-high-level-client` 7.10.2 with `opensearch-rest-high-level-client` 2.19.5 in the dial-service codebase
- Ensure all existing DIAL code search, count, and aggregation operations continue to function correctly against OpenSearch 2.19.5
- Update Docker development environment to run OpenSearch 2.19.5
- Update `vmsetup.sh` to install OpenSearch instead of Elasticsearch
- Produce a clean compilation and passing test suite post-migration
- Unblock future phase where `SearchProcessor` issues hybrid BM25 + vector queries for semantic DIAL discovery

---

## 5. System Context

```
  HTTP Consumer (Portal / Mobile / Service)
              │
              │ POST /search, GET /search/{id}
              ▼
  ┌───────────────────────────────────────────────┐
  │      sunbird-dial-service (Play 2.3)          │
  │                                               │
  │  ┌─────────────────────────────────────────┐  │
  │  │      SearchController / Routes           │  │
  │  └──────────────────┬──────────────────────┘  │
  │                     │                          │
  │  ┌──────────────────▼──────────────────────┐  │
  │  │      SearchProcessor                    │  │
  │  │  BoolQueryBuilder / TermsAggregation    │  │
  │  └──────────────────┬──────────────────────┘  │
  │                     │                          │
  │  ┌──────────────────▼──────────────────────┐  │
  │  │      ElasticSearchUtil                  │  │
  │  │      RestHighLevelClient                │  │
  │  └──────────────────┬──────────────────────┘  │
  └─────────────────────┼─────────────────────────┘
                        │ REST / port 9200
            ┌───────────▼───────────┐
            │     Search Server      │
            │  ES 7.10.2 → OS 2.19.5 │
            └───────────────────────┘
                        ▲
                        │
            ┌───────────┴───────────┐
            │   Kafka Events        │
            │ (content → dialcode)  │
            └───────────────────────┘
```

Kafka producers upstream (content indexing services) emit events that trigger dial-service to index or update DIAL code metadata via `ElasticSearchUtil.indexDocument()`.

---

## 6. Proposed Solution

### 6.1 Approach

OpenSearch forked Elasticsearch at 7.10.2 and preserved the Java client API surface under the `org.opensearch.*` package namespace. The migration is a **mechanical package rename** at the import level combined with a Maven dependency swap. No business logic, query construction, index schema, or configuration changes are required.

### 6.2 Key Properties of the Target Client

| Property | Detail |
|---|---|
| Artifact | `org.opensearch.client:opensearch-rest-high-level-client:2.19.5` |
| Java compatibility | 8, 11, 17, 21 |
| HTTP layer | Apache HttpClient (identical to ES 7.x client) |
| API parity with ES 7.10.2 | Full — same class names, same method signatures, different package prefix |

### 6.3 What Does Not Change

- `ElasticSearchUtil.java` business logic (all search, CRUD, bulk, aggregation methods)
- `SearchProcessor.java` query DSL construction (BoolQuery, TermsAggregation, sorting)
- Index mappings and custom analyzer settings
- Application configuration keys (`search.es_conn_info`, port 9200)
- Play 2.3 controller routing and async pipelines
- Kafka event processing and index updates
- Scala/Java interop and Futures/Promises chains

---

## 7. Detailed Implementation Plan

### 7.1 Maven Dependency — `pom.xml`

**Location:** lines 240–252

```xml
<!-- Remove -->
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>7.10.2</version>
    <exclusions>
        <exclusion>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-cbor</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- Add -->
<dependency>
    <groupId>org.opensearch.client</groupId>
    <artifactId>opensearch-rest-high-level-client</artifactId>
    <version>2.19.5</version>
    <exclusions>
        <exclusion>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-cbor</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

Preserve exclusions to avoid version conflicts with Jackson already declared.

---

### 7.2 Import Updates — `ElasticSearchUtil.java`

**File:** `app/elasticsearch/ElasticSearchUtil.java`  
**Lines:** 14–45

Replace every `org.elasticsearch.*` import with `org.opensearch.*`:

```java
// Before
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

// After
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.*;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.action.ActionListener;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.get.MultiGetItemResponse;
import org.opensearch.action.get.MultiGetRequest;
import org.opensearch.action.get.MultiGetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
```

No changes to any method body.

---

### 7.3 Import Updates — `SearchProcessor.java`

**File:** `app/elasticsearch/SearchProcessor.java`  
**Lines:** 8–25

```java
// Before
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

// After
import org.opensearch.action.search.SearchResponse;
import org.opensearch.common.lucene.search.function.CombineFunction;
import org.opensearch.common.lucene.search.function.FunctionScoreQuery.ScoreMode;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder.Type;
import org.opensearch.index.query.Operator;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.opensearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.opensearch.index.query.functionscore.ScoreFunctionBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
```

No changes to any method body.

---

### 7.4 Test Files — No Direct ES Imports

All test files (`ElasticSearchUtilTest.java`, `SearchProcessorTest.java`, `SearchControllerTest.java`) import only `app.elasticsearch.ElasticSearchUtil` — not `org.elasticsearch.*` directly. Once `ElasticSearchUtil.java` is updated, all tests recompile without further changes.

---

### 7.5 Docker Environment — `docker/docker-compose.yml`

**Location:** lines 39–48

```yaml
# Before
elasticsearch:
  image: docker.elastic.co/elasticsearch/elasticsearch:7.10.2
  container_name: sunbird_es
  ports:
    - "9200:9200"
    - "9300:9300"
  environment:
    - discovery.type=single-node
  volumes:
    - es-data:/usr/share/elasticsearch/data

# After
elasticsearch:
  image: opensearchproject/opensearch:2.19.5
  container_name: sunbird_es
  ports:
    - "9200:9200"
    - "9600:9600"
  environment:
    - discovery.type=single-node
    - plugins.security.disabled=true
  volumes:
    - es-data:/usr/share/opensearch/data
```

Notes:
- Service name `elasticsearch` retained to minimize config changes
- Port `9300` replaced with `9600` (OpenSearch Performance Analyzer)
- Security plugin disabled via environment variable

---

### 7.6 VM Setup Script — `vmsetup.sh`

**Location:** lines 4–7

```bash
# Before
curl -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.10.2-amd64.deb
sudo dpkg -i elasticsearch-7.10.2-amd64.deb
sudo service elasticsearch start

# After
curl -O https://artifacts.opensearch.org/releases/bundle/opensearch/2.19.5/opensearch-2.19.5-linux-x64.deb
sudo dpkg -i opensearch-2.19.5-linux-x64.deb
sudo systemctl start opensearch
```

---

### 7.7 Configuration Files — No Changes

All `application.conf` files reference `search.es_conn_info` in format `host:port`. This format is identical for OpenSearch. No changes needed.

---

### 7.8 Summary of Changed Files

| File | Change Type | Scope |
|---|---|---|
| `pom.xml` | Dependency swap | 4 lines (groupId + artifactId + version) |
| `app/elasticsearch/ElasticSearchUtil.java` | Package rename (imports only) | 32 import lines |
| `app/elasticsearch/SearchProcessor.java` | Package rename (imports only) | 18 import lines |
| `docker/docker-compose.yml` | Image, ports, env vars, volume path | ~5 lines |
| `vmsetup.sh` | Install commands | 3 lines |

No changes to any production method body or business logic.

---

## 8. Testing Strategy

### 8.1 Compilation Verification

```bash
mvn clean install -DskipTests
```

All modules must compile with zero errors.

### 8.2 Unit Tests

```bash
# Start Docker first
docker compose up -d elasticsearch
./docker/init-elasticsearch.sh

# Run tests
mvn test
```

### 8.3 Smoke Tests (Local Docker)

```bash
# Verify OpenSearch health
curl http://localhost:9200/_cluster/health

# Verify indices created
curl http://localhost:9200/dialcode/_stats | python3 -m json.tool
curl http://localhost:9200/dialcodemetrics/_stats | python3 -m json.tool

# Verify service health
curl http://localhost:8088/health
```

### 8.4 Functional Regression Checklist

- [ ] DIAL code search returns correct results
- [ ] Count queries return accurate document counts
- [ ] Aggregations on `status`, `dialCodeCount` return expected buckets
- [ ] Bulk index operations complete without errors
- [ ] Delete operations remove documents correctly
- [ ] Nested queries on content associations work correctly
- [ ] Range queries on timestamps (`createdOn`, `lastUpdatedOn`) return expected date ranges
- [ ] Kafka-triggered indexing updates DIAL code metadata in OpenSearch

---

## 9. Rollout Plan

### Phase 1 — Local Development

- Apply all code changes listed in section 7
- Update Docker environment to OpenSearch 2.19.5
- Verify full project build: `mvn clean install -DskipTests`
- Run unit and integration test suites
- Run smoke test curl commands

### Phase 2 — Staging

- Deploy OpenSearch 2.19.5 to staging Kubernetes
- Restore DIAL code indices from ES 7.10.2 snapshot
- Deploy updated `dial-service` build to staging
- Validate document counts and sample queries match pre-migration baseline
- Monitor error rates for 24 hours

### Phase 3 — Production

- Take snapshot of production ES 7.10.2 `dialcode` and `dialcodemetrics` indices
- Stand up OpenSearch 2.19.5 in production
- Restore snapshot from ES 7.10.2
- Validate document counts and spot-check queries
- Cut over `dial-service` to point to OpenSearch
- Monitor search error rate and latency for 24 hours
- Decommission ES 7.10.2

### Phase 4 — Follow-up (Future)

- Rename K8s service from `elasticsearch` to `opensearch`
- Migrate `ElasticSearchUtil.java` from `RestHighLevelClient` to `opensearch-java` typed client
- Add `knn_vector` field to `dialcode` index mapping for embedding storage
- Extend `SearchProcessor` to issue hybrid BM25 + k-NN queries for semantic DIAL discovery

---

## Appendix: Files Summary

**Changed:**
- `pom.xml` — dependency swap
- `app/elasticsearch/ElasticSearchUtil.java` — import rename
- `app/elasticsearch/SearchProcessor.java` — import rename
- `docker/docker-compose.yml` — image, config, volume path
- `vmsetup.sh` — install commands

**Unchanged:**
- All `conf/application.conf` files
- All test files
- `app/controllers/SearchController.scala`
- Index mappings and settings
- Scala/Java business logic
