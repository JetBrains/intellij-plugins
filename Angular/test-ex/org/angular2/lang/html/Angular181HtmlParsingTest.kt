// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

class Angular181HtmlParsingTest : Angular17HtmlParsingTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_18_1

  fun testLetBlockInvalidId() {
    doTestHtml("""
      @let 12foo = test(12); the end
    """.trimIndent())
  }

  fun testLetBlockInvalidGlued() {
    doTestHtml("""
      @letfoo = test(12); the end
    """.trimIndent())
  }

  fun testLetBlockNoSemicolon() {
    doTestHtml("""
      @let foo = test(12) the end
    """.trimIndent())
  }

  fun testLetBlockNoEquals() {
    doTestHtml("""
      @let foo test(12); the end
    """.trimIndent())
  }

  fun testLetBlockEmptyValue() {
    doTestHtml("""
      @let foo =; the end
    """.trimIndent())
  }

  fun testLetBlockString() {
    doTestHtml("""
      @let foo = "foo" + test(12); the end
    """.trimIndent())
  }

  fun testLetBlockStringUnterminated() {
    doTestHtml("""
      @let foo = "foo + test(12); 
      the end
    """.trimIndent())
  }

  fun testLetBlockStringMultiline() {
    doTestHtml("""
      @let foo = "foo 
        bar
        check" + test(12); the end
    """.trimIndent())
  }

  fun testLetBlockStringEscapeEof() {
    doTestHtml("""
      @let foo = "foo\""".trimIndent())
  }

  fun testLetBlockStrings() {
    doTestHtml("""
      @let foo = "foo\";bar" + 'foo\';bar' + test(12); the end
    """.trimIndent())
  }

  fun testLetBlockExample1() {
    doTestHtml("""
      @let name = user.name;
      @let greeting = 'Hello, ' + name;
      @let data = data${'$'} | async;
      @let pi = 3.1459;
      @let coordinates = {x: 50, y: 100};
      @let longExpression = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit ' +
                            'sed do eiusmod tempor incididunt ut labore et dolore magna ' +
                            'Ut enim ad minim veniam...';
    """.trimIndent())
  }

  fun testLetBlockExample2() {
    doTestHtml("""
      @let topLevel = value;
      <div>
        @let insideDiv = value;
      </div>
      {{topLevel}} <!-- Valid -->
      {{insideDiv}} <!-- Valid -->
      @if (condition) {
        {{topLevel + insideDiv}} <!-- Valid -->
        @let nested = value;
        @if (condition) {
          {{topLevel + insideDiv + nested}} <!-- Valid -->
        }
      }
      <div *ngIf="condition">
        {{topLevel + insideDiv}} <!-- Valid -->
        @let nestedNgIf = value;
        <div *ngIf="condition">
           {{topLevel + insideDiv + nestedNgIf}} <!-- Valid -->
        </div>
      </div>
      {{nested}} <!-- Error, not hoisted from @if -->
      {{nestedNgIf}} <!-- Error, not hoisted from *ngIf -->
    """.trimIndent())
  }

}
