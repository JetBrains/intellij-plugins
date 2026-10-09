// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.lexer.HtmlLexer
import com.intellij.lexer.Lexer
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.lexerFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import org.angular2.Angular2TestUtil
import org.angular2.lang.html.lexer.Angular2HtmlLexer
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test
import java.io.File

// The test application is needed for various XML extension points registration
@TestApplication
open class Angular2HtmlLexerTest {
  protected open val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_2

  protected open val dirPath: String
    get() = Angular2TestUtil.getLexerTestDirPath() + "html/lexer"

  private val lexer by lexerFixture(Angular2TestUtil.getLexerTestDirPath()) { createLexer() }

  @Test
  fun testNoNewline() {
    doTest("<t>a</t>")
  }

  @Test
  fun testNewlines() {
    doTest("<t\n>\r\na\r</t>")
  }

  @Test
  fun testComments() {
    doTest("<!-- {{ v }} -->")
  }

  @Test
  fun testInterpolation1() {
    doTest("<t a=\"{{v}}\" b=\"s{{m}}e\" c='s{{m//c}}e'>")
  }

  @Test
  fun testInterpolation2() {
    doTest("{{ a }}b{{ c // comment }}")
  }

  @Test
  fun testMultiLineComment() {
    doTest("{{ a }}b{{ c // comment\non\nmultiple\nlines }}")
  }

  @Test
  fun testBoundAttributes() {
    doTest("<a [src]=bla() (click)='event()'></a>")
  }

  @Test
  fun testBoundAttributesWithSlash() {
    doTest("<div [class.left-1/2]=\"first\" [class.text-primary/80]='second' [attr.a/b]=third><img [src/set]/></div>")
  }

  @Test
  fun testBoundAttributesIncomplete() {
    doTest("""
             <div [foo/>
             <div [foo>
             <div [foo/bar></div>
             <div [foo/bar [baz]="a/b"></div>
             <div [foo[bar/baz]></div>
             <div [foo[bar>baz] [qux]="a"></div>
             <div [foo]/bar="a"></div>
             <div foo/bar]="a"></div>
             """.trimIndent())
  }

  @Test
  fun testBoundAttributesWithNestedBrackets() {
    doTest("""
             <div [class.[&>svg]:w-4]="a" [class.bg-[url('/a.png')]]="b" [class.content-['a_b']]='c'></div>
             <div [class.[&_[data-x=a]]:p-2]="d" [a/b][c/d]="e" [a b]="f"></div>
             """.trimIndent())
  }

  @Test
  fun testBoundAttributesWithNestedBracketsIncomplete() {
    doTest("""
             <div [a/b]x[c/d>
             <div [foo
               bar]="a"></div>
             <div [a]]/x="b"></div>
             """.trimIndent())
  }

  @Test
  fun testMultipleInterpolations() {
    doTest("{{test}} !=bbb {{foo() - bar()}}")
  }

  @Test
  fun testInterpolationIgnored() {
    doTest("<div> this is ignored {{<interpolation> }}")
  }

  @Test
  fun testInterpolationIgnored2() {
    doTest("this {{ is {{ <ignored/> interpolation }}")
  }

  @Test
  fun testInterpolationIgnored3() {
    doTest("<div foo=\"This {{ is {{ ignored interpolation\"> }}<a foo=\"{{\">")
  }

  @Test
  fun testInterpolationIgnored4() {
    doTest("<div foo='This {{ is {{ ignored interpolation'> }}<a foo='{{'>")
  }

  @Test
  fun testInterpolationEmpty() {
    doTest("{{}}<div foo='{{}}' foo='a{{}}b' bar=\"{{}}\" bar=\"a{{}}b\">{{}}</div>a{{}}b<div>a{{}}b</div>")
  }

  @Test
  fun testInterpolationCharEntityRefs() {
    doTest("&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}<div foo='&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}' bar=\"&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}\">")
  }

  @Test
  fun testInterpolationEntityRefs() {
    doTest("&foo;{{foo&foo;bar}}{{&foo;}}<div foo='&foo;{{foo&foo;bar}}{{&foo;}}' bar=\"&foo;{{foo&foo;bar}}{{&foo;}}\">")
  }

  @Test
  fun testComplex() {
    doTest("""
             <div *ngFor="let contact of value; index as i"
               (click)="contact"
             </div>

             <li *ngFor="let user of userObservable | async as users; index as i; first as isFirst">
               {{i}}/{{users.length}}. {{user}} <span *ngIf="isFirst">default</span>
             </li>

             <tr [style]="{'visible': con}" *ngFor="let contact of contacts; index as i">
               <td>{{i + 1}}</td>
             </tr>
             
             """.trimIndent())
  }

  @Test
  fun testEscapes() {
    doTest("{{today | date:'d \\'days so far in\\' LLLL'}}" +
           "<div [input]=\"'test&quot;test\\u1234\\u123\\n\\r\\t'\">" +
           "<div [input]='\"ttt\" + &apos;str\\u1234ing&apos;'>")
  }

  @Test
  fun testTextInEscapedQuotes() {
    doTest("<div [foo]=\"&quot;test&quot; + 12\">")
  }

  @Test
  fun testTextInEscapedApos() {
    doTest("<div [foo]=\"&apos;test&apos; + 12\">")
  }

  @Test
  fun testExpansionForm() {
    doTest("{one.two, three, =4 {four} =5 {five} foo {bar} }")
  }

  @Test
  fun testExpansionFormWithTextElementsAround() {
    doTest("before{one.two, three, =4 {four}}after")
  }

  @Test
  fun testExpansionFormTagSingleChild() {
    doTest("<div><span>{a, b, =4 {c}}</span></div>")
  }

  @Test
  fun testExpansionFormWithTagsInIt() {
    doTest("{one.two, three, =4 {four <b>a</b>}}")
  }

  @Test
  fun testExpansionFormWithInterpolation() {
    doTest("{one.two, three, =4 {four {{a}}}}")
  }

  @Test
  fun testExpansionFormNested() {
    doTest("{one.two, three, =4 {{xx, yy, =x {one}} }}")
  }

  @Test
  fun testExpansionFormComplex() {
    doTest("<div>Text{ form, open, =23 {{{{foo: 12} }} is {inner, open, =34{{{\"test\"}} cool } =12{<tag test='12'></tag>}}}}}} {}")
  }

  @Test
  fun testScriptSrc() {
    doTest("""
             <body>
             <script src="">var i</script>
             foo
             </body>
             """.trimIndent())
  }

  @Test
  fun testScript() {
    doTest("""
             <body>
             <script>var i</script>
             foo
             </body>
             """.trimIndent())
  }

  @Test
  fun testScriptAngularAttr() {
    doTest("""
             <body>
             <script (foo)="">var i</script>
             foo
             </body>
             """.trimIndent())
  }

  @Test
  fun testScriptWithEventAndAngularAttr() {
    doTest("""
             <script src="//example.com" onerror="console.log(1)" (error)='console.log(1)'onload="console.log(1)" (load)='console.log(1)'>
               console.log(2)
             </script>
             <div></div>
             """.trimIndent())
  }

  @Test
  fun testStyleTag() {
    doTest("""
             <style>
               div {
               }
             </style>
             <div></div>
             """.trimIndent())
  }

  @Test
  fun testStyleAngularAttr() {
    doTest("""
             <style (load)='disabled=true'>
               div {
               }
             </style>
             <div></div>
             """.trimIndent())
  }

  @Test
  fun testStyleWithEventAndAngularAttr() {
    doTest("""
             <style (load)='disabled=true' onload="this.disabled=true" (load)='disabled=true'>
               div {
               }
             </style>
             <div></div>
             """.trimIndent())
  }

  @Test
  fun testStyleAfterBinding() {
    doTest("""
             <div *foo style="width: 13px">
               <span (click)="foo"></span>
             </div>
             """.trimIndent())
  }

  @Test
  fun testStyleAfterStyle() {
    doTest("""
             <div style style *foo='bar'>
               <span style='width: 13px' (click)="foo"></span>
             </div>
             """.trimIndent())
  }

  @Test
  fun testBindingAfterStyle() {
    doTest("""
             <div style *foo='bar'>
               <span style='width: 13px' (click)="foo"></span>
             </div>
             """.trimIndent())
  }

  @Test
  fun testEmptyStructuralDirective() {
    doTest("""
  <div *foo [bar]=""></div>
  <div [bar]="some"></div>
  """.trimIndent())
  }

  @Test
  fun testEmptyHtmlEvent() {
    doTest("""
  <div onclick onclick=""></div>
  <div [bar]="some"></div>
  """.trimIndent())
  }

  @Test
  fun testTextarea() {
    doTest("<textarea>with { some } {{wierd}} &nbsp; <stuff> in it</textarea>")
  }

  @Test
  fun testIfBlock() {
    doTest("""
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
    doTest("""
      @if something doesn't work
    """.trimIndent())
  }

  @Test
  fun testIncompleteBlock2() {
    doTest("""
      @if ( this is not finished
    """.trimIndent())
  }

  @Test
  fun testIncompleteBlock3() {
    doTest("""
      @if ( ) this is not finished
    """.trimIndent())
  }

  @Test
  fun testIncompleteBlock4() {
    doTest("""
      @if ( ) 
      {this is not finished
    """.trimIndent())
  }

  @Test
  fun testIncompleteBlock5() {
    doTest("""
      @if 
      else (
    """.trimIndent())
  }

  @Test
  fun testEmptyIfBlock() {
    doTest("""
      @if () {
   
      }
    """.trimIndent())
  }

  @Test
  fun testBlockEmptyParameters() {
    doTest("""
      @if (; ;foo;) {
   
      }
    """.trimIndent())
  }

  @Test
  fun testBlockNameCanonicalForm() {
    doTest("""
      @block   name with     some spaces and ${'\t'} tabs   (arg)   {}
    """.trimIndent())
  }

  @Test
  fun testLetBlockValid() {
    doTest("""
      @let foo = test(12); the end
    """.trimIndent())
  }

  @Test
  fun testVoidKeyword() {
    doTest("""
      <div (click)='void fun()'></div>
    """)
  }

  @Test
  fun testPowerOperator() {
    doTest("""
      <div (click)='12 ** 2 ** 3'></div>
    """)
  }

  protected fun doTest(text: @NonNls String) {
    PlatformTestUtil.assertSameLinesWithFile(getExpectedFilePath(), lexer.printTokens(text, 0))
    lexer.checkCorrectRestart(text)
    if ((createLexer() as? HtmlLexer)?.isHighlighting == false) {
      lexer.checkCorrectRestartUsingPosition(text)
    }
  }

  protected open fun createLexer(): Lexer {
    return Angular2HtmlLexer(false, templateSyntax, null)
  }

  private fun getExpectedFilePath(): String {
    val basePath = dirPath
    val fileName = lexer.testName + ".txt"
    // Iterate over syntax versions starting from the `templateSyntax` down to V_2
    return Angular2TemplateSyntax.entries.toList().asReversed().asSequence()
             .dropWhile { it != templateSyntax }
             .filter { it != Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS }
             .firstNotNullOfOrNull { syntax ->
               "${basePath}${syntax.dirSuffix}/$fileName".takeIf { File(it).exists() }
             }
           ?: "${basePath}${templateSyntax.dirSuffix}/$fileName"
  }

  private val Angular2TemplateSyntax.dirSuffix: String get() = if (this == Angular2TemplateSyntax.V_2) "" else "_$this"
}
