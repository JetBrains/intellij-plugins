package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.ui.UIUtil;


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
    myFixture.completeBasic();
    String filePath2 = getTestName(true) + "/" + "templates" + "_after.html";
    myFixture.checkResultByFile(filePath2);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MeteorTestUtil.enableMeteor();
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
