package com.intellij.flex.completion;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSCompletionFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSCompletionFixture.jsCompletionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class FlexCssCompletionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSCompletionFixture> js =
    jsCompletionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath(FlexCompletionTest.BASE_PATH), "css")
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(js.get().getTestRootDisposable(), "");

    CodeStyleSettings globalSettings = CodeStyle.getSettings(js.get().getProject());
    CssCodeStyleSettings settings = globalSettings.getCustomSettings(CssCodeStyleSettings.class);
    settings.SPACE_AFTER_COLON = false;
    CamelHumpMatcher.forceStartMatching(js.get().getTestRootDisposable());
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), js.get().getFixture().getTestRootDisposable());
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCssPropertyCompletion() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCssPropertyCompletion1() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCssPropertyCompletion2() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCssPropertyCompletion3() {
    setUpJdk();
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testHtmlCssCompletion() {
    js.get().doTest("", "html");
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
  @Test
  public void testCssSelectorCompletion() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCssSelectorAfterPipeCompletion() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCssSelectorCompletion1() {
    setUpJdk();
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCssColorValueCompletion() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCssEnumValueCompletion() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testCssClassCompletion() {
    js.get().doTest("", "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testDifferentStyleDeclarations1() {
    addDifferentStyleDeclarationsLibrary();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testDifferentStyleDeclarations2() {
    addDifferentStyleDeclarationsLibrary();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testDifferentStyleDeclarations3() {
    setUpJdk();
    addDifferentStyleDeclarationsLibrary();
    LookupElement[] items = js.get().doTest("");
    assertNotNull(items);
    assertTrue(items.length > 100);
    assertTrue(items[0].getLookupString().startsWith("choose color"));
    //assertTrue(items[1].getLookupString().startsWith("first"));
    //assertTrue(items[2].getLookupString().startsWith("second"));
    //assertTrue(items[3].getLookupString().startsWith("undefined"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testDifferentStyleDeclarations4() {
    addDifferentStyleDeclarationsLibrary();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testDifferentStyleDeclarations5() {
    addDifferentStyleDeclarationsLibrary();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testDifferentStyleDeclarations6() {
    addDifferentStyleDeclarationsLibrary();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testDifferentStyleDeclarations7() {
    addDifferentStyleDeclarationsLibrary();
    js.get().doTest("");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testMetadataStyle() {
    String prefix = js.get().getTestName(false);
    js.get().doTestForFiles(new String[]{prefix + "." + "css", prefix + "." + "mxml"}, "", "css");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void _testClassReferenceCompletion() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    myFixture.configureByFiles(FlexCompletionTest.BASE_PATH + js.get().getTestName(false) + ".css");
    new CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(js.get().getProject(), myFixture.getEditor());
    myFixture.checkResultByFile(FlexCompletionTest.BASE_PATH + js.get().getTestName(false) + "_after.css");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testSelectorAfterNamespacePrefix1() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testSelectorAfterNamespacePrefix2() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testPropertyUnderSelectorWithNamespace1() {
    setUpJdk();
    js.get().doTest("");
  }

  @FlexTestOptions({FlexTestOption.WithFlexFacet, FlexTestOption.WithGumboSdk})
  @Test
  public void testPropertyUnderSelectorWithNamespace2() {
    js.get().doTest("");
  }

  private void addDifferentStyleDeclarationsLibrary() {
    FlexTestUtils.addLibrary(js.get().getFixture().getModule(), "Lib", js.get().getTestDataPath(), "DifferentStyleDeclarations.swc", null, null);
    Module module = js.get().getFixture().getModule();
    Disposer.register(js.get().getFixture().getTestRootDisposable(), () -> FlexTestUtils.removeLibrary(module, "Lib"));
  }
}
