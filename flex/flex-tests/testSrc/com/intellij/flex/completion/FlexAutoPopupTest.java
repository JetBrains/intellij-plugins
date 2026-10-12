// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.completion;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.CompletionAutoPopupTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Path;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.testFramework.EdtTestUtil.runInEdtAndWait;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.moduleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
public class FlexAutoPopupTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);
  private final TestFixture<Path> pathFixture = tempPathFixture();
  private final TestFixture<Module> moduleFixture = moduleFixture(projectFixture, pathFixture, true, FlexModuleType.MODULE_TYPE_ID);
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightFixture(projectFixture, pathFixture);
  private CodeInsightTestFixture myFixture;

  private <E extends Throwable> void runTest(@NotNull ThrowableRunnable<E> runnable) throws E {
    CompletionAutoPopupTestUtil.runWithAutoPopupEnabled(myFixture, runnable);
  }

  private void type(@NotNull String s) {
    CompletionAutoPopupTestUtil.type(myFixture, s);
  }

  private @Nullable LookupImpl getLookup() {
    return CompletionAutoPopupTestUtil.getLookup(myFixture);
  }

  @BeforeEach
  void setUp(TestInfo testInfo) throws Throwable {
    myFixture = codeInsightFixture.get();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    String testName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    runInEdtAndWait((ThrowableRunnable<Throwable>)() -> FlexTestUtils.setupFlexSdk(moduleFixture.get(), testName, FlexAutoPopupTest.class, myFixture.getTestRootDisposable()));
    CodeInsightSettings.getInstance().setSelectAutopopupSuggestionsByChars(true);
  }

  @AfterEach
  void tearDown() {
    CodeInsightSettings.getInstance().setSelectAutopopupSuggestionsByChars(false);
    CodeInsightSettings.getInstance().COMPLETION_CASE_SENSITIVE = CodeInsightSettings.FIRST_LETTER;
  }

  @Test
  public void testGenerallyFocusLookup_() {
    runTest(() -> {
      myFixture.configureByText("a.js2", """
          function foo(x,y,z) {}
          foo(<caret>);
      """);
      type("1");
      type(",");
      type("2");
      type(",");
      myFixture.checkResult("""
          function foo(x,y,z) {}
          foo(1,2,<caret>);
      """);
      type("f");
      assert getLookup().isFocused();
    });
  }

  @Test
  public void testGenerallyFocusLookup() {
    runTest(() -> {
      myFixture.configureByText("a.js2", """
          function fooBar(xxxxxx:String):String {
            return fo<caret>
          }
      """);
      type("o");
      assert getLookup().isFocused();
      type(".");
      assert getLookup() != null;
    });
  }

  @Test
  public void testNoPopupInVarName() {
    runTest(() -> {
      myFixture.configureByText("a.js2", """
          var <caret>
      """);
      type("i");
      assert getLookup() == null;
    });
  }

  @Test
  public void testNoPopupInParamName() {
    runTest(() -> {
      myFixture.configureByText("a.js2", """
          function foo(<caret>
      """);
      type("i");
      assert getLookup() == null;
    });
  }

  @Test
  public void testNoPopupInParamName2() {
    runTest(() -> {
      myFixture.configureByText("a.js2", """
          function foo(<caret>
      """);
      type(".");
      assert getLookup() == null;
    });
  }

  @Test
  public void testNoPopupInParamName2_2() {
    runTest(() -> {
      myFixture.configureByText("a.js2", """
          class  C {
            function foo(<caret>) {}
          }
      """);
      type(".");
      assert getLookup() == null;
      type(".");
      assert getLookup() == null;
      type(".");
      assert getLookup() == null;
    });
  }

  @Test
  public void testTypingGet() {
    runTest(() -> {
      myFixture.configureByText("a.js2", """
           function getFoo() {}
          return ge<caret>
      """);
      type("t");
      assert getLookup() != null;
    });
  }

  @Test
  public void testAutopopupAfterCommaInParameterList() {
    runTest(() -> {
      myFixture.configureByText("a.js2", """
           function foo(x, y) {}
           foo(x<caret>)
      """);
      type(",");

      assert getLookup() == null;
      type("O");
      assert getLookup() != null;
    });
  }

  @Test
  public void testAutopopupAfterSpaceInParameterList() {
    runTest(() -> {
      myFixture.configureByText("a.js2", """
           function foo(x, y) {}
           foo(x,<caret>)
      """);
      type(" ");
      assert getLookup() == null;
      type("O");
      assert getLookup() != null;
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testAutoPopupCompletionInsertsImport() {
    runTest(() -> {
      myFixture.configureByText("a.as", """
          package {
          public class Foo {
              var b: UICompone<caret>
          }
          }
      """);
      type("n");
      assertEquals("UIComponent", getLookup().getCurrentItem().getLookupString());
      type("\t");
      myFixture.checkResult("""
          package {
          import mx.core.UIComponent;

          public class Foo {
              var b: UIComponent<caret>
          }
          }
      """);
    });
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  @Test
  public void testAutoPopupCompletionInsertsImportInMxml() throws Throwable {
    runTest(() -> {
      myFixture.configureByText("a.mxml", """
        <?xml version="1.0"?>
        <mx:Application  xmlns:mx="http://www.adobe.com/2006/mxml">
          <mx:Script><![CDATA[
              private function foo():void {
                  var b: UICompone<caret>
              }
          ]]></mx:Script>
        </mx:Application>
      """);
      runInEdtAndWait((ThrowableRunnable<Throwable>)() ->  myFixture.doHighlighting()); // to ensure, injected documents are calculated
      type("n");
      assertEquals("UIComponent", getLookup().getCurrentItem().getLookupString());
      type("\t");
      myFixture.checkResult("""
        <?xml version="1.0"?>
        <mx:Application  xmlns:mx="http://www.adobe.com/2006/mxml">
          <mx:Script><![CDATA[
              import mx.core.UIComponent;

              private function foo():void {
                  var b: UIComponent<caret>
              }
          ]]></mx:Script>
        </mx:Application>
      """);
    });
  }
}
