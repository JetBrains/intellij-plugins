// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class CucumberJvmFormatterTest {
  @Test
  public void testGetFeatureName() {
    String featureHeader = "#language: en\n"
                           + "@wip\n" +
                           "Feature: super puper\n" +
                           "  my feature";
    assertEquals("Feature: super puper", CucumberJvmSMFormatterUtil.getFeatureName(featureHeader));
  }

  @Test
  public void testOutputFormatter()
    throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    CucumberJvm2SMFormatter smFormatter = new CucumberJvm2SMFormatter(new PrintStream(byteArrayOutputStream), "<time>");
    CucumberJava2Mock cucumberJava2Mock = new CucumberJava2Mock();
    smFormatter.setEventPublisher(cucumberJava2Mock);
    cucumberJava2Mock.simulateRun();
    String output = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8).replace("\r", "");

    assertEquals(
      "##teamcity[enteredTheMatrix timestamp = '<time>']\n" +
      "##teamcity[customProgressStatus testsCategory = 'Scenarios' count = '0' timestamp = '<time>']\n" +
      "##teamcity[testSuiteStarted timestamp = '<time>' locationHint = 'file://feature|'' name = 'feature|'']\n" +
      "##teamcity[customProgressStatus type = 'testStarted' timestamp = '<time>']\n" +
      "##teamcity[testSuiteStarted timestamp = '<time>' locationHint = 'file://feature|':1' name = 'scenario']\n" +
      "##teamcity[testStarted timestamp = '<time>' locationHint = 'java:test://org.jetbrains.plugins.cucumber.java.run.CucumberJava2Mock/simulateRun' captureStandardOutput = 'true' name = 'Hook: before']\n" +
      "##teamcity[testFinished timestamp = '<time>' duration = '0' name = 'Hook: before']\n" +
      "##teamcity[testStarted timestamp = '<time>' locationHint = 'file:///features/my.feature:3' captureStandardOutput = 'true' name = 'passing step']\n" +
      "##teamcity[testFinished timestamp = '<time>' duration = '0' name = 'passing step']\n" +
      "##teamcity[testStarted timestamp = '<time>' locationHint = 'file:///features/my.feature:4' captureStandardOutput = 'true' name = 'failing step']\n" +
      "##teamcity[testFailed timestamp = '<time>' details = '' message = '' name = 'failing step' ]\n" +
      "##teamcity[testFinished timestamp = '<time>' duration = '0' name = 'failing step']\n" +
      "##teamcity[testStarted timestamp = '<time>' locationHint = 'file:///features/my.feature:5' captureStandardOutput = 'true' name = '\\bstep with \"special|' symbols']\n" +
      "text\n" +
      "##teamcity[testFinished timestamp = '<time>' duration = '0' name = '\\bstep with \"special|' symbols']\n" +
      "##teamcity[testSuiteFinished timestamp = '<time>' name = 'scenario']\n" +
      "##teamcity[customProgressStatus type = 'testFinished' timestamp = '<time>']\n" +
      "##teamcity[testSuiteFinished timestamp = '<time>' name = 'feature|'']\n",
      output);
  }
}
