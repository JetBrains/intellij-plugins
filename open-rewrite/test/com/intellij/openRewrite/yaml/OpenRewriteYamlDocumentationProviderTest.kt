package com.intellij.openRewrite.yaml

import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider
import com.intellij.openRewrite.OPTION_CLASS_NAME
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.openapi.application.runReadAction
import com.intellij.platform.backend.documentation.DocumentationData
import com.intellij.testFramework.PlatformTestUtil

class OpenRewriteYamlDocumentationProviderTest : OpenRewriteLightHighlightingTestCase() {
  fun testRecipeDocumentation() {
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      recipeList:
        - com.<caret>second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
      displayName: My Recipe
      description: My test recipe
    """.trimIndent())
    doTestDocumentation("<div class='definition'><pre><b>com.second</b><br></pre></div><div class='content'>" +
                        "<em>My Recipe</em><br><br>My test recipe<br><br></div>")
  }

  fun testOptionDocumentation() {
    myFixture.addClass("""
      package com;

      public class MyRecipe extends $RECIPE_CLASS_NAME {
        @$OPTION_CLASS_NAME(required = false)
        public String notRequired;

        @$OPTION_CLASS_NAME(
            displayName = "My Option",
            description = "My option description")
        public String option;
      }
    """.trimIndent())
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      recipeList:
        - com.MyRecipe:
            opt<caret>ion: value
    """.trimIndent())
    doTestDocumentation("<div class='definition'><pre><b>option</b><br><a href=\"psi_element://java.lang.String\">" +
                        "<code><span style=\"color:#0000ff;\">String</span></code></a></pre></div><div class='content'>" +
                        "<em>My Option</em><br><br>My option description<br><br></div><table class='sections'>" +
                        "<tr><td valign='top' class='section'><p>Required:</td><td valign='top'><b>true</b></td></table>")

  }

  private fun doTestDocumentation(expected: String) {
    val targets = PlatformTestUtil.callOnBgtSynchronously({ runReadAction {
      IdeDocumentationTargetProvider.getInstance(project).documentationTargets(editor, file, editor.caretModel.offset)
    } }, 10)!!
    val target = assertOneElement(targets)
    val documentationData = target.computeDocumentation() as DocumentationData
    assertEquals(expected, documentationData.html)
  }
}