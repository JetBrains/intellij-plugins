package com.intellij.clion.embedded.platformio

import com.intellij.build.events.MessageEvent
import com.intellij.clion.embedded.platformio.TestUtils.findExternalModule
import com.intellij.clion.testFramework.nolang.junit5.core.clionProjectTestFixture
import com.intellij.clion.testFramework.nolang.junit5.core.tempDirTestFixture
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemBuildEvent
import com.intellij.openapi.project.Project
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.asSafely
import com.intellij.util.system.OS
import com.jetbrains.cidr.CidrTestDataFixture
import com.jetbrains.cidr.assumptions.ToolSetKindAssumption
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioExecutionTarget
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioProjectResolver
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioRunConfigurationManagerHelper
import com.jetbrains.cidr.cpp.execution.manager.CLionRunConfigurationManager
import com.jetbrains.cidr.external.system.model.ExternalModule
import com.jetbrains.cidr.lang.CLanguageKind
import com.jetbrains.cidr.lang.OCLanguageKind
import com.jetbrains.cidr.lang.workspace.compiler.GCCCompilerKind
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.readText

@TestApplication
class TestProjectResolve {
  private val projectPathFixture = tempDirTestFixture(CidrTestDataFixture.getPlatformioTestData() / "project1")

  private val projectPath by projectPathFixture
  private val project by clionProjectTestFixture(projectPathFixture)

  private val EXPECTED_ACTIVE_INI_FILES = listOf(
    "platformio.ini",
    "pio_included_by_name.ini",
    "pio_included_by_qmark1.ini",
    "pio_included_by_qmarka.ini",
    "pio_included_by_letter_a.ini",
    "pio_included_by_letter_b.ini",
    "configs/included.ini",
    "configs/included_by_asterisk.ini",
    "configs/included_by_asterisk1.ini",
    "configs/included_by_asterisk12.ini"
  )
  private val EXPECTED_SOURCE_FILES = mapOf(
    "main.cpp" to CLanguageKind.CPP,
    "nested.c" to CLanguageKind.C,
    "nested_nested.c" to CLanguageKind.C,
    "forced_included.cpp" to CLanguageKind.CPP,
    "nothingA.cpp" to CLanguageKind.CPP,
    "nothingB.cpp" to CLanguageKind.CPP,
    "nothingC.cpp" to CLanguageKind.CPP,
    "extra.c" to CLanguageKind.C
  )

  @BeforeEach
  fun beforeEach() {
    CLionRunConfigurationManager.getInstance(project).updateRunConfigurations(PlatformioRunConfigurationManagerHelper)
  }

  @Test
  fun testScanFiles() = doTestScanFiles()

  @Test
  fun testScanFiles2023() = doTestScanFiles("-2023")

  private fun doTestScanFiles(suffix: String = "") {
    ToolSetKindAssumption.assumeToolSetKind().isNotRemoteLike()

    val taskId: ExternalSystemTaskId = ExternalSystemTaskId.create(ID, ExternalSystemTaskType.RESOLVE_PROJECT, project)
    val testListener = TaskNotificationListerForTest()
    val projectNode = PlatformioProjectResolverForTest(suffix).resolveProjectInfo(
      id = taskId,
      projectPath = projectPath.absolutePathString(),
      isPreviewMode = true,
      settings = null,
      listener = testListener,
      resolverPolicy = null
    )
    assertEquals(1, testListener.errorMessagesCounter, "Error message counter")

    val service = project.service<PlatformioService>()

    val expectedTargetExePath = if (OS.CURRENT == OS.Windows)
      "D:\\work\\platformio-test\\contrib\\platformio\\testData\\project1\\.pio\\build\\esp-wrover-kit\\firmware.elf"
    else
      "/home/user/platformio-test/contrib/platformio/testData/project1/.pio/build/esp-wrover-kit/firmware.elf"
    assertEquals(expectedTargetExePath, service.targetExecutablePath, "Target Executable Path")
    val expectedSvdPath = if (OS.CURRENT == OS.Windows) "D:\\svd.svd" else "/tmp/svd.svd"
    assertEquals(expectedSvdPath, service.svdPath, "Svd Path")


    assertEquals(listOf(PlatformioExecutionTarget("esp-wrover-kit")), service.envs, "Environments")
    assertEquals(listOf(
      "target-platformio-buildfs",
      "target-platformio-size",
      "target-platformio-upload",
      "target-platformio-upload-monitor",
      "target-platformio-uploadfs",
      "target-platformio-uploadfsota",
      "target-platformio-erase"),
                 service.getActiveActionIds().toList(), "Targets")

    verifyIniFiles()

    verifySources(projectNode!!)

    val expectedIncludePath = if (OS.CURRENT == OS.Windows)
      "-IC:\\Users\\user\\.platformio\\packages\\framework-arduinoespressif32\\libraries\\Wire\\src"
    else
      "-I/home/user/.platformio/packages/framework-arduinoespressif32/libraries/Wire/src"
    val commonSwitches = listOf("-DESP_PLATFORM", "-ggdb", expectedIncludePath)
    val cSwitches = listOf("-std=gnu99")
    val cppSwitches = listOf("-std=gnu++11")

    verifySwitches(projectNode, CLanguageKind.CPP, commonSwitches + cppSwitches, cSwitches)
    verifySwitches(projectNode, CLanguageKind.C, commonSwitches + cSwitches, cppSwitches)

    assertEquals("Changed name", projectNode.data.externalName)
    assertEquals("Changed name", projectNode.data.internalName)
  }

  private fun verifySwitches(projectNode: DataNode<ProjectData>,
                             langKind: OCLanguageKind,
                             mandatorySwitches: List<String>,
                             undesiredSwitches: List<String>) {
    val languageConfig = projectNode.findExternalModule().data
      .resolveConfigurations.first()
      .languageConfigurations.first { it.languageKind == langKind }!!
    assertEquals(GCCCompilerKind, languageConfig.compilerKind)
    val switches = languageConfig.compilerSwitches?.toSet() ?: emptySet()
    val missingSwitches = mandatorySwitches - switches
    assertTrue(missingSwitches.isEmpty()) { "Missing switches for ${langKind.displayName}: ${missingSwitches.joinToString()}" }
    val unexpectedSwitches = switches.intersect(undesiredSwitches.toSet())
    assertTrue(unexpectedSwitches.isEmpty()) { "Unexpected switches for ${langKind.displayName}: ${unexpectedSwitches.joinToString()}" }
  }

  private fun verifySources(projectNode: DataNode<ProjectData>) {
    val externalModule = projectNode.findExternalModule()
    val actualSourceFiles = externalModule
      .data.asSafely<ExternalModule>()!!
      .resolveConfigurations.first()
      .fileConfigurations
      .associate { it.file.name to it.languageKind }
    assertEquals(EXPECTED_SOURCE_FILES, actualSourceFiles, "Source file")
  }

  private fun verifyIniFiles() {
    val activeIniFiles = project.service<PlatformioService>().iniFiles
    val expectedFiles = EXPECTED_ACTIVE_INI_FILES.map<String, @NonNls String> { projectPath.resolve(it).toString() }.toSet()
    assertEquals(expectedFiles, activeIniFiles, "Detected config files")
  }

  inner class PlatformioProjectResolverForTest(private val suffix: String) : PlatformioProjectResolver() {

    /**
     * Mock data is loaded from file pio-project-config.json
     * The file is created by invoking `pio project config --json-output > pio-project-config.json`
     */
    override fun gatherConfigJson(id: ExternalSystemTaskId,
                                  pioRunEventId: String,
                                  project: Project,
                                  listener: ExternalSystemTaskNotificationListener): String {
      return projectPath.resolve("pio-project-config.json").readText()
    }

    /**
     * Mock data is loaded from file pio-project-metadata-esp-wrover-kit.json
     * The file is created by invoking
     * `pio project metadata -e esp-wrover-kit --json-output --json-output-path=pio-project-metadata-esp-wrover-kit.json`
     *  and then `libsource_dirs` is set to `["lib", "extra_lib"]`
     */
    override fun gatherEnvMetadata(id: ExternalSystemTaskId,
                                   pioRunEventId: String,
                                   project: Project,
                                   activeEnvName: String,
                                   listener: ExternalSystemTaskNotificationListener): String {
      val osSuffix = if (OS.CURRENT == OS.Windows) "_win" else ""
      return projectPath.resolve("pio-project-metadata-esp-wrover-kit${suffix}${osSuffix}.json").readText()
    }

    override fun createRunConfigurationIfRequired(project: Project) {}

    /**
     * Mock data is loaded from compile_commands.json or compile_commands_win.json on Windows
     * The file is created by invoking `pio run -t compiledb`,
     * dropping information about framework sources,
     * and modifying the `directory` entries to make the data not rely on absolute paths
     */
    override fun gatherCompDB(id: ExternalSystemTaskId, pioRunEventId: String, project: Project, activeEnvName: String, listener: ExternalSystemTaskNotificationListener, projectPath: String): String {
      val compDbFileName = if (OS.CURRENT == OS.Windows) "compile_commands_win.json" else "compile_commands.json"
      return this@TestProjectResolve.projectPath.resolve(compDbFileName).readText().injectProjectPath()
    }

    private fun String.injectProjectPath() = this.replace("\"directory\": \"\"", "\"directory\": \"${projectPath.absolutePathString().replace("\\", "\\\\")}\"")
  }
}

internal class TaskNotificationListerForTest : ExternalSystemTaskNotificationListener {
  var errorMessagesCounter = 0
  override fun onStatusChange(event: ExternalSystemTaskNotificationEvent) {
    if (event.asSafely<ExternalSystemBuildEvent>()?.buildEvent?.asSafely<MessageEvent>()?.kind == MessageEvent.Kind.ERROR)
      errorMessagesCounter++
  }
}

