package com.jetbrains.lang.dart.refactoring.introduce;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceFinalVariableHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceHandler;

/**
 * @author: Fedor.Korotkov
 */
public class DartIntroduceConstantTest extends DartIntroduceTestBase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/plugins/Dart/testData/refactoring/introduceConstant/");
  }

  @Override
  protected DartIntroduceHandler createHandler() {
    return new DartIntroduceFinalVariableHandler();
  }

  public void testReplaceAll1() throws Throwable {
    doTest();
  }
}
