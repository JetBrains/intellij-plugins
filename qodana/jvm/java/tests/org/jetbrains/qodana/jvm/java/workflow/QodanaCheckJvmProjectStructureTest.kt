package org.jetbrains.qodana.jvm.java.workflow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.RuntimeNotificationCollector
import java.nio.file.Path

class QodanaCheckJvmProjectStructureTest : QodanaPluginLightTestBase() {

  override fun runInDispatchThread() = false

  private fun config() = QodanaConfig.fromYaml(Path.of(project.basePath ?: ""), Path.of("unused"))

  override fun setUp() {
    super.setUp()
    project.service<RuntimeNotificationCollector>().initializeForRun(config())
  }

  fun `test afterConfiguration adds notification for module with unresolved library`(): Unit = runBlocking {
    val libName = "missing-lib-${System.nanoTime()}"
    ApplicationManager.getApplication().runWriteAction {
      val model = ModuleRootManager.getInstance(myFixture.module).modifiableModel
      model.addInvalidLibrary(libName, LibraryTablesRegistrar.APPLICATION_LEVEL)
      model.commit()
    }
    try {
      val collector = project.service<RuntimeNotificationCollector>()
      QodanaCheckJvmProjectStructure().afterConfiguration(config(), project)
      assertThat(collector.notifications).isNotEmpty()
    }
    finally {
      ApplicationManager.getApplication().runWriteAction {
        val model = ModuleRootManager.getInstance(myFixture.module).modifiableModel
        model.orderEntries
          .filter { it.presentableName == libName }
          .forEach { model.removeOrderEntry(it) }
        model.commit()
      }
    }
  }

  fun `test afterConfiguration adds no notification when module has no unresolved dependencies`(): Unit = runBlocking {
    val collector = project.service<RuntimeNotificationCollector>()
    QodanaCheckJvmProjectStructure().afterConfiguration(config(), project)
    val libraryNotifications = collector.notifications.filter { n ->
      n.message?.text?.contains("library") == true || n.message?.text?.contains("was not resolved") == true
    }
    assertThat(libraryNotifications).isEmpty()
  }
}