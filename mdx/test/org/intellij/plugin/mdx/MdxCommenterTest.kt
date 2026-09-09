// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.plugin.mdx

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.application.EDT
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.intellij.testFramework.junit5.fixture.testNameFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.function.Executable
import java.nio.file.Path
import kotlin.io.path.readText

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/commenter")
@Timeout(30)
class MdxCommenterTest {
  private val tempDir = tempPathFixture()
  private val project = projectFixture(tempDir, openAfterCreation = true)

  @Suppress("unused")
  private val module = project.moduleFixture(tempDir, addPathToSourceRoot = true)
  private val fixture by codeInsightFixture(project, tempDir)
  private val testName by testNameFixture(lowerCaseFirstLetter = false)

  @Test
  fun testBlockCommentContexts() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkRoundTripCases("CommentByBlockComment", testName, 21)
    }
  }

  @Test
  fun testLineCommentContexts() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkRoundTripCases("CommentByLineComment", testName, 11)
    }
  }

  @Test
  fun testLineActionUsesTheWholeTouchedLine() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkRoundTrip("CommentByLineComment", testName)
    }
  }

  @Test
  fun testBlockCommentAtCaret() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkBlockCommentCases(testName)
    }
  }

  @Test
  fun testCommentSpacing() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val settings = CodeStyle.getSettings(fixture.project)
      val languages = listOf(org.intellij.plugin.mdx.lang.MdxLanguage, org.intellij.plugin.mdx.js.MdxJSLanguage.INSTANCE)
      val commonSettings = languages.map { settings.getCommonSettings(it) }
      val previous = commonSettings.map { it.LINE_COMMENT_ADD_SPACE to it.BLOCK_COMMENT_ADD_SPACE }
      try {
        for (common in commonSettings) {
          common.LINE_COMMENT_ADD_SPACE = false
          common.BLOCK_COMMENT_ADD_SPACE = true
        }
        checkRoundTrip("CommentByLineComment", "CommentSpacingMdx")
        checkRoundTrip("CommentByBlockComment", "CommentSpacingMdx")
        checkRoundTrip("CommentByBlockComment", "CommentSpacingJavaScriptBlock")
        checkRoundTrip("CommentByLineComment", "CommentSpacingJavaScriptLine")
        commonSettings.last().BLOCK_COMMENT_ADD_SPACE = false
        checkComment("CommentByBlockComment", "CommentSpacingUncomment")
      }
      finally {
        commonSettings.zip(previous).forEach { (common, old) ->
          common.LINE_COMMENT_ADD_SPACE = old.first
          common.BLOCK_COMMENT_ADD_SPACE = old.second
        }
      }
    }
  }

  @Test
  fun testBlockCaretAndUndo() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      fixture.configureByText("comments.mdx", testDataText(testName))
      EditorTestUtil.executeAction(fixture.editor, "CommentByBlockComment")
      fixture.checkResult(testDataText("${testName}_after"))
      EditorTestUtil.executeAction(fixture.editor, $$"$Undo")
      fixture.checkResult(testDataText(testName))
    }
  }

  @Test
  fun testMultipleCarets() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      fixture.configureByText("comments.mdx", testDataText(testName))
      EditorTestUtil.executeAction(fixture.editor, "CommentByLineComment")
      assertEquals(testDataText("${testName}_after"), fixture.editor.document.text)
      EditorTestUtil.executeAction(fixture.editor, $$"$Undo")
      fixture.checkResult(testDataText(testName))
    }
  }

  @Test
  fun testInjectedJavaScriptFence() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkInjectedComment(testName)
    }
  }

  @Test
  fun testInjectedFrontMatter() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkInjectedComment(testName)
    }
  }

  @Test
  fun testTagNameKeepsExistingSyntax() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkComment("CommentByBlockComment", testName)
    }
  }

  @Test
  fun testAttributeKeepsExistingSyntax() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkComment("CommentByBlockComment", testName)
    }
  }

  @Test
  fun testInlineCodeKeepsLiteralDelimiters() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkComment("CommentByBlockComment", testName)
    }
  }

  @Test
  fun testUnknownFenceKeepsMdxSyntax() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkComment("CommentByLineComment", testName)
    }
  }

  @Test
  fun testJavaScriptFenceKeepsHostSyntax() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkComment("CommentByLineComment", testName)
    }
  }

  @Test
  fun testFrontMatterKeepsHostSyntax() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      checkComment("CommentByLineComment", testName)
    }
  }

  private fun checkBlockCommentCases(prefix: String) {
    assertAll((1..5).map { index ->
      Executable { checkComment("CommentByBlockComment", caseName(prefix, index)) }
    })
  }

  private fun checkRoundTripCases(action: String, prefix: String, count: Int) {
    assertAll((1..count).map { index ->
      Executable { checkRoundTrip(action, caseName(prefix, index)) }
    })
  }

  private fun checkComment(action: String, caseName: String) {
    fixture.configureByText("comments.mdx", testDataText(caseName))
    EditorTestUtil.executeAction(fixture.editor, action)
    assertEquals(testDataText("${caseName}_after"), fixture.editor.document.text, caseName)
  }

  private fun checkRoundTrip(action: String, caseName: String) {
    val before = testDataText(caseName)
    checkComment(action, caseName)
    EditorTestUtil.executeAction(fixture.editor, action)
    assertEquals(before.replace("<selection>", "").replace("</selection>", ""), fixture.editor.document.text, before)
  }

  private fun checkInjectedComment(caseName: String) {
    fixture.configureByText("comments.mdx", testDataText(caseName))
    val editor = InjectedLanguageUtil.getEditorForInjectedLanguageNoCommit(fixture.editor, fixture.file)
    val hostEditor = InjectedLanguageEditorUtil.getTopLevelEditor(fixture.editor)
    assertNotSame(hostEditor, editor)
    EditorTestUtil.executeAction(requireNotNull(editor), "CommentByLineComment")
    assertEquals(testDataText("${caseName}_after"), hostEditor.document.text)
  }

  private fun testDataText(caseName: String): String =
    Path.of(fixture.testDataPath, "$caseName.mdx").readText().removeSuffix("\n")

  private fun caseName(prefix: String, index: Int): String = "${prefix}_${index.toString().padStart(2, '0')}"
}
