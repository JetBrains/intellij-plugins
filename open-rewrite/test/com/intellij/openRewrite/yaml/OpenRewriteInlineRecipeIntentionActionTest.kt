package com.intellij.openRewrite.yaml

import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl

class OpenRewriteInlineRecipeIntentionActionTest : OpenRewriteLightHighlightingTestCase() {
  fun testIntentionNotAvailableOnJavaRecipe() {
    myFixture.addClass("""
      package com;
      
      public class MyRecipe extends $RECIPE_CLASS_NAME {
      }
    """.trimIndent())
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
      recipeList:
        - com.<caret>MyRecipe
    """.trimIndent())
    val intention = myFixture.filterAvailableIntentions("Inline recipe").firstOrNull()
    assertNull(intention)
  }

  fun testIntention() {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.first
      recipeList:
        - com.<caret>my.second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.second
      recipeList:
        - com.my.third
        - com.my.fourth
    """.trimIndent())
    val intention = myFixture.filterAvailableIntentions("Inline recipe").firstOrNull()
    assertNotNull(intention)
    myFixture.launchAction(intention!!)
    NonBlockingReadActionImpl.waitForAsyncTaskCompletion()
    myFixture.checkResult("""
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.first
      recipeList:
        - com.my.third
        - com.my.fourth
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.second
      recipeList:
        - com.my.third
        - com.my.fourth
    """.trimIndent())
  }
}