package com.intellij.openRewrite

import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiFile
import com.intellij.ui.DeferredIcon
import com.intellij.ui.LayeredIcon
import com.intellij.ui.icons.RowIcon
import javax.swing.Icon

class OpenRewriteFileServiceTest : OpenRewriteLightHighlightingTestCase() {
  override fun isIconRequired(): Boolean = true

  fun testRecipeYaml() {
    val file = myFixture.addFileToProject(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
      recipeList:
        - org.openrewrite.java.ChangePackage:
            oldPackageName: com
            newPackageName: org
    """.trimIndent())
    assertTrue(isRecipe(file))
    assertEquals(OpenRewriteIcons.OpenRewrite, getFileIcon(file))
  }

  fun testNotTypedYaml() {
    val file = myFixture.addFileToProject(RECIPE_FILE_NAME, """
      name: com.my.Recipe
      recipeList:
        - org.openrewrite.java.ChangePackage:
            oldPackageName: com
            newPackageName: org
    """.trimIndent())
    assertFalse(isRecipe(file))
    assertNotSame(OpenRewriteIcons.OpenRewrite, getFileIcon(file))
  }

  private fun getFileIcon(psiFile: PsiFile): Icon? {
    val deferredIcon = assertInstanceOf(psiFile.getIcon(Iconable.ICON_FLAG_READ_STATUS), DeferredIcon::class.java)
    val rowIcon = assertInstanceOf(deferredIcon.evaluate(), RowIcon::class.java)
    val icon = rowIcon.getIcon(0)
    if (icon is DeferredIcon) {
      return icon.evaluate()
    }
    if (icon is LayeredIcon) {
      return icon.getIcon(0)
    }
    return null
  }
}