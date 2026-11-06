// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ifs

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import training.featuresSuggester.FeatureSuggesterTestUtils.deleteSymbolAtCaret
import training.featuresSuggester.FeatureSuggesterTestUtils.insertNewLineAt
import training.featuresSuggester.FeatureSuggesterTestUtils.moveCaretToLogicalPosition
import training.featuresSuggester.FeatureSuggesterTestUtils.selectBetweenLogicalPositions
import training.featuresSuggester.FeatureSuggesterTestUtils.testInvokeLater
import training.featuresSuggester.FeatureSuggesterTestUtils.typeAndCommit
import training.featuresSuggester.NoSuggestion
import training.featuresSuggester.featureSuggesterFixture

@TestApplication
class UnwrapSuggesterJSTest {
  private val suggester by featureSuggesterFixture(JsSuggestersTestUtils.testDataPath, "JavaScriptCodeExample.js", "Unwrap")

  @Test
  fun `testUnwrap IF statement and get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      moveCaretToLogicalPosition(22, 9)
      deleteSymbolAtCaret()
      selectBetweenLogicalPositions(lineStartIndex = 20, columnStartIndex = 49, lineEndIndex = 20, columnEndIndex = 3)
      deleteSymbolAtCaret()
    }

    testInvokeLater(project) {
      assertSuggestedCorrectly()
    }
  }

  @Test
  fun `testUnwrap one-line IF and get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      selectBetweenLogicalPositions(
        lineStartIndex = 56,
        columnStartIndex = 18,
        lineEndIndex = 56,
        columnEndIndex = 8
      )
      deleteSymbolAtCaret()
      moveCaretToLogicalPosition(56, 14)
      deleteSymbolAtCaret()
    }

    testInvokeLater(project) {
      assertSuggestedCorrectly()
    }
  }

  @Test
  fun `testUnwrap IF with deleting multiline selection and get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      selectBetweenLogicalPositions(lineStartIndex = 19, columnStartIndex = 22, lineEndIndex = 21, columnEndIndex = 5)
      deleteSymbolAtCaret()
      moveCaretToLogicalPosition(20, 9)
      deleteSymbolAtCaret()
    }

    testInvokeLater(project) {
      assertSuggestedCorrectly()
    }
  }

  @Test
  fun `testUnwrap FOR and get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      selectBetweenLogicalPositions(lineStartIndex = 47, columnStartIndex = 36, lineEndIndex = 47, columnEndIndex = 8)
      deleteSymbolAtCaret()
      moveCaretToLogicalPosition(50, 9)
      deleteSymbolAtCaret()
    }

    testInvokeLater(project) {
      assertSuggestedCorrectly()
    }
  }

  @Test
  fun `testUnwrap WHILE and get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      selectBetweenLogicalPositions(lineStartIndex = 52, columnStartIndex = 22, lineEndIndex = 52, columnEndIndex = 0)
      deleteSymbolAtCaret()
      moveCaretToLogicalPosition(55, 9)
      deleteSymbolAtCaret()
    }

    testInvokeLater(project) {
      assertSuggestedCorrectly()
    }
  }

  @Test
  fun `testUnwrap commented IF and don't get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      insertNewLineAt(46, 8)
      typeAndCommit(
        """//if(true) {
              |//i++; j--;
              |//}""".trimMargin()
      )

      selectBetweenLogicalPositions(
        lineStartIndex = 46,
        columnStartIndex = 10,
        lineEndIndex = 46,
        columnEndIndex = 20
      )
      deleteSymbolAtCaret()
      moveCaretToLogicalPosition(48, 11)
      deleteSymbolAtCaret()
    }

    testInvokeLater(project) {
      assertTrue(expectedSuggestion is NoSuggestion)
    }
  }

  @Test
  fun `testUnwrap IF written in string block and don't get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      insertNewLineAt(46, 8)
      typeAndCommit(
        """let string = "if(true) {
              |i++; j--;
              |}"""".trimMargin()
      )

      selectBetweenLogicalPositions(
        lineStartIndex = 46,
        columnStartIndex = 22,
        lineEndIndex = 46,
        columnEndIndex = 32
      )
      deleteSymbolAtCaret()
      moveCaretToLogicalPosition(48, 14)
      deleteSymbolAtCaret()
    }

    testInvokeLater(project) {
      assertTrue(expectedSuggestion is NoSuggestion)
    }
  }
}
