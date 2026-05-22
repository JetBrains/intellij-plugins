package org.jetbrains.qodana.jvm.java.workflow

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.ide.impl.OpenProjectTaskBuilder
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.util.io.NioFiles
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.closeProjectAsync
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class QodanaResolveProjectJdkFromMiscXmlTest : QodanaPluginLightTestBase() {

  override fun runInDispatchThread() = false

  private lateinit var tempDir: Path
  private val ext = QodanaResolveProjectJdkFromMiscXml()

  override fun setUp() {
    super.setUp()
    tempDir = createTempDirectory("qodana-resolve-project-jdk")
    tempDir.resolve(".idea").createDirectories()
  }

  override fun tearDown() {
    var error: Throwable? = null
    try {
      NioFiles.deleteRecursively(tempDir)
    }
    catch (t: Throwable) {
      error = t
    }
    finally {
      super.tearDown()
    }
    if (error != null) {
      throw error
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
      beforeOpen = { project ->
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

  fun `test ProjectRootManager exposes project sdk metadata during beforeOpen even when jdk is missing from table`() {
    runBlocking {
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

  fun `test configureProjectOpening returns early when project sdk name is absent`() {
    runBlocking {
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

  fun `test configureProjectOpening does not add jdk when it is already registered`() {
    runBlocking {
      createMiscXml(miscXmlWithJdk("existing-jdk"))
      val jdk = JavaSdk.getInstance().createJdk("existing-jdk", IdeaTestUtil.requireRealJdkHome(), false)
      edtWriteAction { ProjectJdkTable.getInstance().addJdk(jdk, testRootDisposable) }
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