package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.openRewrite.OPTION_CLASS_NAME
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_FILE_NAME

class OpenRewriteRecipeCompletionTest : OpenRewriteLightHighlightingTestCase() {
  fun testRecipeCompletion() {
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
    assertContainsElements(lookupElementStrings!!, "com.second", "com.third")
    assertDoesntContain(lookupElementStrings, "com.first", "com.style")
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

  fun testRecipeWithRequredOptionCompletion() {
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

  fun testPreconditionCompletion() {
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
    assertContainsElements(lookupElementStrings!!, "com.second", "com.third")
    assertDoesntContain(lookupElementStrings, "com.first", "com.style")
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

  fun testStyleCompletion() {
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
    assertContainsElements(lookupElementStrings!!, "com.second", "com.third")
    assertDoesntContain(lookupElementStrings, "com.first", "com.recipe")
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

  fun testOptionCompletion() {
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

  fun testOptionValueCompletion() {
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

  fun testOptionBooleanValueCompletion() {
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