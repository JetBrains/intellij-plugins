package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/Meteor/testData/testResolve")
public class MeteorTemplatesResolveTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  private PsiPolyVariantReference configureByFileText(String text) {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText("testResolve.js", text);
    return (PsiPolyVariantReference)ReadAction.compute(() -> myFixture.getFile().findReferenceAt(myFixture.getCaretOffset()));
  }

  @Test
  public void testResolved() {
    codeInsight.get().copyDirectoryToProject("module", "module");
    PsiPolyVariantReference expression = configureByFileText("var a = Template.myTemp<caret>late1;");
    ResolveResult[] results = JSTestUtils.runInBackgroundReadActionAndWait(() -> expression.multiResolve(false));
    assertEquals(1, results.length);
  }

  @Test
  public void testNotResolved() {
    codeInsight.get().copyDirectoryToProject("module", "module");
    PsiPolyVariantReference expression = configureByFileText("var a = Template.myTemp<caret>late;");
    ResolveResult[] results = JSTestUtils.runInBackgroundReadActionAndWait(() -> expression.multiResolve(false));

    assertEquals(0, results.length);
  }

  @BeforeEach
  void setUp() {
    MeteorTestUtil.enableMeteor();
    MeteorTestUtil.initMeteorDirs(codeInsight.get().getProject());
  }

  @AfterEach
  void tearDown() {
    MeteorTestUtil.disableMeteor();
  }
}
