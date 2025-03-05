// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.lexer.Lexer
import org.angular2.lang.html.lexer.Angular2HtmlLexer
import org.angular2.Angular2TestUtil

open class Angular2HtmlHighlightingLexerTest : Angular2HtmlLexerTest() {
  override fun createLexer(): Lexer {
    return Angular2HtmlLexer(true, templateSyntax, null)
  }

  fun `testVarWith$`() {
    doTest("""
             {{ publicUsedField }}
             <div [id]="privateUsedField"></div>
             {{ publicUsedConstructorField }}
             <div [id]="privateUsedConstructorField"></div>
             <div (click)="publicUsedMethod()"></div>
             <div (click)="privateUsedMethod()"></div>

             <div *ngIf="myObservable${'$'} | async"></div>

             {{ testFn() }}
             """.trimIndent())
  }

  fun testNestedInterpolations() {
    doTest("""
             <div *ngFor='let card of cards'>
               {{card.name}} - {{card.damage}}
               <button (click)='damage(card)'> Damage </button>
             </div>
             """.trimIndent())
  }

  fun testTemplateLiteral() {
    doTest("""
      {{ { obj: `template literal ${"$"}{ with + `nested text { }` } `, foo: 12} }}
    """.trimIndent())
  }

  override fun getDirPath(): String {
    return Angular2TestUtil.getLexerTestDirPath() + "html/highlightingLexer"
  }
}
