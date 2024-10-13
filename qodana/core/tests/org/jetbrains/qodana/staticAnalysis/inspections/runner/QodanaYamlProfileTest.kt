package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.SourceFolder
import com.intellij.openapi.roots.TestSourcesFilter
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import com.intellij.testFramework.TestDataPath
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfile
import org.junit.Assert
import org.junit.Test
import kotlin.io.path.absolutePathString

/**
 * Tests of yaml profile functionality.
 */
@TestDataPath("\$CONTENT_ROOT/testData/QodanaYamlProfileTest")
class QodanaYamlProfileTest : QodanaRunnerTestCase() {
  @Test
  fun `testYaml profile`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile enable category`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile enable category with similar name`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile ignore`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile ignore 2`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile ignore 3`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile ignore 4`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile ignore 5`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile ignore with global inspection`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile ignore with global inspection 2`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile with include global inspection`(): Unit = runBlocking {
    runYamlTest()
  }

  @Test
  fun `testYaml profile with escape symbols in category`(): Unit = runBlocking {
    val tool = EscapeCategoryTool()
    registerTool(tool)
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
    runYamlTest()
    val rule = manager.qodanaRunner.sarifRun.tool.extensions.flatMap { it.rules }.find { it.id == tool.shortName }
    Assert.assertEquals("""Language/A_B/C\/test""", rule?.relationships?.first()?.target?.id)
  }

  @Test
  fun `testYaml profile enable category with escape symbols`(): Unit = runBlocking {
    val tool = EscapeCategoryTool()
    registerTool(tool)
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
    runYamlTest()
    val rule = manager.qodanaRunner.sarifRun.tool.extensions.flatMap { it.rules }.find { it.id == tool.shortName }
    Assert.assertEquals("""Language/A_B/C\/test""", rule?.relationships?.first()?.target?.id)
    Assert.assertTrue(rule?.defaultConfiguration?.enabled!!)
  }


  @Test
  fun `testInclude global inspection in qodana-yaml`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.starter"),
        include = listOf(InspectScope("unused"))
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testInclude inspection in qodana-yaml with path`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.starter"),
        include = listOf(InspectScope("UNUSED_IMPORT", paths = listOf("test-module/A.java")))
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testYaml profile ignore test`(): Unit = runBlocking {
    val testFolder = markTestsFolderAsTestSource()
    val testSource = testFolder.file?.findChild("B.java") ?: throw AssertionError()
    TestCase.assertTrue(TestSourcesFilter.isTestSources(testSource, project))

    runYamlTest()
  }

  @Test
  fun `testYaml profile ignore test 2`(): Unit = runBlocking {
    val testFolder = markTestsFolderAsTestSource()
    val testSource = testFolder.file?.findChild("B.java") ?: throw AssertionError()
    TestCase.assertTrue(TestSourcesFilter.isTestSources(testSource, project))

    runYamlTest()
  }

  @Test
  fun `testYaml profile with inherited ignoring test`(): Unit = runBlocking {
    val testFolder = markTestsFolderAsTestSource()
    val testSource = testFolder.file?.findChild("B.java") ?: throw AssertionError()
    TestCase.assertTrue(TestSourcesFilter.isTestSources(testSource, project))

    runYamlTest()
  }

  @Test
  fun `testYaml profile with inherited ignoring test global inspection`(): Unit = runBlocking {
    val testFolder = markTestsFolderAsTestSource()
    val testSource = testFolder.file?.findChild("B.java") ?: throw AssertionError()
    TestCase.assertTrue(TestSourcesFilter.isTestSources(testSource, project))

    runYamlTest()
  }

  @Test
  fun `testQodana starter default exclusion`(): Unit = runBlocking {
    markTestsFolderAsTestSource()

    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.starter")
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testQodana recommended default exclusion`(): Unit = runBlocking {
    markTestsFolderAsTestSource()

    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.recommended")
      )
    }

    runAnalysis()
    assertSarifResults()
  }


  @Test
  fun `testYaml profile with old ignores`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        exclude = listOf(InspectScope("ConstantValue")),
      )
    }

    runYamlTest()
  }

  @Test
  fun `testYaml profile with old include`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        include = listOf(InspectScope("ConstantValue"))
      )
    }

    runYamlTest()
  }

  @Test
  fun `testOverride yaml scope with old style`(): Unit = runBlocking {
    markTestsFolderAsTestSource()

    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = QodanaEmbeddedProfile.QODANA_RECOMMENDED.profileName),
        include = listOf(InspectScope("ConstantValue", paths = listOf("test-module/tests"))),
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  private suspend fun markTestsFolderAsTestSource(): SourceFolder {
    val rootManager = ModuleRootManager.getInstance(module).modifiableModel
    val contentRoot = rootManager.contentEntries[0]
    val testFolder = contentRoot.addSourceFolder(contentRoot.url + "/tests", true)
    writeAction { rootManager.commit() }
    return testFolder
  }

  private fun runYamlTest(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(path = getTestDataPath("inspection-profile.yaml").absolutePathString())
      )
    }

    runAnalysis()
    assertSarifResults()
  }
}

class EscapeCategoryTool : LocalInspectionTool() {
  override fun getShortName(): String {
    return "EscapeCategoryTool"
  }

  override fun getGroupDisplayName(): String {
    return "test"
  }

  override fun getGroupPath(): Array<String> {
    return arrayOf("Language", "A/B", """C\""")
  }
}
