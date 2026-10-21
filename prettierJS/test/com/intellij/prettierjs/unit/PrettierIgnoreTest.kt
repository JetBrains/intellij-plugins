// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.openapi.util.TextRange
import com.intellij.prettierjs.formatting.isInsidePrettierIgnoredElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/** Checks [isInsidePrettierIgnoredElement] for the `<selection>` of a file. */
class PrettierIgnoreTest : BasePlatformTestCase() {

  fun testMemberOfIgnoredClass() = assertIgnored("a.ts", """
    // prettier-ignore
    export class A {
        /** doc */   b  =  1
        <selection>/** doc */   c  =  2</selection>
    }
  """)

  fun testWhitespaceInIgnoredClass() = assertIgnored("a.ts", """
    // prettier-ignore
    export class A {
        b  =  1
    <selection>
    </selection>
    }
  """)

  fun testClassAfterIgnoreComment() = assertIgnored("a.ts", """
    // prettier-ignore
    <selection>export class A {
        b  =  1
    }
    </selection>
  """)

  fun testNestedInIgnoredStatement() = assertIgnored("a.js", """
    // prettier-ignore
    const x = {
      a: [1, 2,
          <selection>3</selection>],
    };
  """)

  fun testBlankLineAfterComment() = assertIgnored("a.js", """
    // prettier-ignore

    const x = { <selection>a:1</selection> };
  """)

  fun testOtherCommentBetween() = assertIgnored("a.js", """
    // prettier-ignore
    // eslint-disable-next-line
    const x = { <selection>a:1</selection> };
  """)

  fun testBlockCommentOnSameLine() = assertIgnored("a.js", """
    /* prettier-ignore */ const x = { <selection>a:1</selection> };
  """)

  fun testCommentAfterBrace() = assertIgnored("a.js", """
    function f() { // prettier-ignore
      foo( <selection>1</selection> );
    }
  """)

  fun testDecoratedExportedClass() = assertIgnored("a.ts", """
    // prettier-ignore
    @dec
    export class A { <selection>b  =  1</selection> }
  """)

  fun testExportDefaultClass() = assertIgnored("a.ts", """
    // prettier-ignore
    export default class A { <selection>b  =  1</selection> }
  """)

  fun testJsonProperty() = assertIgnored("a.json", """
    {
      "a": 1,
      // prettier-ignore
      "b": [1, <selection>2</selection>]
    }
  """)

  fun testIgnoreCommentInsideRange() = assertNotIgnored("a.ts", """
    import { x } from "x";

    <selection>// prettier-ignore
    export class A {
        b  =  1
    }</selection>
  """)

  fun testTrailingCommentOfPreviousStatement() = assertNotIgnored("a.js", """
    foo(); // prettier-ignore
    bar( <selection>1</selection> );
  """)

  fun testTrailingCommentOfPreviousMember() = assertNotIgnored("a.ts", """
    class A {
      b = 1 // prettier-ignore
      c = <selection>2</selection>
    }
  """)

  fun testTrailingCommentOfPreviousJsonProperty() = assertNotIgnored("a.json", """
    {
      "a": 1, // prettier-ignore
      "b": [1, <selection>2</selection>]
    }
  """)

  fun testDocComment() = assertNotIgnored("a.js", """
    /** prettier-ignore */
    const x = { <selection>a:1</selection> };
  """)

  fun testOtherComment() = assertNotIgnored("a.js", """
    // prettier-ignore-start
    const x = { <selection>a:1</selection> };
  """)

  fun testCommentBeforePreviousStatement() = assertNotIgnored("a.js", """
    // prettier-ignore
    foo();
    bar( <selection>1</selection> );
  """)

  fun testNoComment() = assertNotIgnored("a.js", """
    const x = { <selection>a:1</selection> };
  """)

  private fun assertIgnored(fileName: String, text: String) = doTest(fileName, text, true)

  private fun assertNotIgnored(fileName: String, text: String) = doTest(fileName, text, false)

  private fun doTest(fileName: String, text: String, expected: Boolean) {
    myFixture.configureByText(fileName, text.trimIndent())
    val selection = myFixture.editor.selectionModel
    val range = TextRange(selection.selectionStart, selection.selectionEnd)
    assertEquals(expected, isInsidePrettierIgnoredElement(myFixture.file, range))
  }
}
