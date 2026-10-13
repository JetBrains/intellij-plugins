package org.intellij.plugins.postcss.parser;

import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.Test;

@TestApplication
public class PostCssParsingCustomMediaTest extends PostCssParsingTest {
  public PostCssParsingCustomMediaTest() {
    super("customMedia");
  }

  @Test
  public void testCustomMediaEmpty() {
    doTest();
  }
  @Test
  public void testCustomMediaEmptyWithoutSemicolon() {
    doTest();
  }
  @Test
  public void testCustomMediaEmptyWithSemicolon() {
    doTest();
  }
  @Test
  public void testCustomMediaNested() {
    doTest();
  }
  @Test
  public void testCustomMediaSimple() {
    doTest();
  }
  @Test
  public void testCustomMediaWithAnd() {
    doTest();
  }
  @Test
  public void testCustomMediaWithComma() {
    doTest();
  }
  @Test
  public void testCustomMediaWithFeature() {
    doTest();
  }
  @Test
  public void testCustomMediaWithFeatureRange() {
    doTest();
  }
  @Test
  public void testCustomMediaWithoutSemicolon() {
    doTest();
  }
  @Test
  public void testCustomMediaWithoutSemicolonBeforeBrace() {
    doTest();
  }
}