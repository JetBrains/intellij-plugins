package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;


@TestApplication
public class HbsLiveTemplatesTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    fixture.get().setTestDataPath(HbTestUtils.BASE_TEST_DATA_PATH + FileUtil.toSystemDependentName("/liveTemplates/"));
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFiles(myTestName + ".hbs");
    expandTemplate();
    myFixture.checkResultByFile(myTestName + ".after.hbs");
  }

  @Test
  public void testItar() {
    doTest();
  }

  private void expandTemplate() {
    CodeInsightTestFixture myFixture = fixture.get();
    EdtTestUtil.runInEdtAndWait(() -> {
      EditorAction action = (EditorAction)ActionManager.getInstance().getAction(IdeActions.ACTION_EXPAND_LIVE_TEMPLATE_BY_TAB);
      action.actionPerformed(myFixture.getEditor(), DataManager.getInstance().getDataContext());
      NonBlockingReadActionImpl.waitForAsyncTaskCompletion();
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
    });
  }
}
