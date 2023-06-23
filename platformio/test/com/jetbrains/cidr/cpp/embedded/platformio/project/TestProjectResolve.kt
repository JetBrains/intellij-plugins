package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.build.events.MessageEvent
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.*
import com.intellij.openapi.externalSystem.model.task.event.ExternalSystemBuildEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioTargetData
import com.jetbrains.cidr.cpp.execution.manager.CLionRunConfigurationManager
import com.jetbrains.cidr.external.system.model.ExternalModule
import com.jetbrains.cidr.lang.CLanguageKind
import com.jetbrains.cidr.lang.OCLanguageKind
import com.jetbrains.cidr.lang.workspace.compiler.GCCCompilerKind
import junit.framework.TestCase
import junit.framework.TestCase.assertTrue
import org.jetbrains.annotations.NonNls
import java.nio.file.Paths

class TestProjectResolve : LightPlatformTestCase() {
  private val BASE_TEST_DATA_PATH: String = PathManager.getHomePath() + "/contrib/platformio/testData"
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
  private val expectedSourceFiles = mapOf(
    "main.cpp" to CLanguageKind.CPP,
    "nested.c" to CLanguageKind.C,
    "forced_included.cpp" to CLanguageKind.CPP,
    "nothingA.cpp" to CLanguageKind.CPP,
    "nothingB.cpp" to CLanguageKind.CPP,
    "nothingC.cpp" to CLanguageKind.CPP,
  )

  private lateinit var projectPath: String
  private lateinit var projectDir: VirtualFile

  override fun setUp() {
    super.setUp()
    projectPath = "$BASE_TEST_DATA_PATH/project1"
    projectDir = VfsUtil.findFile(Paths.get(projectPath), true)!!
    WriteAction.run<Throwable> {
      CLionRunConfigurationManager.getInstance(project).updateRunConfigurations(PlatformioRunConfigurationManagerHelper)
    }
  }

  fun testScanFiles() = doTestScanFiles()

  fun testScanFiles2023() = doTestScanFiles("-2023")

  private fun doTestScanFiles(suffix: String = "") {
    val taskId: ExternalSystemTaskId = ExternalSystemTaskId.create(ID, ExternalSystemTaskType.RESOLVE_PROJECT, project)
    val testListener = TaskNotificationListerForTest()
    val projectNode = PlatformioProjectResolverForTest(suffix).resolveProjectInfo(
      id = taskId,
      projectPath = projectPath,
      isPreviewMode = true,
      settings = null,
      listener = testListener,
      resolverPolicy = null
    )
    assertEquals("Error message counter", 1, testListener.errorMessagesCounter)

    val service = project.service<PlatformioService>()

    assertEquals("Target Executable Path",
                 "D:\\work\\platformio-test\\contrib\\platformio\\testData\\project1\\.pio\\build\\esp-wrover-kit\\firmware.elf",
                 service.targetExecutablePath)
    assertEquals("Svd Path", "D:\\svd.svd", service.svdPath)


    assertEquals("Environments", listOf(PlatformioExecutionTarget("esp-wrover-kit")), service.envs)
    assertEquals("Targets", listOf(
      PlatformioTargetData("buildfs", "Build Filesystem Image", null, "Platform"),
      PlatformioTargetData("size", "Program Size", "Calculate program size", "Platform"),
      PlatformioTargetData("upload", "Upload", null, "Platform"),
      PlatformioTargetData("uploadfs", "Upload Filesystem Image", null, "Platform"),
      PlatformioTargetData("uploadfsota", "Upload Filesystem Image OTA", null, "Platform"),
      PlatformioTargetData("erase", "Erase Flash", null, "Platform")
    ), service.targets)

    verifyIniFiles(projectDir)

    verifySources(projectNode!!)

    val commonSwitches = listOf("-DESP_PLATFORM", "-ggdb",
                                "-IC:\\Users\\user\\.platformio\\packages\\framework-arduinoespressif32\\libraries\\Wire\\src")
    val cSwitches = listOf("-std=gnu99")
    val cppSwitches = listOf("-std=gnu++11")

    verifySwitches(projectNode, CLanguageKind.CPP, commonSwitches + cppSwitches, cSwitches)
    verifySwitches(projectNode, CLanguageKind.C, commonSwitches + cSwitches, cppSwitches)
  }

  private fun verifySwitches(projectNode: DataNode<ProjectData>,
                             langKind: OCLanguageKind,
                             mandatorySwitches: List<String>,
                             undesiredSwitches: List<String>) {
    val languageConfig = (projectNode.children.first().children.first().data as ExternalModule)
      .resolveConfigurations.first()
      .languageConfigurations.first { it.languageKind == langKind }!!
    assertEquals(GCCCompilerKind, languageConfig.compilerKind)
    val switches = languageConfig.compilerSwitches.toSet()
    val missingSwitches = mandatorySwitches - switches
    assertTrue("Missing switches for ${langKind.displayName}: ${missingSwitches.joinToString()}", missingSwitches.isEmpty())
    val unexpectedSwitches = switches.intersect(undesiredSwitches)
    assertTrue("Unexpected switches for ${langKind.displayName}: ${unexpectedSwitches.joinToString()}", unexpectedSwitches.isEmpty())

  }

  private fun verifySources(projectNode: DataNode<ProjectData>) {
    assertEquals(ProjectKeys.PROJECT, projectNode.key)
    assertEquals(1, projectNode.children.size)
    val moduleNode = projectNode.children.first()
    assertEquals(ProjectKeys.MODULE, moduleNode.key)
    val externalModule = moduleNode.children.first()
    val actualSourceFiles = externalModule
      .data.asSafely<ExternalModule>()!!
      .resolveConfigurations.first()
      .fileConfigurations.associate { it.file.name to it.languageKind }
    assertEquals("Source file", expectedSourceFiles, actualSourceFiles)
  }

  private fun verifyIniFiles(projectDir: VirtualFile) {
    val activeIniFiles = project.service<PlatformioService>().iniFiles
    val expectedFiles = EXPECTED_ACTIVE_INI_FILES.map<String, @NonNls String> { projectDir.findFileByRelativePath(it)!!.path }.toSet()
    assertEquals("Detected config files", expectedFiles, activeIniFiles)
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
      return VfsUtil.loadText(projectDir.findChild("pio-project-config.json")!!)
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
      return projectDir.findChild("pio-project-metadata-esp-wrover-kit${suffix}.json")!!.readText()
    }

    override fun createRunConfigurationIfRequired(project: Project) {}
  }
}

internal class TaskNotificationListerForTest : ExternalSystemTaskNotificationListenerAdapter() {
  var errorMessagesCounter = 0
  override fun onStatusChange(event: ExternalSystemTaskNotificationEvent) {
    if (event.asSafely<ExternalSystemBuildEvent>()?.buildEvent?.asSafely<MessageEvent>()?.kind == MessageEvent.Kind.ERROR)
      errorMessagesCounter++
  }
}

