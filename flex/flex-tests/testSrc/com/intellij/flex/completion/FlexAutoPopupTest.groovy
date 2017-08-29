package com.intellij.flex.completion

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.completion.CompletionAutoPopupTestCase
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.flex.util.FlexTestUtils
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler
import com.intellij.lang.javascript.JSTestOption
import com.intellij.lang.javascript.JSTestOptions
import com.intellij.lang.javascript.flex.FlexModuleType
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import org.jetbrains.annotations.NotNull

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath
import static com.intellij.testFramework.EdtTestUtil.runInEdtAndWait

class FlexAutoPopupTest extends CompletionAutoPopupTestCase {

  @NotNull
  protected LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor() {
      @NotNull
      ModuleType getModuleType() {
        return FlexModuleType.getInstance()
      }
    }
  }

  @Override
  protected void setUp() {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))))
    super.setUp()
    runInEdtAndWait { FlexTestUtils.setupFlexSdk(myModule, getTestName(false), FlexAutoPopupTest.class, myFixture.getProjectDisposable()) }
    CodeInsightSettings.instance.SELECT_AUTOPOPUP_SUGGESTIONS_BY_CHARS = true
  }

  @Override
  protected void tearDown() {
    CodeInsightSettings.instance.SELECT_AUTOPOPUP_SUGGESTIONS_BY_CHARS = false
    CodeInsightSettings.instance.COMPLETION_CASE_SENSITIVE = CodeInsightSettings.FIRST_LETTER
    super.tearDown()
  }

  void testGenerallyFocusLookup_() {
    myFixture.configureByText("a.js2", """
        function foo(x,y,z) {}
        foo(<caret>);
    """)
    type '1'
    type ','
    type '2'
    type ','
    myFixture.checkResult """
        function foo(x,y,z) {}
        foo(1,2,<caret>);
    """
    type("f")
    assert lookup.focused
  }

  void testGenerallyFocusLookup() {
    myFixture.configureByText("a.js2", """
        function foo(xxxxxx:String):String {
          return f<caret>
        }
    """)
    type 'o'
    assert lookup.focused
    type '.'
    assert lookup
  }

  void testNoPopupInVarName() {
    myFixture.configureByText("a.js2", """
        var <caret>
    """)
    type 'i'
    assert lookup == null
  }

  void testNoPopupInParamName() {
    myFixture.configureByText("a.js2", """
        function foo(<caret>
    """)
    type 'i'
    assert lookup == null
  }

  void testNoPopupInParamName2() {
    myFixture.configureByText("a.js2", """
        function foo(<caret>
    """)
    type '.'
    assert lookup == null
  }

  void testNoPopupInParamName2_2() {
    myFixture.configureByText("a.js2", """
        class  C {
          function foo(<caret>) {}
        }
    """)
    type '.'
    assert lookup == null
    type '.'
    assert lookup == null
    type '.'
    assert lookup == null
  }

  void testTypingGet() {
    myFixture.configureByText("a.js2", """
         function getFoo() {}
        return ge<caret>
    """)
    type 't'
    assert lookup != null
  }

  void testAutopopupAfterCommaInParameterList() {
    myFixture.configureByText("a.js2", """
         function foo(x, y) {}
         foo(x<caret>)
    """)
    type ','

    assert lookup == null
    type 'O'
    assert lookup != null
  }

  void testAutopopupAfterSpaceInParameterList() {
    myFixture.configureByText("a.js2", """
         function foo(x, y) {}
         foo(x,<caret>)
    """)
    type ' '
    assert lookup == null
    type 'O'
    assert lookup != null
  }

  @JSTestOptions([JSTestOption.WithFlexFacet])
  void testAutoPopupCompletionInsertsImport() {
    CamelHumpMatcher.forceStartMatching(getTestRootDisposable())
    myFixture.configureByText("a.as", """
        package {
        public class Foo {
            var b: UICompone<caret>
        }
        }
    """)
    type 'n'
    assertEquals 'UIComponent', lookup.currentItem.lookupString
    type '\t'
    myFixture.checkResult """
        package {
        import mx.core.UIComponent;

        public class Foo {
            var b: UIComponent<caret>
        }
        }
    """
  }

  @JSTestOptions([JSTestOption.WithFlexFacet])
  void testAutoPopupCompletionInsertsImportInMxml() {
    CamelHumpMatcher.forceStartMatching(getTestRootDisposable())
    myFixture.configureByText("a.mxml", """
      <?xml version="1.0"?>
      <mx:Application  xmlns:mx="http://www.adobe.com/2006/mxml">
        <mx:Script><![CDATA[
            private function foo():void {
                var b: UICompone<caret>
            }
        ]]></mx:Script>
      </mx:Application>
    """)
    runInEdtAndWait { myFixture.doHighlighting() } // to ensure injected documents are calculated
    type 'n'
    assertEquals 'UIComponent', lookup.currentItem.lookupString
    type '\t'
    myFixture.checkResult """
      <?xml version="1.0"?>
      <mx:Application  xmlns:mx="http://www.adobe.com/2006/mxml">
        <mx:Script><![CDATA[
            import mx.core.UIComponent;

            private function foo():void {
                var b: UIComponent<caret>
            }
        ]]></mx:Script>
      </mx:Application>
    """
  }
}
