package com.intellij.flex.editor;

import com.intellij.application.options.CodeStyle;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.intellij.testFramework.LightProjectDescriptor;

public class FlexEditorTest extends JSBaseEditorTestCase {
  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "as_editor/");
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_editor/");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  protected void doGtTestWithJSSupportLoaderAndFlex() {
    doTestWithJSSupportLoaderAndFlex('>');
  }

  protected void doTestWithJSSupportLoaderAndFlex(final char ch) {
    doTestWithJSSupportLoaderAndFlex(() -> doTypingTest("mxml", String.valueOf(ch)));
  }

  protected void doTestWithJSSupportLoaderAndFlex(final Runnable run) {
    FlexTestUtils.setupFlexSdk(myFixture.getModule(), getTestName(false), this.getClass(), myFixture.getTestRootDisposable());
    run.run();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertQuoteInMxml() {
    doTestWithJSSupportLoaderAndFlex(() -> doInsertQuoteTest("mxml", '\''));
  }

  public void testInsertDeleteBracket() {
    _testInsertDeleteBracket("mxml");
  }

  public void testEnter3_5() {
    doEnterTestForExtension("js2");
  }

  public void testEnter3_6() {
    doEnterTestForExtension("js2");
  }

  public void testEnter3_8() {
    doEnterTestForExtension("js2");
  }

  public void testEnter3_10() {
    doEnterTestForExtension("js2");
  }

  public void testEnter3_11() {
    doEnterTestForExtension("js2");
  }

  public void testEnter3_12() {
    doEnterTestForExtension("js2");
  }

  public void testEnter3_22() {
    doEnterTestForExtension("as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEnter3_3InXml() {
    doEnterTestForExtension("mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEnter3_4InXml() {
    doEnterTestForExtension("mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEnter3_5InXml() {
    doEnterTestForExtension("mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEnter3_6InXml() {
    doEnterTestForExtension("mxml");
  }

  public void testEnter8() {
    doEnterTest(getTestName(false), "js2");
  }

  public void testReformatInjectedProblem() {
    JSTestUtils.registerScriptTagInjector(getProject(), JavaScriptSupportLoader.ECMA_SCRIPT_L4, myFixture.getTestRootDisposable());
    doFormatterTest("mxml");
  }

  public void testReformatInjectedProblem2() {
    doFormatterTest("mxml");
  }

  public void testReformatInjectedProblem3() {
    doFormatterTest("mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertCdataOnGt() {
    doGtTestWithJSSupportLoaderAndFlex();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertCdataOnGt2() {
    doGtTestWithJSSupportLoaderAndFlex();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertCdataOnGt3() {
    doGtTestWithJSSupportLoaderAndFlex();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertCdataOnGt4() {
    final CodeStyleSettings codeSettings = CodeStyle.getSettings(getProject());
    final XmlCodeStyleSettings xmlSettings = codeSettings.getCustomSettings(XmlCodeStyleSettings.class);
    int currWsAroundCData = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
    doGtTestWithJSSupportLoaderAndFlex();
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = currWsAroundCData;
  }

  public void testInsertPairRBrace() {
    doTestWithJSSupportLoaderAndFlex('{');
  }

  public void testInsertPairRBrace2() {
    doTestWithJSSupportLoaderAndFlex('{');
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testInsertPairRBrace3() {
    doTestWithJSSupportLoaderAndFlex('{');
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testInsertRBraceOnEnter() {
    doTestWithJSSupportLoaderAndFlex('\n');
  }

  public void testOvertypeRBrace() {
    myFixture.setCaresAboutInjection(false);
    doTestWithJSSupportLoaderAndFlex('}');
  }

  public void testOvertypeRBrace2() {
    myFixture.setCaresAboutInjection(false);
    doTestWithJSSupportLoaderAndFlex('}');
  }

  public void testInsertBraceOnEnter() {
    _testInsertBraceOnEnter("", "js2");
    _testInsertBraceOnEnter("2", "js2");

    final JSCodeStyleSettings codeSettings =
      CodeStyle.getSettings(getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);
    try {
      codeSettings.INDENT_PACKAGE_CHILDREN = JSCodeStyleSettings.INDENT;
      _testInsertBraceOnEnter("3", "js2");
    }
    finally {
      codeSettings.INDENT_PACKAGE_CHILDREN = JSCodeStyleSettings.DO_NOT_INDENT;
    }
  }

  public void testSmartEnterFunction() {
    final String testName = getTestName(false);
    doSmartEnterTest(testName + "_6", "js2");
    doSmartEnterTest(testName + "InInterfaceWithoutSemicolon", "js2");
    doSmartEnterTest(testName + "InInterfaceWithoutParameterList", "js2");
  }

  public void testSmartEnterStatement() {
    final String testName = getTestName(false);
    doSmartEnterTest(testName + "_6", "js2");
    doSmartEnterTest(testName + "_14_5", "js2");
    doSmartEnterTest(testName + "_14_6", "js2");
    doSmartEnterTest(testName + "_14_7", "js2");
    doSmartEnterTest(testName + "_14_8", "js2");
  }

  public void testSmartEnterClass() {
    doSmartEnterTest(getTestName(false), "js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testSmartEnterFunctionInInjected() {
    doSmartEnterTest(getTestName(false), "mxml");
  }

  public void testCopyReference() {
    performCopyRefAndPasteTest();
  }

  public void testCopyReference_2() {
    performCopyRefAndPasteTest();
  }

  public void testCopyReference2() {
    performCopyRefAndPasteTest();
  }

  public void testCopyReference3() {
    performCopyRefAndPasteTest();
  }

  public void testCopyReference4() {
    performCopyRefAndPasteTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCopyReference5() {
    doTestWithJSSupportLoaderAndFlex(this::performCopyRefAndPasteTest);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCopyReference6() {
    performCopyRefAndPasteTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCopyReferenceInMxml() {
    performCopyRefAndPasteTest("mxml");
  }

  public void testGotoNextPrevMethod() {
    myFixture.configureByFile(getTestName(false) + ".js2");
    doTestNextPrevMethod();
  }

  public void testGotoNextPrevMethod2() {
    myFixture.configureByFile(getTestName(false) + ".mxml");
    doTestNextPrevMethod();
  }
}
