package com.intellij.lang.javascript;

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexCssCompletionTest extends BaseJSCompletionTestCase {
  protected Runnable myAfterCommitRunnable = null;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myAfterCommitRunnable = null;

    CodeStyleSettings globalSettings = CodeStyleSettingsManager.getSettings(getProject());
    CssCodeStyleSettings settings = globalSettings.getCustomSettings(CssCodeStyleSettings.class);
    settings.SPACE_AFTER_COLON = false;
    CamelHumpMatcher.forceStartMatching(getTestRootDisposable());
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }

  @Override
  protected String getBasePath() {
    return FlexCompletionTest.BASE_PATH;
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    JSTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  @Override
  protected String getExtension() {
    return "css";
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssPropertyCompletion() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssPropertyCompletion1() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssPropertyCompletion2() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssPropertyCompletion3() throws Exception {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testHtmlCssCompletion() throws Exception {
    doTest("", "html");
  }

  /*@JSTestOptions({WithJsSupportLoader, WithFlexFacet})
  public void testHtmlCssCompletion1() throws Exception {
    final VirtualFile[] files= new VirtualFile[]{
      getVirtualFile(getBasePath() + getTestName(false) + ".css"),
      getVirtualFile(getBasePath() + "HtmlFileReferringToCss.html")
    };
    doTestForFiles(files, "", "css");
  }*/

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssSelectorCompletion() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssSelectorAfterPipeCompletion() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssSelectorCompletion1() throws Exception {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssColorValueCompletion() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssEnumValueCompletion() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testCssClassCompletion() throws Exception {
    doTest("", "mxml");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testDifferentStyleDeclarations1() throws Exception {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testDifferentStyleDeclarations2() throws Exception {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testDifferentStyleDeclarations3() throws Exception {
    addDifferentStyleDeclarationsLibrary();
    addDifferentStyleDeclarationsLibrary();
    LookupElement[] items = doTest("");
    assertNotNull(items);
    assertTrue(items.length > 1);
    assertTrue(items[0].getLookupString().startsWith("first"));
    assertTrue(items[1].getLookupString().startsWith("second"));
    assertTrue(items[2].getLookupString().startsWith("undefined"));
    assertTrue(items[3].getLookupString().startsWith("choose color"));
    assertFalse(items[4].getLookupString().startsWith("choose color"));
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testDifferentStyleDeclarations4() throws Exception {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testDifferentStyleDeclarations5() throws Exception {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testDifferentStyleDeclarations6() throws Exception {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testDifferentStyleDeclarations7() throws Exception {
    addDifferentStyleDeclarationsLibrary();
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testMetadataStyle() throws Exception {
    String prefix = this.getBasePath() + getTestName(false);
    final VirtualFile[] vFiles = new VirtualFile[]{getVirtualFile(prefix + "." + "css"), getVirtualFile(prefix + "." + "mxml")};
    doTestForFiles(vFiles, "", "css");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader, JSTestOption.WithFlexFacet})
  public void testClassReferenceCompletion() throws Exception {
    configureByFiles(null, FlexCompletionTest.BASE_PATH + getTestName(false) + ".css");
    new CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(myProject, myEditor);
    checkResultByFile(FlexCompletionTest.BASE_PATH + getTestName(false) + "_after.css");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testSelectorAfterNamespacePrefix1() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testSelectorAfterNamespacePrefix2() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPropertyUnderSelectorWithNamespace1() throws Exception {
    doTest("");
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testPropertyUnderSelectorWithNamespace2() throws Exception {
    doTest("");
  }

  private void addDifferentStyleDeclarationsLibrary() {
    myAfterCommitRunnable = new Runnable() {
      @Override
      public void run() {
        JSTestUtils.addLibrary(myModule, "Lib", getTestDataPath() + getBasePath(), "DifferentStyleDeclarations.swc", null, null);
      }
    };
  }
}
