package com.intellij.dts

import com.intellij.dts.core.lang.psi.DtsTypes
import com.intellij.openapi.application.EDT
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class DtsIncrementalHighlightingTest {
  private val tempPathFixture = tempPathFixture()
  private val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)

  private val dtsFixture by dtsFixture(projectFixture)

  private val myFixture
    get() = dtsFixture.codeInsight


  @Test
  fun testV1() = doTest("/dts-v1<caret>", "/", listOf(DtsTypes.V1))

  @Suppress("SameParameterValue")
  private fun doTest(text: String, character: String, tokenTypes: List<IElementType>): Unit = timeoutRunBlocking {
    require(text.contains("<caret>")) {
      "Test text must contain \"<caret>\" to indicate caret position"
    }

    myFixture.configureByText("test.dtsi", text)
    myFixture.type(character)

    val attributes = withContext(Dispatchers.EDT) {
      val iterator = myFixture.editor.highlighter.createIterator(0)
      sequence {
        while (!iterator.atEnd()) {
          yield(iterator.tokenType)
          iterator.advance()
        }
      }.toList()
    }

    assertEquals(tokenTypes, attributes)
  }
}