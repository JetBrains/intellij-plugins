package com.intellij.openRewrite.maven

import com.intellij.maven.testFramework.fixtures.MavenVersionArguments
import com.intellij.maven.testFramework.fixtures.configureProjectPom
import com.intellij.maven.testFramework.fixtures.getTestPsiFile
import com.intellij.maven.testFramework.fixtures.importProjectAsync
import com.intellij.maven.testFramework.fixtures.mavenDomFixture
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.smartReadAction
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedClass
import org.junit.jupiter.params.provider.ArgumentsSource

@TestApplication
@ParameterizedClass
@ArgumentsSource(MavenVersionArguments::class)
class OpenRewriteMigrationActionMavenTest(mavenVersion: String, modelVersion: String) {
  private val maven by mavenDomFixture(initialPom = null, mavenVersion = mavenVersion, modelVersion = modelVersion)

  @Test
  fun testJakartaMigrateAction() = runBlocking {
    maven.configureProjectPom("""
      <groupId>com.example</groupId>
      <artifactId>demo</artifactId>
      <version>1.0-SNAPSHOT</version>

      <dependencies>
        <dependency>
          <groupId>javax.inject</groupId>
          <artifactId>javax.inject</artifactId>
          <version>1</version>
        </dependency>
      </dependencies>
    """.trimIndent())
    maven.importProjectAsync()
    doTestAction(true, true)
  }

  @Test
  fun testJakartaMigrateActionDisabled() = runBlocking {
    maven.configureProjectPom("""
      <groupId>com.example</groupId>
      <artifactId>demo</artifactId>
      <version>1.0-SNAPSHOT</version>

      <dependencies>
        <dependency>
          <groupId>javax.inject</groupId>
          <artifactId>javax.inject</artifactId>
          <version>1</version>
        </dependency>
        <dependency>
          <groupId>jakarta.inject</groupId>
          <artifactId>jakarta.inject-api</artifactId>
          <version>1.0</version>
        </dependency>
      </dependencies>
    """.trimIndent())
    maven.importProjectAsync()
    doTestAction(false, true)
  }

  @Test
  fun testMigrateActionDisabled() = runBlocking {
    maven.configureProjectPom("""
      <groupId>com.example</groupId>
      <artifactId>demo</artifactId>
      <version>1.0-SNAPSHOT</version>

      <dependencies>
      </dependencies>
    """.trimIndent())
    maven.importProjectAsync()
    doTestAction(false, false)
  }

  private suspend fun doTestAction(toolbarExpected: Boolean, popupExpected: Boolean) {
    val psiFile = maven.getTestPsiFile(maven.projectPom)
    val dataContext = DataContext { dataId ->
      when (dataId) {
        PlatformDataKeys.PROJECT.name -> maven.project
        PlatformDataKeys.PSI_FILE.name -> psiFile
        else -> null
      }
    }
    smartReadAction(maven.project) {
      val action = ActionManager.getInstance().getAction("OpenRewrite.Migration.Action") ?: error("Migration action not found")

      val toolbarEvent = AnActionEvent.createFromAnAction(action, null, ActionPlaces.TOOLBAR,
                                                          dataContext)
      ActionUtil.updateAction(action, toolbarEvent)
      assertEquals(toolbarExpected, toolbarEvent.presentation.isEnabledAndVisible)

      val popupEvent = AnActionEvent.createFromAnAction(action, null, ActionPlaces.POPUP,
                                                        dataContext)
      ActionUtil.updateAction(action, popupEvent)
      assertEquals(popupExpected, popupEvent.presentation.isEnabledAndVisible)
    }
  }
}