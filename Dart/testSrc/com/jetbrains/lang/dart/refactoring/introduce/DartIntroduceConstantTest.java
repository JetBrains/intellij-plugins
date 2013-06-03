package com.jetbrains.lang.dart.refactoring.introduce;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceFinalVariableHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceHandler;
import com.jetbrains.lang.dart.util.DartTestUtils;

/**
 * @author: Fedor.Korotkov
 */
public class DartIntroduceConstantTest extends DartIntroduceTestBase {
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/refactoring/introduceConstant/");
  }

  @Override
  protected DartIntroduceHandler createHandler() {
    return new DartIntroduceFinalVariableHandler();
  }

  public void testReplaceAll1() throws Throwable {
    doTest();
  }
}
