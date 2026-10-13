package com.intellij.openRewrite

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.ui.DeferredIcon
import com.intellij.ui.IconManager
import com.intellij.ui.LayeredIcon
import com.intellij.ui.icons.CoreIconManager
import com.intellij.ui.icons.RowIcon
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.swing.Icon

class OpenRewriteFileServiceTest : OpenRewriteLightHighlightingTestCase() {
  @BeforeEach
  fun activateIcons() {
    // ensure that IconLoader will not use a fake empty icon
    IconManager.activate(CoreIconManager())
  }

  @AfterEach
  fun deactivateIcons() {
    IconManager.deactivate()
    IconLoader.clearCacheInTests()
  }

  @Test
  fun testRecipeYaml(): Unit = timeoutRunBlocking {
    val file = myFixture.addFileToProject(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
      recipeList:
        - org.openrewrite.java.ChangePackage:
            oldPackageName: com
            newPackageName: org
    """.trimIndent())
    assertTrue(readAction { isRecipe(file) })
    assertEquals(OpenRewriteIcons.OpenRewrite, getFileIcon(file))
  }

  @Test
  fun testNotTypedYaml(): Unit = timeoutRunBlocking {
    val file = myFixture.addFileToProject(RECIPE_FILE_NAME, """
      name: com.my.Recipe
      recipeList:
        - org.openrewrite.java.ChangePackage:
            oldPackageName: com
            newPackageName: org
    """.trimIndent())
    assertFalse(readAction { isRecipe(file) })
    assertNotSame(OpenRewriteIcons.OpenRewrite, getFileIcon(file))
  }

  // in unit tests, `ElementBase` defers the icon only on the EDT
  private suspend fun getFileIcon(psiFile: PsiFile): Icon? = withContext(Dispatchers.EDT) {
    val deferredIcon = assertInstanceOf(DeferredIcon::class.java, psiFile.getIcon(Iconable.ICON_FLAG_READ_STATUS))
    val rowIcon = assertInstanceOf(RowIcon::class.java, deferredIcon.evaluate())
    val icon = rowIcon.getIcon(0)
    when (icon) {
      is DeferredIcon -> icon.evaluate()
      is LayeredIcon -> icon.getIcon(0)
      else -> null
    }
  }
}