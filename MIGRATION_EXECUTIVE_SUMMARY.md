# Executive Summary: Play Framework & Pekko Migration

## Quick Overview

**Project:** Sunbird DIAL Service  
**Current:** Play 2.4.6 + Akka 2.4.x (EOL since 2016)  
**Target:** Play 2.9.x + Apache Pekko 1.0.x  
**Timeline:** 3-5 months  
**Team Size:** 2-3 developers + 1 QA  

---

## Why This Migration is Needed

### 1. Akka License Change ⚠️
- **Old:** Apache 2.0 (Open Source)
- **New:** BSL 1.1 (Commercial/Paid in production)
- **Solution:** Migrate to Apache Pekko (open-source fork)

### 2. Security & Support 🔒
- Play 2.4.6 is End-of-Life (since 2016)
- No security patches or bug fixes
- Multiple known CVEs

### 3. Compliance 📋
- License audit requirements
- Open source compliance
- Avoiding commercial dependencies

---

## Current System Analysis

### Akka Usage (Good News! ✅)
- **Minimal usage:** Only 3 files affected
- **No actor systems:** Just utility imports
- **Easy migration:** Package name changes only

```
Files with Akka imports:
1. app/managers/DialcodeManager.java (akka.util.Timeout)
2. app/elasticsearch/ElasticSearchUtil.java (akka.dispatch.Futures)  
3. app/elasticsearch/SearchProcessor.java (akka.dispatch.Mapper)
```

### Play Framework Usage (Main Challenge ⚠️)
- Current: Play 2.4.6 (July 2016)
- Uses deprecated APIs:
  - `GlobalSettings` → needs DI migration
  - `F.Promise` → needs CompletionStage migration
  - ThreadLocal context → needs explicit passing

---

## Migration Strategy

### Recommended Approach: **Incremental** (Lower Risk)

```
Play 2.4.6 + Akka 2.4.x (Current)
    ↓ Upgrade (2 weeks)
Play 2.5.x + Akka 2.5
    ↓ Upgrade + API changes (2-3 weeks)
Play 2.6.x + Akka 2.5
    ↓ Upgrade (2 weeks)
Play 2.7.x + Akka 2.6
    ↓ Upgrade (1-2 weeks)
Play 2.8.x + Akka 2.6
    ↓ Migrate to Pekko (2-3 weeks)
Play 2.9.x + Pekko 1.0.x (Target)
```

**Total:** 15-22 weeks (3.5-5 months)

### Alternative: **Direct Jump** (Higher Risk, Faster)

```
Play 2.4.6 + Akka 2.4.x → Play 2.9.x + Pekko 1.0.x
```

**Total:** 12-15 weeks (3 months)  
⚠️ Only recommended for experienced teams

---

## Key Changes Required

### 1. Akka → Pekko (EASY ✅)
```diff
- import akka.util.Timeout;
+ import org.apache.pekko.util.Timeout;
```
**Effort:** 1-2 days

### 2. Global.java Rewrite (HARD 🔴)
- Remove `GlobalSettings` (deprecated)
- Implement `ApplicationLoader` with DI
- Migrate request lifecycle hooks

**Effort:** 5-8 days

### 3. Promise → CompletionStage (MEDIUM 🟡)
```java
// OLD
Promise<Result> result = Promise.promise(() -> work());

// NEW  
CompletionStage<Result> result = supplyAsync(() -> work());
```
**Effort:** 8-10 days

### 4. Configuration Updates (EASY ✅)
```hocon
# OLD
akka { ... }

# NEW
pekko { ... }
```
**Effort:** 1 day

---

## Effort Breakdown

| Phase | Duration | Developer Days |
|-------|----------|----------------|
| Analysis & Planning | 2 weeks | 15 |
| Play Incremental Upgrades | 6-8 weeks | 46 |
| Pekko Migration | 2-3 weeks | 13 |
| Build System (if needed) | 2-4 weeks | 15 |
| Testing & Validation | 2-3 weeks | 23 |
| Deployment & Support | 1-2 weeks | 10 |
| **TOTAL** | **15-22 weeks** | **124-134 days** |

---

## Benefits

### Technical ✅
- ✅ Avoid Akka commercial license
- ✅ Security patches & updates
- ✅ Modern async patterns (Java 17/21)
- ✅ Better performance
- ✅ HTTP/2 support

### Business 💰
- ✅ Cost savings (no Akka license fees)
- ✅ Open source compliance
- ✅ Future-proof codebase
- ✅ Easier talent acquisition

### Risk Reduction 🛡️
- ✅ Supported framework
- ✅ Active community
- ✅ Apache Foundation backing

---

## Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Breaking API changes | HIGH | Incremental upgrades, extensive testing |
| Maven plugin support | MEDIUM | Research early, SBT fallback plan |
| Timeline overrun | MEDIUM | Buffer time, experienced team |
| Production issues | HIGH | Blue-green deployment, rollback plan |
| Skill gaps | MEDIUM | Training, documentation |

---

## Cost Estimate

### Team Composition
- 2 Senior Developers (Play/Scala experience)
- 1 DevOps Engineer
- 1 QA Engineer
- 1 Technical Lead (part-time oversight)

### Timeline Options

**Option A: Incremental (Recommended)**
- Duration: 4-5 months
- Risk: Lower
- Team: 2-3 developers
- Cost: ~$80K-120K (rough estimate)

**Option B: Direct Jump**
- Duration: 3 months  
- Risk: Higher
- Team: 3 experienced developers
- Cost: ~$60K-90K (rough estimate)

---

## Decision Matrix

| Factor | Score (1-5) | Notes |
|--------|-------------|-------|
| Business Value | 5 | License compliance, security |
| Technical Feasibility | 4 | Achievable but requires effort |
| Risk Level | 3 | Manageable with proper planning |
| Resource Availability | 3 | Need 2-3 dedicated developers |
| Urgency | 4 | Play 2.4.6 EOL, security concerns |
| **Overall** | **3.8/5** | **RECOMMENDED TO PROCEED** |

---

## Recommendations

### Immediate (Next 2 Weeks) 📋
1. ✅ Approve migration project
2. 🔲 Allocate team resources
3. 🔲 Set up test environment
4. 🔲 Improve test coverage (target: 80%+)
5. 🔲 Research Maven plugin support
6. 🔲 Create detailed migration backlog

### Short-term (Next 3 Months) 🚀
1. 🔲 Start incremental Play upgrades
2. 🔲 Migrate GlobalSettings to DI
3. 🔲 Convert Promise to CompletionStage
4. 🔲 Comprehensive testing at each step

### Long-term (Post-Migration) 📈
1. 🔲 Establish quarterly dependency reviews
2. 🔲 Implement automated security scanning
3. 🔲 Document lessons learned
4. 🔲 Train team on modern patterns

---

## Success Criteria

### Must Have ✅
- [ ] All Akka dependencies replaced with Pekko
- [ ] Play Framework upgraded to 2.9.x
- [ ] All tests passing
- [ ] No security vulnerabilities introduced
- [ ] Performance maintained or improved

### Nice to Have ⭐
- [ ] Build system migrated to SBT (if needed)
- [ ] Test coverage improved (>80%)
- [ ] Documentation updated
- [ ] CI/CD pipeline modernized

---

## Next Steps

### For Management
1. **Review this summary** and full report
2. **Approve project** and allocate budget
3. **Assign team** (2-3 developers + QA)
4. **Set timeline expectations** (3-5 months)

### For Technical Team
1. **Read full report:** `PLAY_PEKKO_MIGRATION_REPORT.md`
2. **Set up development environment** for experimentation
3. **Create test suite** enhancement plan
4. **Research Maven plugin** compatibility
5. **Create detailed Jira/GitHub** issues

### For Stakeholders
1. **Understand business impact** (license, security, cost)
2. **Plan for feature freeze** during migration
3. **Prepare deployment strategy** (staging, blue-green)
4. **Communicate timeline** to customers

---

## Questions & Answers

### Q: Why can't we just update Akka?
**A:** Akka 2.7+ requires commercial license ($$$) for production use. Pekko is the open-source alternative.

### Q: Can we skip Play upgrade and just migrate to Pekko?
**A:** No. Play 2.4-2.8 internally use Akka. Only Play 2.9+ supports Pekko.

### Q: What if we do nothing?
**A:** 
- Running EOL software (security risk)
- Potential license compliance issues
- Harder to maintain and hire for
- Accumulating technical debt

### Q: What's the fastest path?
**A:** Direct jump to Play 2.9 + Pekko (3 months), but higher risk.

### Q: What's the safest path?
**A:** Incremental upgrades through Play 2.5, 2.6, 2.7, 2.8, then 2.9 (4-5 months).

### Q: Do we need to migrate to SBT?
**A:** Maybe. Maven support for Play 2.8+ is uncertain. We'll know after Phase 1.

### Q: What breaks during migration?
**A:** Main breakages:
- GlobalSettings → ApplicationLoader (rewrite)
- F.Promise → CompletionStage (refactor async code)
- Filter API updates
- Configuration changes

### Q: Can we do this in parallel with feature work?
**A:** Not recommended. Focus on migration for 3-5 months, then resume features.

---

## Conclusion

### Should We Proceed? **YES ✅**

**Reasons:**
1. ✅ Akka license change makes it necessary
2. ✅ Security (Play 2.4.6 is EOL)
3. ✅ Low Akka usage makes Pekko migration easy
4. ✅ Benefits outweigh costs
5. ✅ Manageable timeline (3-5 months)

**Recommended Approach:**
- Use **incremental migration** (lower risk)
- Allocate **2-3 experienced developers**
- Plan for **4-5 months**
- Start immediately

---

## Contact & Resources

**For Questions:**
- Technical Lead: [TBD]
- Project Manager: [TBD]

**Resources:**
- Full Report: `PLAY_PEKKO_MIGRATION_REPORT.md`
- Play 2.9 Docs: https://www.playframework.com/documentation/2.9.x/
- Apache Pekko: https://pekko.apache.org/

---

**Document Version:** 1.0  
**Date:** 2025-10-10  
**Status:** Draft for Approval  
**Next Review:** [TBD after stakeholder review]
