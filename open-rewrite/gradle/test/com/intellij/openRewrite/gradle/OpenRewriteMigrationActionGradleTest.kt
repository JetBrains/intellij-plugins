package com.intellij.openRewrite.gradle

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.plugins.gradle.importing.GradleImportingTestCase
import org.junit.Test
import org.junit.runners.Parameterized
import java.util.concurrent.Callable

class OpenRewriteMigrationActionGradleTest : GradleImportingTestCase() {
  companion object {
    @Parameterized.Parameters(name = "with Gradle-{0}")
    @JvmStatic
    fun data(): Collection<Array<Any>> = listOf(*arrayOf(arrayOf(BASE_GRADLE_VERSION)))
  }

  @Test
  fun testJakartaMigrateAction() {
    val file = createProjectSubFile("build.gradle", """
      plugins {
          id 'java'
      }
      repositories {
          mavenCentral()
      }
      dependencies {
          implementation 'javax.inject:javax.inject:1'
      }
    """.trimIndent())
    importProject()
    doTestAction(true, true, getDataContext(file))
  }

  @Test
  fun testJakartaMigrateActionDisabled() {
    val file = createProjectSubFile("build.gradle", """
      plugins {
          id 'java'
      }
      repositories {
          mavenCentral()
      }
      dependencies {
          implementation 'javax.inject:javax.inject:1'
          implementation 'jakarta.inject:jakarta.inject-api:1.0'
      }
    """.trimIndent())
    importProject()
    doTestAction(false, true, getDataContext(file))
  }

  @Test
  fun testMigrateActionDisabled() {
    val file = createProjectSubFile("build.gradle", """
      plugins {
          id 'java'
      }
      repositories {
          mavenCentral()
      }
      dependencies {
      }
    """.trimIndent())
    importProject()
    doTestAction(false, false, getDataContext(file))
  }

  private fun getDataContext(file: VirtualFile): DataContext {
    return DataContext { dataId ->
      when (dataId) {
        PlatformDataKeys.PROJECT.name -> myProject
        PlatformDataKeys.PSI_FILE.name -> PsiManager.getInstance(myProject).findFile(file)
        else -> null
      }
    }
  }

  private fun doTestAction(toolbarExpected: Boolean, popupExpected: Boolean, dataContext: DataContext) {
    ReadAction.nonBlocking(Callable {
      val action = ActionManager.getInstance().getAction("OpenRewrite.Migration.Action") ?: error("Migration action not found")

      val toolbarEvent = AnActionEvent.createFromAnAction(action, null, ActionPlaces.TOOLBAR, dataContext)
      ActionUtil.updateAction(action, toolbarEvent)
      assertEquals(toolbarExpected, toolbarEvent.presentation.isEnabledAndVisible)

      val popupEvent = AnActionEvent.createFromAnAction(action, null, ActionPlaces.POPUP, dataContext)
      ActionUtil.updateAction(action, popupEvent)
      assertEquals(popupExpected, popupEvent.presentation.isEnabledAndVisible)
    })
      .inSmartMode(myProject)
      .executeSynchronously()
  }
}