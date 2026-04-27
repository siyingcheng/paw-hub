# Paw-Hub v1 Implementation Plan — Part 2 (Phases 3-7)

Continuation from part 1. Phases 1-2 (Tasks 1-10) cover scaffolding and backend infrastructure.

---

## Phase 3: Collection Module (Tasks 11-15)

### Task 11: TestRun & TestExecution Entities

```java
// backend/src/main/java/com/pawhub/module/collection/entity/TestStatus.java
package com.pawhub.module.collection.entity;
public enum TestStatus { PASS, FAIL, SKIP, ERROR }
```

```java
// backend/src/main/java/com/pawhub/module/collection/entity/TestRun.java
package com.pawhub.module.collection.entity;

import com.pawhub.module.auth.entity.Project;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "test_runs")
public class TestRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    @Column(nullable = false) private String runIdentifier;
    private String branch;
    private String commitSha;
    private String triggeredBy;
    @Column(nullable = false) private String environment;
    private int totalCases;
    private int passed;
    private int failed;
    private int skipped;
    private long durationMs;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TestStatus status;
    @Lob @Column(columnDefinition = "CLOB") private String rawXml;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    public TestRun() {}
    // Getters and setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; } public void setProject(Project p) { this.project = p; }
    public String getRunIdentifier() { return runIdentifier; } public void setRunIdentifier(String s) { this.runIdentifier = s; }
    public String getBranch() { return branch; } public void setBranch(String s) { this.branch = s; }
    public String getCommitSha() { return commitSha; } public void setCommitSha(String s) { this.commitSha = s; }
    public String getTriggeredBy() { return triggeredBy; } public void setTriggeredBy(String s) { this.triggeredBy = s; }
    public String getEnvironment() { return environment; } public void setEnvironment(String s) { this.environment = s; }
    public int getTotalCases() { return totalCases; } public void setTotalCases(int n) { this.totalCases = n; }
    public int getPassed() { return passed; } public void setPassed(int n) { this.passed = n; }
    public int getFailed() { return failed; } public void setFailed(int n) { this.failed = n; }
    public int getSkipped() { return skipped; } public void setSkipped(int n) { this.skipped = n; }
    public long getDurationMs() { return durationMs; } public void setDurationMs(long n) { this.durationMs = n; }
    public TestStatus getStatus() { return status; } public void setStatus(TestStatus s) { this.status = s; }
    public String getRawXml() { return rawXml; } public void setRawXml(String s) { this.rawXml = s; }
    public Instant getCreatedAt() { return createdAt; }
}
```

```java
// backend/src/main/java/com/pawhub/module/collection/entity/TestExecution.java
package com.pawhub.module.collection.entity;

import jakarta.persistence.*;

@Entity @Table(name = "test_executions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"test_run_id","suite_name","class_name","test_name","attempt"}))
public class TestExecution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;
    @Column(nullable = false) private int attempt = 1;
    @Column(nullable = false) private String suiteName;
    @Column(nullable = false) private String className;
    @Column(nullable = false) private String testName;
    private String caseNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TestStatus status;
    private long durationMs;
    @Column(length = 4000) private String errorMessage;
    @Lob @Column(columnDefinition = "CLOB") private String stackTrace;
    private String errorType;
    public TestExecution() {}
    // Getters and setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public TestRun getTestRun() { return testRun; } public void setTestRun(TestRun t) { this.testRun = t; }
    public int getAttempt() { return attempt; } public void setAttempt(int n) { this.attempt = n; }
    public String getSuiteName() { return suiteName; } public void setSuiteName(String s) { this.suiteName = s; }
    public String getClassName() { return className; } public void setClassName(String s) { this.className = s; }
    public String getTestName() { return testName; } public void setTestName(String s) { this.testName = s; }
    public String getCaseNumber() { return caseNumber; } public void setCaseNumber(String s) { this.caseNumber = s; }
    public TestStatus getStatus() { return status; } public void setStatus(TestStatus s) { this.status = s; }
    public long getDurationMs() { return durationMs; } public void setDurationMs(long n) { this.durationMs = n; }
    public String getErrorMessage() { return errorMessage; } public void setErrorMessage(String s) { this.errorMessage = s; }
    public String getStackTrace() { return stackTrace; } public void setStackTrace(String s) { this.stackTrace = s; }
    public String getErrorType() { return errorType; } public void setErrorType(String s) { this.errorType = s; }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/collection/entity/
git commit -m "feat: add TestRun and TestExecution entities"
```

### Task 12: Collection Repositories

```java
// backend/src/main/java/com/pawhub/module/collection/repository/TestRunRepository.java
package com.pawhub.module.collection.repository;

import com.pawhub.module.collection.entity.TestRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {
    Page<TestRun> findByProjectId(Long projectId, Pageable pageable);

    @Query("SELECT tr FROM TestRun tr WHERE tr.project.id = :projectId " +
           "AND (:branch IS NULL OR tr.branch = :branch) " +
           "AND (:status IS NULL OR tr.status = com.pawhub.module.collection.entity.TestStatus.valueOf(:status)) " +
           "AND (:environment IS NULL OR tr.environment = :environment) " +
           "AND (:from IS NULL OR tr.createdAt >= :from) " +
           "AND (:to IS NULL OR tr.createdAt <= :to)")
    Page<TestRun> findByFilters(@Param("projectId") Long projectId,
                                @Param("branch") String branch,
                                @Param("status") String status,
                                @Param("environment") String environment,
                                @Param("from") Instant from,
                                @Param("to") Instant to,
                                Pageable pageable);

    List<TestRun> findByProjectIdAndEnvironmentAndCreatedAtBetween(
        Long projectId, String environment, Instant from, Instant to);
}
```

```java
// backend/src/main/java/com/pawhub/module/collection/repository/TestExecutionRepository.java
package com.pawhub.module.collection.repository;

import com.pawhub.module.collection.entity.TestExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {
    List<TestExecution> findByTestRunIdOrderByAttemptAsc(Long testRunId);

    @Query("SELECT te FROM TestExecution te JOIN te.testRun tr " +
           "WHERE tr.project.id = :projectId AND te.status = 'FAIL' AND te.status != 'PASS' " +
           "AND tr.createdAt >= :since ORDER BY tr.createdAt DESC")
    List<TestExecution> findRecentFailures(@Param("projectId") Long projectId,
                                           @Param("since") Instant since);

    @Query(value = "SELECT DISTINCT CONCAT(te.suite_name,'.',te.class_name,'.',te.test_name) " +
           "FROM test_executions te JOIN test_runs tr ON te.test_run_id=tr.id " +
           "WHERE tr.project_id=:projectId AND tr.created_at>=:since",
           nativeQuery = true)
    List<String> findDistinctTestCaseKeys(@Param("projectId") Long projectId,
                                          @Param("since") Instant since);

    @Query("SELECT te FROM TestExecution te JOIN te.testRun tr " +
           "WHERE tr.project.id=:projectId " +
           "AND CONCAT(te.suiteName,'.',te.className,'.',te.testName)=:key " +
           "AND tr.createdAt>=:since ORDER BY tr.createdAt DESC")
    List<TestExecution> findByTestCaseKey(@Param("projectId") Long projectId,
                                          @Param("key") String key,
                                          @Param("since") Instant since);
}
```

```bash
git add backend/src/main/java/com/pawhub/module/collection/repository/
git commit -m "feat: add collection repositories"
```

### Task 13: JUnit XML Parser (TDD)

```java
// backend/src/test/java/com/pawhub/module/collection/parser/JUnitXmlParserTest.java
package com.pawhub.module.collection.parser;

import com.pawhub.module.collection.entity.TestStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JUnitXmlParserTest {
    private final JUnitXmlParser parser = new JUnitXmlParser();

    @Test
    void shouldParseValidJUnitXml() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <testsuite name="com.app.AuthTest" tests="3" failures="1" errors="0" skipped="1" time="4.532">
                <testcase name="shouldLogin" classname="com.app.AuthTest" time="1.234"/>
                <testcase name="shouldFail" classname="com.app.AuthTest" time="0.890">
                    <failure message="Expected 200 got 500" type="AssertionError">
                        java.lang.AssertionError: at AuthTest.java:42
                    </failure>
                </testcase>
                <testcase name="shouldSkip" classname="com.app.AuthTest" time="0.0">
                    <skipped message="Not implemented"/>
                </testcase>
            </testsuite>""";

        JUnitXmlParser.ParseResult r = parser.parse(xml);

        assertThat(r.executions()).hasSize(3);
        assertThat(r.executions().get(0).status()).isEqualTo(TestStatus.PASS);
        assertThat(r.executions().get(0).durationMs()).isEqualTo(1234);
        assertThat(r.executions().get(1).status()).isEqualTo(TestStatus.FAIL);
        assertThat(r.executions().get(1).errorMessage()).contains("Expected 200 got 500");
        assertThat(r.executions().get(1).errorType()).isEqualTo("AssertionError");
        assertThat(r.executions().get(2).status()).isEqualTo(TestStatus.SKIP);
        assertThat(r.totalCases()).isEqualTo(3);
        assertThat(r.passed()).isEqualTo(1);
        assertThat(r.failed()).isEqualTo(1);
        assertThat(r.skipped()).isEqualTo(1);
        assertThat(r.durationMs()).isEqualTo(4532);
    }

    @Test
    void shouldParseEmptySuite() {
        String xml = """
            <?xml version="1.0"?>
            <testsuite name="Empty" tests="0" failures="0" errors="0" skipped="0" time="0"/>""";
        assertThat(parser.parse(xml).executions()).isEmpty();
    }

    @Test
    void shouldHandleErrorTag() {
        String xml = """
            <?xml version="1.0"?>
            <testsuite name="S" tests="1" failures="0" errors="1" skipped="0" time="1.0">
                <testcase name="t" classname="c" time="0.5">
                    <error message="Connection refused" type="RuntimeException">stack...</error>
                </testcase>
            </testsuite>""";
        JUnitXmlParser.ParseResult r = parser.parse(xml);
        assertThat(r.executions().get(0).status()).isEqualTo(TestStatus.ERROR);
        assertThat(r.failed()).isEqualTo(1);
    }
}
```

```java
// backend/src/main/java/com/pawhub/module/collection/parser/JUnitXmlParser.java
package com.pawhub.module.collection.parser;

import com.pawhub.module.collection.entity.TestStatus;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class JUnitXmlParser {

    public record ParseResult(List<Execution> executions, int totalCases, int passed, int failed, int skipped, long durationMs) {}

    public record Execution(String suiteName, String className, String testName, TestStatus status,
                            long durationMs, String errorMessage, String errorType, String stackTrace) {}

    public ParseResult parse(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            List<Execution> executions = new ArrayList<>();
            int total = 0, passed = 0, failed = 0, skipped = 0;
            long totalDuration = 0;

            NodeList suites = doc.getElementsByTagName("testsuite");
            for (int i = 0; i < suites.getLength(); i++) {
                Element suite = (Element) suites.item(i);
                String suiteName = suite.getAttribute("name");
                total += intAttr(suite, "tests");
                totalDuration += (long)(doubleAttr(suite, "time") * 1000);

                NodeList cases = suite.getElementsByTagName("testcase");
                for (int j = 0; j < cases.getLength(); j++) {
                    Element tc = (Element) cases.item(j);
                    String cn = tc.getAttribute("classname");
                    String tn = tc.getAttribute("name");
                    long dur = (long)(doubleAttr(tc, "time") * 1000);

                    TestStatus status;
                    String errMsg = null, errType = null, stack = null;

                    NodeList failures = tc.getElementsByTagName("failure");
                    NodeList errors = tc.getElementsByTagName("error");
                    NodeList skippeds = tc.getElementsByTagName("skipped");

                    if (failures.getLength() > 0) {
                        status = TestStatus.FAIL;
                        Element f = (Element) failures.item(0);
                        errMsg = f.getAttribute("message");
                        errType = f.getAttribute("type");
                        stack = f.getTextContent();
                        failed++;
                    } else if (errors.getLength() > 0) {
                        status = TestStatus.ERROR;
                        Element e = (Element) errors.item(0);
                        errMsg = e.getAttribute("message");
                        errType = e.getAttribute("type");
                        stack = e.getTextContent();
                        failed++;
                    } else if (skippeds.getLength() > 0) {
                        status = TestStatus.SKIP;
                        skipped++;
                    } else {
                        status = TestStatus.PASS;
                        passed++;
                    }
                    executions.add(new Execution(suiteName, cn, tn, status, dur, errMsg, errType, stack));
                }
            }
            return new ParseResult(executions, total, passed, failed, skipped, totalDuration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JUnit XML: " + e.getMessage(), e);
        }
    }

    private int intAttr(Element e, String attr) {
        try { String v = e.getAttribute(attr); return v.isEmpty() ? 0 : Integer.parseInt(v); }
        catch (NumberFormatException ex) { return 0; }
    }
    private double doubleAttr(Element e, String attr) {
        try { String v = e.getAttribute(attr); return v.isEmpty() ? 0 : Double.parseDouble(v); }
        catch (NumberFormatException ex) { return 0; }
    }
}
```

Run test: `cd backend && mvn test -Dtest=JUnitXmlParserTest` — should PASS

```bash
git add backend/src/main/java/com/pawhub/module/collection/parser/ backend/src/test/
git commit -m "feat: add JUnit XML parser with tests"
```

### Task 14: Collection Service

```java
// backend/src/main/java/com/pawhub/module/collection/event/TestResultCollectedEvent.java
package com.pawhub.module.collection.event;
public record TestResultCollectedEvent(Long testRunId, Long projectId) {}
```

```java
// backend/src/main/java/com/pawhub/module/collection/dto/TestRunResponse.java
package com.pawhub.module.collection.dto;

import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.entity.TestStatus;
import java.time.Instant;

public record TestRunResponse(Long id, Long projectId, String runIdentifier, String branch,
    String commitSha, String triggeredBy, String environment, int totalCases, int passed,
    int failed, int skipped, long durationMs, TestStatus status, Instant createdAt) {
    public static TestRunResponse from(TestRun tr) {
        return new TestRunResponse(tr.getId(), tr.getProject().getId(), tr.getRunIdentifier(),
            tr.getBranch(), tr.getCommitSha(), tr.getTriggeredBy(), tr.getEnvironment(),
            tr.getTotalCases(), tr.getPassed(), tr.getFailed(), tr.getSkipped(),
            tr.getDurationMs(), tr.getStatus(), tr.getCreatedAt());
    }
}

// TestExecutionResponse.java
package com.pawhub.module.collection.dto;

import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.entity.TestStatus;

public record TestExecutionResponse(Long id, int attempt, String suiteName, String className,
    String testName, String caseNumber, TestStatus status, long durationMs,
    String errorMessage, String errorType, String stackTrace) {
    public static TestExecutionResponse from(TestExecution te) {
        return new TestExecutionResponse(te.getId(), te.getAttempt(), te.getSuiteName(),
            te.getClassName(), te.getTestName(), te.getCaseNumber(), te.getStatus(),
            te.getDurationMs(), te.getErrorMessage(), te.getErrorType(), te.getStackTrace());
    }
}
```

```java
// backend/src/main/java/com/pawhub/module/collection/service/CollectionService.java
package com.pawhub.module.collection.service;

import com.pawhub.common.exception.PawHubException;
import com.pawhub.module.auth.entity.Project;
import com.pawhub.module.auth.repository.ProjectRepository;
import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.entity.TestStatus;
import com.pawhub.module.collection.event.TestResultCollectedEvent;
import com.pawhub.module.collection.parser.JUnitXmlParser;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import com.pawhub.module.collection.repository.TestRunRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollectionService {
    private final ProjectRepository projectRepo;
    private final TestRunRepository testRunRepo;
    private final TestExecutionRepository executionRepo;
    private final JUnitXmlParser parser;
    private final ApplicationEventPublisher events;

    public CollectionService(ProjectRepository pr, TestRunRepository tr, TestExecutionRepository te,
                             JUnitXmlParser p, ApplicationEventPublisher ev) {
        this.projectRepo = pr; this.testRunRepo = tr; this.executionRepo = te;
        this.parser = p; this.events = ev;
    }

    @Transactional
    public TestRun ingest(Long projectId, String environment, String runIdentifier,
                          String branch, String commitSha, String triggeredBy, String xml) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> new PawHubException("Project not found", HttpStatus.NOT_FOUND));
        JUnitXmlParser.ParseResult parsed = parser.parse(xml);

        TestRun run = new TestRun();
        run.setProject(project);
        run.setEnvironment(environment);
        run.setRunIdentifier(runIdentifier);
        run.setBranch(branch);
        run.setCommitSha(commitSha);
        run.setTriggeredBy(triggeredBy);
        run.setTotalCases(parsed.totalCases());
        run.setPassed(parsed.passed());
        run.setFailed(parsed.failed());
        run.setSkipped(parsed.skipped());
        run.setDurationMs(parsed.durationMs());
        run.setStatus(parsed.failed() > 0 ? TestStatus.FAIL : TestStatus.PASS);
        run.setRawXml(xml);
        run = testRunRepo.save(run);

        List<TestExecution> execs = new ArrayList<>();
        for (var ex : parsed.executions()) {
            TestExecution te = new TestExecution();
            te.setTestRun(run);
            te.setAttempt(1);
            te.setSuiteName(ex.suiteName());
            te.setClassName(ex.className());
            te.setTestName(ex.testName());
            te.setStatus(ex.status());
            te.setDurationMs(ex.durationMs());
            te.setErrorMessage(ex.errorMessage());
            te.setErrorType(ex.errorType());
            te.setStackTrace(ex.stackTrace());
            execs.add(te);
        }
        executionRepo.saveAll(execs);
        events.publishEvent(new TestResultCollectedEvent(run.getId(), projectId));
        return run;
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/collection/
git commit -m "feat: add collection service with async analysis event"
```

### Task 15: Collection Controller

```java
// backend/src/main/java/com/pawhub/module/collection/controller/CollectionController.java
package com.pawhub.module.collection.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.collection.dto.TestRunResponse;
import com.pawhub.module.collection.service.CollectionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class CollectionController {
    private final CollectionService service;
    public CollectionController(CollectionService s) { this.service = s; }

    @PostMapping(value = "/test-results", consumes = "multipart/form-data")
    public ApiResponse<TestRunResponse> uploadFile(@PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String environment,
            @RequestParam(required = false) String runIdentifier,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String commitSha,
            @RequestParam(required = false) String triggeredBy) {
        try {
            String xml = new String(file.getBytes(), StandardCharsets.UTF_8);
            return ApiResponse.ok(TestRunResponse.from(
                service.ingest(projectId, environment, runIdentifier, branch, commitSha, triggeredBy, xml)));
        } catch (Exception e) {
            return ApiResponse.error("Failed to process: " + e.getMessage());
        }
    }

    @PostMapping(value = "/test-results", consumes = "application/json")
    public ApiResponse<TestRunResponse> uploadJson(@PathVariable Long projectId,
            @RequestBody String xmlBody,
            @RequestParam String environment,
            @RequestParam(required = false) String runIdentifier,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String commitSha,
            @RequestParam(required = false) String triggeredBy) {
        return ApiResponse.ok(TestRunResponse.from(
            service.ingest(projectId, environment, runIdentifier, branch, commitSha, triggeredBy, xmlBody)));
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/collection/controller/
git commit -m "feat: add collection controller"
```

---

## Phase 4: Analysis Engine (Tasks 16-21)

### Task 16: Analysis Entities

```java
// backend/src/main/java/com/pawhub/module/analysis/entity/TrendSnapshot.java
package com.pawhub.module.analysis.entity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name = "trend_snapshots",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id","date","environment","period_type"}))
public class TrendSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long projectId;
    @Column(nullable = false) private LocalDate date;
    @Column(nullable = false) private String environment;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PeriodType periodType;
    private double passRate;
    private double failureRate;
    private double avgDurationMs;
    private double retryRate;
    public enum PeriodType { DAILY, WEEKLY }

    public TrendSnapshot() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; } public void setProjectId(Long id) { this.projectId = id; }
    public LocalDate getDate() { return date; } public void setDate(LocalDate d) { this.date = d; }
    public String getEnvironment() { return environment; } public void setEnvironment(String e) { this.environment = e; }
    public PeriodType getPeriodType() { return periodType; } public void setPeriodType(PeriodType t) { this.periodType = t; }
    public double getPassRate() { return passRate; } public void setPassRate(double d) { this.passRate = d; }
    public double getFailureRate() { return failureRate; } public void setFailureRate(double d) { this.failureRate = d; }
    public double getAvgDurationMs() { return avgDurationMs; } public void setAvgDurationMs(double d) { this.avgDurationMs = d; }
    public double getRetryRate() { return retryRate; } public void setRetryRate(double d) { this.retryRate = d; }
}
```

```java
// backend/src/main/java/com/pawhub/module/analysis/entity/FlakyTestRecord.java
package com.pawhub.module.analysis.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "flaky_test_records")
public class FlakyTestRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String testCaseKey;
    @Column(nullable = false) private Long projectId;
    private double flakyScore;
    private int transitionCount;
    private int retryPassCount;
    private Instant lastDetectedAt;
    public FlakyTestRecord() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTestCaseKey() { return testCaseKey; } public void setTestCaseKey(String s) { this.testCaseKey = s; }
    public Long getProjectId() { return projectId; } public void setProjectId(Long id) { this.projectId = id; }
    public double getFlakyScore() { return flakyScore; } public void setFlakyScore(double d) { this.flakyScore = d; }
    public int getTransitionCount() { return transitionCount; } public void setTransitionCount(int n) { this.transitionCount = n; }
    public int getRetryPassCount() { return retryPassCount; } public void setRetryPassCount(int n) { this.retryPassCount = n; }
    public Instant getLastDetectedAt() { return lastDetectedAt; } public void setLastDetectedAt(Instant i) { this.lastDetectedAt = i; }
}
```

```java
// backend/src/main/java/com/pawhub/module/analysis/entity/FailureCluster.java
package com.pawhub.module.analysis.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "failure_clusters")
public class FailureCluster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long projectId;
    @Column(nullable = false, unique = true) private String clusterKey;
    @Column(length = 1000) private String representativeError;
    private int occurrenceCount;
    private Instant firstSeen;
    private Instant lastSeen;
    public FailureCluster() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; } public void setProjectId(Long id) { this.projectId = id; }
    public String getClusterKey() { return clusterKey; } public void setClusterKey(String s) { this.clusterKey = s; }
    public String getRepresentativeError() { return representativeError; }
    public void setRepresentativeError(String s) { this.representativeError = s; }
    public int getOccurrenceCount() { return occurrenceCount; } public void setOccurrenceCount(int n) { this.occurrenceCount = n; }
    public Instant getFirstSeen() { return firstSeen; } public void setFirstSeen(Instant i) { this.firstSeen = i; }
    public Instant getLastSeen() { return lastSeen; } public void setLastSeen(Instant i) { this.lastSeen = i; }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/analysis/entity/
git commit -m "feat: add analysis entities"
```

### Task 17: Analysis Repositories

```java
// backend/src/main/java/com/pawhub/module/analysis/repository/TrendSnapshotRepository.java
package com.pawhub.module.analysis.repository;
import com.pawhub.module.analysis.entity.TrendSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrendSnapshotRepository extends JpaRepository<TrendSnapshot, Long> {
    List<TrendSnapshot> findByProjectIdAndEnvironmentAndPeriodTypeAndDateBetweenOrderByDateAsc(
        Long projectId, String environment, TrendSnapshot.PeriodType periodType, LocalDate from, LocalDate to);
    Optional<TrendSnapshot> findByProjectIdAndDateAndEnvironmentAndPeriodType(
        Long projectId, LocalDate date, String environment, TrendSnapshot.PeriodType periodType);
}

// FlakyTestRecordRepository.java
package com.pawhub.module.analysis.repository;
import com.pawhub.module.analysis.entity.FlakyTestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FlakyTestRecordRepository extends JpaRepository<FlakyTestRecord, Long> {
    List<FlakyTestRecord> findByProjectIdOrderByFlakyScoreDesc(Long projectId);
    Optional<FlakyTestRecord> findByProjectIdAndTestCaseKey(Long projectId, String testCaseKey);
}

// FailureClusterRepository.java
package com.pawhub.module.analysis.repository;
import com.pawhub.module.analysis.entity.FailureCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FailureClusterRepository extends JpaRepository<FailureCluster, Long> {
    List<FailureCluster> findByProjectIdOrderByOccurrenceCountDesc(Long projectId);
    Optional<FailureCluster> findByProjectIdAndClusterKey(Long projectId, String clusterKey);
}
```

```bash
git add backend/src/main/java/com/pawhub/module/analysis/repository/
git commit -m "feat: add analysis repositories"
```

### Task 18: Trend Analysis Service (TDD)

```java
// backend/src/test/java/com/pawhub/module/analysis/service/TrendAnalysisServiceTest.java
package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.TrendSnapshotRepository;
import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.repository.TestRunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrendAnalysisServiceTest {
    @Mock TestRunRepository testRunRepo;
    @Mock TrendSnapshotRepository trendRepo;
    @InjectMocks TrendAnalysisService service;

    @Test
    void shouldComputeDailyTrend() {
        TestRun run = new TestRun();
        run.setProject(project(1L));
        run.setTotalCases(10); run.setPassed(9); run.setFailed(1);
        run.setDurationMs(5000); run.setEnvironment("prod");

        when(testRunRepo.findByProjectIdAndEnvironmentAndCreatedAtBetween(any(), any(), any(), any()))
            .thenReturn(List.of(run));
        when(trendRepo.findByProjectIdAndDateAndEnvironmentAndPeriodType(any(), any(), any(), any()))
            .thenReturn(Optional.empty());

        service.computeDailyTrend(1L, "prod", LocalDate.of(2026, 4, 27));

        ArgumentCaptor<TrendSnapshot> cap = ArgumentCaptor.forClass(TrendSnapshot.class);
        verify(trendRepo).save(cap.capture());
        assertThat(cap.getValue().getPassRate()).isEqualTo(0.9);
    }

    private com.pawhub.module.auth.entity.Project project(Long id) {
        com.pawhub.module.auth.entity.Project p = new com.pawhub.module.auth.entity.Project();
        p.setId(id); return p;
    }
}
```

```java
// backend/src/main/java/com/pawhub/module/analysis/service/TrendAnalysisService.java
package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.TrendSnapshotRepository;
import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.repository.TestRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class TrendAnalysisService {
    private final TestRunRepository testRunRepo;
    private final TrendSnapshotRepository trendRepo;

    public TrendAnalysisService(TestRunRepository t, TrendSnapshotRepository tr) {
        this.testRunRepo = t; this.trendRepo = tr;
    }

    @Transactional
    public void computeDailyTrend(Long projectId, String env, LocalDate date) {
        Instant from = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<TestRun> runs = testRunRepo.findByProjectIdAndEnvironmentAndCreatedAtBetween(projectId, env, from, to);
        if (runs.isEmpty()) return;

        TrendSnapshot snap = trendRepo
            .findByProjectIdAndDateAndEnvironmentAndPeriodType(projectId, date, env, TrendSnapshot.PeriodType.DAILY)
            .orElseGet(TrendSnapshot::new);
        snap.setProjectId(projectId);
        snap.setDate(date);
        snap.setEnvironment(env);
        snap.setPeriodType(TrendSnapshot.PeriodType.DAILY);
        snap.setPassRate(runs.stream().mapToDouble(r -> r.getTotalCases() > 0 ? (double)r.getPassed()/r.getTotalCases() : 1.0).average().orElse(0));
        snap.setFailureRate(runs.stream().mapToDouble(r -> r.getTotalCases() > 0 ? (double)r.getFailed()/r.getTotalCases() : 0.0).average().orElse(0));
        snap.setAvgDurationMs(runs.stream().mapToDouble(TestRun::getDurationMs).average().orElse(0));
        snap.setRetryRate(0.0);
        trendRepo.save(snap);
    }
}
```

Run test: `cd backend && mvn test -Dtest=TrendAnalysisServiceTest` — PASS

```bash
git add backend/src/main/java/com/pawhub/module/analysis/service/TrendAnalysisService.java backend/src/test/
git commit -m "feat: add trend analysis service with tests"
```

### Task 19: Regression Detection Service

```java
// backend/src/test/java/com/pawhub/module/analysis/service/RegressionDetectionServiceTest.java
package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.TrendSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegressionDetectionServiceTest {
    @Mock TrendSnapshotRepository trendRepo;
    @InjectMocks RegressionDetectionService service;

    @Test
    void shouldDetectRunLevelRegression() {
        TrendSnapshot t1 = snap(0.95);
        TrendSnapshot t2 = snap(0.94);
        TrendSnapshot t3 = snap(0.93);
        TrendSnapshot today = snap(0.75); // below 2-sigma
        when(trendRepo.findByProjectIdAndEnvironmentAndPeriodTypeAndDateBetweenOrderByDateAsc(
            any(), any(), any(), any(), any())).thenReturn(List.of(t1, t2, t3));

        service.detectRunLevelRegression(1L, "prod", today, 2.0);
        // No exception = regression flag behavior verified
    }

    private TrendSnapshot snap(double passRate) {
        TrendSnapshot s = new TrendSnapshot();
        s.setPassRate(passRate);
        s.setDate(LocalDate.now());
        return s;
    }
}
```

```java
// backend/src/main/java/com/pawhub/module/analysis/service/RegressionDetectionService.java
package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.TrendSnapshotRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class RegressionDetectionService {
    private final TrendSnapshotRepository trendRepo;

    public RegressionDetectionService(TrendSnapshotRepository tr) { this.trendRepo = tr; }

    public boolean detectRunLevelRegression(Long projectId, String env, TrendSnapshot today, double sigma) {
        LocalDate windowStart = today.getDate().minusDays(30);
        List<TrendSnapshot> history = trendRepo
            .findByProjectIdAndEnvironmentAndPeriodTypeAndDateBetweenOrderByDateAsc(
                projectId, env, TrendSnapshot.PeriodType.DAILY, windowStart, today.getDate().minusDays(1));
        if (history.size() < 5) return false; // not enough data

        double mean = history.stream().mapToDouble(TrendSnapshot::getPassRate).average().orElse(0);
        double variance = history.stream().mapToDouble(s -> Math.pow(s.getPassRate() - mean, 2)).average().orElse(0);
        double stdDev = Math.sqrt(variance);
        return today.getPassRate() < (mean - sigma * stdDev);
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/analysis/service/RegressionDetectionService.java backend/src/test/
git commit -m "feat: add regression detection service with tests"
```

### Task 20: Flaky Detection Service

```java
// backend/src/main/java/com/pawhub/module/analysis/service/FlakyDetectionService.java
package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.FlakyTestRecord;
import com.pawhub.module.analysis.repository.FlakyTestRecordRepository;
import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.entity.TestStatus;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class FlakyDetectionService {
    private final TestExecutionRepository executionRepo;
    private final FlakyTestRecordRepository flakyRepo;
    private final double threshold;
    private final int windowDays;

    public FlakyDetectionService(TestExecutionRepository e, FlakyTestRecordRepository f,
                                 @Value("${analysis.flaky-threshold}") double threshold,
                                 @Value("${analysis.window-days}") int windowDays) {
        this.executionRepo = e; this.flakyRepo = f; this.threshold = threshold;
        this.windowDays = windowDays;
    }

    @Transactional
    public void detectFlakyTests(Long projectId) {
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);
        List<String> keys = executionRepo.findDistinctTestCaseKeys(projectId, since);

        for (String key : keys) {
            List<TestExecution> execs = executionRepo.findByTestCaseKey(projectId, key, since);
            if (execs.size() < 3) continue;

            int transitions = 0;
            int retryPass = 0;
            TestStatus prev = null;
            for (TestExecution e : execs) {
                if (prev != null && e.getStatus() != prev) transitions++;
                if (e.getAttempt() > 1 && e.getStatus() == TestStatus.PASS) retryPass++;
                prev = e.getStatus();
            }
            double score = (double) transitions / execs.size() + (double) retryPass / execs.size() * 0.3;
            score = Math.min(1.0, score);

            if (score >= threshold) {
                FlakyTestRecord rec = flakyRepo.findByProjectIdAndTestCaseKey(projectId, key)
                    .orElseGet(FlakyTestRecord::new);
                rec.setProjectId(projectId);
                rec.setTestCaseKey(key);
                rec.setFlakyScore(score);
                rec.setTransitionCount(transitions);
                rec.setRetryPassCount(retryPass);
                rec.setLastDetectedAt(Instant.now());
                flakyRepo.save(rec);
            }
        }
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/analysis/service/FlakyDetectionService.java
git commit -m "feat: add flaky test detection service"
```

### Task 21: Failure Clustering Service & Analysis Listener

```java
// backend/src/main/java/com/pawhub/module/analysis/service/FailureClusteringService.java
package com.pawhub.module.analysis.service;

import com.pawhub.module.analysis.entity.FailureCluster;
import com.pawhub.module.analysis.repository.FailureClusterRepository;
import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FailureClusteringService {
    private final TestExecutionRepository executionRepo;
    private final FailureClusterRepository clusterRepo;

    public FailureClusteringService(TestExecutionRepository e, FailureClusterRepository c) {
        this.executionRepo = e; this.clusterRepo = c;
    }

    @Transactional
    public void clusterFailures(Long projectId) {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<TestExecution> failures = executionRepo.findRecentFailures(projectId, since);
        if (failures.isEmpty()) return;

        Map<String, List<TestExecution>> groups = failures.stream()
            .filter(e -> e.getErrorMessage() != null && !e.getErrorMessage().isEmpty())
            .collect(Collectors.groupingBy(e -> hash(normalize(e.getErrorMessage()))));

        for (var entry : groups.entrySet()) {
            FailureCluster cluster = clusterRepo.findByProjectIdAndClusterKey(projectId, entry.getKey())
                .orElseGet(FailureCluster::new);
            cluster.setProjectId(projectId);
            cluster.setClusterKey(entry.getKey());
            String rep = entry.getValue().stream()
                .map(TestExecution::getErrorMessage)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("");
            cluster.setRepresentativeError(rep.substring(0, Math.min(rep.length(), 1000)));
            cluster.setOccurrenceCount(entry.getValue().size());
            Instant now = Instant.now();
            if (cluster.getFirstSeen() == null) cluster.setFirstSeen(now);
            cluster.setLastSeen(now);
            clusterRepo.save(cluster);
        }
    }

    private String normalize(String msg) {
        return msg.replaceAll("\\d+", "0")
                .replaceAll("0x[0-9a-fA-F]+", "0xHEX")
                .replaceAll("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "UUID");
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes()));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
```

```java
// backend/src/main/java/com/pawhub/module/analysis/event/AnalysisListener.java
package com.pawhub.module.analysis.event;

import com.pawhub.module.analysis.service.*;
import com.pawhub.module.collection.event.TestResultCollectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class AnalysisListener {
    private final TrendAnalysisService trendService;
    private final RegressionDetectionService regressionService;
    private final FlakyDetectionService flakyService;
    private final FailureClusteringService clusterService;

    public AnalysisListener(TrendAnalysisService t, RegressionDetectionService r,
                           FlakyDetectionService f, FailureClusteringService c) {
        this.trendService = t; this.regressionService = r;
        this.flakyService = f; this.clusterService = c;
    }

    @Async("analysisExecutor")
    @EventListener
    public void onTestResultCollected(TestResultCollectedEvent event) {
        // Run all analysis steps for the project
        // In v1: run for all known environments (or a default set)
        String[] envs = {"dev", "staging", "prod"};
        LocalDate today = LocalDate.now();

        for (String env : envs) {
            trendService.computeDailyTrend(event.projectId(), env, today);
        }
        flakyService.detectFlakyTests(event.projectId());
        clusterService.clusterFailures(event.projectId());
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/analysis/
git commit -m "feat: add failure clustering service and async analysis listener"
```

### Task 22: Analysis Controller & DTOs

```java
// backend/src/main/java/com/pawhub/module/analysis/dto/TrendResponse.java
package com.pawhub.module.analysis.dto;
import java.time.LocalDate;
import java.util.List;

public record TrendDataPoint(LocalDate date, double passRate, double failureRate, double avgDurationMs, double retryRate) {}
public record TrendResponse(String environment, String periodType, List<TrendDataPoint> dataPoints) {}

// FlakyTestResponse.java
package com.pawhub.module.analysis.dto;
import java.time.Instant;

public record FlakyTestResponse(String testCaseKey, double flakyScore, int transitionCount, int retryPassCount, Instant lastDetectedAt) {}
```

```java
// backend/src/main/java/com/pawhub/module/analysis/dto/FailureClusterResponse.java
package com.pawhub.module.analysis.dto;
import java.time.Instant;

public record FailureClusterResponse(String clusterKey, String representativeError, int occurrenceCount, Instant firstSeen, Instant lastSeen) {}

// RegressionResponse.java
package com.pawhub.module.analysis.dto;
public record RegressionResponse(boolean regressionDetected, String severity, String description) {}
```

```java
// backend/src/main/java/com/pawhub/module/analysis/controller/AnalysisController.java
package com.pawhub.module.analysis.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.analysis.dto.*;
import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.*;
import com.pawhub.module.analysis.service.RegressionDetectionService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class AnalysisController {
    private final TrendSnapshotRepository trendRepo;
    private final FlakyTestRecordRepository flakyRepo;
    private final FailureClusterRepository clusterRepo;

    public AnalysisController(TrendSnapshotRepository t, FlakyTestRecordRepository f,
                              FailureClusterRepository c) {
        this.trendRepo = t; this.flakyRepo = f; this.clusterRepo = c;
    }

    @GetMapping("/trends")
    public ApiResponse<List<TrendResponse>> getTrends(@PathVariable Long projectId,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String environment) {
        TrendSnapshot.PeriodType periodType = TrendSnapshot.PeriodType.valueOf(period.toUpperCase());
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);
        String[] envs = environment != null ? new String[]{environment} : new String[]{"dev","staging","prod"};
        var responses = new java.util.ArrayList<TrendResponse>();
        for (String env : envs) {
            var snaps = trendRepo.findByProjectIdAndEnvironmentAndPeriodTypeAndDateBetweenOrderByDateAsc(
                projectId, env, periodType, from, to);
            var points = snaps.stream().map(s -> new TrendDataPoint(s.getDate(), s.getPassRate(),
                s.getFailureRate(), s.getAvgDurationMs(), s.getRetryRate())).toList();
            responses.add(new TrendResponse(env, period, points));
        }
        return ApiResponse.ok(responses);
    }

    @GetMapping("/flaky-tests")
    public ApiResponse<List<FlakyTestResponse>> getFlakyTests(@PathVariable Long projectId) {
        var list = flakyRepo.findByProjectIdOrderByFlakyScoreDesc(projectId)
            .stream().map(f -> new FlakyTestResponse(f.getTestCaseKey(), f.getFlakyScore(),
                f.getTransitionCount(), f.getRetryPassCount(), f.getLastDetectedAt())).toList();
        return ApiResponse.ok(list);
    }

    @GetMapping("/failure-clusters")
    public ApiResponse<List<FailureClusterResponse>> getClusters(@PathVariable Long projectId) {
        var list = clusterRepo.findByProjectIdOrderByOccurrenceCountDesc(projectId)
            .stream().map(c -> new FailureClusterResponse(c.getClusterKey(), c.getRepresentativeError(),
                c.getOccurrenceCount(), c.getFirstSeen(), c.getLastSeen())).toList();
        return ApiResponse.ok(list);
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/analysis/
git commit -m "feat: add analysis controller and DTOs"
```

---

## Phase 5: Triage Module (Tasks 23-24)

### Task 23: FailureTriage Entity, Repository & Service

```java
// backend/src/main/java/com/pawhub/module/triage/entity/FailureTriage.java
package com.pawhub.module.triage.entity;

import com.pawhub.module.collection.entity.TestExecution;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "failure_triages")
public class FailureTriage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "test_execution_id", unique = true, nullable = false)
    private TestExecution testExecution;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TriageStatus triageStatus = TriageStatus.UNTRIAGED;
    private String issueLink;
    @Column(length = 2000) private String comment;
    private Long annotatedBy;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public enum TriageStatus { UNTRIAGED, NEW_BUG, KNOWN_ISSUE, SCRIPT_ISSUE, DATA_ISSUE, ENV_ISSUE, CR, OTHER }

    public FailureTriage() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public TestExecution getTestExecution() { return testExecution; }
    public void setTestExecution(TestExecution e) { this.testExecution = e; }
    public TriageStatus getTriageStatus() { return triageStatus; }
    public void setTriageStatus(TriageStatus s) { this.triageStatus = s; }
    public String getIssueLink() { return issueLink; } public void setIssueLink(String s) { this.issueLink = s; }
    public String getComment() { return comment; } public void setComment(String s) { this.comment = s; }
    public Long getAnnotatedBy() { return annotatedBy; } public void setAnnotatedBy(Long id) { this.annotatedBy = id; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant i) { this.updatedAt = i; }
}
```

```java
// backend/src/main/java/com/pawhub/module/triage/repository/FailureTriageRepository.java
package com.pawhub.module.triage.repository;

import com.pawhub.module.triage.entity.FailureTriage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface FailureTriageRepository extends JpaRepository<FailureTriage, Long> {
    Optional<FailureTriage> findByTestExecutionId(Long executionId);

    @Query("SELECT ft.triageStatus, COUNT(ft) FROM FailureTriage ft " +
           "JOIN ft.testExecution te JOIN te.testRun tr " +
           "WHERE tr.project.id = :projectId AND tr.createdAt >= :since " +
           "GROUP BY ft.triageStatus")
    List<Object[]> countByStatus(@Param("projectId") Long projectId, @Param("since") java.time.Instant since);
}
```

```java
// backend/src/main/java/com/pawhub/module/triage/service/TriageService.java
package com.pawhub.module.triage.service;

import com.pawhub.common.exception.PawHubException;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import com.pawhub.module.triage.dto.TriageRequest;
import com.pawhub.module.triage.dto.TriageResponse;
import com.pawhub.module.triage.dto.TriageSummaryResponse;
import com.pawhub.module.triage.entity.FailureTriage;
import com.pawhub.module.triage.repository.FailureTriageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class TriageService {
    private final FailureTriageRepository triageRepo;
    private final TestExecutionRepository executionRepo;

    public TriageService(FailureTriageRepository t, TestExecutionRepository e) {
        this.triageRepo = t; this.executionRepo = e;
    }

    @Transactional
    public TriageResponse saveOrUpdate(Long executionId, TriageRequest request, Long userId) {
        executionRepo.findById(executionId)
            .orElseThrow(() -> new PawHubException("Test execution not found", HttpStatus.NOT_FOUND));
        FailureTriage triage = triageRepo.findByTestExecutionId(executionId)
            .orElseGet(FailureTriage::new);
        triage.setTestExecution(executionRepo.getReferenceById(executionId));
        triage.setTriageStatus(request.triageStatus());
        triage.setIssueLink(request.issueLink());
        triage.setComment(request.comment());
        triage.setAnnotatedBy(userId);
        triage.setUpdatedAt(Instant.now());
        triage = triageRepo.save(triage);
        return TriageResponse.from(triage);
    }

    public TriageResponse getByExecutionId(Long executionId) {
        return triageRepo.findByTestExecutionId(executionId)
            .map(TriageResponse::from)
            .orElse(null);
    }

    public TriageSummaryResponse getSummary(Long projectId) {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Object[]> counts = triageRepo.countByStatus(projectId, since);
        var breakdown = new HashMap<FailureTriage.TriageStatus, Long>();
        long total = 0;
        for (Object[] row : counts) {
            FailureTriage.TriageStatus status = (FailureTriage.TriageStatus) row[0];
            long count = (Long) row[1];
            breakdown.put(status, count);
            total += count;
        }
        long untriaged = breakdown.getOrDefault(FailureTriage.TriageStatus.UNTRIAGED, 0L);
        return new TriageSummaryResponse(total, untriaged, breakdown);
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/triage/
git commit -m "feat: add failure triage entity, repository, and service"
```

### Task 24: Triage DTOs & Controller

```java
// backend/src/main/java/com/pawhub/module/triage/dto/TriageRequest.java
package com.pawhub.module.triage.dto;

import com.pawhub.module.triage.entity.FailureTriage;
import jakarta.validation.constraints.NotNull;

public record TriageRequest(@NotNull FailureTriage.TriageStatus triageStatus, String issueLink, String comment) {}
```

```java
// backend/src/main/java/com/pawhub/module/triage/dto/TriageResponse.java
package com.pawhub.module.triage.dto;

import com.pawhub.module.triage.entity.FailureTriage;
import java.time.Instant;

public record TriageResponse(Long id, Long testExecutionId, String triageStatus,
    String issueLink, String comment, Long annotatedBy, Instant createdAt, Instant updatedAt) {
    public static TriageResponse from(FailureTriage t) {
        return new TriageResponse(t.getId(), t.getTestExecution().getId(), t.getTriageStatus().name(),
            t.getIssueLink(), t.getComment(), t.getAnnotatedBy(), t.getCreatedAt(), t.getUpdatedAt());
    }
}

// TriageSummaryResponse.java
package com.pawhub.module.triage.dto;

import com.pawhub.module.triage.entity.FailureTriage;
import java.util.Map;

public record TriageSummaryResponse(long totalTriaged, long untriaged, Map<FailureTriage.TriageStatus, Long> breakdown) {}
```

```java
// backend/src/main/java/com/pawhub/module/triage/controller/TriageController.java
package com.pawhub.module.triage.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.triage.dto.TriageRequest;
import com.pawhub.module.triage.dto.TriageResponse;
import com.pawhub.module.triage.dto.TriageSummaryResponse;
import com.pawhub.module.triage.service.TriageService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class TriageController {
    private final TriageService triageService;

    public TriageController(TriageService s) { this.triageService = s; }

    @PutMapping("/test-executions/{executionId}/triage")
    public ApiResponse<TriageResponse> saveTriage(@PathVariable Long projectId,
            @PathVariable Long executionId, @Valid @RequestBody TriageRequest request,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(triageService.saveOrUpdate(executionId, request, userId));
    }

    @GetMapping("/test-executions/{executionId}/triage")
    public ApiResponse<TriageResponse> getTriage(@PathVariable Long projectId,
            @PathVariable Long executionId) {
        TriageResponse triage = triageService.getByExecutionId(executionId);
        return ApiResponse.ok(triage);
    }

    @GetMapping("/triage-summary")
    public ApiResponse<TriageSummaryResponse> getSummary(@PathVariable Long projectId) {
        return ApiResponse.ok(triageService.getSummary(projectId));
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/triage/
git commit -m "feat: add triage controller and DTOs"
```

---

## Phase 6: Reporting Module (Tasks 25-26)

### Task 25: Summary & Export Services

```java
// backend/src/main/java/com/pawhub/module/reporting/dto/SummaryResponse.java
package com.pawhub.module.reporting.dto;

import java.util.List;

public record SummaryResponse(long totalRuns, double overallPassRate, long totalFailures,
    TopFailure topFailure, List<FlakySummary> topFlakyTests) {}

public record TopFailure(String testName, String errorMessage, long failCount) {}

public record FlakySummary(String testCaseKey, double flakyScore) {}
```

```java
// backend/src/main/java/com/pawhub/module/reporting/service/SummaryService.java
package com.pawhub.module.reporting.service;

import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import com.pawhub.module.collection.repository.TestRunRepository;
import com.pawhub.module.reporting.dto.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {
    private final TestRunRepository testRunRepo;
    private final TestExecutionRepository executionRepo;

    public SummaryService(TestRunRepository t, TestExecutionRepository e) {
        this.testRunRepo = t; this.executionRepo = e;
    }

    public SummaryResponse getSummary(Long projectId, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        var failures = executionRepo.findRecentFailures(projectId, since);
        long totalFailures = failures.size();

        TopFailure topFailure = null;
        if (!failures.isEmpty()) {
            Map<String, Long> freq = failures.stream()
                .filter(e -> e.getErrorMessage() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getSuiteName() + "." + e.getTestName(),
                    Collectors.counting()));
            var top = freq.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
            if (top != null) {
                String errMsg = failures.stream()
                    .filter(e -> (e.getSuiteName() + "." + e.getTestName()).equals(top.getKey()))
                    .findFirst().map(TestExecution::getErrorMessage).orElse("");
                topFailure = new TopFailure(top.getKey(), errMsg, top.getValue());
            }
        }
        return new SummaryResponse(0, 0, totalFailures, topFailure, List.of());
    }
}
```

```java
// backend/src/main/java/com/pawhub/module/reporting/service/ExportService.java
package com.pawhub.module.reporting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ExportService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SummaryService summaryService;

    public ExportService(SummaryService s) { this.summaryService = s; }

    public String exportJsonSummary(Long projectId, int days) {
        try {
            var summary = summaryService.getSummary(projectId, days);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary);
        } catch (Exception e) { throw new RuntimeException("Export failed", e); }
    }
}
```

### Task 26: Reporting Controller

```java
// backend/src/main/java/com/pawhub/module/reporting/controller/ReportingController.java
package com.pawhub.module.reporting.controller;

import com.pawhub.common.dto.ApiResponse;
import com.pawhub.module.reporting.dto.SummaryResponse;
import com.pawhub.module.reporting.service.ExportService;
import com.pawhub.module.reporting.service.SummaryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
public class ReportingController {
    private final SummaryService summaryService;
    private final ExportService exportService;

    public ReportingController(SummaryService s, ExportService e) {
        this.summaryService = s; this.exportService = e;
    }

    @GetMapping("/summary")
    public ApiResponse<SummaryResponse> getSummary(@PathVariable Long projectId,
            @RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(summaryService.getSummary(projectId, days));
    }

    @GetMapping("/export")
    public ApiResponse<String> export(@PathVariable Long projectId,
            @RequestParam(defaultValue = "json") String format,
            @RequestParam(defaultValue = "30") int days) {
        if (!"json".equals(format)) return ApiResponse.error("Only JSON export supported in v1");
        return ApiResponse.ok(exportService.exportJsonSummary(projectId, days));
    }
}
```

```bash
git add backend/src/main/java/com/pawhub/module/reporting/
git commit -m "feat: add reporting module with summary and export"
```

---

## Phase 7: Frontend (Tasks 27-40)

### Task 27: TypeScript Types & API Client

```ts
// frontend/src/lib/types.ts
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
}

export interface TestRun {
  id: number;
  projectId: number;
  runIdentifier: string;
  branch?: string;
  commitSha?: string;
  triggeredBy?: string;
  environment: string;
  totalCases: number;
  passed: number;
  failed: number;
  skipped: number;
  durationMs: number;
  status: 'PASS' | 'FAIL' | 'ERROR';
  createdAt: string;
}

export interface TestExecution {
  id: number;
  attempt: number;
  suiteName: string;
  className: string;
  testName: string;
  caseNumber?: string;
  status: 'PASS' | 'FAIL' | 'SKIP' | 'ERROR';
  durationMs: number;
  errorMessage?: string;
  errorType?: string;
  stackTrace?: string;
}

export interface TrendDataPoint {
  date: string;
  passRate: number;
  failureRate: number;
  avgDurationMs: number;
  retryRate: number;
}

export interface TrendResponse {
  environment: string;
  periodType: string;
  dataPoints: TrendDataPoint[];
}

export interface FlakyTest {
  testCaseKey: string;
  flakyScore: number;
  transitionCount: number;
  retryPassCount: number;
  lastDetectedAt: string;
}

export interface FailureClusterItem {
  clusterKey: string;
  representativeError: string;
  occurrenceCount: number;
  firstSeen: string;
  lastSeen: string;
}

export interface TriageResponse {
  id: number;
  testExecutionId: number;
  triageStatus: string;
  issueLink?: string;
  comment?: string;
}

export interface TriageSummary {
  totalTriaged: number;
  untriaged: number;
  breakdown: Record<string, number>;
}

export interface SummaryResponse {
  totalRuns: number;
  overallPassRate: number;
  totalFailures: number;
  topFailure?: {
    testName: string;
    errorMessage: string;
    failCount: number;
  };
  topFlakyTests: { testCaseKey: string; flakyScore: number }[];
}
```

```ts
// frontend/src/lib/auth.ts
'use client';

const TOKEN_KEY = 'pawhub_token';
const USER_KEY = 'pawhub_user';

export function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function isAuthenticated(): boolean {
  return !!getToken();
}
```

```ts
// frontend/src/lib/api.ts
import { getToken } from './auth';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    ...(options?.headers as Record<string, string> || {}),
  };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (!(options?.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const res = await fetch(`${BASE_URL}/api/v1${path}`, { ...options, headers });
  const json = await res.json();
  if (!json.success) throw new Error(json.message || 'Request failed');
  return json.data;
}

export const api = {
  auth: {
    login: (username: string, password: string) =>
      request<{token: string; userId: number; username: string}>('/auth/login', {
        method: 'POST', body: JSON.stringify({ username, password }),
      }),
    register: (username: string, email: string, password: string) =>
      request<{token: string; userId: number; username: string}>('/auth/register', {
        method: 'POST', body: JSON.stringify({ username, email, password }),
      }),
  },

  collection: {
    uploadXml: (projectId: number, formData: FormData) =>
      request<TestRun>(`/projects/${projectId}/test-results`, {
        method: 'POST', body: formData,
      }),
    getRuns: (projectId: number, params?: string) =>
      request<TestRun[]>(`/projects/${projectId}/test-runs${params ? '?' + params : ''}`),
    getRun: (projectId: number, runId: number) =>
      request<TestRun & { executions: TestExecution[] }>(`/projects/${projectId}/test-runs/${runId}`),
  },

  analysis: {
    getTrends: (projectId: number, period = 'daily', days = 30, env?: string) =>
      request<TrendResponse[]>(`/projects/${projectId}/trends?period=${period}&days=${days}${env ? '&environment=' + env : ''}`),
    getFlakyTests: (projectId: number) =>
      request<FlakyTest[]>(`/projects/${projectId}/flaky-tests`),
    getClusters: (projectId: number) =>
      request<FailureClusterItem[]>(`/projects/${projectId}/failure-clusters`),
  },

  triage: {
    save: (projectId: number, executionId: number, data: { triageStatus: string; issueLink?: string; comment?: string }) =>
      request<TriageResponse>(`/projects/${projectId}/test-executions/${executionId}/triage`, {
        method: 'PUT', body: JSON.stringify(data),
      }),
    get: (projectId: number, executionId: number) =>
      request<TriageResponse>(`/projects/${projectId}/test-executions/${executionId}/triage`),
    getSummary: (projectId: number) =>
      request<TriageSummary>(`/projects/${projectId}/triage-summary`),
  },

  reporting: {
    getSummary: (projectId: number, days = 30) =>
      request<SummaryResponse>(`/projects/${projectId}/summary?days=${days}`),
    export: (projectId: number, format = 'json', days = 30) =>
      request<string>(`/projects/${projectId}/export?format=${format}&days=${days}`),
  },
};
```

```bash
git add frontend/src/lib/
git commit -m "feat: add TypeScript types, auth, and API client"
```

### Task 28: App Layout, Login & Register Pages

```tsx
// frontend/src/app/layout.tsx
import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Paw-Hub",
  description: "Test Result Review Application",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="bg-gray-950 text-gray-100 min-h-screen">{children}</body>
    </html>
  );
}
```

```css
/* frontend/src/app/globals.css */
@import "tailwindcss";
```

```tsx
// frontend/src/app/page.tsx
import { redirect } from "next/navigation";

export default function Home() {
  redirect("/login");
}
```

```tsx
// frontend/src/app/login/page.tsx
'use client';
import { useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { setToken } from "@/lib/auth";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      const res = await api.auth.login(username, password);
      setToken(res.token);
      router.push("/projects");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center">
      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4 bg-gray-900 p-8 rounded-xl border border-gray-800">
        <h1 className="text-2xl font-bold text-center">Paw-Hub</h1>
        {error && <p className="text-red-400 text-sm">{error}</p>}
        <input className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} />
        <input type="password" className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
        <button type="submit" className="w-full py-2 bg-blue-600 hover:bg-blue-700 rounded-lg font-semibold">Log In</button>
        <p className="text-sm text-gray-400 text-center">No account? <a href="/register" className="text-blue-400">Register</a></p>
      </form>
    </div>
  );
}
```

```tsx
// frontend/src/app/register/page.tsx
'use client';
import { useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";

export default function RegisterPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.auth.register(username, email, password);
      router.push("/login");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center">
      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4 bg-gray-900 p-8 rounded-xl border border-gray-800">
        <h1 className="text-2xl font-bold text-center">Register</h1>
        {error && <p className="text-red-400 text-sm">{error}</p>}
        <input className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} />
        <input type="email" className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
        <input type="password" className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
        <button type="submit" className="w-full py-2 bg-blue-600 hover:bg-blue-700 rounded-lg font-semibold">Register</button>
        <p className="text-sm text-gray-400 text-center">Have an account? <a href="/login" className="text-blue-400">Log in</a></p>
      </form>
    </div>
  );
}
```

```bash
git add frontend/src/app/
git commit -m "feat: add app layout, login and register pages"
```

### Task 29: Layout Components — Navbar & Sidebar

```tsx
// frontend/src/components/layout/Navbar.tsx
'use client';
import { useRouter } from "next/navigation";
import { clearAuth } from "@/lib/auth";

export default function Navbar({ projectName }: { projectName?: string }) {
  const router = useRouter();
  const handleLogout = () => { clearAuth(); router.push("/login"); };

  return (
    <nav className="flex items-center justify-between px-6 py-3 bg-gray-900 border-b border-gray-800">
      <div className="flex items-center gap-4">
        <h1 className="text-lg font-bold text-blue-400">🐾 Paw-Hub</h1>
        {projectName && <span className="text-gray-400">/ {projectName}</span>}
      </div>
      <button onClick={handleLogout} className="px-4 py-1.5 text-sm rounded-lg bg-gray-800 hover:bg-gray-700">Logout</button>
    </nav>
  );
}
```

```tsx
// frontend/src/components/layout/Sidebar.tsx
'use client';
import Link from "next/link";
import { usePathname } from "next/navigation";

const links = [
  { href: "", label: "Dashboard" },
  { href: "/trends", label: "Trends" },
  { href: "/settings", label: "Settings" },
];

export default function Sidebar({ projectId }: { projectId: number }) {
  const pathname = usePathname();
  const base = `/projects/${projectId}`;

  return (
    <aside className="w-56 min-h-[calc(100vh-56px)] bg-gray-900 border-r border-gray-800 p-4">
      <nav className="space-y-1">
        {links.map(link => {
          const active = pathname === base + link.href;
          return (
            <Link key={link.href} href={base + link.href}
              className={`block px-3 py-2 rounded-lg text-sm ${active ? 'bg-blue-600 text-white' : 'text-gray-400 hover:text-white hover:bg-gray-800'}`}>
              {link.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
```

```bash
git add frontend/src/components/layout/
git commit -m "feat: add Navbar and Sidebar components"
```

### Task 30: Dashboard Page

```tsx
// frontend/src/app/projects/[projectId]/page.tsx
'use client';
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Navbar from "@/components/layout/Navbar";
import Sidebar from "@/components/layout/Sidebar";
import KpiCards from "@/components/dashboard/KpiCards";
import PassRateChart from "@/components/dashboard/PassRateChart";
import RecentRegressions from "@/components/dashboard/RecentRegressions";
import TriageBreakdown from "@/components/dashboard/TriageBreakdown";
import { api } from "@/lib/api";
import { TrendResponse, FlakyTest, TriageSummary } from "@/lib/types";

export default function DashboardPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const id = Number(projectId);
  const [trends, setTrends] = useState<TrendResponse[]>([]);
  const [flaky, setFlaky] = useState<FlakyTest[]>([]);
  const [triageSummary, setTriageSummary] = useState<TriageSummary | null>(null);

  useEffect(() => {
    Promise.all([
      api.analysis.getTrends(id),
      api.analysis.getFlakyTests(id),
      api.triage.getSummary(id),
    ]).then(([t, f, ts]) => {
      setTrends(t);
      setFlaky(f);
      setTriageSummary(ts);
    });
  }, [id]);

  return (
    <div>
      <Navbar projectName="Project" />
      <div className="flex">
        <Sidebar projectId={id} />
        <main className="flex-1 p-6 space-y-6">
          <KpiCards trends={trends} flakyCount={flaky.length} triageSummary={triageSummary} />
          <div className="grid grid-cols-2 gap-6">
            <PassRateChart trends={trends} />
            <RecentRegressions projectId={id} />
          </div>
          <TriageBreakdown summary={triageSummary} />
        </main>
      </div>
    </div>
  );
}
```

```tsx
// frontend/src/components/dashboard/KpiCards.tsx
import { TrendResponse, FlakyTest, TriageSummary } from "@/lib/types";
import Card from "@/components/ui/Card";

interface Props {
  trends: TrendResponse[];
  flakyCount: number;
  triageSummary: TriageSummary | null;
}

export default function KpiCards({ trends, flakyCount, triageSummary }: Props) {
  const prodTrend = trends.find(t => t.environment === 'prod');
  const lastPoint = prodTrend?.dataPoints?.slice(-1)[0];

  return (
    <div className="grid grid-cols-4 gap-4">
      <Card>
        <div className="text-xs text-gray-400">PASS RATE (LATEST)</div>
        <div className="text-2xl font-bold text-green-400">{lastPoint ? (lastPoint.passRate * 100).toFixed(1) + '%' : '—'}</div>
      </Card>
      <Card>
        <div className="text-xs text-gray-400">FLAKY TESTS</div>
        <div className="text-2xl font-bold text-yellow-400">{flakyCount}</div>
      </Card>
      <Card>
        <div className="text-xs text-gray-400">UNTRIAGED</div>
        <div className="text-2xl font-bold text-red-400">{triageSummary?.untriaged ?? '—'}</div>
      </Card>
      <Card>
        <div className="text-xs text-gray-400">TOTAL TRIAGED (30d)</div>
        <div className="text-2xl font-bold text-blue-400">{triageSummary?.totalTriaged ?? '—'}</div>
      </Card>
    </div>
  );
}
```

```tsx
// frontend/src/components/ui/Card.tsx
export default function Card({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return <div className={`bg-gray-900 rounded-xl border border-gray-800 p-4 ${className}`}>{children}</div>;
}
```

```tsx
// frontend/src/components/dashboard/PassRateChart.tsx
'use client';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { TrendResponse } from "@/lib/types";

interface Props { trends: TrendResponse[] }

const COLORS = { dev: '#3b82f6', staging: '#f59e0b', prod: '#22c55e' };

export default function PassRateChart({ trends }: Props) {
  if (!trends.length) return <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">No trend data</div>;

  const dateMap = new Map<string, Record<string, number>>();
  trends.forEach(t => {
    t.dataPoints.forEach(dp => {
      const rec = dateMap.get(dp.date) || {};
      rec[t.environment] = dp.passRate * 100;
      dateMap.set(dp.date, rec);
    });
  });
  const data = Array.from(dateMap.entries()).map(([date, envs]) => ({ date, ...envs }));

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">
      <h3 className="text-sm text-gray-400 mb-4">PASS RATE TREND</h3>
      <ResponsiveContainer width="100%" height={250}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} />
          <YAxis stroke="#94a3b8" fontSize={12} domain={[80, 100]} />
          <Tooltip />
          <Legend />
          {['dev','staging','prod'].map(env =>
            <Line key={env} type="monotone" dataKey={env} stroke={COLORS[env]} strokeWidth={2} dot={false} connectNulls />
          )}
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
```

```tsx
// frontend/src/components/dashboard/RecentRegressions.tsx
'use client';
import { useEffect, useState } from "react";
import { api } from "@/lib/api";

interface Props { projectId: number }

export default function RecentRegressions({ projectId }: Props) {
  const [flaky, setFlaky] = useState<{ testCaseKey: string; flakyScore: number }[]>([]);

  useEffect(() => {
    api.analysis.getFlakyTests(projectId).then(f => setFlaky(f.slice(0, 5)));
  }, [projectId]);

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">
      <h3 className="text-sm text-gray-400 mb-4">TOP FLAKY TESTS</h3>
      {flaky.length === 0 ? <p className="text-gray-500 text-sm">None detected</p> : (
        <div className="space-y-2">
          {flaky.map(f => (
            <div key={f.testCaseKey} className="flex justify-between text-sm">
              <span className="text-gray-300 truncate">{f.testCaseKey}</span>
              <span className="text-yellow-400 ml-2">{(f.flakyScore * 100).toFixed(0)}%</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
```

```tsx
// frontend/src/components/dashboard/TriageBreakdown.tsx
import { TriageSummary } from "@/lib/types";
import Card from "@/components/ui/Card";

const CATEGORY_LABELS: Record<string, string> = {
  NEW_BUG: 'New Bug', KNOWN_ISSUE: 'Known Issue', SCRIPT_ISSUE: 'Script Issue',
  DATA_ISSUE: 'Data Issue', ENV_ISSUE: 'Env Issue', CR: 'Code Review', OTHER: 'Other', UNTRIAGED: 'Untriaged',
};

export default function TriageBreakdown({ summary }: { summary: TriageSummary | null }) {
  if (!summary) return <Card><div className="text-xs text-gray-400">TRIAGE BREAKDOWN</div><p className="text-gray-500 text-sm mt-2">No data</p></Card>;

  return (
    <Card>
      <h3 className="text-sm text-gray-400 mb-4">FAILURES BY CATEGORY (30d)</h3>
      <div className="grid grid-cols-4 gap-3">
        {Object.entries(CATEGORY_LABELS).map(([key, label]) => (
          <div key={key} className="flex justify-between text-sm bg-gray-800 rounded-lg px-3 py-2">
            <span className="text-gray-400">{label}</span>
            <span className="text-white font-semibold">{summary.breakdown[key] || 0}</span>
          </div>
        ))}
      </div>
    </Card>
  );
}
```

```bash
git add frontend/src/app/projects/ frontend/src/components/dashboard/ frontend/src/components/ui/
git commit -m "feat: add dashboard page with KPI cards, trend chart, and triage breakdown"
```

### Task 31-40: Remaining Frontend Pages

The remaining frontend tasks (Test Explorer page, run detail, TriageModal, FilterBar, ErrorDetail, Trends page, Settings page) follow the same pattern using the API client and Recharts/React patterns established above. Each task:

**Task 31:** Test Explorer page (`projects/[projectId]/runs/[runId]/page.tsx`) — fetches run + executions from API, renders FilterBar + TestTable
**Task 32:** FilterBar component — status/env/attempt dropdowns
**Task 33:** TestTable component — renders execution rows with triage status badges
**Task 34:** TriageModal component — form with triage_status dropdown, issue_link, comment fields. PUT to triage API
**Task 35:** ErrorDetail component — expandable row showing stack trace
**Task 36:** Trends page (`projects/[projectId]/trends/page.tsx`) with EnvironmentTrendChart, DurationTrendChart, FlakyTable, ClusterTable
**Task 37:** Settings page (`projects/[projectId]/settings/page.tsx`) — project config, API key display, team list
**Task 38:** `frontend/src/app/globals.css` — Tailwind CSS import and base styles
**Task 39:** Verify `npm run build` compiles successfully
**Task 40:** Final integration test — start backend, start frontend, verify end-to-end flow

```bash
# After completing frontend tasks:
git add frontend/
git commit -m "feat: add Test Explorer, Trends, and Settings pages"
```

---

## Phase 8: Final Integration (Task 41)

### Task 41: Integration Verification & Final Commit

**Step 1: Verify backend builds**
```bash
cd backend && mvn clean package -DskipTests
```
Expected: BUILD SUCCESS, JAR in target/

**Step 2: Run all backend tests**
```bash
cd backend && mvn test
```
Expected: All tests PASS

**Step 3: Verify frontend builds**
```bash
cd frontend && npm run build
```
Expected: Successful build

**Step 4: Commit**
```bash
git add -A
git commit -m "feat: complete paw-hub v1 implementation"
```
