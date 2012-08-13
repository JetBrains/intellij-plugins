package org.jetbrains.plugins.cucumber.java.run;

import gherkin.formatter.*;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: Andrey.Vokin
 * Date: 8/10/12
 */
public class CucumberJavaSMFormatter implements Formatter, Reporter {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ");

    private static final String TEMPLATE_TEST_STARTED = "##teamcity[testStarted timestamp = '%s' locationHint = 'file:///%s' captureStandardOutput = 'true' name = '%s']\n\n";
    private static final String TEMPLATE_TEST_FAILED = "##teamcity[testFailed timestamp = '%s' details = '%s' message = '%s' name = '%s']\n\n";
    private static final String TEMPLATE_TEST_FINISHED = "##teamcity[testFinished timestamp = '%s' diagnosticInfo = 'cucumber  f/s=(1344855950447, 1344855950447), duration=0, time.now=%s' duration = '0' name = '%s']\n\n";

    private static final String TEMPLATE_ENTER_THE_MATRIX = "##teamcity[enteredTheMatrix timestamp = '%s']\n\n";

    private static final String TEMPLATE_TEST_SUITE_STARTED = "##teamcity[testSuiteStarted timestamp = '%s' locationHint = 'file://%s' name = '%s']\n\n";
    private static final String TEMPLATE_TEST_SUITE_FINISHED = "##teamcity[testSuiteFinished timestamp = '%s' name = '%s']\n\n";


    private Appendable appendable;

    private Queue<Step> queue;

    private String currentScenarioName;

    private String uri;

    public CucumberJavaSMFormatter(Appendable appendable) {
        this.appendable = appendable;
        queue = new ArrayDeque<Step>();
    }

    @Override
    public void uri(String s) {
        uri = s;
    }

    @Override
    public void feature(Feature feature) {
        final String featureFullName = "Feature: " + feature.getName();
        out(String.format(TEMPLATE_ENTER_THE_MATRIX, getCurrentTime()));
        out(String.format(TEMPLATE_TEST_SUITE_STARTED, getCurrentTime(), uri + ":" + feature.getLine(), featureFullName));
    }

    @Override
    public void background(Background background) {
        out("Background\n");
    }

    @Override
    public void scenario(Scenario scenario) {
        queue.clear();

        final String scenarioFullName = "Scenario: " + scenario.getName();
        currentScenarioName = scenarioFullName;
        out(String.format(TEMPLATE_TEST_SUITE_STARTED, getCurrentTime(), uri + ":" + scenario.getLine(), scenarioFullName));
    }

    @Override
    public void scenarioOutline(ScenarioOutline outline) {

        queue.clear();
        final String scenarioFullName = "Scenario: " + outline.getName();
        currentScenarioName = scenarioFullName;
        out(String.format(TEMPLATE_TEST_SUITE_STARTED, getCurrentTime(), uri + ":" + outline.getLine(), scenarioFullName));
    }

    @Override
    public void examples(Examples examples) {
        out("Examples\n");
    }

    @Override
    public void step(Step step) {
        queue.add(step);
        final String stepFullName = step.getKeyword() + " " + step.getName();
        out(String.format(TEMPLATE_TEST_STARTED, getCurrentTime(), uri + ":" + step.getLine(), stepFullName));
    }

    @Override
    public void eof() {
    }

    @Override
    public void syntaxError(String s, String s1, List<String> strings, String s2, Integer integer) {
        out("Syntax error\n");
    }

    @Override
    public void done() {
        out("done\n");
    }

    @Override
    public void close() {
    }

    @Override
    public void before(Match match, Result result) {
        out("before\n");
    }

    @Override
    public void result(Result result) {
        Step step = queue.poll();
        assert step != null;
        String stepFullName = step.getKeyword() + " " + step.getName();
        if (result.getStatus().equals(Result.FAILED)) {
            out(String.format(TEMPLATE_TEST_FAILED, getCurrentTime(), "", escape(result.getErrorMessage()), stepFullName));
        }

        String currentTime = getCurrentTime();
        out(String.format(TEMPLATE_TEST_FINISHED, currentTime, currentTime, stepFullName));

        if (queue.size() == 0) {
            out(String.format(TEMPLATE_TEST_SUITE_FINISHED, getCurrentTime(), currentScenarioName));
        }
    }

    @Override
    public void after(Match match, Result result) {
        out("after\n");
    }

    @Override
    public void match(Match match) {
    }

    @Override
    public void embedding(String s, InputStream inputStream) {
        out("embedding\n");
    }

    @Override
    public void write(String s) {
        out(s);
    }

    private String getCurrentTime() {
        return DATE_FORMAT.format(new Date());
    }

    private String escape(String source) {
        return source.replace("\n", "|n").replace("'", "|'");
    }

    private void out(String s) {
        try {
            appendable.append(s);
        } catch (IOException ignored) {
        }
    }
}
