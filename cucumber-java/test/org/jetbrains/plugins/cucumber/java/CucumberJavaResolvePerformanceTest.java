// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.tools.ide.metrics.benchmark.Benchmark;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@NotNullByDefault
public class CucumberJavaResolvePerformanceTest extends BasePlatformTestCase {

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "performance";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }

  public void testVeryLargeNumberOfStepDefinitionsAnnotationStyle() throws IOException {
    doTest();
  }

  public void testVeryLargeNumberOfStepDefinitionsLambdaStyle() throws IOException {
    doTest();
  }

  /// This performance test measures how much time the following scenario takes.
  ///
  /// ### Scenario
  ///
  /// The user has a project containing Gherkin feature files, with steps implemented in Java.
  ///
  /// 1. The user opens a feature file.
  ///
  /// _Now, the IDE resolves step definitions. Unresolved step is highlighted._
  ///
  /// 2. The user edits a step in the feature file, causing a previously resolved step to become unresolved.
  ///
  /// _Now the IDE has to resolve the newly edited step again and highlight it (since it is now unresolved)._
  ///
  /// 3. The user sees that the step they edited is now unresolved (because it is highlighted).
  private void doTest() throws IOException {
    myFixture.enableInspections(new CucumberStepInspection());
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile("test.feature");

    VirtualFile templateFile = myFixture.findFileInTempDir("StepsTemplate.java");
    String stepDefinitionTemplate = new String(templateFile.contentsToByteArray(), StandardCharsets.UTF_8);

    for (int i = 0; i < 10_000; i++) {
      myFixture.addFileToProject(String.format("steps/Steps%d.java", i), stepDefinitionTemplate.replace("NUM", String.valueOf(i)));
    }

    Benchmark.newBenchmark(getTestName(false), () -> {
        myFixture.doHighlighting();
        myFixture.type(" --- oops now this step will be unresolved ---");
        List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
        assertNotEmpty(highlightInfos);
      })
      .setup(() -> PsiManager.getInstance(getProject()).dropPsiCaches())
      .start();
  }
}
