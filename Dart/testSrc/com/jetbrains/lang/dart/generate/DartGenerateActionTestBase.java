package com.jetbrains.lang.dart.generate;

import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.intellij.testFramework.PlatformTestCase;
import com.jetbrains.lang.dart.ide.generation.*;

/**
 * @author: Fedor.Korotkov
 */
abstract public class DartGenerateActionTestBase extends LightPlatformCodeInsightTestCase {
  protected DartGenerateActionTestBase() {
    PlatformTestCase.initPlatformLangPrefix();
  }

  protected void doOverrideTest() {
    doTest(new DartOverrideMethodHandler());
  }

  protected void doImplementTest() {
    doTest(new DartImplementMethodHandler());
  }

  protected void doGetterSetterTest(CreateGetterSetterFix.Strategy strategy) {
    doTest(new DartGenerateAccessorHandler(strategy) {
      @Override
      protected String getTitle() {
        return "";
      }
    });
  }

  protected void doConstructor() {
    doTest(new DartGenerateConstructorHandler());
  }

  protected void doTest(BaseDartGenerateHandler anAction) {
    configure();
    anAction.invoke(getProject(), getEditor(), getFile());
    checkResultByFile(getTestName(false) + ".txt");
  }

  protected void configure() {
    configureByFile(getTestName(false) + ".dart");
  }
}
