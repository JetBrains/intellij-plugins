package com.intellij.openRewrite.run

import com.intellij.icons.AllIcons
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class OpenRewriteRecipeRunLineMarkerProviderTest : OpenRewriteLightHighlightingTestCase() {
  @Test
  fun testYamlRecipe() = onEdt {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      na<caret>me: com.my.Recipe
    """.trimIndent())
    val marks = myFixture.findGuttersAtCaret()
    val mark = marks.find { it.icon == AllIcons.RunConfigurations.TestState.Run }
    assertNotNull(mark)
  }

  @Test
  fun testYamlRecipeImDumbMode() = onEdt {
    myFixture.configureByText(RECIPE_FILE_NAME, """
        type: specs.openrewrite.org/v1beta/recipe
        na<caret>me: com.my.Recipe
      """.trimIndent())
    DumbModeTestUtils.runInDumbModeSynchronously(myFixture.getProject()) {
      CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, testRootDisposable)
      val marks = myFixture.findGuttersAtCaret()
      val mark = marks.find { it.icon == AllIcons.RunConfigurations.TestState.Run }
      assertNotNull(mark)
    }
  }


}