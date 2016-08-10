package org.intellij.plugins.postcss.parser;

public class PostCssParsingCustomMediaTest extends PostCssParsingTest {
  public PostCssParsingCustomMediaTest() {
    super("customMedia");
  }

  public void testCustomMediaEmpty() {
    doTest();
  }
  public void testCustomMediaEmptyWithoutSemicolon() {
    doTest();
  }
  public void testCustomMediaEmptyWithSemicolon() {
    doTest();
  }
  public void testCustomMediaNested() {
    doTest();
  }
  public void testCustomMediaSimple() {
    doTest();
  }
  public void testCustomMediaWithAnd() {
    doTest();
  }
  public void testCustomMediaWithComma() {
    doTest();
  }
  public void testCustomMediaWithFeature() {
    doTest();
  }
  public void testCustomMediaWithFeatureRange() {
    doTest();
  }
  public void testCustomMediaWithoutSemicolon() {
    doTest();
  }
}