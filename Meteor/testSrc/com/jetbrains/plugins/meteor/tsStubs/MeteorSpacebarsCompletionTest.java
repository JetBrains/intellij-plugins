package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdater;

import java.util.concurrent.Future;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdaterKt.findAndInitMeteorRoots;

public class MeteorSpacebarsCompletionTest extends CodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return MeteorTestUtil.getBasePath() + "/testSpacebarsCompletion/";
  }

  public void testCompletionTemplates() {
    doSimpleTest();
  }

  public void testCompletionTemplateHelper() {
    doSimpleTest();
  }

  public void testCompletionGlobalHelper() {
    doSimpleTest();
  }

  public void testCompletionGlobalHelperForBlockTag() {
    doSimpleTest();
  }

  private void doSimpleTest() {
    String filePath = getTestName(true) + "/" + "templates" + ".html";
    final String fullPath = getTestName(true) + "/module";
    myFixture.copyDirectoryToProject(fullPath, "module");
    UIUtil.dispatchAllInvocationEvents();
    myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject(filePath, StringUtil.getShortName(filePath, '/')));
    updateMeteorStatus();

    myFixture.completeBasic();
    String filePath2 = getTestName(true) + "/" + "templates" + "_after.html";
    myFixture.checkResultByFile(filePath2);
  }

  private void updateMeteorStatus() {
    findAndInitMeteorRoots(getProject());
    Future<?> updateFinished = getApplication().executeOnPooledThread(MeteorLibraryUpdater.getInstance(getProject())::waitForUpdate);
    PlatformTestUtil.waitForFuture(updateFinished);
    IndexingTestUtil.waitUntilIndexesAreReady(getProject());
    assertTrue(MeteorFacade.getInstance().isMeteorProject(getProject()));
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MeteorTestUtil.enableMeteor();
    MeteorLibraryUpdater.getInstance(getProject()).waitForUpdate();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      MeteorTestUtil.disableMeteor();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }
}
