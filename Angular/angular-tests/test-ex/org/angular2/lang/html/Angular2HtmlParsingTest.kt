// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.javascript.testFramework.web.JSHtmlParsingTest
import com.intellij.psi.PsiFile
import com.intellij.testFramework.junit5.TestApplication
import org.angular2.Angular2TestUtil
import org.angular2.lang.expr.Angular2ParserDefinition
import org.angular2.lang.html.lexer.Angular2HtmlEmbeddedContentSupport
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@TestApplication
open class Angular2HtmlParsingTest : JSHtmlParsingTest("html") {

  protected open val templateSyntax: Angular2TemplateSyntax = Angular2TemplateSyntax.V_2

  @BeforeEach
  fun setUp() {
    configureFromParserDefinition(
      when (templateSyntax) {
        Angular2TemplateSyntax.V_22 -> Angular22HtmlParserDefinition()
        Angular2TemplateSyntax.V_20 -> Angular20HtmlParserDefinition()
        Angular2TemplateSyntax.V_18_1 -> Angular181HtmlParserDefinition()
        Angular2TemplateSyntax.V_17 -> Angular17HtmlParserDefinition()
        Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS,
        Angular2TemplateSyntax.V_2,
          -> Angular2HtmlParserDefinition()
      }, "html")

    sequenceOf(Angular2ParserDefinition(), Angular2HtmlParserDefinition()).forEach(::registerParserDefinition)
  }

  override fun getEmbeddedContentSupports(): List<HtmlEmbeddedContentSupport> {
    return super.getEmbeddedContentSupports() + Angular2HtmlEmbeddedContentSupport()
  }

  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "html/parser"
  }

  override fun checkResult(fullDataPath: String, targetDataName: String, file: PsiFile) {
    val dataPathNoSlash = fullDataPath.removeSuffix("/")
    val adjustedDataPath = // Iterate over syntax versions starting from the `templateSyntax` down to V_2
      Angular2TemplateSyntax.entries.toList().asReversed().asSequence()
        .dropWhile { it != templateSyntax }
        .filter { it != Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS }
        .firstNotNullOfOrNull { syntax ->
          "${dataPathNoSlash}${syntax.dirSuffix}/".takeIf { File("$it$targetDataName.txt").exists() }
        }
      ?: "${dataPathNoSlash}${templateSyntax.dirSuffix}/"
    super.checkResult(adjustedDataPath, targetDataName, file)
  }

  private val Angular2TemplateSyntax.dirSuffix: String get() = if (this == Angular2TemplateSyntax.V_2) "" else "_$this"

  @Test
  fun testNgParseElementsInsideNgTemplate() {
    doTestHtml("<ng-template><span></span></ng-template>")
  }

  @Test
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

  @Test
  fun testNgCloseVoidElementsOnTextNodes() {
    doTestHtml("<p>before<br>after</p>")
  }

  @Test
  fun testNgSupportOptionalEndTags() {
    doTestHtml("<div><p>1<p>2</div>")
  }

  @Test
  fun testNgSupportNestedElements() {
    doTestHtml("<ul><li><ul><li></li></ul></li></ul>")
  }

  @Test
  fun testNgSupportSelfClosingVoidElements() {
    doTestHtml("<input />")
  }

  @Test
  fun testNgParseExpansionForms1() {
    doTestHtml("<div>before{messages.length, plural, =0 {You have <b>no</b> messages} =1 {One {{message}}}}after</div>")
  }

  @Test
  fun testNgParseExpansionForms2() {
    doTestHtml("<div><span>{a, plural, =0 {b}}</span></div>")
  }

  @Test
  fun testNgParseExpansionForms3() {
    doTestHtml("{messages.length, plural, =0 { {p.gender, select, male {m}} }}")
  }

  @Test
  fun testNgErrorOnUnterminatedExpansionForm() {
    doTestHtml("{messages.length, plural, =0 {one}")
  }

  @Test
  fun testNgICUWithNumbers() {
    doTestHtml("{sex, select, male {m} female {f} 0 {other}}")
  }

  @Test
  fun testNgErrorOnUnterminatedExpansionCase() {
    doTestHtml("{messages.length, plural, =0 {one")
  }

  @Test
  fun testNgErrorOnInvalidHTMLInExpansionCase() {
    doTestHtml("{messages.length, plural, =0 {<div>}}")
  }

  @Test
  fun testNgWhitespacesInExpansionCase() {
    doTestHtml("{ messages . length,  plural ,  =0   {  <div> } }")
  }

  @Test
  fun testExpansionFormComplex() {
    doTestHtml("<div>Text{ form, open, =23 {{{{foo: 12} }} is {inner, open, =34{{{\"test\"}} cool } =12{<tag test='12'></tag>}}}}}} {}")
  }

  @Test
  fun testNgReportUnexpectedClosingTag() {
    doTestHtml("<div></p></div>")
  }

  @Test
  fun testNgReportSubsequentOpenTagWithoutCloseTag() {
    doTestHtml("<div</div>")
  }

  @Test
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

  @Test
  fun testNgParseEvents() {
    doTestHtml("<div (window:event)='v'></div>" +
               "<div (event)='v'></div>" +
               "<div data-(event)='v'></div>" +
               "<div (some-event)='v'></div>" +
               "<div (someEvent)='v'></div>" +
               "<div on-event='v'></div>")
  }

  @Test
  fun testNgParseAnimationEvents() {
    doTestHtml("<a (@click)='doStuff()'></a>" +
               "<b on-animate-click='doStuff()'></b>" +
               "<a (@click.done)='doStuff()'></a>" +
               "<b on-animate-click.start='doStuff()'></b>")
  }

  @Test
  fun testNgParseReferences() {
    doTestHtml("<div #a></div>" +
               "<div ref-a></div>" +
               "<div a #a='dirA'></div>" +
               "<div #a-b></div>" +
               "<div #></div>" +
               "<div ref- ></div>")
  }

  @Test
  fun testNgParseVariables() {
    doTestHtml("<div let-a></div>" +
               "<ng-template let-a='b'></ng-template>")
  }

  @Test
  fun testNgParseInlineTemplates() {
    doTestHtml("<div *ngIf></div>" +
               "<div *ngIf='condition'></div>" +
               "<div *ngIf='#a=b'>Report error on vars with #</div>" +
               "<div *ngIf='let a=b'></div>" +
               "<div data-*ngIf='let a=b'></div>" +
               "<div *ngIf='expr as local'></div>")
  }

  @Test
  fun testNgReportErrorsInExpressions() {
    doTestHtml("<div [prop]='a b'></div>")
  }

  @Test
  fun testNgBindingAttributeComplex() {
    doTestHtml("<div (lang)=\"{'current':i == (wordIndex | async)}\"></div>")
  }

  @Test
  fun testNgCss() {
    doTestHtml("<div *ngFor=\"let something of items\" class=\"inDaClass foo\" style=\"color: #fff\"></div>")
  }

  @Test
  fun testNgBindingElvis() {
    doTestHtml("<div lang=\"{{interpolation?.here}}\"></div>")
  }

  @Test
  fun testNgBindingNullishCoalescing() {
    doTestHtml("<div lang=\"{{interpolation ?? 'fallback'}}\"></div>")
  }

  @Test
  fun testNgEntity() {
    doTestHtml("<div>{{foo ? ' &mdash;' + bar : \"\"}}</div>")
  }

  @Test
  fun testNgStringWithEntity() {
    doTestHtml("{{ &quot;fo&#123;o\" }}")
  }

  @Test
  fun testNgStringWithEntity2() {
    doTestHtml("<div [input]='&apos;foo&quot;&dash;&apos;'")
  }

  @Test
  fun testNgStringWithEntity3() {
    doTestHtml("<div [input]='&apos;foo&quot;&dash;&apos;\"second\"'")
  }

  @Test
  fun testNgWeb20713() {
    doTestHtml("<h5>Last Updated: {{(viewModel.lastUpdated$ | async) | date:'mediumTime'}}</h5>")
  }

  @Test
  fun testNgWeb24804() {
    doTestHtml("<div *myStructuralDirective style=\"z-index: 10;\"></div>")
  }

  @Test
  fun testNgTextInterpolation() {
    doTestHtml("""
                 <div>my {{interpolated}} text</div>
                 <div>my{{interpolated}}text</div>
                 <div>my{{double}}{{interpolated}}text</div>
                 <div>my{{double}}double{{interpolated}}text</div>
                 """.trimIndent())
  }

  @Test
  fun testNgTextInterpolationWithLineBreaks() {
    doTestHtml("""
                 {{todo
                             | started : status
                             | search : term
                             }}
                             """.trimIndent())
  }

  @Test
  fun testNgIgnoredInterpolation() {
    doTestHtml("this {{ is {{ <ignored/> interpolation }}")
  }

  @Test
  fun testNgIgnoredInterpolationInTag() {
    doTestHtml("<div>this{{is{{<ignored/> interpolation}}</div>another{{ignored{{<interpolation/>")
  }

  @Test
  fun testNgInterpolationEmpty() {
    doTestHtml("empty {{}} interpolation")
  }

  @Test
  fun testNgScriptWithEventAndAngularAttr() {
    doTestHtml("""
                 <script src="//example.com" onerror="console.log(1)" (error)='console.log(1)'onload="console.log(1)" (load)='console.log(1)'>
                   console.log(2)
                 </script>
                 <div></div>
                 """.trimIndent())
  }

  @Test
  fun testNgStyleTag() {
    doTestHtml("""
                 <style>
                   div {
                   }
                 </style>
                 <div></div>
                 """.trimIndent())
  }

  @Test
  fun testNgStyleAngularAttr() {
    doTestHtml("""
                 <style (load)='disabled=true'>
                   div {
                   }
                 </style>
                 <div></div>
                 """.trimIndent())
  }

  @Test
  fun testNgStyleWithEventAndAngularAttr() {
    doTestHtml("""
                 <style (load)='disabled=true' onload="this.disabled=true" (load)='disabled=true'>
                   div {
                   }
                 </style>
                 <div></div>
                 """.trimIndent())
  }

  @Test
  fun testNgContentSelect() {
    doTestHtml("<div><ng-content select='foo,bar'></ng-content></div>")
  }

  @Test
  fun testNgNonBindable() {
    doTestHtml("<div><span ngNonBindable>f{{bar}}a</span>f{{foo}}a</div>")
  }

  @Test
  fun testNgNonBindable2() {
    doTestHtml("<div><span ngNonBindable ngNonBindable>s{{bar}}e</span>s{{foo}}e</div>")
  }

  @Test
  fun testNgNonBindable3() {
    doTestHtml("<div><span ngNonBindable ngNonBindable>{{bar}}<b ngNonBindable>{{boo}}</b></span>{{foo}}</div>")
  }

  @Test
  fun testNgNonBindable4() {
    doTestHtml("<p ngNonBindable>{{foo}}<p>{{bar}}")
  }

  @Test
  fun testNgNonQuotedAttrs() {
    doTestHtml("""
                 <div (click)=doIt()></div>
                 <div [id]=foo></div>
                 <div #foo=bar></div>
                 <ng-content select=[header-content]></ng-content>
                 
                 """.trimIndent())
  }

  @Test
  fun testNgBoundAttributesWithSlash() {
    doTestHtml("""
                 <div [class.left-1/2]="first" [attr.a/b]=second></div>
                 <div [class.left-1/></div>
                 """.trimIndent())
  }

  @Test
  fun testNgBoundAttributesWithNestedBrackets() {
    doTestHtml("""
                 <div [class.[&>svg]:w-4]="first" [class.bg-[url('/a.png')]]="second"></div>
                 <div [class.[&>svg]:w-4="third"></div>
                 """.trimIndent())
  }

  @Test
  fun testEmptyLetAndRef() {
    doTestHtml("<ng-template let-/><div let-/><div #/><div ref-/>")
  }


  @Test
  fun testIfBlock() {
    doTestHtml("""
      @if ( user.isHuman ) {
        <human-profile [data]="user" />
      } @else if 
      (user.isRobot) 
      {
          <robot-profile [data]="user" />
      } @else {
        <p>The profile is unknown!
      }
    """.trimIndent())
  }

  @Test
  fun testIncompleteBlock1() {
    doTestHtml("""
      @if something doesn't work
    """.trimIndent())
  }

  @Test
  fun testIncompleteBlock2() {
    doTestHtml("""
      @if ( this is not finished
    """.trimIndent())
  }

  @Test
  fun testIncompleteBlock3() {
    doTestHtml("""
      @if ( ) this is not finished
    """.trimIndent())
  }

  @Test
  fun testIncompleteBlock4() {
    doTestHtml("""
      @if ( ) 
      {this is not finished
    """.trimIndent())
  }

  @Test
  fun testIncompleteBlock5() {
    doTestHtml("""
      @if 
      else (
    """.trimIndent())
  }

  @Test
  fun testLetBlockValid() {
    doTestHtml("""
      @let foo = test(12); the end
    """.trimIndent())
  }

  @Test
  fun testTypeOfSimple() {
    doTestHtml("""
      {{typeof value === 'string'}}
    """.trimIndent())
  }

  @Test
  fun testDoubleBracesWithinStringInterpolation() {
    doTestHtml("""
      <label>{{ 'Version: {{version}}' | translate { version: '13}}' } }}</label>
    """.trimIndent())
  }

  @Test
  fun testInterpolationEscape() {
    doTestHtml("""
      <label>{{ 'Version: \<a>' }}</label>
    """.trimIndent())
  }

  @Test
  fun testInterpolationComment() {
    doTestHtml("""
      <label>{{ 'Version:' // ' }}</label>
    """.trimIndent())
  }

  @Test
  fun testInterpolatedHrefUrl() {
    doTestHtml("""
      <main class="main">
        <a href="{{ test + 'https://' }}">{{ test + 'foo' }}</a>

        @if (playerSummary !== undefined) {
          {{ test + 'foo' + test }}
        }
      </main>
    """.trimIndent())
  }

  @Test
  fun testTemplateLiteral() {
    doTestHtml("""
      <a [href]="`https://www.google.com?q=${'$'}{test + {a:12}.a}`"></a>
      {{ `https://www.google.com?q=${'$'}{test + {a:12}.a}` }}
    """.trimIndent())
  }

  @Test
  fun testVoidKeyword() {
    doTestHtml("""
      <div (click)='void fun()'></div>
    """)
  }

  @Test
  fun testInElementComments() {
    doTestHtml("""
      <div
        // comment 1
        attr1="value1"
        /* comment 2 */ [input]="value2" /*
          comment 3
        */
        (output)="handler()" // comment 4
      ></div>
      <div attr// glued
      ></div>
      <div // swallows the tag end ></div>
      <span></span>
    """.trimIndent())
  }

  @Test
  fun testInElementCommentInEndTag() {
    doTestHtml("""
      <div></div // comment>
      <span></span>
    """.trimIndent())
  }

  @Test
  fun testInElementCommentUnterminated() {
    doTestHtml("""
      <div /* comment
      <span></span>
    """.trimIndent())
  }

  @Test
  fun testPowerOperator() {
    doTestHtml("""
      <div (click)='12 ** 2 ** 3'></div>
    """)
  }

  @Test
  fun testAssignmentExpressions() {
    doTestHtml("""
      {{ test += 1 }}
      <div (click)='test += 1'></div>
      <div (click)='test = 1 + 2 + 3'></div>
    """.trimIndent())
  }

  @Test
  fun testMultilineStringInInterpolation() {
    doTestHtml("""
      Label: <b></b>
        <span>{{ (false) ? 'Pretty short string' : 'Really really long string which will take up multiple lines
        and that should be okay, but in this case it is not.' }}</span>
    """.trimIndent())
  }

}
