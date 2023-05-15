package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.plugins.meteor.spacebars.inspection.MeteorUnresolvedSymbolInspection;


public class MeteorInspectionTest extends BasePlatformTestCase {
  @Override
  protected String getBasePath() {
    return MeteorTestUtil.getBasePath() + "/inspections";
  }

  @Override
  protected void setUp() throws Exception {
    MeteorTestUtil.enableMeteor();
    super.setUp();
    MeteorProjectTestBase.initMeteorDirs(getProject());
  }

  @Override
  protected boolean runInDispatchThread() {
    return false;
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

  public void testUnresolvedTemplate() {
    doTest();
  }

  private void doTest() {
    ApplicationManager.getApplication().invokeAndWait(() -> myFixture.testInspection(getTestName(false), new LocalInspectionToolWrapper(new MeteorUnresolvedSymbolInspection())));
  }
}
