package com.intellij.flex.editor;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.intellij.testFramework.EditorTestUtil;

import java.util.concurrent.Callable;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexEditorTest extends JSBaseEditorTestCase {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_editor/");
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  protected void doGtTestWithJSSupportLoaderAndFlex() throws Exception {
    doTestWithJSSupportLoaderAndFlex('>');
  }

  protected void doTestWithJSSupportLoaderAndFlex(final char ch) throws Exception {
    doTestWithJSSupportLoaderAndFlex(() -> {
      doTypingTest("mxml", String.valueOf(ch));
      return null;
    });
  }

  protected void doTestWithJSSupportLoaderAndFlex(final Callable<Void> call) throws Exception {
    doTestWithJSSupport((Callable<Object>)() -> {
      FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), this.getClass());
      return call.call();
    });
  }

  private void performCopyRefAndPasteTest() throws Exception {
    performCopyRefAndPasteTest("js2");
  }

  private void performCopyRefAndPasteTest(String ext) throws Exception {
    String testName = getTestName(false);
    configureByFile(testName + "_src." + ext);
    EditorTestUtil.performReferenceCopy(getEditor());
    configureByFile(testName + "." + ext);
    EditorTestUtil.performPaste(getEditor());
    checkResultByFile(testName + "_after." + ext);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertQuoteInMxml() throws Exception {
    doTestWithJSSupportLoaderAndFlex(() -> {
      doInsertQuoteTest("mxml", '\'');
      return null;
    });
  }

  public void testInsertDeleteBracket() throws Exception {
    _testInsertDeleteBracket("mxml");
  }

  public void testEnter3_5() throws Exception {
    doEnterTestWithJSSupport("js2");
  }

  public void testEnter3_6() throws Exception {
    doEnterTestWithJSSupport("js2");
  }

  public void testEnter3_8() throws Exception {
    doEnterTestWithJSSupport("js2");
  }

  public void testEnter3_10() throws Exception {
    doEnterTestWithJSSupport("js2");
  }

  public void testEnter3_11() throws Exception {
    doEnterTestWithJSSupport("js2");
  }

  public void testEnter3_12() throws Exception {
    doEnterTestWithJSSupport("js2");
  }

  public void testEnter3_22() throws Exception {
    doEnterTestWithJSSupport("as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEnter3_3InXml() throws Exception {
    doEnterTestWithJSSupport("mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEnter3_4InXml() throws Exception {
    doEnterTestWithJSSupport("mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEnter3_5InXml() throws Exception {
    doEnterTestWithJSSupport("mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testEnter3_6InXml() throws Exception {
    doEnterTestWithJSSupport("mxml");
  }

  public void testEnter8() throws Exception {
    doEnterTest(getTestName(false), "js2");
  }

  public void testReformatInjectedProblem() throws Exception {
    JSTestUtils.registerScriptTagInjector(myProject, JavaScriptSupportLoader.ECMA_SCRIPT_L4, getTestRootDisposable());
    doFormatterTest("mxml");
  }

  public void testReformatInjectedProblem2() throws Exception {
    doFormatterTest("mxml");
  }

  public void testReformatInjectedProblem3() throws Exception {
    doFormatterTest("mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertCdataOnGt() throws Exception {
    doGtTestWithJSSupportLoaderAndFlex();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertCdataOnGt2() throws Exception {
    doGtTestWithJSSupportLoaderAndFlex();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertCdataOnGt3() throws Exception {
    doGtTestWithJSSupportLoaderAndFlex();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testInsertCdataOnGt4() throws Exception {
    final CodeStyleSettings codeSettings = CodeStyleSettingsManager.getSettings(getProject());
    final XmlCodeStyleSettings xmlSettings = codeSettings.getCustomSettings(XmlCodeStyleSettings.class);
    int currWsAroundCData = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
    doGtTestWithJSSupportLoaderAndFlex();
    xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = currWsAroundCData;
  }

  public void testInsertPairRBrace() throws Exception {
    doTestWithJSSupportLoaderAndFlex('{');
  }

  public void testInsertPairRBrace2() throws Exception {
    doTestWithJSSupportLoaderAndFlex('{');
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testInsertPairRBrace3() throws Exception {
    doTestWithJSSupportLoaderAndFlex('{');
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testInsertRBraceOnEnter() throws Exception {
    doTestWithJSSupportLoaderAndFlex('\n');
  }

  public void testOvertypeRBrace() throws Exception {
    doTestWithJSSupportLoaderAndFlex('}');
  }

  public void testOvertypeRBrace2() throws Exception {
    doTestWithJSSupportLoaderAndFlex('}');
  }

  public void testInsertBraceOnEnter() throws Exception {
    _testInsertBraceOnEnter("", "js2");
    _testInsertBraceOnEnter("2", "js2");

    final JSCodeStyleSettings codeSettings =
      CodeStyleSettingsManager.getSettings(getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);
    try {
      codeSettings.INDENT_PACKAGE_CHILDREN = JSCodeStyleSettings.INDENT;
      _testInsertBraceOnEnter("3", "js2");
    }
    finally {
      codeSettings.INDENT_PACKAGE_CHILDREN = JSCodeStyleSettings.DO_NOT_INDENT;
    }
  }

  public void testSmartEnterFunction() throws Exception {
    final String testName = getTestName(false);
    doSmartEnterTest(testName + "_6", "js2");
    doSmartEnterTest(testName + "_7", "js2");
  }

  public void testSmartEnterStatement() throws Exception {
    final String testName = getTestName(false);
    doSmartEnterTest(testName + "_6", "js2");
    doSmartEnterTest(testName + "_14_5", "js2");
    doSmartEnterTest(testName + "_14_6", "js2");
    doSmartEnterTest(testName + "_14_7", "js2");
    doSmartEnterTest(testName + "_14_8", "js2");
  }

  public void testSmartEnterClass() throws Exception {
    doSmartEnterTest(getTestName(false), "js2");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testSmartEnterFunctionInInjected() throws Exception {
    doSmartEnterTest(getTestName(false), "mxml");
  }

  public void testCopyReference() throws Exception {
    performCopyRefAndPasteTest();
  }

  public void testCopyReference_2() throws Exception {
    performCopyRefAndPasteTest();
  }

  public void testCopyReference2() throws Exception {
    performCopyRefAndPasteTest();
  }

  public void testCopyReference3() throws Exception {
    performCopyRefAndPasteTest();
  }

  public void testCopyReference4() throws Exception {
    performCopyRefAndPasteTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCopyReference5() throws Exception {
    doTestWithJSSupportLoaderAndFlex(() -> {
      performCopyRefAndPasteTest();
      return null;
    });
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCopyReference6() throws Exception {
    doTestWithJSSupport(() -> {
      performCopyRefAndPasteTest();
      return null;
    });
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCopyReferenceInMxml() throws Exception {
    doTestWithJSSupport(() -> {
      performCopyRefAndPasteTest("mxml");
      return null;
    });
  }

  public void testGotoNextPrevMethod() throws Exception {
    configureByFile(getTestName(false) + ".js2");
    doTestNextPrevMethod();
  }

  public void testGotoNextPrevMethod2() throws Exception {
    configureByFile(getTestName(false) + ".mxml");
    doTestNextPrevMethod();
  }
}
