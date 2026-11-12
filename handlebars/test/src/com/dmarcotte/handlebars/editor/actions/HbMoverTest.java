package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class HbMoverTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    fixture.get().setTestDataPath(HbTestUtils.BASE_TEST_DATA_PATH + "/mover");
  }

  @Test
  public void testMoveHtmlTextWhenOpenHtmlAsHandlebars(@TestDisposable Disposable disposable) {
    HbTestUtils.setOpenHtmlAsHandlebars(true, fixture.get().getProject(), disposable);
    doTest("hbs");
  }

  @Test
  public void testMoveHbsTag() {
    doTest("hbs");
  }

  private void doTest(@NotNull String ext) {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(myTestName + "." + ext);
    myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_UP_ACTION);
    myFixture.checkResultByFile(myTestName + "_after." + ext);
  }
}
