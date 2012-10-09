package org.jetbrains.plugins.cucumber.java.run;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.List;
import java.util.Queue;

/**
 * User: Andrey.Vokin
 * Date: 8/10/12
 */
public class CucumberJavaSMFormatter implements Formatter, Reporter {
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ");

  private static final String TEMPLATE_TEST_STARTED =
    "##teamcity[testStarted timestamp = '%s' locationHint = 'file:///%s' captureStandardOutput = 'true' name = '%s']";
  private static final String TEMPLATE_TEST_FAILED =
    "##teamcity[testFailed timestamp = '%s' details = '%s' message = '%s' name = '%s' %s]";
  private static final String TEMPLATE_TEST_PENDING =
    "##teamcity[testIgnored name = '%s' message = 'Skipped step' timestamp = '%s']";

  private static final String TEMPLATE_TEST_FINISHED =
    "##teamcity[testFinished timestamp = '%s' diagnosticInfo = 'cucumber  f/s=(1344855950447, 1344855950447), duration=0, time.now=%s' duration = '0' name = '%s']";

  private static final String TEMPLATE_ENTER_THE_MATRIX = "##teamcity[enteredTheMatrix timestamp = '%s']";

  private static final String TEMPLATE_TEST_SUITE_STARTED =
    "##teamcity[testSuiteStarted timestamp = '%s' locationHint = 'file://%s' name = '%s']";
  private static final String TEMPLATE_TEST_SUITE_FINISHED = "##teamcity[testSuiteFinished timestamp = '%s' name = '%s']";

  public static final String RESULT_STATUS_PENDING = "pending";

  private Appendable appendable;

  private Queue<String> queue;

  private String uri;
  private String currentFeatureName;

  private boolean beforeExampleSection;

  private ScenarioOutline currentScenarioOutline;

  private Scenario currentScenario;

  private Queue<Step> currentSteps;

  @SuppressWarnings("UnusedDeclaration")
  public CucumberJavaSMFormatter(Appendable appendable) {
    this.appendable = appendable;
    queue = new ArrayDeque<String>();
    currentSteps = new ArrayDeque<Step>();
  }

  @Override
  public void feature(Feature feature) {
    currentFeatureName = "Feature: " + feature.getName();
    outCommand(String.format(TEMPLATE_ENTER_THE_MATRIX, getCurrentTime()));
    outCommand(String.format(TEMPLATE_TEST_SUITE_STARTED, getCurrentTime(), uri + ":" + feature.getLine(), currentFeatureName));
  }

  @Override
  public void scenario(Scenario scenario) {
    closeScenario();
    if (scenario.getKeyword().equals("Scenario")) {
      closeScenarioOutline();
      currentSteps.clear();
    }
    currentScenario = scenario;
    outCommand(String.format(TEMPLATE_TEST_SUITE_STARTED, getCurrentTime(), uri + ":" + scenario.getLine(), getName(currentScenario)));

    while (queue.size() > 0) {
      String smMessage = queue.poll();
      outCommand(smMessage);
    }
  }

  @Override
  public void scenarioOutline(ScenarioOutline outline) {
    queue.clear();
    currentSteps.clear();

    closePreviousScenarios();
    currentScenarioOutline = outline;
    currentScenario = null;
    beforeExampleSection = true;
    outCommand(
      String.format(TEMPLATE_TEST_SUITE_STARTED, getCurrentTime(), uri + ":" + outline.getLine(), getName(currentScenarioOutline)));
  }

  @Override
  public void examples(Examples examples) {
    beforeExampleSection = false;
    outCommand(String.format(TEMPLATE_TEST_SUITE_STARTED, getCurrentTime(), uri + ":" + examples.getLine(), "Examples:"));
  }

  @Override
  public void step(Step step) {
    if (beforeExampleSection) {
      return;
    }
    currentSteps.add(step);
    outCommand(String.format(TEMPLATE_TEST_STARTED, getCurrentTime(), uri + ":" + step.getLine(), getName(step)), true);
  }

  @Override
  public void result(Result result) {
    Step currentStep = currentSteps.poll();
    String stepFullName = getName(currentStep);
    if (result.getStatus().equals(Result.FAILED)) {
      String fullMessage = result.getErrorMessage().replace("\r", "").replace("\t", "  ");
      String[] messageInfo = fullMessage.split("\n", 2);
      final String message;
      final String details;
      if (messageInfo.length == 2) {
        message = messageInfo[0].trim();
        details = messageInfo[1].trim();
      }
      else {
        message = fullMessage;
        details = "";
      }

      outCommand(String.format(TEMPLATE_TEST_FAILED, getCurrentTime(), escape(details), escape(message), stepFullName, ""), true);
    }
    else if (result.getStatus().equals(RESULT_STATUS_PENDING)) {
      outCommand(String.format(TEMPLATE_TEST_PENDING, stepFullName, getCurrentTime()), true);
    }
    else if (result.equals(Result.UNDEFINED)) {
      String message = "Undefined step: " + getName(currentStep);
      String details = "";
      outCommand(String.format(TEMPLATE_TEST_FAILED, getCurrentTime(), escape(details), escape(message), stepFullName, "error = 'true'"),
                 true);
    }

    String currentTime = getCurrentTime();
    outCommand(String.format(TEMPLATE_TEST_FINISHED, currentTime, currentTime, stepFullName), true);
  }

  private void closeScenario() {
    if (currentScenario != null) {
      outCommand(String.format(TEMPLATE_TEST_SUITE_FINISHED, getCurrentTime(), getName(currentScenario)));
    }
  }

  private void closeScenarioOutline() {
    if (currentScenarioOutline != null) {
      if (!beforeExampleSection) {
        outCommand(String.format(TEMPLATE_TEST_SUITE_FINISHED, getCurrentTime(), "Examples:"));
      }
      outCommand(String.format(TEMPLATE_TEST_SUITE_FINISHED, getCurrentTime(), getName(currentScenarioOutline)));
    }
  }

  private void closePreviousScenarios() {
    closeScenario();
    closeScenarioOutline();
  }

  @Override
  public void background(Background background) {
    closeScenario();
    currentScenario = null;
  }

  @Override
  public void done() {
    closePreviousScenarios();
    outCommand(String.format(TEMPLATE_TEST_SUITE_FINISHED, getCurrentTime(), currentFeatureName));
  }

  @Override
  public void uri(String s) {
    uri = s;
  }

  @Override
  public void eof() {
  }

  @Override
  public void syntaxError(String s, String s1, List<String> strings, String s2, Integer integer) {
    outCommand("Syntax error\n");
  }

  @Override
  public void after(Match match, Result result) {
    outCommand("after\n");
  }

  @Override
  public void match(Match match) {
  }

  @Override
  public void embedding(String mimeType, byte[] data) {
    outCommand("embedding\n");
  }

  @Override
  public void write(String s) {
    out(s);
  }

  @Override
  public void close() {
    out("Close\n");
  }

  @Override
  public void before(Match match, Result result) {
    outCommand("before\n");
  }

  private String getCurrentTime() {
    return DATE_FORMAT.format(new Date());
  }

  private String escape(String source) {
    return source.replace("\n", "|n").replace("'", "|'");
  }

  private void outCommand(String s) {
    outCommand(s, false);
  }

  private void outCommand(String s, boolean waitForScenario) {
    if (currentScenario == null && waitForScenario) {
      queue.add(s);
    }
    else {
      try {
        appendable.append("\n");
        appendable.append(s);
        appendable.append("\n");
      }
      catch (IOException ignored) {
      }
    }
  }

  private void out(String s) {
    try {
      appendable.append(s);
    }
    catch (IOException ignored) {
    }
  }

  private String getName(Scenario scenario) {
    if (scenario.getKeyword().equals("Scenario Outline")) {
      return "Scenario: Line: " + scenario.getLine();
    }
    else {
      return "Scenario: " + scenario.getName();
    }
  }

  private String getName(ScenarioOutline outline) {
    return "Scenario Outline: " + outline.getName();
  }

  private String getName(Step step) {
    return step.getKeyword() + " " + step.getName();
  }
}
