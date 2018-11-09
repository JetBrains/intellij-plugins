// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.openapi.vfs.CharsetToolkit;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

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
    String output = new String(byteArrayOutputStream.toByteArray(), CharsetToolkit.UTF8_CHARSET).replace("\r", "");

    assertEquals(
      "##teamcity[enteredTheMatrix timestamp = '<time>']\n" +
      "##teamcity[customProgressStatus testsCategory = 'Scenarios' count = '0' timestamp = '<time>']\n" +
      "##teamcity[testSuiteStarted timestamp = '<time>' locationHint = 'file://feature|'' name = 'feature|'']\n" +
      "##teamcity[testSuiteStarted timestamp = '<time>' locationHint = 'file://feature|':1' name = 'scenario']\n" +
      "##teamcity[testStarted timestamp = '<time>' locationHint = 'java:test://org.jetbrains.plugins.cucumber.java.run.CucumberJava2Mock/simulateRun' captureStandardOutput = 'true' name = 'Hook: before']\n" +
      "##teamcity[testFinished timestamp = '<time>' duration = '0' name = 'Hook: before']\n" +
      "##teamcity[testStarted timestamp = '<time>' locationHint = 'file:///features/my.feature:3' captureStandardOutput = 'true' name = 'passing step']\n" +
      "##teamcity[testFinished timestamp = '<time>' duration = '0' name = 'passing step']\n" +
      "##teamcity[testStarted timestamp = '<time>' locationHint = 'file:///features/my.feature:4' captureStandardOutput = 'true' name = 'failing step']\n" +
      "##teamcity[testFailed timestamp = '<time>' details = '' message = '' name = 'failing step' ]\n" +
      "##teamcity[testFinished timestamp = '<time>' duration = '0' name = 'failing step']\n" +
      "##teamcity[testStarted timestamp = '<time>' locationHint = 'file:///features/my.feature:5' captureStandardOutput = 'true' name = '\\bstep with \"special|\' symbols']\n" +
      "##teamcity[testFinished timestamp = '<time>' duration = '0' name = '\\bstep with \"special|\' symbols']\n" +
      "##teamcity[testSuiteFinished timestamp = '<time>' name = 'scenario']\n" +
      "##teamcity[testSuiteFinished timestamp = '<time>' name = 'feature|'']\n",
      output);
  }
}
