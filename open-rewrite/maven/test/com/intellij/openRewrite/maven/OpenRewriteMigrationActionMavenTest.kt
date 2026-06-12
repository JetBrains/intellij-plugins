package com.intellij.openRewrite.maven

import com.intellij.ide.DataManager
import com.intellij.maven.testFramework.MavenDomTestCase
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.smartReadAction
import kotlinx.coroutines.runBlocking
import org.junit.Test

class OpenRewriteMigrationActionMavenTest : MavenDomTestCase() {
  @Test
  fun testJakartaMigrateAction() = runBlocking {
    configureProjectPom("""
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
    importProjectAsync()
    doTestAction(true, true)
  }

  @Test
  fun testJakartaMigrateActionDisabled() = runBlocking {
    configureProjectPom("""
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
    importProjectAsync()
    doTestAction(false, true)
  }

  @Test
  fun testMigrateActionDisabled() = runBlocking {
    configureProjectPom("""
      <groupId>com.example</groupId>
      <artifactId>demo</artifactId>
      <version>1.0-SNAPSHOT</version>

      <dependencies>
      </dependencies>
    """.trimIndent())
    importProjectAsync()
    doTestAction(false, false)
  }

  private suspend fun doTestAction(toolbarExpected: Boolean, popupExpected: Boolean) {
    val editor = getEditor()
    smartReadAction(fixture.project) {
      val action = ActionManager.getInstance().getAction("OpenRewrite.Migration.Action") ?: error("Migration action not found")

      val toolbarEvent = AnActionEvent.createFromAnAction(action, null, ActionPlaces.TOOLBAR,
                                                          DataManager.getInstance().getDataContext(editor.contentComponent))
      ActionUtil.updateAction(action, toolbarEvent)
      assertEquals(toolbarExpected, toolbarEvent.presentation.isEnabledAndVisible)

      val popupEvent = AnActionEvent.createFromAnAction(action, null, ActionPlaces.POPUP,
                                                        DataManager.getInstance().getDataContext(editor.contentComponent))
      ActionUtil.updateAction(action, popupEvent)
      assertEquals(popupExpected, popupEvent.presentation.isEnabledAndVisible)
    }
  }
}