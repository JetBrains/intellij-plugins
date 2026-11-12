package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdater;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdaterKt.findAndInitMeteorRoots;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/Meteor/testData/testSpacebarsCompletion")
public class MeteorSpacebarsCompletionTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);
  private String testName;

  @Test
  public void testCompletionTemplates() {
    doSimpleTest();
  }

  @Test
  public void testCompletionTemplateHelper() {
    doSimpleTest();
  }

  @Test
  public void testCompletionGlobalHelper() {
    doSimpleTest();
  }

  @Test
  public void testCompletionGlobalHelperForBlockTag() {
    doSimpleTest();
  }

  private void doSimpleTest() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    String filePath = testName + "/" + "templates" + ".html";
    final String fullPath = testName + "/module";
    myFixture.copyDirectoryToProject(fullPath, "module");
    EdtTestUtil.runInEdtAndWait(() -> PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue());
    myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject(filePath, StringUtil.getShortName(filePath, '/')));
    updateMeteorStatus();

    myFixture.completeBasic();
    String filePath2 = testName + "/" + "templates" + "_after.html";
    myFixture.checkResultByFile(filePath2);
  }

  private void updateMeteorStatus() {
    Project project = codeInsight.get().getProject();
    ReadAction.run(() -> findAndInitMeteorRoots(project));
    MeteorLibraryUpdater.getInstance(project).waitForUpdate();
    IndexingTestUtil.waitUntilIndexesAreReady(project);
    assertTrue(MeteorFacade.getInstance().isMeteorProject(project));
  }

  @BeforeEach
  void setUp(TestInfo testInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    MeteorTestUtil.enableMeteor();
    MeteorLibraryUpdater.getInstance(codeInsight.get().getProject()).waitForUpdate();
  }

  @AfterEach
  void tearDown() {
    MeteorTestUtil.disableMeteor();
  }
}
