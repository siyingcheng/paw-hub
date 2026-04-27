package com.pawhub.config;

import com.pawhub.module.analysis.entity.FailureCluster;
import com.pawhub.module.analysis.entity.FlakyTestRecord;
import com.pawhub.module.analysis.entity.TrendSnapshot;
import com.pawhub.module.analysis.repository.FailureClusterRepository;
import com.pawhub.module.analysis.repository.FlakyTestRecordRepository;
import com.pawhub.module.analysis.repository.TrendSnapshotRepository;
import com.pawhub.module.auth.entity.*;
import com.pawhub.module.auth.repository.*;
import com.pawhub.module.collection.entity.TestExecution;
import com.pawhub.module.collection.entity.TestRun;
import com.pawhub.module.collection.entity.TestStatus;
import com.pawhub.module.collection.repository.TestExecutionRepository;
import com.pawhub.module.collection.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("default")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final Random rng = new Random(42);

    private final OrganizationRepository orgRepo;
    private final TeamRepository teamRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final MembershipRepository membershipRepo;
    private final PasswordEncoder passwordEncoder;
    private final TestRunRepository testRunRepo;
    private final TestExecutionRepository executionRepo;
    private final TrendSnapshotRepository trendRepo;
    private final FlakyTestRecordRepository flakyRepo;
    private final FailureClusterRepository clusterRepo;

    public DataInitializer(OrganizationRepository orgRepo, TeamRepository teamRepo,
                           ProjectRepository projectRepo, UserRepository userRepo,
                           MembershipRepository membershipRepo, PasswordEncoder passwordEncoder,
                           TestRunRepository testRunRepo, TestExecutionRepository executionRepo,
                           TrendSnapshotRepository trendRepo, FlakyTestRecordRepository flakyRepo,
                           FailureClusterRepository clusterRepo) {
        this.orgRepo = orgRepo;
        this.teamRepo = teamRepo;
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
        this.membershipRepo = membershipRepo;
        this.passwordEncoder = passwordEncoder;
        this.testRunRepo = testRunRepo;
        this.executionRepo = executionRepo;
        this.trendRepo = trendRepo;
        this.flakyRepo = flakyRepo;
        this.clusterRepo = clusterRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("Data already seeded, skipping.");
            return;
        }
        log.info("Seeding demo data...");

        // --- Auth: org, team, project, users ---
        Organization org = orgRepo.save(new Organization("PawCorp"));
        Team team = teamRepo.save(new Team("QA Team", org));
        Project project = projectRepo.save(new Project("mobile-app", team));

        User alice = userRepo.save(new User("alice", "alice@pawcorp.com", passwordEncoder.encode("pass123")));
        User bob   = userRepo.save(new User("bob", "bob@pawcorp.com", passwordEncoder.encode("pass123")));
        User carol = userRepo.save(new User("carol", "carol@pawcorp.com", passwordEncoder.encode("pass123")));

        membershipRepo.save(new Membership(alice, team, MembershipRole.ADMIN));
        membershipRepo.save(new Membership(bob, team, MembershipRole.QA));
        membershipRepo.save(new Membership(carol, team, MembershipRole.VIEWER));

        // --- Test suites ---
        String[][] suites = {
            {"com.app.AuthTest",        "shouldLogin", "shouldLogout", "shouldRefreshToken", "shouldRejectInvalidPassword"},
            {"com.app.ApiTest",         "createUser", "getUserList", "updateUser", "deleteUser", "searchUsers"},
            {"com.app.DatabaseTest",    "connectDB", "migrateSchema", "queryPerformance", "rollbackTransaction"},
            {"com.app.UITest",          "renderDashboard", "clickNavigation", "formSubmission", "responsiveLayout"},
            {"com.app.IntegrationTest", "endToEndFlow", "apiGatewayHealth", "messageQueueConnect", "cacheInvalidation"},
        };

        String[] envs = {"dev", "staging", "prod"};

        // --- Generate 30 days of test runs (1-3 per day) ---
        List<TestRun> allRuns = new ArrayList<>();
        for (int day = 30; day >= 0; day--) {
            Instant dayStart = Instant.now().minus(day, ChronoUnit.DAYS);
            int runsToday = rng.nextInt(3) + 1;
            for (int r = 0; r < runsToday; r++) {
                String env = envs[rng.nextInt(envs.length)];
                int totalCases = 0, passed = 0, failed = 0, skipped = 0;
                List<TestExecution> execs = new ArrayList<>();

                TestRun run = new TestRun();
                run.setProject(project);
                run.setEnvironment(env);
                run.setRunIdentifier("build-" + (1000 + day * 10 + r));
                run.setBranch(rng.nextBoolean() ? "main" : "feature/test-branch");
                run.setCommitSha(String.format("%040x", rng.nextLong()));
                run.setTriggeredBy(rng.nextBoolean() ? "alice" : "ci-bot");
                run.setCreatedAt(dayStart);

                // Generate test executions
                for (String[] suite : suites) {
                    String suiteName = suite[0];
                    for (int ti = 1; ti < suite.length; ti++) {
                        totalCases++;
                        int attempt = 1;
                        TestStatus status = rollStatus(env);
                        if (status == TestStatus.FAIL && rng.nextDouble() < 0.3) {
                            attempt = 2; // retry
                        }

                        for (int att = 1; att <= attempt; att++) {
                            TestExecution te = new TestExecution();
                            te.setTestRun(run);
                            te.setAttempt(att);
                            te.setSuiteName(suiteName);
                            te.setClassName(suiteName);
                            te.setTestName(suite[ti]);
                            te.setCaseNumber("QA-" + (100 + ti));
                            te.setDurationMs(10 + rng.nextInt(2000));

                            if (att == 2) {
                                te.setStatus(rng.nextDouble() < 0.6 ? TestStatus.PASS : TestStatus.FAIL);
                            } else {
                                te.setStatus(status);
                            }
                            te.setStatus(att == attempt ? (att == 2 ? (rng.nextDouble() < 0.6 ? TestStatus.PASS : TestStatus.FAIL) : status) : status);

                            if (te.getStatus() == TestStatus.FAIL || te.getStatus() == TestStatus.ERROR) {
                                String[] errors = {
                                    "java.lang.AssertionError: Expected 200 but got 500",
                                    "java.net.ConnectException: Connection refused to db.internal:5432",
                                    "org.postgresql.util.PSQLException: FATAL: too many connections",
                                    "java.lang.NullPointerException at AuthService.authenticate(AuthService.java:156)",
                                    "java.util.concurrent.TimeoutException: Request timed out after 30s",
                                    "org.springframework.dao.DataIntegrityViolationException: duplicate key",
                                };
                                te.setErrorMessage(errors[rng.nextInt(errors.length)]);
                                te.setErrorType(te.getErrorMessage().split(":")[0].trim());
                                te.setStackTrace("at " + suite[0] + "." + suite[ti] + "(Unknown Source)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:42)");
                            }
                            execs.add(te);
                        }
                        if (status == TestStatus.PASS || (attempt == 2 && rng.nextDouble() < 0.6)) passed++;
                        else if (status == TestStatus.SKIP) skipped++;
                        else failed++;
                    }
                }

                run.setTotalCases(totalCases);
                run.setPassed(passed);
                run.setFailed(failed);
                run.setSkipped(skipped);
                run.setDurationMs(execs.stream().mapToLong(TestExecution::getDurationMs).sum());
                run.setStatus(failed > totalCases * 0.2 ? TestStatus.FAIL : TestStatus.PASS);
                run.setRawXml("<testsuite name=\"test\">" + totalCases + " cases</testsuite>");
                run = testRunRepo.save(run);

                for (TestExecution te : execs) {
                    te.setTestRun(run);
                    executionRepo.save(te);
                }

                allRuns.add(run);
            }
        }

        // --- Trend snapshots (daily, per env) ---
        for (String env : envs) {
            for (int d = 30; d >= 0; d--) {
                LocalDate date = LocalDate.now().minusDays(d);
                TrendSnapshot snap = new TrendSnapshot();
                snap.setProjectId(project.getId());
                snap.setDate(date);
                snap.setEnvironment(env);
                snap.setPeriodType(TrendSnapshot.PeriodType.DAILY);

                double base = env.equals("prod") ? 0.94 : env.equals("staging") ? 0.90 : 0.85;
                double noise = (rng.nextDouble() - 0.5) * 0.08;
                snap.setPassRate(Math.min(1.0, base + noise));
                snap.setFailureRate(1.0 - snap.getPassRate());
                snap.setAvgDurationMs(3000 + rng.nextInt(5000));
                trendRepo.save(snap);
            }
        }

        // --- Flaky test records ---
        String[] flakyCases = {
            "com.app.AuthTest.shouldRefreshToken",
            "com.app.ApiTest.searchUsers",
            "com.app.DatabaseTest.queryPerformance",
            "com.app.UITest.responsiveLayout",
            "com.app.IntegrationTest.cacheInvalidation",
        };
        for (String key : flakyCases) {
            FlakyTestRecord f = new FlakyTestRecord();
            f.setProjectId(project.getId());
            f.setTestCaseKey(key);
            f.setFlakyScore(0.3 + rng.nextDouble() * 0.6);
            f.setTransitionCount(rng.nextInt(15) + 3);
            f.setRetryPassCount(rng.nextInt(5));
            f.setLastDetectedAt(Instant.now().minus(rng.nextInt(7), ChronoUnit.DAYS));
            flakyRepo.save(f);
        }

        // --- Failure clusters ---
        String[][] clusters = {
            {"e3b0c44298fc1c14", "java.lang.AssertionError: Expected 200 but got 500", "23", "12"},
            {"a7ffc6f8bf1ed76", "java.net.ConnectException: Connection refused to db.internal:5432", "18", "8"},
            {"d2d2d2d2d2d2d2d", "org.postgresql.util.PSQLException: FATAL: too many connections", "11", "3"},
            {"c4c4c4c4c4c4c4c", "java.lang.NullPointerException at AuthService.authenticate", "7", "5"},
        };
        for (String[] c : clusters) {
            FailureCluster fc = new FailureCluster();
            fc.setProjectId(project.getId());
            fc.setClusterKey(c[0]);
            fc.setRepresentativeError(c[1]);
            fc.setOccurrenceCount(Integer.parseInt(c[2]));
            fc.setFirstSeen(Instant.now().minus(30, ChronoUnit.DAYS));
            fc.setLastSeen(Instant.now().minus(Integer.parseInt(c[3]), ChronoUnit.DAYS));
            clusterRepo.save(fc);
        }

        log.info("Seed complete: {} users, {} test runs, {} executions created",
            userRepo.count(), testRunRepo.count(), executionRepo.count());
    }

    private TestStatus rollStatus(String env) {
        return switch (env) {
            case "prod" -> {
                double d = rng.nextDouble();
                yield d < 0.88 ? TestStatus.PASS : d < 0.95 ? TestStatus.FAIL : d < 0.98 ? TestStatus.SKIP : TestStatus.ERROR;
            }
            case "staging" -> {
                double d = rng.nextDouble();
                yield d < 0.82 ? TestStatus.PASS : d < 0.92 ? TestStatus.FAIL : d < 0.96 ? TestStatus.SKIP : TestStatus.ERROR;
            }
            default -> {
                double d = rng.nextDouble();
                yield d < 0.75 ? TestStatus.PASS : d < 0.88 ? TestStatus.FAIL : d < 0.95 ? TestStatus.SKIP : TestStatus.ERROR;
            }
        };
    }
}
