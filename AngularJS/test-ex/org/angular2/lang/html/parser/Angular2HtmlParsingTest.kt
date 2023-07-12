// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.html.HtmlParsingTest
import com.intellij.html.embedding.HtmlEmbeddedContentSupport.Companion.register
import com.intellij.javascript.JSHtmlEmbeddedContentSupport
import com.intellij.lang.LanguageASTFactory
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.css.CSSParserDefinition
import com.intellij.lang.html.HTMLParserDefinition
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.javascript.dialects.ECMA6ParserDefinition
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.util.TextRange
import com.intellij.pom.tree.events.TreeChangeEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.css.CssElementDescriptorProvider
import com.intellij.psi.css.CssEmbeddedTokenTypesProvider
import com.intellij.psi.css.CssHtmlEmbeddedContentSupport
import com.intellij.psi.css.impl.CssTreeElementFactory
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorProviderImpl
import com.intellij.psi.impl.BlockSupportImpl
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.UsefulTestCase
import org.angular2.lang.expr.parser.Angular2ParserDefinition
import org.angular2.lang.html.lexer.Angular2HtmlEmbeddedContentSupport
import org.angularjs.AngularTestUtil

class Angular2HtmlParsingTest : HtmlParsingTest("", "html",
                                                Angular2HtmlParserDefinition(),
                                                Angular2ParserDefinition(),
                                                JavascriptParserDefinition(),
                                                HTMLParserDefinition(),
                                                CSSParserDefinition()) {
  override fun setUp() {
    super.setUp()
    registerExtensions(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider::class.java, listOf(
      CssEmbeddedTokenTypesProvider()))
    register(application, getTestRootDisposable(),
             CssHtmlEmbeddedContentSupport::class.java, JSHtmlEmbeddedContentSupport::class.java,
             Angular2HtmlEmbeddedContentSupport::class.java)
    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, CssTreeElementFactory())
    registerExtensionPoint(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProvider::class.java)
    registerExtension(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProviderImpl())
    application.registerService(
      CssElementDescriptorFactory2::class.java,
      CssElementDescriptorFactory2("css-parsing-tests.xml"))
    assert(JSLanguageLevel.DEFAULT == JSLanguageLevel.ES6)
    registerParserDefinition(ECMA6ParserDefinition())
  }

  override fun checkResult(targetDataName: String, file: PsiFile) {
    super.checkResult(targetDataName, file)
    ensureReparsingConsistent(file)
  }

  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestExDataPath(Angular2HtmlParsingTest::class.java)
  }

  fun testNgParseElementsInsideNgTemplate() {
    doTestHtml("<ng-template><span></span></ng-template>")
  }

  fun testNgSupportVoidElements() {
    doTestHtml("<link rel=\"author license\" href=\"/about\">")
  }

  fun _testNgNotErrorOnVoidHtml5Elements() {
    doTestHtml("<map><area></map><div><br></div><colgroup><col></colgroup>" +
               "<div><embed></div><div><hr></div><div><img></div><div><input></div>" +
               "<object><param>/<object><audio><source></audio><audio><track></audio>" +
               "<p><wbr></p>")
  }

  fun _testNgReportClosingTagForVoidElement() {
    doTestHtml("<input></input>")
  }

  fun _testNgReportSelfClosingHtmlElement() {
    doTestHtml("<p />")
  }

  fun testNgCloseVoidElementsOnTextNodes() {
    doTestHtml("<p>before<br>after</p>")
  }

  fun testNgSupportOptionalEndTags() {
    doTestHtml("<div><p>1<p>2</div>")
  }

  fun testNgSupportNestedElements() {
    doTestHtml("<ul><li><ul><li></li></ul></li></ul>")
  }

  fun testNgSupportSelfClosingVoidElements() {
    doTestHtml("<input />")
  }

  fun testNgParseExpansionForms1() {
    doTestHtml("<div>before{messages.length, plural, =0 {You have <b>no</b> messages} =1 {One {{message}}}}after</div>")
  }

  fun testNgParseExpansionForms2() {
    doTestHtml("<div><span>{a, plural, =0 {b}}</span></div>")
  }

  fun testNgParseExpansionForms3() {
    doTestHtml("{messages.length, plural, =0 { {p.gender, select, male {m}} }}")
  }

  fun testNgErrorOnUnterminatedExpansionForm() {
    doTestHtml("{messages.length, plural, =0 {one}")
  }

  fun testNgICUWithNumbers() {
    doTestHtml("{sex, select, male {m} female {f} 0 {other}}")
  }

  fun testNgErrorOnUnterminatedExpansionCase() {
    doTestHtml("{messages.length, plural, =0 {one")
  }

  fun testNgErrorOnInvalidHTMLInExpansionCase() {
    doTestHtml("{messages.length, plural, =0 {<div>}}")
  }

  fun testNgWhitespacesInExpansionCase() {
    doTestHtml("{ messages . length,  plural ,  =0   {  <div> } }")
  }

  fun testExpansionFormComplex() {
    doTestHtml("<div>Text{ form, open, =23 {{{{foo: 12} }} is {inner, open, =34{{{\"test\"}} cool } =12{<tag test='12'></tag>}}}}}} {}")
  }

  fun testNgReportUnexpectedClosingTag() {
    doTestHtml("<div></p></div>")
  }

  fun testNgReportSubsequentOpenTagWithoutCloseTag() {
    doTestHtml("<div</div>")
  }

  fun testNgParseBoundProperties() {
    doTestHtml("<div [someProp]='v'></div>" +
               "<div [some-prop]='v'></div>" +
               "<div [dot.name]='v'></div>" +
               "<div [attr.someAttr]='v'></div>" +
               "<div [class.some-class]='v'></div>" +
               "<div [style.someStyle]='v'></div>" +
               "<div data-[style.someStyle]='v'></div>" +
               "<div bind-prop='v'></div>" +
               "<div prop='{{v}}'></div>" +
               "<div bind-animate-someAnimation='val'></div>" +
               "<div [@someAnimation]='v'></div>" +
               "<div @someAnimation='v'></div>")
  }

  fun testNgParseEvents() {
    doTestHtml("<div (window:event)='v'></div>" +
               "<div (event)='v'></div>" +
               "<div data-(event)='v'></div>" +
               "<div (some-event)='v'></div>" +
               "<div (someEvent)='v'></div>" +
               "<div on-event='v'></div>")
  }

  fun testNgParseAnimationEvents() {
    doTestHtml("<a (@click)='doStuff()'></a>" +
               "<b on-animate-click='doStuff()'></b>" +
               "<a (@click.done)='doStuff()'></a>" +
               "<b on-animate-click.start='doStuff()'></b>")
  }

  fun testNgParseReferences() {
    doTestHtml("<div #a></div>" +
               "<div ref-a></div>" +
               "<div a #a='dirA'></div>" +
               "<div #a-b></div>" +
               "<div #></div>" +
               "<div ref- ></div>")
  }

  fun testNgParseVariables() {
    doTestHtml("<div let-a></div>" +
               "<ng-template let-a='b'></ng-template>")
  }

  fun testNgParseInlineTemplates() {
    doTestHtml("<div *ngIf></div>" +
               "<div *ngIf='condition'></div>" +
               "<div *ngIf='#a=b'>Report error on vars with #</div>" +
               "<div *ngIf='let a=b'></div>" +
               "<div data-*ngIf='let a=b'></div>" +
               "<div *ngIf='expr as local'></div>")
  }

  fun testNgReportErrorsInExpressions() {
    doTestHtml("<div [prop]='a b'></div>")
  }

  fun testNgBindingAttributeComplex() {
    doTestHtml("<div (lang)=\"{'current':i == (wordIndex | async)}\"></div>")
  }

  fun testNgCss() {
    doTestHtml("<div *ngFor=\"let something of items\" class=\"inDaClass foo\" style=\"color: #fff\"></div>")
  }

  fun testNgBindingElvis() {
    doTestHtml("<div lang=\"{{interpolation?.here}}\"></div>")
  }

  fun testNgBindingNullishCoalescing() {
    doTestHtml("<div lang=\"{{interpolation ?? 'fallback'}}\"></div>")
  }

  fun testNgEntity() {
    doTestHtml("<div>{{foo ? ' &mdash;' + bar : \"\"}}</div>")
  }

  fun testNgStringWithEntity() {
    doTestHtml("{{ &quot;fo&#123;o\" }}")
  }

  fun testNgStringWithEntity2() {
    doTestHtml("<div [input]='&apos;foo&quot;&dash;&apos;'")
  }

  fun testNgStringWithEntity3() {
    doTestHtml("<div [input]='&apos;foo&quot;&dash;&apos;\"second\"'")
  }

  fun testNgWeb20713() {
    doTestHtml("<h5>Last Updated: {{(viewModel.lastUpdated$ | async) | date:'mediumTime'}}</h5>")
  }

  fun testNgWeb24804() {
    doTestHtml("<div *myStructuralDirective style=\"z-index: 10;\"></div>")
  }

  fun testNgTextInterpolation() {
    doTestHtml("""
                 <div>my {{interpolated}} text</div>
                 <div>my{{interpolated}}text</div>
                 <div>my{{double}}{{interpolated}}text</div>
                 <div>my{{double}}double{{interpolated}}text</div>
                 """.trimIndent())
  }

  fun testNgTextInterpolationWithLineBreaks() {
    doTestHtml("""
                 {{todo
                             | started : status
                             | search : term
                             }}
                             """.trimIndent())
  }

  fun testNgIgnoredInterpolation() {
    doTestHtml("this {{ is {{ <ignored/> interpolation }}")
  }

  fun testNgIgnoredInterpolationInTag() {
    doTestHtml("<div>this{{is{{<ignored/> interpolation}}</div>another{{ignored{{<interpolation/>")
  }

  fun testNgInterpolationEmpty() {
    doTestHtml("empty {{}} interpolation")
  }

  fun testNgScriptWithEventAndAngularAttr() {
    doTestHtml("""
                 <script src="//example.com" onerror="console.log(1)" (error)='console.log(1)'onload="console.log(1)" (load)='console.log(1)'>
                   console.log(2)
                 </script>
                 <div></div>
                 """.trimIndent())
  }

  fun testNgStyleTag() {
    doTestHtml("""
                 <style>
                   div {
                   }
                 </style>
                 <div></div>
                 """.trimIndent())
  }

  fun testNgStyleAngularAttr() {
    doTestHtml("""
                 <style (load)='disabled=true'>
                   div {
                   }
                 </style>
                 <div></div>
                 """.trimIndent())
  }

  fun testNgStyleWithEventAndAngularAttr() {
    doTestHtml("""
                 <style (load)='disabled=true' onload="this.disabled=true" (load)='disabled=true'>
                   div {
                   }
                 </style>
                 <div></div>
                 """.trimIndent())
  }

  fun testNgContentSelect() {
    doTestHtml("<div><ng-content select='foo,bar'></ng-content></div>")
  }

  fun testNgNonBindable() {
    doTestHtml("<div><span ngNonBindable>f{{bar}}a</span>f{{foo}}a</div>")
  }

  fun testNgNonBindable2() {
    doTestHtml("<div><span ngNonBindable ngNonBindable>s{{bar}}e</span>s{{foo}}e</div>")
  }

  fun testNgNonBindable3() {
    doTestHtml("<div><span ngNonBindable ngNonBindable>{{bar}}<b ngNonBindable>{{boo}}</b></span>{{foo}}</div>")
  }

  fun testNgNonBindable4() {
    doTestHtml("<p ngNonBindable>{{foo}}<p>{{bar}}")
  }

  fun testNgNonQuotedAttrs() {
    doTestHtml("""
                 <div (click)=doIt()></div>
                 <div [id]=foo></div>
                 <div #foo=bar></div>
                 <ng-content select=[header-content]></ng-content>
                 
                 """.trimIndent())
  }

  fun testEmptyLetAndRef() {
    doTestHtml("<ng-template let-/><div let-/><div #/><div ref-/>")
  }

  companion object {
    private fun ensureReparsingConsistent(file: PsiFile) {
      DebugUtil.performPsiModification<RuntimeException>("ensureReparsingConsistent") {
        val fileText = file.getText()
        val diffLog = BlockSupportImpl().reparseRange(
          file, file.getNode(), TextRange.allOf(fileText), fileText, EmptyProgressIndicator(), fileText)
        val event: TreeChangeEvent = diffLog.performActualPsiChange(file)
        UsefulTestCase.assertEmpty(event.getChangedElements())
      }
    }
  }
}
