package com.dmarcotte.handlebars.format;

import com.intellij.lang.javascript.JavaScriptFileType;

public class HbFormatterSampleFileTest extends HbFormatterTest {

  public void testContactsSampleFile()
    throws Exception {
    doFileBasedTest(getTestName(false) + ".html");
  }

  public void testTodosSampleFile()
    throws Exception {
    doFileBasedTest(getTestName(false) + ".html");
  }

  public void testHbTagHashAlignment()
    throws Exception {
    doFileBasedTest();
  }

  /**
   * Test out formatting with a non-HTML template data language
   *
   */
  public void testSampleFileWithCustomTemplateDataLang()
    throws Exception {
    doFileBasedTest("JSSampleFile.hbs", JavaScriptFileType.INSTANCE);
  }
}
