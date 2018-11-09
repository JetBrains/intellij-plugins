package org.intellij.plugins.postcss.parser;

public class PostCssParsingMediaRangesTest extends PostCssParsingTest {
  public PostCssParsingMediaRangesTest() {
    super("mediaRanges");
  }

  public void testMediaRangeNameValue() {
    doTest();
  }
  public void testMediaRangeValueName() {
    doTest();
  }
  public void testMediaRangeValueNameValue() {
    doTest();
  }
  public void testMediaPlainFeature() {
    doTest();
  }
  public void testMediaRangeAndOtherFeatures() {
    doTest();
  }
  public void testMediaRangeWithoutUnit() {
    doTest();
  }
  public void testMediaRangeWithMinus() {
    doTest();
  }
  public void testMediaRangeIncomplete() {
    doTest();
  }
  public void testMediaRangeTwoValuesIncomplete() {
    doTest();
  }
  public void testMediaRangeValueIncomplete() {
    doTest();
  }
  public void testMediaRangeOperatorExpected() {
    doTest();
  }
  public void testMediaRangeWithIncorrectRParen() {
    doTest();
  }
}