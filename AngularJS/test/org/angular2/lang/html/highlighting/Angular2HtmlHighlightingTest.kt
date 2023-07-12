// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting

import com.intellij.lexer.Lexer
import org.angular2.lang.html.lexer.Angular2HtmlLexerTest

open class Angular2HtmlHighlightingTest : Angular2HtmlLexerTest() {
  override fun createLexer(): Lexer {
    return Angular2HtmlHighlightingLexer(true, null, null)
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
}
