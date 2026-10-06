package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.openRewrite.OPTION_CLASS_NAME
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_FILE_NAME
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OpenRewriteRecipeCompletionTest : OpenRewriteLightHighlightingTestCase() {
  @Test
  fun testRecipeCompletion() = onEdt {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      recipeList:
        - com.<caret>
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.third
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.style
    """.trimIndent())
    myFixture.completeBasic()
    val lookupElementStrings = myFixture.lookupElementStrings
    assertTrue(lookupElementStrings!!.containsAll(listOf("com.second", "com.third")), lookupElementStrings.toString())
    assertTrue(lookupElementStrings.intersect(setOf("com.first", "com.style")).isEmpty(), lookupElementStrings.toString())
    myFixture.finishLookup(Lookup.REPLACE_SELECT_CHAR)
    myFixture.checkResult("""
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      recipeList:
        - com.second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.third
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.style
    """.trimIndent())
  }

  @Test
  fun testRecipeWithRequredOptionCompletion() = onEdt {
    myFixture.addClass("""
      package com;
      
      public class MyRecipe extends $RECIPE_CLASS_NAME {
        @$OPTION_CLASS_NAME
        public String option;  
      }
    """.trimIndent())
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my
      recipeList:
        - com.<caret>
    """.trimIndent())
    myFixture.completeBasic()
    myFixture.checkResult("""
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my
      recipeList:
        - com.MyRecipe:
            option: <caret>
    """.trimIndent())
  }

  @Test
  fun testPreconditionCompletion() = onEdt {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      preconditions:
        - com.<caret>
      recipeList:
        - com.third
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.third
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.style
    """.trimIndent())
    myFixture.completeBasic()
    val lookupElementStrings = myFixture.lookupElementStrings
    assertTrue(lookupElementStrings!!.containsAll(listOf("com.second", "com.third")), lookupElementStrings.toString())
    assertTrue(lookupElementStrings.intersect(setOf("com.first", "com.style")).isEmpty(), lookupElementStrings.toString())
    myFixture.finishLookup(Lookup.REPLACE_SELECT_CHAR)
    myFixture.checkResult("""
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      preconditions:
        - com.second
      recipeList:
        - com.third
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.third
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.style
    """.trimIndent())
  }

  @Test
  fun testStyleCompletion() = onEdt {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/style
      name: com.first
      styleConfigs:
        - com.<caret>
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.second
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.third
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.recipe
    """.trimIndent())
    myFixture.completeBasic()
    val lookupElementStrings = myFixture.lookupElementStrings
    assertTrue(lookupElementStrings!!.containsAll(listOf("com.second", "com.third")), lookupElementStrings.toString())
    assertTrue(lookupElementStrings.intersect(setOf("com.first", "com.recipe")).isEmpty(), lookupElementStrings.toString())
    myFixture.finishLookup(Lookup.REPLACE_SELECT_CHAR)
    myFixture.checkResult("""
      type: specs.openrewrite.org/v1beta/style
      name: com.first
      styleConfigs:
        - com.second
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.second
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.third
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.recipe
    """.trimIndent())
  }

  @Test
  fun testOptionCompletion() = onEdt {
    myFixture.addClass("""
      package com;
      
      public class MyRecipe extends $RECIPE_CLASS_NAME {
        @$OPTION_CLASS_NAME
        public String option;  
      }
    """.trimIndent())
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my
      recipeList:
        - com.MyRecipe:
            <caret>
    """.trimIndent())
    myFixture.completeBasic()
    myFixture.finishLookup(Lookup.REPLACE_SELECT_CHAR)
    myFixture.checkResult("""
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my
      recipeList:
        - com.MyRecipe:
            option: <caret>
    """.trimIndent())
  }

  @Test
  fun testOptionValueCompletion() = onEdt {
    myFixture.addClass("""
      package com;
      
      public class MyRecipe extends $RECIPE_CLASS_NAME {
        @$OPTION_CLASS_NAME(valid = {"one", "two"})
        public String option;  
      }
    """.trimIndent())
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my
      recipeList:
        - com.MyRecipe:
            option: <caret>
    """.trimIndent())
    myFixture.completeBasic()
    myFixture.finishLookup(Lookup.REPLACE_SELECT_CHAR)
    myFixture.checkResult("""
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my
      recipeList:
        - com.MyRecipe:
            option: one
    """.trimIndent())
  }

  @Test
  fun testOptionBooleanValueCompletion() = onEdt {
    myFixture.addClass("""
      package com;
      
      public class MyRecipe extends $RECIPE_CLASS_NAME {
        @$OPTION_CLASS_NAME
        public boolean option;  
      }
    """.trimIndent())
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my
      recipeList:
        - com.MyRecipe:
            option: <caret>
    """.trimIndent())
    myFixture.completeBasic()
    myFixture.finishLookup(Lookup.REPLACE_SELECT_CHAR)
    myFixture.checkResult("""
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my
      recipeList:
        - com.MyRecipe:
            option: false
    """.trimIndent())
  }
}