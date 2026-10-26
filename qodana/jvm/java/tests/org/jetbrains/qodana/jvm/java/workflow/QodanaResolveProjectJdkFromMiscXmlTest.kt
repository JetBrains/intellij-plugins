package org.jetbrains.qodana.jvm.java.workflow

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.ide.impl.OpenProjectTaskBuilder
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.application.backgroundWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.util.io.NioFiles
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.closeProjectAsync
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@TestApplication
class QodanaResolveProjectJdkFromMiscXmlTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  private lateinit var tempDir: Path
  private val ext = QodanaResolveProjectJdkFromMiscXml()

  @BeforeEach
  fun setUp() {
    tempDir = createTempDirectory("qodana-resolve-project-jdk")
    tempDir.resolve(".idea").createDirectories()
  }

  @AfterEach
  fun tearDown() {
    if (::tempDir.isInitialized) {
      NioFiles.deleteRecursively(tempDir)
    }
  }

  private fun createMiscXml(content: String) {
    tempDir.resolve(".idea/misc.xml").writeText(content)
  }

  private fun config() = QodanaConfig.fromYaml(tempDir, Path.of("unused"))

  private suspend fun openProjectAndRunBeforeOpen(
    block: suspend (Project) -> Unit = {},
    configureTask: suspend OpenProjectTaskBuilder.() -> Unit = {},
  ): Project {
    return ProjectManagerEx.getInstanceEx().openProjectAsync(tempDir, OpenProjectTask builder@{
      forceOpenInNewFrame = true
      useDefaultProjectAsTemplate = false
      runConversionBeforeOpen = false
      runConfigurators = false
      showWelcomeScreen = false
      beforeOpenTasks += { project ->
        block(project)
        true
      }
      runBlockingCancellable {
        configureTask(this@builder)
      }
    }) ?: error("Failed to open test project at $tempDir")
  }

  private fun miscXmlWithJdk(name: String) = """
    <?xml version="1.0" encoding="UTF-8"?>
    <project version="4">
      <component name="ProjectRootManager" project-jdk-name="$name" project-jdk-type="JavaSDK" />
    </project>
  """.trimIndent()

  @Test
  fun `test ProjectRootManager exposes project sdk metadata during beforeOpen even when jdk is missing from table`() {
    timeoutRunBlocking {
      createMiscXml(miscXmlWithJdk("missing-jdk-17"))

      var sdkNameInBeforeOpen: String? = null
      var sdkTypeInBeforeOpen: String? = null
      val project = openProjectAndRunBeforeOpen(block = { openedProject ->
        val rootManager = ProjectRootManager.getInstance(openedProject)
        sdkNameInBeforeOpen = rootManager.projectSdkName
        sdkTypeInBeforeOpen = rootManager.projectSdkTypeName
      })

      try {
        assertThat(ProjectJdkTable.getInstance().findJdk("missing-jdk-17")).isNull()
        assertThat(sdkNameInBeforeOpen).isEqualTo("missing-jdk-17")
        assertThat(sdkTypeInBeforeOpen).isEqualTo(JavaSdk.getInstance().name)
      }
      finally {
        project.closeProjectAsync()
      }
    }
  }

  @Test
  fun `test configureProjectOpening returns early when project sdk name is absent`() {
    timeoutRunBlocking {
      createMiscXml("""
        <?xml version="1.0" encoding="UTF-8"?>
        <project version="4">
          <component name="ProjectRootManager" />
        </project>
      """.trimIndent())

      var sdkNameInBeforeOpen: String? = "something"
      val countBefore = ProjectJdkTable.getInstance().allJdks.size
      val config = config()
      val project = openProjectAndRunBeforeOpen(block = { openedProject ->
        sdkNameInBeforeOpen = ProjectRootManager.getInstance(openedProject).projectSdkName
      }, configureTask = {
        ext.configureProjectOpening(config, this)
      })

      try {
        assertThat(sdkNameInBeforeOpen).isNull()
        assertThat(ProjectJdkTable.getInstance().allJdks.size).isEqualTo(countBefore)
      }
      finally {
        project.closeProjectAsync()
      }
    }
  }

  @Test
  fun `test configureProjectOpening does not add jdk when it is already registered`() {
    timeoutRunBlocking {
      createMiscXml(miscXmlWithJdk("existing-jdk"))
      val jdk = JavaSdk.getInstance().createJdk("existing-jdk", IdeaTestUtil.requireRealJdkHome(), false)
      backgroundWriteAction { ProjectJdkTable.getInstance().addJdk(jdk, qodanaFixture.testRootDisposable) }
      val countBefore = ProjectJdkTable.getInstance().allJdks.size

      val config = config()
      val project = openProjectAndRunBeforeOpen(configureTask = {
        ext.configureProjectOpening(config, this)
      })

      try {
        assertThat(ProjectJdkTable.getInstance().allJdks.size).isEqualTo(countBefore)
      }
      finally {
        project.closeProjectAsync()
      }
    }
  }
}