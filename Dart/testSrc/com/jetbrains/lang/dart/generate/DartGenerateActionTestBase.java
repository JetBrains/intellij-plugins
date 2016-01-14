package com.jetbrains.lang.dart.generate;

import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.jetbrains.lang.dart.ide.generation.*;
import org.jetbrains.annotations.NotNull;

abstract public class DartGenerateActionTestBase extends LightPlatformCodeInsightTestCase {
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

  protected void doNamedConstructor() {
    doTest(new DartGenerateNamedConstructorHandler());
  }

  protected void doTest(BaseDartGenerateHandler anAction) {
    configure();
    anAction.invoke(getProject(), getEditor(), getFile());
    checkResultByFile(getTestName(false) + ".txt");
  }

  @Override
  protected void checkResultByText(String message,
                                   @NotNull String fileText,
                                   boolean ignoreTrailingSpaces,
                                   String filePath) {

    //remove all
    super.checkResultByText(message, fileText.replaceAll("<spaces>", ""), ignoreTrailingSpaces, filePath);
  }

  protected void configure() {
    configureByFile(getTestName(false) + ".dart");
  }
}
