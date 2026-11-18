// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.util.TextRange
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.prettierjs.formatting.isInsidePrettierIgnoredElement
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Checks [isInsidePrettierIgnoredElement] for the `<selection>` of a file. */
@TestApplication
class PrettierIgnoreTest {
  companion object {
    private val projectFixture = sharedCodeInsightProjectFixture()
  }

  private val myFixture by sharedCodeInsightFixture(projectFixture)

  @Test
  fun testMemberOfIgnoredClass() = assertIgnored("a.ts", """
    // prettier-ignore
    export class A {
        /** doc */   b  =  1
        <selection>/** doc */   c  =  2</selection>
    }
  """)

  @Test
  fun testWhitespaceInIgnoredClass() = assertIgnored("a.ts", """
    // prettier-ignore
    export class A {
        b  =  1
    <selection>
    </selection>
    }
  """)

  @Test
  fun testClassAfterIgnoreComment() = assertIgnored("a.ts", """
    // prettier-ignore
    <selection>export class A {
        b  =  1
    }
    </selection>
  """)

  @Test
  fun testNestedInIgnoredStatement() = assertIgnored("a.js", """
    // prettier-ignore
    const x = {
      a: [1, 2,
          <selection>3</selection>],
    };
  """)

  @Test
  fun testBlankLineAfterComment() = assertIgnored("a.js", """
    // prettier-ignore

    const x = { <selection>a:1</selection> };
  """)

  @Test
  fun testOtherCommentBetween() = assertIgnored("a.js", """
    // prettier-ignore
    // eslint-disable-next-line
    const x = { <selection>a:1</selection> };
  """)

  @Test
  fun testBlockCommentOnSameLine() = assertIgnored("a.js", """
    /* prettier-ignore */ const x = { <selection>a:1</selection> };
  """)

  @Test
  fun testCommentAfterBrace() = assertIgnored("a.js", """
    function f() { // prettier-ignore
      foo( <selection>1</selection> );
    }
  """)

  @Test
  fun testDecoratedExportedClass() = assertIgnored("a.ts", """
    // prettier-ignore
    @dec
    export class A { <selection>b  =  1</selection> }
  """)

  @Test
  fun testExportDefaultClass() = assertIgnored("a.ts", """
    // prettier-ignore
    export default class A { <selection>b  =  1</selection> }
  """)

  @Test
  fun testJsonProperty() = assertIgnored("a.json", """
    {
      "a": 1,
      // prettier-ignore
      "b": [1, <selection>2</selection>]
    }
  """)

  @Test
  fun testIgnoreCommentInsideRange() = assertNotIgnored("a.ts", """
    import { x } from "x";

    <selection>// prettier-ignore
    export class A {
        b  =  1
    }</selection>
  """)

  @Test
  fun testTrailingCommentOfPreviousStatement() = assertNotIgnored("a.js", """
    foo(); // prettier-ignore
    bar( <selection>1</selection> );
  """)

  @Test
  fun testTrailingCommentOfPreviousMember() = assertNotIgnored("a.ts", """
    class A {
      b = 1 // prettier-ignore
      c = <selection>2</selection>
    }
  """)

  @Test
  fun testTrailingCommentOfPreviousJsonProperty() = assertNotIgnored("a.json", """
    {
      "a": 1, // prettier-ignore
      "b": [1, <selection>2</selection>]
    }
  """)

  @Test
  fun testDocComment() = assertNotIgnored("a.js", """
    /** prettier-ignore */
    const x = { <selection>a:1</selection> };
  """)

  @Test
  fun testOtherComment() = assertNotIgnored("a.js", """
    // prettier-ignore-start
    const x = { <selection>a:1</selection> };
  """)

  @Test
  fun testCommentBeforePreviousStatement() = assertNotIgnored("a.js", """
    // prettier-ignore
    foo();
    bar( <selection>1</selection> );
  """)

  @Test
  fun testNoComment() = assertNotIgnored("a.js", """
    const x = { <selection>a:1</selection> };
  """)

  private fun assertIgnored(fileName: String, text: String) = doTest(fileName, text, true)

  private fun assertNotIgnored(fileName: String, text: String) = doTest(fileName, text, false)

  private fun doTest(fileName: String, text: String, expected: Boolean) {
    myFixture.configureByText(fileName, text.trimIndent())
    val actual = runReadActionBlocking {
      val selection = myFixture.editor.selectionModel
      val range = TextRange(selection.selectionStart, selection.selectionEnd)
      isInsidePrettierIgnoredElement(myFixture.file, range)
    }
    assertEquals(expected, actual)
  }
}
