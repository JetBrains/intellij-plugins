package com.dmarcotte.handlebars.format;

import com.intellij.openapi.fileTypes.StdFileTypes;

public class HbFormatterSampleFileTest extends HbFormatterTest {

  public void testContactsSampleFile()
    throws Exception {
    doFileBasedTest();
  }

  public void testTodosSampleFile()
    throws Exception {
    doFileBasedTest();
  }

  /**
   * Test out formatting with a non-HTML template data language
   *
   * @throws Exception
   */
  public void _testSampleFileWithCustomTemplateDataLang()
    throws Exception {
    doFileBasedTest("JavaSampleFile.hbs", StdFileTypes.JAVA);
  }
}
