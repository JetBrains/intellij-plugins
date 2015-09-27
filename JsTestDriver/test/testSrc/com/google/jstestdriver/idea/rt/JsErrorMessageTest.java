package com.google.jstestdriver.idea.rt;

import com.google.jstestdriver.idea.JstdTestRoot;
import com.google.jstestdriver.idea.rt.util.JsErrorMessage;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class JsErrorMessageTest extends TestCase {

  private File myBasePath;

  public void setUp() throws Exception {
    super.setUp();
    myBasePath = JstdTestRoot.getTestDataDir();
  }

  public void testReferenceErrorParsing() throws Exception {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:2: Uncaught ReferenceError: gg is not defined";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 2);
    Assert.assertEquals(errorMessage.getColumnNumber(), null);
    Assert.assertEquals(errorMessage.getErrorName(), "Uncaught ReferenceError");
  }

  public void testStrangeErrorName() throws Exception {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:1: Uncaught #<Object>";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 1);
    Assert.assertEquals(errorMessage.getColumnNumber(), null);
    Assert.assertEquals(errorMessage.getErrorName(), "Uncaught Error");
  }

  public void testColumnNumber() throws Exception {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:1:10: Uncaught ReferenceError";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 1);
    Assert.assertEquals(errorMessage.getColumnNumber(), new Integer(10));
    Assert.assertEquals(errorMessage.getErrorName(), "Uncaught ReferenceError");
  }

  public void testOperaError() throws Exception {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:1: Uncaught exception: ReferenceError: Undefined variable: gg";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 1);
    Assert.assertEquals(errorMessage.getColumnNumber(), null);
    Assert.assertEquals(errorMessage.getErrorName(), "Uncaught ReferenceError");
  }

  public void testName() throws Exception {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:1: ReferenceError: s is not defined";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 1);
    Assert.assertEquals(errorMessage.getColumnNumber(), null);
    Assert.assertEquals(errorMessage.getErrorName(), "ReferenceError");
  }

  public void testUncaughtError() throws Exception {
    String text = "error loading file: /test/assertFramework/jstd/structure/emptyTestCase.js:301: Uncaught Error: xhrFailed";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/jstd/structure/emptyTestCase.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 301);
    Assert.assertEquals(errorMessage.getColumnNumber(), null);
    Assert.assertEquals(errorMessage.getErrorName(), "Uncaught Error");
  }
}
