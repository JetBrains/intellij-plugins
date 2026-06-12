package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.hints.declarative.impl.inlayRenderer.DeclarativeInlayRenderer
import com.intellij.codeInsight.hints.declarative.impl.views.TextInlayPresentationEntry
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.openapi.command.WriteCommandAction

class OpenRewriteRecipeInlayHintsProviderTest : OpenRewriteLightHighlightingTestCase() {
  fun testScalarRecipeInlay() {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
      recipeList:
        - com.my.Other<hint text="Other recipe"/>
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Other
      displayName: Other recipe
    """.trimIndent())
    doTestInlays()
  }

  fun testScalarPreconditionInlay() {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
      preconditions:
        - com.my.Other<hint text="Other recipe"/>
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Other
      displayName: Other recipe
    """.trimIndent())
    doTestInlays()
  }

  fun testScalarStyleInlay() {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/style
      name: com.my.Style
      styleConfigs:
        - com.my.Other<hint text="Other style"/>
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.my.Other
      displayName: Other style
    """.trimIndent())
    doTestInlays()
  }

  fun testMappingRecipeInlay() {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
      recipeList:
        - com.my.Other:<caret>
            param: value
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Other
      displayName: Other recipe
    """.trimIndent())
    // Insert hint description after configuring to collect local descriptors
    WriteCommandAction.writeCommandAction(myFixture.project).run<Throwable> {
      myFixture.editor.document.insertString(myFixture.caretOffset, "<hint text=\"Other recipe\"/>")
    }
    doTestInlays()
  }

  private fun doTestInlays() {
    myFixture.allowTreeAccessForAllFiles()
    myFixture.testInlays(
      {
        (it.renderer as DeclarativeInlayRenderer).presentationList.getEntries().joinToString { entry ->
          (entry as TextInlayPresentationEntry).text
        }
      },
      { it.renderer is DeclarativeInlayRenderer })
  }
}