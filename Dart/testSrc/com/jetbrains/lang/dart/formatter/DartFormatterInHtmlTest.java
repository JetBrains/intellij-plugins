package com.jetbrains.lang.dart.formatter;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormatterTestCase;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartFormatterInHtmlTest extends FormatterTestCase {

  protected String getFileExtension() {
    return "html";
  }

  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  @Override
  protected String getBasePath() {
    return "formatter/html";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setTestStyleSettings();
  }

  @Override
  protected void doTest(String resultNumber) throws Exception {
    String testName = getTestName(false);
    doTest(testName + "." + getFileExtension(), testName + "_after." + getFileExtension(), resultNumber);
  }

  private static void setTestStyleSettings() {
    final CommonCodeStyleSettings settings = getSettings(DartLanguage.INSTANCE);
    CommonCodeStyleSettings.IndentOptions indentOptions = settings.getIndentOptions();
    assertNotNull(indentOptions);
    indentOptions.INDENT_SIZE = 2;
    indentOptions.CONTINUATION_INDENT_SIZE = 2;
    indentOptions.TAB_SIZE = 2;

    settings.KEEP_BLANK_LINES_IN_CODE = 2;
    settings.METHOD_BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;
    settings.BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;
    settings.ALIGN_MULTILINE_PARAMETERS = false;
    settings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS = false;
    settings.KEEP_FIRST_COLUMN_COMMENT = false;
  }

  public void testDefault() throws Exception {
    doTest();
  }
}

