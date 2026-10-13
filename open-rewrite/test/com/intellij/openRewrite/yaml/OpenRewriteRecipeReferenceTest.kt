package com.intellij.openRewrite.yaml

import com.intellij.openRewrite.OPTION_CLASS_NAME
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.openRewrite.recipe.OpenRewriteOptionPsiElement
import com.intellij.openRewrite.recipe.OpenRewriteRecipePsiElement
import com.intellij.openapi.application.readAction
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.intellij.testFramework.common.timeoutRunBlocking
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class OpenRewriteRecipeReferenceTest : OpenRewriteLightHighlightingTestCase() {
  @Test
  fun testScalarRecipeReference(): Unit = timeoutRunBlocking {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      recipeList:
        - com.<caret>second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
    """.trimIndent())
    readAction {
      val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
      val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java)
      val reference = scalar!!.references.find { it is OpenRewriteYamlRecipeReferenceProvider.RecipeReference }
      assertNotNull(reference)
      assertInstanceOf(OpenRewriteRecipePsiElement::class.java, reference!!.resolve())
    }
  }

  @Test
  fun testScalarPreconditionReference(): Unit = timeoutRunBlocking {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      preconditions:
        - com.<caret>second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
    """.trimIndent())
    readAction {
      val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
      val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java)
      val reference = scalar!!.references.find { it is OpenRewriteYamlRecipeReferenceProvider.RecipeReference }
      assertNotNull(reference)
      assertInstanceOf(OpenRewriteRecipePsiElement::class.java, reference!!.resolve())
    }
  }

  @Test
  fun testScalarStyleReference(): Unit = timeoutRunBlocking {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/style
      name: com.first
      styleConfigs:
        - com.<caret>second
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.second
    """.trimIndent())
    readAction {
      val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
      val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java)
      val reference = scalar!!.references.find { it is OpenRewriteYamlRecipeReferenceProvider.RecipeReference }
      assertNotNull(reference)
      assertInstanceOf(OpenRewriteRecipePsiElement::class.java, reference!!.resolve())
    }
  }

  @Test
  fun testKeyValueRecipeReference(): Unit = timeoutRunBlocking {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      recipeList:
        - com.<caret>second:
            a: b
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
    """.trimIndent())
    readAction {
      val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
      val keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java)
      val reference = keyValue!!.references.find { it is OpenRewriteYamlRecipeReferenceProvider.RecipeReference }
      assertNotNull(reference)
      assertInstanceOf(OpenRewriteRecipePsiElement::class.java, reference!!.resolve())
    }
  }

  @Test
  fun testOptionKeyReference(): Unit = timeoutRunBlocking {
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
            opt<caret>ion: value
    """.trimIndent())
    readAction {
      val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
      val keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java)
      val reference = keyValue!!.references.find { it is OpenRewriteYamlRecipeOptionReferenceProvider.RecipeOptionReference }
      assertNotNull(reference)
      assertInstanceOf(OpenRewriteOptionPsiElement::class.java, reference!!.resolve())
    }
  }

  @Test
  fun testOptionValueReference(): Unit = timeoutRunBlocking {
    myFixture.addClass("""
      package com;
      
      public class MyRecipe extends $RECIPE_CLASS_NAME {
        @$OPTION_CLASS_NAME()
        public String option;  
      }
    """.trimIndent())
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my
      recipeList:
        - com.MyRecipe:
            option: val<caret>ue
    """.trimIndent())
    readAction {
      val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
      val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java)
      val reference = scalar!!.references.find { it is OpenRewriteYamlRecipeOptionValueReferenceProvider.RecipeOptionValueReference }
      assertNotNull(reference)
    }
  }
}