package com.intellij.clion.embedded.platformio

import com.intellij.clion.embedded.platformio.TestUtils.findExternalModule
import com.intellij.clion.testFramework.nolang.junit5.core.clionProjectTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.tempDirTestFixture
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import com.intellij.openapi.project.Project
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.system.OS
import com.jetbrains.cidr.CidrTestDataFixture
import com.jetbrains.cidr.assumptions.ToolSetKindAssumption
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioProjectResolver
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioRunConfigurationManagerHelper
import com.jetbrains.cidr.cpp.execution.manager.CLionRunConfigurationManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.readText

@TestApplication
class TestProjectLibScan {
  private val projectDirFixture = tempDirTestFixture(CidrTestDataFixture.getPlatformioTestData()  / "project-scan-libraries")

  private val projectDir by projectDirFixture
  private val project by clionProjectTestFixture(projectDirFixture)

  private val EXPECTED_SOURCE_FILES = setOf(
    "/src/main.cpp",
    "/lib/confusing-name/src/confusing-name.cpp",
    "/lib/confusing-name-no-src/confusing-name-no-src.cpp",
    "/lib/confusing-name-nested-src/main/src/nested/confusing-name-nested-src.cpp",
    "/lib/confusing-name-nested-src/main/src/confusing-name-nested-src.cpp",
  )

  @BeforeEach
  fun beforeEach() {
    CLionRunConfigurationManager.getInstance(project).updateRunConfigurations(PlatformioRunConfigurationManagerHelper)
  }

  @Test
  fun testScanLibraries() {
    ToolSetKindAssumption.assumeToolSetKind().isNotRemoteLike()

    val taskId: ExternalSystemTaskId = ExternalSystemTaskId.create(ID, ExternalSystemTaskType.RESOLVE_PROJECT, project)
    val testListener = ExternalSystemTaskNotificationListener.NULL_OBJECT
    val projectNode = PlatformioProjectResolverForTest().resolveProjectInfo(
      id = taskId,
      projectPath = projectDir.absolutePathString(),
      isPreviewMode = true,
      settings = null,
      listener = testListener,
      resolverPolicy = null
    )!!
    val actualSourceFiles = projectNode.findExternalModule().data
      .resolveConfigurations.first()
      .fileConfigurations.associateBy { it.file.path.replace(projectDir.absolutePathString(), "").replace('\\', '/') }
    assertEquals(this@TestProjectLibScan.EXPECTED_SOURCE_FILES, actualSourceFiles.keys, "Source file")
    val switchesWithDefines = actualSourceFiles["/lib/confusing-name-no-src/confusing-name-no-src.cpp"]!!.compilerSwitches
    assertNotNull(switchesWithDefines)
    assertTrue(switchesWithDefines.contains("-DMANDATORY_DEFINE_B1"), "MANDATORY_DEFINE_B1")
    assertTrue(switchesWithDefines.contains("-DMANDATORY_DEFINE_B2"), "MANDATORY_DEFINE_B2")
  }

  private inner class PlatformioProjectResolverForTest() : PlatformioProjectResolver() {

    /**
     * Mock data is loaded from file pio-project-config.json
     * The file is created by invoking `pio project config --json-output > pio-project-config.json`
     */
    override fun gatherConfigJson(id: ExternalSystemTaskId,
                                  pioRunEventId: String,
                                  project: Project,
                                  listener: ExternalSystemTaskNotificationListener): String {
      return projectDir.resolve("pio-project-config.json").readText()
    }

    /**
     * Mock data is loaded from file pio-project-metadata-esp-wrover-kit.json
     * The file is created by invoking
     * `pio project metadata -e esp32 --json-output --json-output-path=pio-project-metadata-esp-wrover-kit.json`
     *  and then `libsource_dirs` is set to `["lib"]`
     *
     */
    override fun gatherEnvMetadata(id: ExternalSystemTaskId,
                                   pioRunEventId: String,
                                   project: Project,
                                   activeEnvName: String,
                                   listener: ExternalSystemTaskNotificationListener): String {
      val osSuffix = if (OS.CURRENT == OS.Windows) "_win" else ""
      return projectDir.resolve("pio-project-metadata${osSuffix}.json").readText().replace("T:", projectDir.absolutePathString())
    }

    override fun createRunConfigurationIfRequired(project: Project) {}

    /**
     * Mock data is loaded from compile_commands.json or compile_commands_win.json on Windows
     * The file is created by invoking `pio run -t compiledb`,
     * dropping information about framework sources,
     * and clearing the `directory` entries to make the data not rely on absolute paths.
     * We inject the actual project directory here.
     */
    override fun gatherCompDB(id: ExternalSystemTaskId, pioRunEventId: String, project: Project, activeEnvName: String, listener: ExternalSystemTaskNotificationListener, projectPath: String): String {
      val compDbFileName = if (OS.CURRENT == OS.Windows) "compile_commands_win.json" else "compile_commands.json"
      return projectDir.resolve(compDbFileName).readText().injectProjectPath()
    }

    private fun String.injectProjectPath() = this.replace("\"directory\": \"\"", "\"directory\": \"${projectDir.absolutePathString().replace("\\", "\\\\")}\"")
  }
}
