package org.intellij.plugins.postcss.resolve;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/resolve/customMedia")
public class PostCssCustomMediaResolveTest extends PostCssResolveTest {

  public void testResolve() {
    doTest();
  }

  public void testResolveInCustomMediaAtRule() {
    doTest();
  }

  public void testResolveWithImport() {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testResolveWithoutImport() {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testResolveMulti() {
    doTest(2);
  }

  public void testResolveMultiInDifferentFiles() {
    myFixture.configureByFile("definition.pcss");
    doTest(2);
  }

  public void testInline() {
    doTest(1, "html");
  }

  @Override
  @NotNull
  protected String getTestDataSubdir() {
    return "customMedia";
  }
}