package com.intellij.flex.editor;

import com.intellij.application.options.CodeStyle;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSEditorFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSEditorFixture.jsEditorFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class FlexEditorTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSEditorFixture> js =
    jsEditorFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("as_editor/"))
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(js.get().getTestRootDisposable(), "as_editor/");
  }

  protected void doGtTestWithJSSupportLoaderAndFlex() {
    doTestWithJSSupportLoaderAndFlex('>');
  }

  protected void doTestWithJSSupportLoaderAndFlex(final char ch) {
    doTestWithJSSupportLoaderAndFlex(() -> js.get().doTypingTest("mxml", String.valueOf(ch)));
  }

  protected void doTestWithJSSupportLoaderAndFlex(final Runnable run) {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.setupFlexSdk(myFixture.getModule(), js.get().getTestName(false), this.getClass(), myFixture.getTestRootDisposable());
    run.run();
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInsertQuoteInMxml() {
    EdtTestUtil.runInEdtAndWait(() -> {
      doTestWithJSSupportLoaderAndFlex(() -> js.get().doInsertQuoteTest("mxml", '\''));
    });
  }

  @Test
  public void testInsertDeleteBracket() {
    js.get()._testInsertDeleteBracket("mxml");
  }

  @Test
  public void testEnter3_5() {
    js.get().doEnterTestForExtension("js2");
  }

  @Test
  public void testEnter3_6() {
    js.get().doEnterTestForExtension("js2");
  }

  @Test
  public void testEnter3_8() {
    js.get().doEnterTestForExtension("js2");
  }

  @Test
  public void testEnter3_10() {
    js.get().doEnterTestForExtension("js2");
  }

  @Test
  public void testEnter3_11() {
    js.get().doEnterTestForExtension("js2");
  }

  @Test
  public void testEnter3_12() {
    js.get().doEnterTestForExtension("js2");
  }

  @Test
  public void testEnter3_22() {
    js.get().doEnterTestForExtension("as");
  }

  @Test
  public void testEnter3_3InXml() {
    js.get().doEnterTestForExtension("mxml");
  }

  @Test
  public void testEnter3_4InXml() {
    js.get().doEnterTestForExtension("mxml");
  }

  @Test
  public void testEnter3_5InXml() {
    js.get().doEnterTestForExtension("mxml");
  }

  @Test
  public void testEnter3_6InXml() {
    js.get().doEnterTestForExtension("mxml");
  }

  @Test
  public void testEnter8() {
    js.get().doEnterTest(js.get().getTestName(false), "js2");
  }

  @Test
  public void testReformatInjectedProblem() {
    EdtTestUtil.runInEdtAndWait(() -> {
      JSTestUtils.registerScriptTagInjector(js.get().getProject(), FlexSupportLoader.ECMA_SCRIPT_L4, js.get().getFixture().getTestRootDisposable());
      js.get().doFormatterTest("mxml");
    });
  }

  @Test
  public void testReformatInjectedProblem2() {
    js.get().doFormatterTest("mxml");
  }

  @Test
  public void testReformatInjectedProblem3() {
    js.get().doFormatterTest("mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInsertCdataOnGt() {
    EdtTestUtil.runInEdtAndWait(() -> {
      doGtTestWithJSSupportLoaderAndFlex();
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInsertCdataOnGt2() {
    EdtTestUtil.runInEdtAndWait(() -> {
      doGtTestWithJSSupportLoaderAndFlex();
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInsertCdataOnGt3() {
    EdtTestUtil.runInEdtAndWait(() -> {
      doGtTestWithJSSupportLoaderAndFlex();
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testInsertCdataOnGt4() {
    EdtTestUtil.runInEdtAndWait(() -> {
      final CodeStyleSettings codeSettings = CodeStyle.getSettings(js.get().getProject());
      final XmlCodeStyleSettings xmlSettings = codeSettings.getCustomSettings(XmlCodeStyleSettings.class);
      int currWsAroundCData = xmlSettings.XML_WHITE_SPACE_AROUND_CDATA;
      xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
      doGtTestWithJSSupportLoaderAndFlex();
      xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = currWsAroundCData;
    });
  }

  @Test
  public void testInsertPairRBrace() {
    EdtTestUtil.runInEdtAndWait(() -> {
      doTestWithJSSupportLoaderAndFlex('{');
    });
  }

  @Test
  public void testInsertPairRBrace2() {
    EdtTestUtil.runInEdtAndWait(() -> {
      doTestWithJSSupportLoaderAndFlex('{');
    });
  }

  @Test
  public void testInsertPairRBrace3() {
    EdtTestUtil.runInEdtAndWait(() -> {
      doTestWithJSSupportLoaderAndFlex('{');
    });
  }

  @Test
  public void testInsertRBraceOnEnter() {
    EdtTestUtil.runInEdtAndWait(() -> {
      doTestWithJSSupportLoaderAndFlex('\n');
    });
  }

  @Test
  public void testOvertypeRBrace() {
    EdtTestUtil.runInEdtAndWait(() -> {
      js.get().getFixture().setCaresAboutInjection(false);
      doTestWithJSSupportLoaderAndFlex('}');
    });
  }

  @Test
  public void testOvertypeRBrace2() {
    EdtTestUtil.runInEdtAndWait(() -> {
      js.get().getFixture().setCaresAboutInjection(false);
      doTestWithJSSupportLoaderAndFlex('}');
    });
  }

  @Test
  public void testInsertBraceOnEnter() {
    EdtTestUtil.runInEdtAndWait(() -> {
      js.get()._testInsertBraceOnEnter("", "js2");
      js.get()._testInsertBraceOnEnter("2", "js2");

      final JSCodeStyleSettings codeSettings =
        CodeStyle.getSettings(js.get().getProject()).getCustomSettings(ECMA4CodeStyleSettings.class);
      try {
        codeSettings.INDENT_PACKAGE_CHILDREN = JSCodeStyleSettings.INDENT;
        js.get()._testInsertBraceOnEnter("3", "js2");
      }
      finally {
        codeSettings.INDENT_PACKAGE_CHILDREN = JSCodeStyleSettings.DO_NOT_INDENT;
      }
    });
  }

  @Test
  public void testSmartEnterFunction() {
    EdtTestUtil.runInEdtAndWait(() -> {
      final String testName = js.get().getTestName(false);
      js.get().doSmartEnterTest(testName + "_6", "js2");
      js.get().doSmartEnterTest(testName + "InInterfaceWithoutSemicolon", "js2");
      js.get().doSmartEnterTest(testName + "InInterfaceWithoutParameterList", "js2");
    });
  }

  @Test
  public void testSmartEnterStatement() {
    EdtTestUtil.runInEdtAndWait(() -> {
      final String testName = js.get().getTestName(false);
      js.get().doSmartEnterTest(testName + "_6", "js2");
      js.get().doSmartEnterTest(testName + "_14_5", "js2");
      js.get().doSmartEnterTest(testName + "_14_6", "js2");
      js.get().doSmartEnterTest(testName + "_14_7", "js2");
      js.get().doSmartEnterTest(testName + "_14_8", "js2");
    });
  }

  @Test
  public void testSmartEnterClass() {
    js.get().doSmartEnterTest(js.get().getTestName(false), "js2");
  }

  @Test
  public void testSmartEnterFunctionInInjected() {
    js.get().doSmartEnterTest(js.get().getTestName(false), "mxml");
  }

  @Test
  public void testCopyReference() {
    js.get().performCopyRefAndPasteTest();
  }

  @Test
  public void testCopyReference_2() {
    js.get().performCopyRefAndPasteTest();
  }

  @Test
  public void testCopyReference2() {
    js.get().performCopyRefAndPasteTest();
  }

  @Test
  public void testCopyReference3() {
    js.get().performCopyRefAndPasteTest();
  }

  @Test
  public void testCopyReference4() {
    js.get().performCopyRefAndPasteTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testCopyReference5() {
    EdtTestUtil.runInEdtAndWait(() -> {
      doTestWithJSSupportLoaderAndFlex(js.get()::performCopyRefAndPasteTest);
    });
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testCopyReference6() {
    js.get().performCopyRefAndPasteTest();
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testCopyReferenceInMxml() {
    js.get().performCopyRefAndPasteTest("mxml");
  }

  @Test
  public void testGotoNextPrevMethod() {
    js.get().getFixture().configureByFile(js.get().getTestName(false) + ".js2");
    js.get().doTestNextPrevMethod();
  }

  @Test
  public void testGotoNextPrevMethod2() {
    js.get().getFixture().configureByFile(js.get().getTestName(false) + ".mxml");
    js.get().doTestNextPrevMethod();
  }
}
