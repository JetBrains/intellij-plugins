// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.application.options.CodeStyle
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugins.postcss.settings.PostCssCodeStyleSettings
import org.jetbrains.vuejs.lang.VueCommenterTest.CommentStyle.BLOCK
import org.jetbrains.vuejs.lang.VueCommenterTest.CommentStyle.LINE
import org.junit.jupiter.api.Test

@TestApplication
class VueCommenterTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/commenter/")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project

  @Test
  fun testCss() = doTest(LINE)
  @Test
  fun testSass() = doTest(LINE)
  @Test
  fun testScss() = doTest(LINE)
  @Test
  fun testLess() = doTest(LINE)
  @Test
  fun testStylus() = doTest(LINE)

  @Test
  fun testBindingLineComment() = doTest(LINE)
  @Test
  fun testBindingBlockComment() = doTest(BLOCK)

  @Test
  fun testStyleAttrLineComment() = doTest(LINE)
  @Test
  fun testStyleAttrBlockComment() = doTest(BLOCK)

  @Test
  fun testWholeStyleLineComment() = doTest(LINE)

  @Test
  fun testCommentHtmlByLineComment() = JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
    val htmlSettings = it.getCommonSettings(HTMLLanguage.INSTANCE)
    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
    doTest(0, "CommentByLineComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = true
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
    doTest(0, "CommentByLineComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = true
    doTest(1, "CommentByLineComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = true
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = true
    doTest(1, "CommentByLineComment")
  }

  @Test
  fun testCommentCssByLineComment() = JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
    val settings = CodeStyle.getSettings(myFixture.project).getCustomSettings(PostCssCodeStyleSettings::class.java)
    val initialValue = settings.COMMENTS_INLINE_STYLE
    try {
      settings.COMMENTS_INLINE_STYLE = true
      doTest(0, "CommentByLineComment")
      settings.COMMENTS_INLINE_STYLE = false
      doTest(1, "CommentByLineComment")
    }
    finally {
      settings.COMMENTS_INLINE_STYLE = initialValue
    }
  }

  @Test
  fun testCommentByBlockComment() = JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
    val htmlSettings = it.getCommonSettings(HTMLLanguage.INSTANCE)
    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
    doTest(0, "CommentByBlockComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = true
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
    doTest(0, "CommentByBlockComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = true
    doTest(0, "CommentByBlockComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = true
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = true
    doTest(0, "CommentByBlockComment")
  }

  @Test
  fun testCommentByBlockComment2() = JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
    val htmlSettings = it.getCommonSettings(HTMLLanguage.INSTANCE)
    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
    doTest(0, "CommentByBlockComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = true
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
    doTest(1, "CommentByBlockComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = true
    doTest(0, "CommentByBlockComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = true
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = true
    doTest(1, "CommentByBlockComment")
  }

  @Test
  fun testCommentByBlockComment3() = JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
    val htmlSettings = it.getCommonSettings(HTMLLanguage.INSTANCE)
    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
    doTest(0, "CommentByBlockComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = true
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
    doTest(1, "CommentByBlockComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = true
    doTest(0, "CommentByBlockComment")

    htmlSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = true
    htmlSettings.LINE_COMMENT_AT_FIRST_COLUMN = true
    doTest(1, "CommentByBlockComment")
  }

  private fun doTest(commentStyle: CommentStyle) {
    val name = hybrid.getTestName(true)
    myFixture.configureByFile("$name.vue")
    myFixture.performEditorAction(if (commentStyle == LINE) "CommentByLineComment" else "CommentByBlockComment")
    myFixture.checkResultByFile("${name}_after.vue")
  }

  private fun doTest(id: Int, action: String) {
    val testName = hybrid.getTestName(true)
    myFixture.configureByFile("$testName.vue")
    try {
      myFixture.performEditorAction(action)
      myFixture.checkResultByFile("${testName}_after_$id.vue")
    }
    finally {
      myFixture.configureByFile("$testName.vue")
    }
  }

  private enum class CommentStyle {
    LINE,
    BLOCK
  }

}
