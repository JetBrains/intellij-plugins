// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.integration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.PlatformTestUtil;
import org.jetbrains.plugins.cucumber.java.run.CucumberJavaFeatureRunConfigurationProducer;

/**
 * abstract not to run in on buildserver
 */
@SuppressWarnings("unused")
public abstract class CucumberJavaIntegrationTest extends JavaTestFrameworkIntegrationTest {
  private static final String EXPECTED_TREE_OF_TESTS =
    "-[root]\n" +
    " -Feature: test\n" +
    "  -Scenario: passing\n" +
    "   Given normal step\n" +
    "   And step with parameter \"param\"\n" +
    "  -Scenario: failing\n" +
    "   Given normal step\n" +
    "   Given failing step\n" +
    "  -Scenario: failing comparison\n" +
    "   Given normal step\n" +
    "   Given failing comparing step\n" +
    "  -Scenario: pending\n" +
    "   Given normal step\n" +
    "   Given pending step\n" +
    "  -Scenario: undefined\n" +
    "   Given normal step\n" +
    "   Given undefined step\n" +
    "  -Scenario: lambda passing\n" +
    "   Given normal step lambda\n" +
    "  -Scenario: lambda failing\n" +
    "   Given normal step lambda\n" +
    "   Given failing step lambda\n" +
    "  -Scenario: lambda pending\n" +
    "   Given normal step lambda\n" +
    "   Given pending step lambda\n" +
    "  -Scenario Outline: outline\n" +
    "   -Examples:\n" +
    "    -Scenario: Line: 39\n" +
    "     Given normal step\n" +
    "     And step with parameter \"value1\"\n" +
    "     And step with parameter \"value1\"\n" +
    "    -Scenario: Line: 40\n" +
    "     Given normal step\n" +
    "     And step with parameter \"value2\"\n" +
    "     And step with parameter \"value2\"";

  public void testCucumber_java_1_0() throws ExecutionException, InterruptedException {
    doTest(EXPECTED_TREE_OF_TESTS);
  }

  public void testCucumber_java_1_2() throws ExecutionException, InterruptedException {
    doTest(EXPECTED_TREE_OF_TESTS);
  }

  public void testCucumber_java_2_0() throws ExecutionException, InterruptedException {
    doTest(EXPECTED_TREE_OF_TESTS);
  }

  public void testCucumber_java_2_4() throws ExecutionException, InterruptedException {
    doTest(EXPECTED_TREE_OF_TESTS);
  }

  public void testCucumber_java_3_0() throws ExecutionException, InterruptedException {
    doTest(EXPECTED_TREE_OF_TESTS);
  }

  public void testCucumber_java_4_5() throws ExecutionException, InterruptedException {
    doTest(EXPECTED_TREE_OF_TESTS);
  }

  public void testCucumber_java_4_7() throws ExecutionException, InterruptedException {
    doTest(EXPECTED_TREE_OF_TESTS);
  }

  public void testCucumber_java_5_0() throws ExecutionException, InterruptedException {
    doTest(EXPECTED_TREE_OF_TESTS);
  }

  @Override
  protected RunConfiguration getRunConfiguration() {
    CucumberJavaFeatureRunConfigurationProducer runConfigurationProducer =  new CucumberJavaFeatureRunConfigurationProducer();
    PsiElement element = PlatformTestUtil.findElementBySignature("my feature", "src/test/resources/test.feature", getProject());
    return PlatformTestUtil.getRunConfiguration(element, runConfigurationProducer);
  }
}
