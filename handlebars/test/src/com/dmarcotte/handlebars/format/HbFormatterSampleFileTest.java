package com.dmarcotte.handlebars.format;

import com.intellij.openapi.fileTypes.StdFileTypes;

public class HbFormatterSampleFileTest extends HbFormatterTest {

  public void testSampleFile1()
    throws Exception {
    doFileBasedTest("TodosSampleFile.hbs");
  }

  public void testSampleFile2()
    throws Exception {
    doFileBasedTest("ContactsSampleFile.hbs");
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
