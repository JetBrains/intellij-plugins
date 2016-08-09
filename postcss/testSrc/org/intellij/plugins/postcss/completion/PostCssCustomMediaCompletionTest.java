package org.intellij.plugins.postcss.completion;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomMediaCompletionTest extends PostCssCompletionTest {

  public void testCustomMediaTopLevel() {
    doTest();
  }

  public void testCustomMediaInsideRuleset() {
    doTest();
  }

  public void testCustomMediaInsideAtRule() {
    doTest();
  }

  public void testCustomMediaInsideNest() {
    doTest();
  }

  public void testSpaceAfterCaret() {
    doTest();
  }

  public void testSemicolonAfterCaret() {
    doTest();
  }

  public void testSpaceAndSemicolonAfterCaret() {
    doTest();
  }

  public void testSemicolonWithWhitespacesAfterCaret() {
    doTest();
  }

  public void testCustomMediaType() {
    doTest();
  }

  public void testCustomMediaTwoDefinitions() {
    doTestPreferred("--test", "--test2");
  }

  public void testCustomMediaPriorityWithImport() {
    myFixture.configureByFile("definition.pcss");
    doTestPreferred(Pair.create("test", "definition.pcss:1"), Pair.create("z-in-file", "customMediaPriorityWithImport.pcss:3"));
  }

  public void testCustomMediaPriorityWithoutImport() {
    myFixture.configureByFile("definition.pcss");
    doTestPreferred(Pair.create("z-in-file", "customMediaPriorityWithoutImport.pcss:1"), Pair.create("test", "definition.pcss:1"));
  }

  public void testCustomMediaWithImport() {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testCustomMediaWithoutImport() {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testCustomMediaInInline() {
    myFixture.testCompletion(getTestName(true) + ".html", getTestName(true) + "_after.html");
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customMedia";
  }
}