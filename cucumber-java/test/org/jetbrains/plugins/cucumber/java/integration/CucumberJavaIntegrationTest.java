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
    """
      -[root]
       -Feature: test
        -Scenario: passing
         Given normal step
         And step with parameter "param"
        -Scenario: failing
         Given normal step
         Given failing step
        -Scenario: failing comparison
         Given normal step
         Given failing comparing step
        -Scenario: pending
         Given normal step
         Given pending step
        -Scenario: undefined
         Given normal step
         Given undefined step
        -Scenario: lambda passing
         Given normal step lambda
        -Scenario: lambda failing
         Given normal step lambda
         Given failing step lambda
        -Scenario: lambda pending
         Given normal step lambda
         Given pending step lambda
        -Scenario Outline: outline
         -Examples:
          -Scenario: Line: 39
           Given normal step
           And step with parameter "value1"
           And step with parameter "value1"
          -Scenario: Line: 40
           Given normal step
           And step with parameter "value2"
           And step with parameter "value2\"""";

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
