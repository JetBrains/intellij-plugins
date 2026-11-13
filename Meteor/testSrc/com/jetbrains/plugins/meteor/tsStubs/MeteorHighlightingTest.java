package com.jetbrains.plugins.meteor.tsStubs;


import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/Meteor/testData/testHighlighting")
public class MeteorHighlightingTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUp() {
    MeteorTestUtil.enableMeteor();
    MeteorTestUtil.initMeteorDirs(codeInsight.get().getProject());
    enableInspections();
  }

  @AfterEach
  void tearDown() {
    MeteorTestUtil.disableMeteor();
  }

  @Test
  public void testTemplateWithoutName() {
    doTest("templateWithoutName");
  }

  private void enableInspections() {
    codeInsight.get().enableInspections(new RequiredAttributesInspection());
  }

  private void doTest(String testName) {
    codeInsight.get().testHighlighting(testName + ".spacebars");
  }

  @Test
  public void testResolveToJSInScriptTag() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    Project project = myFixture.getProject();
    String testName = "ResolveToJSInScriptTag";
    final boolean oldValue = HbConfig.shouldOpenHtmlAsHandlebars(project);
    try {
      HbConfig.setShouldOpenHtmlAsHandlebars(true, project);
      PsiReference reference = myFixture.getReferenceAtCaretPosition(testName + ".js", testName + ".html");
      assertInstanceOf(JSReferenceExpression.class, reference);
      // The result may be null if the Hb file element type is not a stub. The test checks only that resolve does not throw an assertion.
      JSTestUtils.runInBackgroundReadActionAndWait(() -> reference.resolve());
    }
    finally {
      HbConfig.setShouldOpenHtmlAsHandlebars(oldValue, project);
    }
  }
}
