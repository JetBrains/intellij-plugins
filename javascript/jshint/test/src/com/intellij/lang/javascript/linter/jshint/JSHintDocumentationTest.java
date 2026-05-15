package com.intellij.lang.javascript.linter.jshint;

import junit.framework.TestCase;

public class JSHintDocumentationTest extends TestCase {
  public void testDocumentationParsing() {
    assertNotNull(JSHintDocumentation.getInstance());
  }
}
