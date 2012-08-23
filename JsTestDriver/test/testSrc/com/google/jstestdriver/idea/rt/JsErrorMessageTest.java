package com.google.jstestdriver.idea.rt;

import com.google.jstestdriver.idea.util.JsErrorMessage;
import com.intellij.openapi.application.PathManager;
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
    myBasePath = new File(PathManager.getHomePath(), "contrib/JsTestDriver/test/testData/");
  }

  public void testReferenceErrorParsing() throws Exception {
    String text = "error loading file: /test/assertFramework/jasmine/structure/emailValidator.js:2: Uncaught ReferenceError: gg is not defined";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/jasmine/structure/emailValidator.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 2);
    Assert.assertEquals(errorMessage.getColumnNumber(), null);
    Assert.assertEquals(errorMessage.getErrorName(), "ReferenceError");
  }

  public void testStrangeErrorName() throws Exception {
    String text = "error loading file: /test/assertFramework/qunit/structure/basicUsage.js:1: Uncaught #<Object>";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/qunit/structure/basicUsage.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 1);
    Assert.assertEquals(errorMessage.getColumnNumber(), null);
    Assert.assertEquals(errorMessage.getErrorName(), "Error");
  }

  public void testColumnNumber() throws Exception {
    String text = "error loading file: /test/assertFramework/qunit/structure/basicUsage.js:1:10: Uncaught ReferenceError";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/qunit/structure/basicUsage.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 1);
    Assert.assertEquals(errorMessage.getColumnNumber(), new Integer(10));
    Assert.assertEquals(errorMessage.getErrorName(), "ReferenceError");
  }

  public void testOperaError() throws Exception {
    String text = "error loading file: /test/assertFramework/qunit/structure/basicUsage.js:1: Uncaught exception: ReferenceError: Undefined variable: gg";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/qunit/structure/basicUsage.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 1);
    Assert.assertEquals(errorMessage.getColumnNumber(), null);
    Assert.assertEquals(errorMessage.getErrorName(), "ReferenceError");
  }

  public void testName() throws Exception {
    String text = "error loading file: /test/assertFramework/qunit/structure/basicUsage.js:1: ReferenceError: s is not defined";
    JsErrorMessage errorMessage = JsErrorMessage.parseFromText(text, myBasePath);
    Assert.assertNotNull(errorMessage);
    Assert.assertEquals(errorMessage.getFileWithError(), new File(myBasePath, "assertFramework/qunit/structure/basicUsage.js"));
    Assert.assertEquals(errorMessage.getLineNumber(), 1);
    Assert.assertEquals(errorMessage.getColumnNumber(), null);
    Assert.assertEquals(errorMessage.getErrorName(), "ReferenceError");
  }
}
