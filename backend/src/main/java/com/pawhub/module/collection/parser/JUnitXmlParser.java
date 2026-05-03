package com.pawhub.module.collection.parser;

import com.pawhub.module.collection.entity.TestStatus;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JUnitXmlParser {

    public record ParseResult(List<Execution> executions, int totalCases, int passed, int failed, int skipped, int retried, long durationMs) {}

    public record Execution(String suiteName, String className, String testName, int attempt, TestStatus status,
                            long durationMs, String errorMessage, String errorType, String stackTrace) {}

    public ParseResult parse(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            List<Execution> raw = new ArrayList<>();
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
                    raw.add(new Execution(suiteName, cn, tn, 1, status, dur, errMsg, errType, stack));
                }
            }

            // Assign attempt numbers: group by (suite, class, name), number sequentially
            Map<String, Integer> seen = new HashMap<>();
            List<Execution> executions = new ArrayList<>();
            for (Execution e : raw) {
                String key = e.suiteName() + "#" + e.className() + "#" + e.testName();
                int attempt = seen.merge(key, 1, Integer::sum);
                executions.add(new Execution(e.suiteName(), e.className(), e.testName(),
                    attempt, e.status(), e.durationMs(), e.errorMessage(), e.errorType(), e.stackTrace()));
            }
            int retried = (int) seen.values().stream().filter(v -> v > 1).count();

            return new ParseResult(executions, total, passed, failed, skipped, retried, totalDuration);
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
