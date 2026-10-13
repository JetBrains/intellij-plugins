package org.intellij.plugins.postcss.parser;

import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.Test;

@TestApplication
public class PostCssParsingMediaRangesTest extends PostCssParsingTest {
  public PostCssParsingMediaRangesTest() {
    super("mediaRanges");
  }

  @Test
  public void testMediaRangeNameValue() {
    doTest();
  }
  @Test
  public void testMediaRangeValueName() {
    doTest();
  }
  @Test
  public void testMediaRangeValueNameValue() {
    doTest();
  }
  @Test
  public void testMediaPlainFeature() {
    doTest();
  }
  @Test
  public void testMediaRangeAndOtherFeatures() {
    doTest();
  }
  @Test
  public void testMediaRangeWithoutUnit() {
    doTest();
  }
  @Test
  public void testMediaRangeWithMinus() {
    doTest();
  }
  @Test
  public void testMediaRangeIncomplete() {
    doTest();
  }
  @Test
  public void testMediaRangeTwoValuesIncomplete() {
    doTest();
  }
  @Test
  public void testMediaRangeValueIncomplete() {
    doTest();
  }
  @Test
  public void testMediaRangeOperatorExpected() {
    doTest();
  }
  @Test
  public void testMediaRangeWithIncorrectRParen() {
    doTest();
  }
}