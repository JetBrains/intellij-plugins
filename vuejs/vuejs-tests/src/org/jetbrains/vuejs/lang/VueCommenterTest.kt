// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.application.options.CodeStyle
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.plugins.postcss.settings.PostCssCodeStyleSettings

class VueCommenterTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/commenter/"

  fun testCss() = doTest()
  fun testSass() = doTest()
  fun testScss() = doTest()
  fun testLess() = doTest()
  fun testStylus() = doTest()

  fun testBindingLineComment() = doTest()
  fun testBindingBlockComment() = doTest(false)

  fun testWholeStyleLineComment() = doTest()

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

  fun doTest(lineCommenter: Boolean = true) {
    val name = getTestName(true)
    myFixture.configureByFile("$name.vue")
    myFixture.performEditorAction(if (lineCommenter) "CommentByLineComment" else "CommentByBlockComment")
    myFixture.checkResultByFile("${name}_after.vue")
  }

  private fun doTest(id: Int, action: String) {
    val testName = getTestName(true)
    myFixture.configureByFile("$testName.vue")
    try {
      myFixture.performEditorAction(action)
      myFixture.checkResultByFile("${testName}_after_$id.vue")
    }
    finally {
      myFixture.configureByFile("$testName.vue")
    }
  }

}
