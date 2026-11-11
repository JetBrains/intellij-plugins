// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.diagrams;

import com.intellij.lang.javascript.modules.diagram.JSModuleDiagramChecker;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.angularjs.diagram.AngularModulesProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
public class DiagramsTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  @BeforeEach
  void setUp() {
    codeInsightFixture.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(DiagramsTest.class));
  }

  @Test
  public void testAngularJSDiagrams() {
    CodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureFromTempProjectFile("components/components.module.js");

    ReadAction.runBlocking(() -> {
      final GlobalSearchScope scope = GlobalSearchScope.fileScope(myFixture.getProject(), myFixture.getFile().getVirtualFile());
      final JSModuleDiagramChecker checker = new JSModuleDiagramChecker(myFixture.getProject(), scope);

      checker.assertExisting("components/components.module.js")
        .assertImported("components", "auth/auth.module.js", "components.auth");
    });
  }

  @Test
  public void testProviderSingleFileResults() {
    CodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureFromTempProjectFile("components/components.module.js");

    AngularModulesProvider provider = new AngularModulesProvider();
    assertEquals(1, ReadAction.computeBlocking(() -> provider.getDependencies(myFixture.getFile())).size());
  }

}
