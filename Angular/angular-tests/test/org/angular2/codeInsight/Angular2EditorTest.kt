// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.CodeInsightTestUtil
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestNoService
import org.angular2.TestTsGoProxy
import org.junit.Test

@TestNoService
@TestTsGoProxy
class Angular2EditorTest : Angular2TestCase("editor") {

  @Test
  fun testBlockTyping() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "<div></div><!--within tag-->\n <!--space-->\ntext", extension = "html", checkResult = true) {
      moveToOffsetBySignature("<div><caret></div>")
      type("@if (")
      moveToOffsetBySignature("()<caret>")
      type("{\n<div>")
      moveToOffsetBySignature("<caret> <!--space-->")
      type("@if (")
      moveToOffsetBySignature("<caret>text")
      type("@if (")
    }

  @Test
  fun testBlockFolding() {
    doFoldingTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                  extension = "html")
  }

  @Test
  fun testClosingBraceWithInterpolation() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "@if (foo) { {{foo}} } @else { <caret>", extension = "html", checkResult = true) {
      type("}")
    }

  @Test
  fun testDeletingParenInParams() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "@if (((<caret>))) { }", extension = "html", checkResult = true) {
      type("\b\b\b")
    }

  @Test
  fun testIfBlockExtendSelection() =
    doWordSelectionTest()

  @Test
  fun testIfElseBlockExtendSelection() =
    doWordSelectionTest()

  @Test
  fun testSwitchBlockExtendSelection() =
    doWordSelectionTest()

  @Test
  fun testTopLevelBlockEmmetExpansion() =
    doEditorTypingTest(checkResult = true, extension = "html") {
      type("\t")
    }

  @Test
  fun testIncompleteStringInInterpolation() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0,
                     fileContents = "<main>{{ <caret> }}</main>", extension = "html", checkResult = true) {
      type("'sdf'")
    }

  @Test
  fun testInlineTemplateHighlightingOnType() =
    doEditorTypingTest(Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0, checkResult = false) {
      myFixture.enableInspections(Angular2TemplateInspectionsProvider())

      checkHighlighting(getGoldFileName(null, "1.ts"))
      moveToOffsetBySignature("| pe<caret>rcent")

      repeat(5) {
        type("a")
        checkHighlighting(getGoldFileName(null, "2.ts"))
        type("\b")
        checkHighlighting(getGoldFileName(null, "1.ts"))
      }
    }

  @Test
  fun testInElementCommentSlashMidHeader() =
    doInElementCommentTest("<div <caret> ></div>", "<div /<caret> ></div>") {
      type("/")
    }

  @Test
  fun testInElementCommentTypedBetweenAttributes() =
    doInElementCommentTest("""<div <caret> class="x">t</div>""", """<div /* c */<caret> class="x">t</div>""") {
      type("/* c */")
    }

  @Test
  fun testInElementCommentTypedBeforeTagEnd() =
    doInElementCommentTest("<div <caret>>x</div>", "<div // c<caret>>x</div>") {
      type("// c")
    }

  @Test
  fun testInElementCommentSlashConvertsEmptyTag() =
    doInElementCommentTest("<div <caret>></div>", "<div /<caret>>") {
      type("/")
    }

  @Test
  fun testInElementCommentSlashClosesUnclosedTag() =
    doInElementCommentTest("<div <caret>", "<div /><caret>") {
      type("/")
    }

  @Test
  fun testInElementCommentTypedInInlineTemplate() =
    doInElementCommentTest(
      """
        import {Component} from "@angular/core";
        @Component({template: `<div <caret> class="x"></div>`})
        export class MyComp {}
      """.trimIndent(),
      """
        import {Component} from "@angular/core";
        @Component({template: `<div /* c */<caret> class="x"></div>`})
        export class MyComp {}
      """.trimIndent(),
      extension = "ts",
    ) {
      type("/* c */")
    }

  @Test
  fun testInElementCommentLine() =
    doInElementCommentTest(
      """
        <div
            attr1="a"
            <caret>attr2="b"
        >
            Content
        </div>
      """.trimIndent(),
      """
        <div
            attr1="a"
            // attr2="b"
        >
            Content
        </div>
      """.trimIndent(),
    ) {
      performEditorAction("CommentByLineComment")
    }

  @Test
  fun testInElementUncommentLine() =
    doInElementCommentTest(
      """
        <div
            attr1="a"
            // <caret>attr2="b"
        >
        </div>
      """.trimIndent(),
      """
        <div
            attr1="a"
            attr2="b"
        >
        </div>
      """.trimIndent(),
    ) {
      performEditorAction("CommentByLineComment")
    }

  @Test
  fun testInElementCommentSeveralLines() =
    doInElementCommentTest(
      """
        <div
            <selection>attr1="a"
            attr2="b"</selection><caret>
        >
        </div>
      """.trimIndent(),
      """
        <div
            // attr1="a"
            // attr2="b"
        >
        </div>
      """.trimIndent(),
    ) {
      performEditorAction("CommentByLineComment")
    }

  @Test
  fun testInElementCommentBlock() =
    doInElementCommentTest(
      """
        <div
            <selection>attr1="a"</selection><caret>
            attr2="b"
        >
        </div>
      """.trimIndent(),
      """
        <div
            /*attr1="a"*/
            attr2="b"
        >
        </div>
      """.trimIndent(),
    ) {
      performEditorAction("CommentByBlockComment")
    }

  @Test
  fun testInElementCommentContentLineIsHtmlComment() =
    doInElementCommentTest(
      """
        <div
            attr1="a"
        >
        Content<caret>
        </div>
      """.trimIndent(),
      """
        <div
            attr1="a"
        >
        <!--Content-->
        </div>
      """.trimIndent(),
    ) {
      performEditorAction("CommentByLineComment")
    }

  @Test
  fun testInElementCommentLineInInlineTemplate() =
    doInElementCommentTest(
      """
        import {Component} from "@angular/core";
        @Component({
          template: `
            <div
                attr1="a"
                <caret>attr2="b"
            ></div>
          `
        })
        export class MyComp {}
      """.trimIndent(),
      """
        import {Component} from "@angular/core";
        @Component({
          template: `
            <div
                attr1="a"
                // attr2="b"
            ></div>
          `
        })
        export class MyComp {}
      """.trimIndent(),
      extension = "ts",
    ) {
      performEditorAction("CommentByLineComment")
    }

  private fun doInElementCommentTest(
    before: String,
    after: String,
    extension: String = "html",
    action: CodeInsightTestFixture.() -> Unit,
  ) =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_22_0_8, fileContents = before, extension = extension) {
      action()
      checkResult(after)
    }

  private fun doWordSelectionTest() =
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_17_3_0, configureFile = false) {
      CodeInsightTestUtil.doWordSelectionTestOnDirectory(myFixture, getTestName(true), "html")
    }

}