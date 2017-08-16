package org.intellij.plugins.postcss.resolve;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/resolve/customPropertySet")
public class PostCssCustomPropertySetResolveTest extends PostCssResolveTest {

  public void testResolve() {
    doTest();
  }

  public void testResolveInCustomPropertySet() {
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
    return "customPropertySet";
  }
}