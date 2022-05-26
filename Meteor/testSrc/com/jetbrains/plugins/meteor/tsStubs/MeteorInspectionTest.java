package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
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
    myFixture.testInspection(getTestName(false), new LocalInspectionToolWrapper(new MeteorUnresolvedSymbolInspection()));
  }
}
