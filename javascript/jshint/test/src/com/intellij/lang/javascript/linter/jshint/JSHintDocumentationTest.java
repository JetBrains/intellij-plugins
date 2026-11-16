package com.intellij.lang.javascript.linter.jshint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSHintDocumentationTest {
  @Test
  public void testDocumentationParsing() {
    assertNotNull(JSHintDocumentation.getInstance());
  }
}
