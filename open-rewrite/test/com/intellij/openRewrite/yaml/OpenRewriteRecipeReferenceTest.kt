package com.intellij.openRewrite.yaml

import com.intellij.openRewrite.OPTION_CLASS_NAME
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.openRewrite.recipe.OpenRewriteOptionPsiElement
import com.intellij.openRewrite.recipe.OpenRewriteRecipePsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class OpenRewriteRecipeReferenceTest : OpenRewriteLightHighlightingTestCase() {
  fun testScalarRecipeReference() {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      recipeList:
        - com.<caret>second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
    """.trimIndent())
    val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
    val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java)
    val reference = scalar!!.references.find { it is OpenRewriteYamlRecipeReferenceProvider.RecipeReference }
    assertNotNull(reference)
    assertInstanceOf(reference!!.resolve(), OpenRewriteRecipePsiElement::class.java)
  }

  fun testScalarPreconditionReference() {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      preconditions:
        - com.<caret>second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
    """.trimIndent())
    val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
    val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java)
    val reference = scalar!!.references.find { it is OpenRewriteYamlRecipeReferenceProvider.RecipeReference }
    assertNotNull(reference)
    assertInstanceOf(reference!!.resolve(), OpenRewriteRecipePsiElement::class.java)
  }

  fun testScalarStyleReference() {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/style
      name: com.first
      styleConfigs:
        - com.<caret>second
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.second
    """.trimIndent())
    val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
    val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java)
    val reference = scalar!!.references.find { it is OpenRewriteYamlRecipeReferenceProvider.RecipeReference }
    assertNotNull(reference)
    assertInstanceOf(reference!!.resolve(), OpenRewriteRecipePsiElement::class.java)
  }

  fun testKeyValueRecipeReference() {
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
    val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
    val keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java)
    val reference = keyValue!!.references.find { it is OpenRewriteYamlRecipeReferenceProvider.RecipeReference }
    assertNotNull(reference)
    assertInstanceOf(reference!!.resolve(), OpenRewriteRecipePsiElement::class.java)
  }

  fun testOptionKeyReference() {
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
    val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
    val keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java)
    val reference = keyValue!!.references.find { it is OpenRewriteYamlRecipeOptionReferenceProvider.RecipeOptionReference }
    assertNotNull(reference)
    assertInstanceOf(reference!!.resolve(), OpenRewriteOptionPsiElement::class.java)
  }

  fun testOptionValueReference() {
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
    val element = PsiUtilCore.getElementAtOffset(myFixture.file, myFixture.caretOffset)
    val scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar::class.java)
    val reference = scalar!!.references.find { it is OpenRewriteYamlRecipeOptionValueReferenceProvider.RecipeOptionValueReference }
    assertNotNull(reference)
  }
}