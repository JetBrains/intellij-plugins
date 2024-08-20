// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.completion;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CompletionAutoPopupTestCase;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;

import static com.intellij.testFramework.EdtTestUtil.runInEdtAndWait;

public class FlexAutoPopupTest extends CompletionAutoPopupTestCase {

  @Override
  @NotNull
  protected LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @Override
      public @NotNull String getModuleTypeId() {
        return FlexModuleType.getInstance().getId();

      }
    };
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");

    super.setUp();
    try {
      runInEdtAndWait((ThrowableRunnable<Throwable>)() -> FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), FlexAutoPopupTest.class, myFixture.getProjectDisposable()));
    }
    catch (Throwable e) {
      throw new RuntimeException(e);
    }
    CodeInsightSettings.getInstance().setSelectAutopopupSuggestionsByChars(true);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      CodeInsightSettings.getInstance().setSelectAutopopupSuggestionsByChars(false);
      CodeInsightSettings.getInstance().COMPLETION_CASE_SENSITIVE = CodeInsightSettings.FIRST_LETTER;
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  public void testGenerallyFocusLookup_() {
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
  }

  public void testGenerallyFocusLookup() {
    myFixture.configureByText("a.js2", """
        function fooBar(xxxxxx:String):String {
          return fo<caret>
        }
    """);
    type("o");
    assert getLookup().isFocused();
    type(".");
    assert getLookup() != null;
  }

  public void testNoPopupInVarName() {
    myFixture.configureByText("a.js2", """
        var <caret>
    """);
    type("i");
    assert getLookup() == null;
  }

  public void testNoPopupInParamName() {
    myFixture.configureByText("a.js2", """
        function foo(<caret>
    """);
    type("i");
    assert getLookup() == null;
  }

  public void testNoPopupInParamName2() {
    myFixture.configureByText("a.js2", """
        function foo(<caret>
    """);
    type(".");
    assert getLookup() == null;
  }

  public void testNoPopupInParamName2_2() {
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
  }

  public void testTypingGet() {
    myFixture.configureByText("a.js2", """
         function getFoo() {}
        return ge<caret>
    """);
    type("t");
    assert getLookup() != null;
  }

  public void testAutopopupAfterCommaInParameterList() {
    myFixture.configureByText("a.js2", """
         function foo(x, y) {}
         foo(x<caret>)
    """);
    type(",");

    assert getLookup() == null;
    type("O");
    assert getLookup() != null;
  }

  public void testAutopopupAfterSpaceInParameterList() {
    myFixture.configureByText("a.js2", """
         function foo(x, y) {}
         foo(x,<caret>)
    """);
    type(" ");
    assert getLookup() == null;
    type("O");
    assert getLookup() != null;
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testAutoPopupCompletionInsertsImport() {
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
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testAutoPopupCompletionInsertsImportInMxml() throws Throwable {
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
  }
}
