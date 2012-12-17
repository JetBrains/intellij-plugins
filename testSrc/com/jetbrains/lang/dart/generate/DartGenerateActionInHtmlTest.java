package com.jetbrains.lang.dart.generate;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.generation.CreateGetterSetterFix;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartGenerateActionInHtmlTest extends DartGenerateActionTestBase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/generate/html/");
  }

  @Override
  protected void configure() {
    configureByFile(getTestName(false) + ".html");
  }

  public void testImplement3() throws Throwable {
    doImplementTest();
  }

  public void testOverride3() throws Throwable {
    doOverrideTest();
  }

  public void testGetterSetter3() throws Throwable {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTERSETTER);
  }
}
