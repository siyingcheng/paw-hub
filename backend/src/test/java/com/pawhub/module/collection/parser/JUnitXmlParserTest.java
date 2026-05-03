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

    @Test
    void shouldDetectRetries() {
        String xml = """
            <?xml version="1.0"?>
            <testsuite name="S" tests="4" failures="0" errors="0" skipped="0" time="3.0">
                <testcase name="flaky" classname="c" time="1.0"/>
                <testcase name="flaky" classname="c" time="1.0"/>
                <testcase name="flaky" classname="c" time="1.0"/>
                <testcase name="stable" classname="c" time="0.0"/>
            </testsuite>""";
        JUnitXmlParser.ParseResult r = parser.parse(xml);
        assertThat(r.executions()).hasSize(4);
        assertThat(r.executions().get(0).attempt()).isEqualTo(1);
        assertThat(r.executions().get(1).attempt()).isEqualTo(2);
        assertThat(r.executions().get(2).attempt()).isEqualTo(3);
        assertThat(r.executions().get(3).attempt()).isEqualTo(1);
        assertThat(r.retried()).isEqualTo(1); // only "flaky" was retried
    }
}
