package com.jetbrains.lang.dart.generate;

import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.jetbrains.lang.dart.ide.generation.*;
import org.jetbrains.annotations.NotNull;

abstract public class DartGenerateActionTestBase extends LightPlatformCodeInsightTestCase {
  protected void doOverrideTest() {
    doTest(new DartOverrideMethodHandler());
  }

  protected void doEqualsAndHashcodeTest() {
    doTest(new DartGenerateEqualsAndHashcodeHandler());
  }

  protected void doImplementTest() {
    doTest(new DartImplementMethodHandler());
  }

  protected void doGetterSetterTest(CreateGetterSetterFix.Strategy strategy) {
    doTest(new DartGenerateAccessorHandler(strategy) {
      @Override
      @NotNull
      protected String getTitle() {
        return "";
      }
    });
  }

  protected void doConstructorTest() {
    doTest(new DartGenerateConstructorHandler());
  }

  protected void doNamedConstructorTest() {
    doTest(new DartGenerateNamedConstructorHandler());
  }

  protected void doToStringTest() {
    doTest(new DartGenerateToStringHandler());
  }

  protected void doTest(BaseDartGenerateHandler anAction) {
    configure();
    anAction.invoke(getProject(), getEditor(), getFile());
    checkResultByFile(getTestName(false) + ".txt");
  }

  @Override
  protected void checkResultByText(String message,
                                   @NotNull String expectedFileText,
                                   boolean ignoreTrailingSpaces,
                                   String filePath) {

    //remove all
    super.checkResultByText(message, expectedFileText.replaceAll("<spaces>", ""), ignoreTrailingSpaces, filePath);
  }

  protected void configure() {
    configureByFile(getTestName(false) + ".dart");
  }
}
