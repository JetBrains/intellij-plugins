// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.angular2.lang.html.lexer.Angular2HtmlLexer
import org.angularjs.AngularTestUtil
import org.jetbrains.annotations.NonNls

open class Angular2HtmlLexerTest : LexerTestCase() {
  private var myFixture: IdeaProjectTestFixture? = null

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()

    // needed for various XML extension points registration
    myFixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR, getTestName(false)).getFixture()
    myFixture!!.setUp()
  }

  @Throws(Exception::class)
  override fun tearDown() {
    try {
      myFixture!!.tearDown()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testNoNewline() {
    doTest("<t>a</t>")
  }

  fun testNewlines() {
    doTest("<t\n>\r\na\r</t>")
  }

  fun testComments() {
    doTest("<!-- {{ v }} -->")
  }

  fun testInterpolation1() {
    doTest("<t a=\"{{v}}\" b=\"s{{m}}e\" c='s{{m//c}}e'>")
  }

  fun testInterpolation2() {
    doTest("{{ a }}b{{ c // comment }}")
  }

  fun testMultiLineComment() {
    doTest("{{ a }}b{{ c // comment\non\nmultiple\nlines }}")
  }

  fun testBoundAttributes() {
    doTest("<a [src]=bla() (click)='event()'></a>")
  }

  fun testMultipleInterpolations() {
    doTest("{{test}} !=bbb {{foo() - bar()}}")
  }

  fun testInterpolationIgnored() {
    doTest("<div> this is ignored {{<interpolation> }}")
  }

  fun testInterpolationIgnored2() {
    doTest("this {{ is {{ <ignored/> interpolation }}")
  }

  fun testInterpolationIgnored3() {
    doTest("<div foo=\"This {{ is {{ ignored interpolation\"> }}<a foo=\"{{\">")
  }

  fun testInterpolationIgnored4() {
    doTest("<div foo='This {{ is {{ ignored interpolation'> }}<a foo='{{'>")
  }

  fun testInterpolationEmpty() {
    doTest("{{}}<div foo='{{}}' foo='a{{}}b' bar=\"{{}}\" bar=\"a{{}}b\">{{}}</div>a{{}}b<div>a{{}}b</div>")
  }

  fun testInterpolationCharEntityRefs() {
    doTest("&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}<div foo='&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}' bar=\"&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}\">")
  }

  fun testInterpolationEntityRefs() {
    doTest("&foo;{{foo&foo;bar}}{{&foo;}}<div foo='&foo;{{foo&foo;bar}}{{&foo;}}' bar=\"&foo;{{foo&foo;bar}}{{&foo;}}\">")
  }

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

  fun testEscapes() {
    doTest("{{today | date:'d \\'days so far in\\' LLLL'}}" +
           "<div [input]=\"'test&quot;test\\u1234\\u123\\n\\r\\t'\">" +
           "<div [input]='\"ttt\" + &apos;str\\u1234ing&apos;'>")
  }

  fun testTextInEscapedQuotes() {
    doTest("<div [foo]=\"&quot;test&quot; + 12\">")
  }

  fun testTextInEscapedApos() {
    doTest("<div [foo]=\"&apos;test&apos; + 12\">")
  }

  fun testExpansionForm() {
    doTest("{one.two, three, =4 {four} =5 {five} foo {bar} }")
  }

  fun testExpansionFormWithTextElementsAround() {
    doTest("before{one.two, three, =4 {four}}after")
  }

  fun testExpansionFormTagSingleChild() {
    doTest("<div><span>{a, b, =4 {c}}</span></div>")
  }

  fun testExpansionFormWithTagsInIt() {
    doTest("{one.two, three, =4 {four <b>a</b>}}")
  }

  fun testExpansionFormWithInterpolation() {
    doTest("{one.two, three, =4 {four {{a}}}}")
  }

  fun testExpansionFormNested() {
    doTest("{one.two, three, =4 {{xx, yy, =x {one}} }}")
  }

  fun testExpansionFormComplex() {
    doTest("<div>Text{ form, open, =23 {{{{foo: 12} }} is {inner, open, =34{{{\"test\"}} cool } =12{<tag test='12'></tag>}}}}}} {}")
  }

  fun testScriptSrc() {
    doTest("""
             <body>
             <script src="">var i</script>
             foo
             </body>
             """.trimIndent())
  }

  fun testScript() {
    doTest("""
             <body>
             <script>var i</script>
             foo
             </body>
             """.trimIndent())
  }

  fun testScriptAngularAttr() {
    doTest("""
             <body>
             <script (foo)="">var i</script>
             foo
             </body>
             """.trimIndent())
  }

  fun testScriptWithEventAndAngularAttr() {
    doTest("""
             <script src="//example.com" onerror="console.log(1)" (error)='console.log(1)'onload="console.log(1)" (load)='console.log(1)'>
               console.log(2)
             </script>
             <div></div>
             """.trimIndent())
  }

  fun testStyleTag() {
    doTest("""
             <style>
               div {
               }
             </style>
             <div></div>
             """.trimIndent())
  }

  fun testStyleAngularAttr() {
    doTest("""
             <style (load)='disabled=true'>
               div {
               }
             </style>
             <div></div>
             """.trimIndent())
  }

  fun testStyleWithEventAndAngularAttr() {
    doTest("""
             <style (load)='disabled=true' onload="this.disabled=true" (load)='disabled=true'>
               div {
               }
             </style>
             <div></div>
             """.trimIndent())
  }

  fun testStyleAfterBinding() {
    doTest("""
             <div *foo style="width: 13px">
               <span (click)="foo"></span>
             </div>
             """.trimIndent())
  }

  fun testStyleAfterStyle() {
    doTest("""
             <div style style *foo='bar'>
               <span style='width: 13px' (click)="foo"></span>
             </div>
             """.trimIndent())
  }

  fun testBindingAfterStyle() {
    doTest("""
             <div style *foo='bar'>
               <span style='width: 13px' (click)="foo"></span>
             </div>
             """.trimIndent())
  }

  fun testEmptyStructuralDirective() {
    doTest("""
  <div *foo [bar]=""></div>
  <div [bar]="some"></div>
  """.trimIndent())
  }

  fun testEmptyHtmlEvent() {
    doTest("""
  <div onclick onclick=""></div>
  <div [bar]="some"></div>
  """.trimIndent())
  }

  fun testTextarea() {
    doTest("<textarea>with { some } {{wierd}} &nbsp; <stuff> in it</textarea>")
  }

  override fun doTest(text: @NonNls String) {
    super.doTest(text)
    checkCorrectRestart(text)
  }

  override fun createLexer(): Lexer {
    return Angular2HtmlLexer(true, null)
  }

  override fun getDirPath(): String {
    return AngularTestUtil.getLexerTestDirPath() + "html/lexer"
  }
}
