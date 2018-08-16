package com.google.jstestdriver.idea.rt;

import com.google.jstestdriver.idea.JstdTestRoot;
import com.google.jstestdriver.idea.common.JsErrorMessage;
import junit.framework.TestCase;

import java.io.File;

public class JsErrorMessageTest extends TestCase {

  private File myBasePath;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myBasePath = JstdTestRoot.getTestDataDir();
  }

  public void testReferenceErrorParsing() {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:2: Uncaught ReferenceError: gg is not defined";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    assertNotNull(errorMessage);
    assertEquals(new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"), errorMessage.getFileWithError());
    assertEquals(2, errorMessage.getLineNumber());
    assertEquals(null, errorMessage.getColumnNumber());
    assertEquals("Uncaught ReferenceError", errorMessage.getErrorName());
  }

  public void testStrangeErrorName() {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:1: Uncaught #<Object>";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    assertNotNull(errorMessage);
    assertEquals(new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"), errorMessage.getFileWithError());
    assertEquals(1, errorMessage.getLineNumber());
    assertEquals(null, errorMessage.getColumnNumber());
    assertEquals("Uncaught Error", errorMessage.getErrorName());
  }

  public void testColumnNumber() {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:1:10: Uncaught ReferenceError";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    assertNotNull(errorMessage);
    assertEquals(new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"), errorMessage.getFileWithError());
    assertEquals(1, errorMessage.getLineNumber());
    assertEquals(new Integer(10), errorMessage.getColumnNumber());
    assertEquals("Uncaught ReferenceError", errorMessage.getErrorName());
  }

  public void testOperaError() {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:1: Uncaught exception: ReferenceError: Undefined variable: gg";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    assertNotNull(errorMessage);
    assertEquals(new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"), errorMessage.getFileWithError());
    assertEquals(1, errorMessage.getLineNumber());
    assertEquals(null, errorMessage.getColumnNumber());
    assertEquals("Uncaught ReferenceError", errorMessage.getErrorName());
  }

  public void testName() {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:1: ReferenceError: s is not defined";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    assertNotNull(errorMessage);
    assertEquals(new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"), errorMessage.getFileWithError());
    assertEquals(1, errorMessage.getLineNumber());
    assertEquals(null, errorMessage.getColumnNumber());
    assertEquals("ReferenceError", errorMessage.getErrorName());
  }

  public void testUncaughtError() {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:301: Uncaught Error: xhrFailed";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    assertNotNull(errorMessage);
    assertEquals(new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"), errorMessage.getFileWithError());
    assertEquals(301, errorMessage.getLineNumber());
    assertEquals(null, errorMessage.getColumnNumber());
    assertEquals("Uncaught Error", errorMessage.getErrorName());
  }
}
