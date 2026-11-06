// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ifs

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import training.featuresSuggester.FeatureSuggesterTestUtils.focusEditor
import training.featuresSuggester.FeatureSuggesterTestUtils.logicalPositionToOffset
import training.featuresSuggester.FeatureSuggesterTestUtils.performFindInFileAction
import training.featuresSuggester.FeatureSuggesterTestUtils.testInvokeLater
import training.featuresSuggester.NoSuggestion
import training.featuresSuggester.featureSuggesterFixture

@TestApplication
class FileStructureSuggesterJSTest {
  private val suggester by featureSuggesterFixture(JsSuggestersTestUtils.testDataPath, "JavaScriptCodeExample.js", "File structure")

  @Test
  fun `testFind field and get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      val fromOffset = logicalPositionToOffset(8, 0)
      performFindInFileAction("field", fromOffset)
      focusEditor()
    }

    testInvokeLater(project) {
      assertSuggestedCorrectly()
    }
  }

  @Test
  fun `testFind method and get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      val fromOffset = logicalPositionToOffset(4, 0)
      performFindInFileAction("mai", fromOffset)
      focusEditor()
    }

    testInvokeLater(project) {
      assertSuggestedCorrectly()
    }
  }

  @Test
  fun `testFind function parameter and don't get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      val fromOffset = logicalPositionToOffset(14, 0)
      performFindInFileAction("args", fromOffset)
      focusEditor()
    }

    testInvokeLater(project) {
      assertTrue(expectedSuggestion is NoSuggestion)
    }
  }

  @Test
  fun `testFind local variable declaration and don't get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      val fromOffset = logicalPositionToOffset(14, 0)
      performFindInFileAction("abc", fromOffset)
      focusEditor()
    }

    testInvokeLater(project) {
      assertTrue(expectedSuggestion is NoSuggestion)
    }
  }

  @Test
  fun `testFind variable usage and don't get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      val fromOffset = logicalPositionToOffset(18, 0)
      performFindInFileAction("abc", fromOffset)
      focusEditor()
    }

    testInvokeLater(project) {
      assertTrue(expectedSuggestion is NoSuggestion)
    }
  }

  @Test
  fun `testFind method usage and don't get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      val fromOffset = logicalPositionToOffset(18, 0)
      performFindInFileAction("main", fromOffset)
      focusEditor()
    }

    testInvokeLater(project) {
      assertTrue(expectedSuggestion is NoSuggestion)
    }
  }

  @Test
  fun `testFind type usage and don't get suggestion`(): Unit = suggester.runTest {
    with(myFixture) {
      val fromOffset = logicalPositionToOffset(16, 25)
      performFindInFileAction("anotherCl", fromOffset)
      focusEditor()
    }

    testInvokeLater(project) {
      assertTrue(expectedSuggestion is NoSuggestion)
    }
  }
}
