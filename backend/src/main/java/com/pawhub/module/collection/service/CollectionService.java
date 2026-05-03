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
        run.setRetried(0); // TODO: detect retries from XML (attempt > 1)
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

    public TestRun getRun(Long projectId, Long runId) {
        return testRunRepo.findById(runId)
            .filter(r -> r.getProject().getId().equals(projectId))
            .orElseThrow(() -> new PawHubException("Test run not found", HttpStatus.NOT_FOUND));
    }

    public List<TestExecution> getExecutions(Long runId) {
        return executionRepo.findByTestRunIdOrderByAttemptAsc(runId);
    }
}
