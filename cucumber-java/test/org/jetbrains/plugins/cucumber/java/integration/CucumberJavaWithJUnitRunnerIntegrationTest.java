// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.integration;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.junit.TestInClassConfigurationProducer;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.PlatformTestUtil;

/**
 * abstract not to run in on buildserver
 */
@SuppressWarnings("unused")
public abstract class CucumberJavaWithJUnitRunnerIntegrationTest extends JavaTestFrameworkIntegrationTest {
  private static final String EXPECTED_TREE_OF_TESTS =
    "-[root]\n" +
    " -test\n" +
    "  test.passing\n" +
    "  test.failing\n" +
    "  test.failing comparison\n" +
    "  test.pending\n" +
    "  test.undefined\n" +
    "  test.lambda passing\n" +
    "  test.lambda failing\n" +
    "  test.lambda pending\n" +
    "  test.outline\n" +
    "  test.outline";

  public void testCucumber_java_5_0_junit() throws ExecutionException, InterruptedException {
    doTest(EXPECTED_TREE_OF_TESTS);
  }

  @Override
  protected RunConfiguration getRunConfiguration() {
    TestInClassConfigurationProducer runConfigurationProducer = new TestInClassConfigurationProducer();
    PsiElement element = PlatformTestUtil.findElementBySignature("MyCucumberTest", "src/test/java/com/sample/MyCucumberTest.java", getProject());
    return PlatformTestUtil.getRunConfiguration(element, runConfigurationProducer);
  }
}
