package com.intellij.flex.completion;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings;
import com.intellij.testFramework.LightProjectDescriptor;

public class FlexCssCompletionTest extends BaseJSCompletionTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");

    CodeStyleSettings globalSettings = CodeStyle.getSettings(getProject());
    CssCodeStyleSettings settings = globalSettings.getCustomSettings(CssCodeStyleSettings.class);
    settings.SPACE_AFTER_COLON = false;
    CamelHumpMatcher.forceStartMatching(getTestRootDisposable());
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(FlexCompletionTest.BASE_PATH);
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @Override
  protected String getExtension() {
    return "css";
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssPropertyCompletion() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssPropertyCompletion1() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssPropertyCompletion2() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssPropertyCompletion3() {
    setUpJdk();
    doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testHtmlCssCompletion() {
    doTest("", "html");
  }

  /*@FlexTestOptions({WithJsSupportLoader, WithFlexFacet})
  public void testHtmlCssCompletion1() {
    final VirtualFile[] files= new VirtualFile[]{
      getVirtualFile(getBasePath() + getTestName(false) + ".css"),
      getVirtualFile(getBasePath() + "HtmlFileReferringToCss.html")
    };
    doTestForFiles(files, "", "css");
  }*/

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssSelectorCompletion() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssSelectorAfterPipeCompletion() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssSelectorCompletion1() {
    setUpJdk();
    doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssColorValueCompletion() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssEnumValueCompletion() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCssClassCompletion() {
    doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testDifferentStyleDeclarations1() {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testDifferentStyleDeclarations2() {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testDifferentStyleDeclarations3() {
    setUpJdk();
    addDifferentStyleDeclarationsLibrary();
    LookupElement[] items = doTest("");
    assertNotNull(items);
    assertTrue(items.length > 100);
    assertTrue(items[0].getLookupString().startsWith("choose color"));
    //assertTrue(items[1].getLookupString().startsWith("first"));
    //assertTrue(items[2].getLookupString().startsWith("second"));
    //assertTrue(items[3].getLookupString().startsWith("undefined"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testDifferentStyleDeclarations4() {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testDifferentStyleDeclarations5() {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testDifferentStyleDeclarations6() {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testDifferentStyleDeclarations7() {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testMetadataStyle() {
    String prefix = getTestName(false);
    doTestForFiles(new String[]{prefix + "." + "css", prefix + "." + "mxml"}, "", "css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void _testClassReferenceCompletion() {
    myFixture.configureByFiles(FlexCompletionTest.BASE_PATH + getTestName(false) + ".css");
    new CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(getProject(), myFixture.getEditor());
    myFixture.checkResultByFile(FlexCompletionTest.BASE_PATH + getTestName(false) + "_after.css");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testSelectorAfterNamespacePrefix1() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testSelectorAfterNamespacePrefix2() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testPropertyUnderSelectorWithNamespace1() {
    setUpJdk();
    doTest("");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  public void testPropertyUnderSelectorWithNamespace2() {
    doTest("");
  }

  private void addDifferentStyleDeclarationsLibrary() {
    FlexTestUtils.addLibrary(getModule(), "Lib", getTestDataPath(), "DifferentStyleDeclarations.swc", null, null);
    Disposer.register(myFixture.getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(getModule(), "Lib"));
  }
}
