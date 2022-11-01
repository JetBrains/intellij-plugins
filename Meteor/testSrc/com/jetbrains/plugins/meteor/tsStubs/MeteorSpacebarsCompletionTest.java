package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.ide.action.MeteorLibraryUpdater;


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
    MeteorLibraryUpdater.get(getProject()).scheduleProjectUpdate();
    while (!StartupManagerEx.getInstanceEx(getProject()).postStartupActivityPassed()) {
      UIUtil.dispatchAllInvocationEvents();
    }
    MeteorLibraryUpdater.get(getProject()).waitForUpdate();
    UIUtil.dispatchAllInvocationEvents();
    MeteorLibraryUpdater.get(getProject()).waitForUpdate();
    assertTrue(MeteorFacade.getInstance().isMeteorProject(getProject()));
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MeteorTestUtil.enableMeteor();
    MeteorLibraryUpdater.get(getProject()).waitForUpdate();
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
