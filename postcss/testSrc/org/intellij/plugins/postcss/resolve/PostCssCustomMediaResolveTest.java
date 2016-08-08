package org.intellij.plugins.postcss.resolve;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/resolve/customMedia")
public class PostCssCustomMediaResolveTest extends PostCssResolveTest {

  public void testResolve() throws Throwable {
    doTest();
  }

  public void testResolveInCustomMediaAtRule() throws Throwable {
    doTest();
  }

  public void testResolveWithImport() throws Throwable {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testResolveWithoutImport() throws Throwable {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testResolveMulti() throws Throwable {
    doTest(2);
  }

  public void testResolveMultiInDifferentFiles() throws Throwable {
    myFixture.configureByFile("definition.pcss");
    doTest(2);
  }

  public void testInline() throws Throwable {
    doTest(1, "html");
  }

  @Override
  @NotNull
  protected String getTestDataSubdir() {
    return "customMedia";
  }
}