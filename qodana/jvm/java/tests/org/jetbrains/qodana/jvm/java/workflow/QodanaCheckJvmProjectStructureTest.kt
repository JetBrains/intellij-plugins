package org.jetbrains.qodana.jvm.java.workflow

import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.RuntimeNotificationCollector
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path

@TestApplication
class QodanaCheckJvmProjectStructureTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  private fun config() = QodanaConfig.fromYaml(Path.of(qodanaFixture.project.basePath ?: ""), Path.of("unused"))

  @BeforeEach
  fun setUp() {
    qodanaFixture.project.service<RuntimeNotificationCollector>().initializeForRun(config())
  }

  @Test
  fun `test afterConfiguration adds notification for module with unresolved library`(): Unit = timeoutRunBlocking {
    val libName = "missing-lib-${System.nanoTime()}"
    backgroundWriteAction {
      val model = ModuleRootManager.getInstance(qodanaFixture.module).modifiableModel
      model.addInvalidLibrary(libName, LibraryTablesRegistrar.APPLICATION_LEVEL)
      model.commit()
    }
    try {
      val collector = qodanaFixture.project.service<RuntimeNotificationCollector>()
      QodanaCheckJvmProjectStructure().afterConfiguration(config(), qodanaFixture.project)
      assertThat(collector.notifications).isNotEmpty()
    }
    finally {
      backgroundWriteAction {
        val model = ModuleRootManager.getInstance(qodanaFixture.module).modifiableModel
        model.orderEntries
          .filter { it.presentableName == libName }
          .forEach { model.removeOrderEntry(it) }
        model.commit()
      }
    }
  }

  @Test
  fun `test afterConfiguration adds no notification when module has no unresolved dependencies`(): Unit = timeoutRunBlocking {
    val collector = qodanaFixture.project.service<RuntimeNotificationCollector>()
    QodanaCheckJvmProjectStructure().afterConfiguration(config(), qodanaFixture.project)
    val libraryNotifications = collector.notifications.filter { n ->
      n.message?.text?.contains("library") == true || n.message?.text?.contains("was not resolved") == true
    }
    assertThat(libraryNotifications).isEmpty()
  }
}