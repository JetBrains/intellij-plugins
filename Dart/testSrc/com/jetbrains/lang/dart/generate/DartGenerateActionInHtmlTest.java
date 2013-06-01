package com.jetbrains.lang.dart.generate;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.generation.CreateGetterSetterFix;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartGenerateActionInHtmlTest extends DartGenerateActionTestBase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + FileUtil.toSystemDependentName("/generate/html/");
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
