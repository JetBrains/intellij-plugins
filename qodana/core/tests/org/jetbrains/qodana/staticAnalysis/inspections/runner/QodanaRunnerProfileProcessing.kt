package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.profile.codeInspection.PROFILE_DIR
import com.jetbrains.qodana.sarif.model.Level
import org.jetbrains.qodana.staticAnalysis.ConfigTester
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.registerDynamicExternalInspectionsInTests
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class QodanaRunnerProfileProcessing : QodanaRunnerTestCase() {
  private val testConfigureObject = ConfigTester()
  override fun setUp() {
    super.setUp()
    manager.registerEmbeddedProfilesTestProvider()
    registerDynamicExternalInspectionsInTests(testConfigureObject, testRootDisposable)
  }

  @Test
  fun `import default settings`() {
    assertEquals(false, testConfigureObject.configured)
    assertEquals(false, testConfigureObject.deconfigured)
    runAnalysis()
    assertSarifEnabledRules("inspection-1", "inspection-2", "inspection-3")
    val allRules = qodanaRunner().sarifRun.tool.extensions.flatMap { it.rules }
    val rulesToLevels = allRules.associate { it.id to it.defaultConfiguration.level }
    assertEquals(Level.WARNING, rulesToLevels["inspection-1"])
    assertEquals(Level.WARNING, rulesToLevels["inspection-3"])
    assertEquals(true, testConfigureObject.configured)
    assertEquals(true, testConfigureObject.deconfigured)
  }

  @Test
  fun `import custom settings`() {
    runAnalysis()
    assertSarifEnabledRules("inspection-1", "inspection-3")
    val allRules = qodanaRunner().sarifRun.tool.extensions.flatMap { it.rules }
    val rulesToLevels = allRules.associate { it.id to it.defaultConfiguration.level }
    assertEquals(Level.WARNING, rulesToLevels["inspection-1"])
    assertEquals(Level.ERROR, rulesToLevels["inspection-3"])
  }

  @Test
  fun `embedded profile`() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.starter")
      )
    }
    runAnalysis()
    assertSarifEnabledRules("inspection-1", "inspection-3")
    val allRules = qodanaRunner().sarifRun.tool.extensions.flatMap { it.rules }
    val rulesToLevels = allRules.associate { it.id to it.defaultConfiguration.level }
    assertEquals(Level.WARNING, rulesToLevels["inspection-1"])
    assertEquals(Level.ERROR, rulesToLevels["inspection-3"])
  }

  @Test
  fun `project profile`() {
    copyProjectProfiles()
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.project")
      )
    }
    runAnalysis()
    assertSarifEnabledRules("inspection-1", "inspection-3")
  }

  @Test
  fun `application profile`() {
    runWithIdeaApplicationProfiles {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig(name = "qodana.application")
        )
      }
      runAnalysis()
      assertSarifEnabledRules("inspection-1", "inspection-3")
    }
  }

  @Test
  fun `single tool`() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:inspection-1")
      )
    }
    runAnalysis()
    assertSarifEnabledRules("inspection-1")
  }

  @Test
  fun `empty profile`() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "empty")
      )
    }
    runAnalysis()
    assertSarifEnabledRules()
  }

  @Test
  fun `default profile`() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "Default")
      )
    }
    runAnalysis()
    assertContainsElements(getEnabledRules(), "inspection-1", "inspection-2", "inspection-3")
  }

  @Test
  fun `profile locked with single inspection`() {
    runAnalysis()
    assertContainsElements(getEnabledRules(), "inspection-1")
  }

  @Test
  fun `profile not locked`() {
    runAnalysis()
    assertContainsElements(getEnabledRules(), "inspection-1", "inspection-2", "inspection-3")
  }

  @Test
  fun `profile manager test`() {
    runWithIdeaApplicationProfiles {
      copyProjectProfiles()
      assertDoesNotThrow { loadProfileByName("Default") }
      assertDoesNotThrow { loadProfileByName("Project Default") }

      assertDoesNotThrow { loadProfileByName("qodana.application") }
      assertDoesNotThrow { loadProfileByName("qodana.project") }

      assertThrows(QodanaException::class.java) { loadProfileByName("default") }
      assertThrows(QodanaException::class.java) { loadProfileByName("ProjectDefault") }

      assertDoesNotThrow { loadProfileByName("qodana.sanity") }
      assertDoesNotThrow { loadProfileByName("qodana.recommended") }
      assertDoesNotThrow { loadProfileByName("qodana.starter") }
      assertDoesNotThrow { loadProfileByName("qodana.starter.old") }
      assertDoesNotThrow { loadProfileByName("qodana.recommended.old") }
    }
  }

  @Test
  fun `config applied`() {
    updateQodanaConfig {
      it.copy(
        exclude = listOf(
          InspectScope("inspection-1", listOf("test-module/C.java", "test-module/D.java")),
          InspectScope("inspection-3", listOf("test-module/B.java", "test-module/C.java"))
        ),
      )
    }
    runAnalysis()
    assertSarifEnabledRules("inspection-1", "inspection-2", "inspection-3")
    // 1 - B, 2 - B, C, D, 3 - D
    assertSarifResults()
  }

  @Test
  fun `external tool in yaml`() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(path = getTestDataPath("inspection-profile.yaml").absolutePathString())
      )
    }

    runAnalysis()
    assertSarifEnabledRules("inspection-1", "inspection-3")
  }

  private fun loadProfileByName(name: String): LoadedProfile {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = name)
      )
    }
    return loadInspectionProfile()
  }

  private fun getEnabledRules(): List<String> {
    val allRules = qodanaRunner().sarifRun.tool.extensions.flatMap { it.rules }
    return allRules.filter { it.defaultConfiguration.enabled }.map { it.id }
  }

  private fun copyProjectProfiles() {
    val dir = Path.of(javaClass.simpleName, PROFILE_DIR)
    val sourceDirectory = testData.resolve(dir).toFile()
    val targetDir = qodanaConfig.projectPath.resolve(".$PROFILE_DIR").toFile()
    FileUtil.createDirectory(targetDir)
    FileUtil.copyDir(sourceDirectory, targetDir, null)
  }

  private fun runWithIdeaApplicationProfiles(action: () -> Unit) {
    val configPath = copyConfig()
    try {
      action()
    }
    finally {
      removeConfig(configPath)
    }
  }

  private fun copyConfig(): String {
    val dir = Path.of(javaClass.simpleName, "config")
    val sourceDirectory = testData.resolve(dir).toFile()
    return PathManager.getConfigPath().also {
      FileUtil.copyDir(sourceDirectory, File(it), null)
    }
  }

  private fun removeConfig(dir: String) {
    FileUtil.delete(Path.of(dir, InspectionProfileManager.INSPECTION_DIR))
  }
}
