package org.jetbrains.qodana.dart

import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.util.io.NioFiles
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.HeavyTestHelper
import com.intellij.codeInspection.lang.InspectionExtensionsFactory
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import com.jetbrains.qodana.sarif.model.Level
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileYamlConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsProvider
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

@TestDataPath($$"$CONTENT_ROOT/../test-data/DartAnalyzeGlobalInspectionQodanaTest")
class DartAnalyzeGlobalInspectionQodanaTest : QodanaRunnerTestCase() {
  private lateinit var analyzeResponses: MutableMap<String, RecordedDartAnalyzeResponse>
  private lateinit var recordedExecutions: MutableList<Pair<String, String>>

  override val testData: Path = Path.of(PluginPathManager.getPluginHomePath("qodana"), "dart", "test-data")
  override val testDataBasePath: Path = Path.of(javaClass.simpleName)

  override fun setUp() {
    super.setUp()

    ExternalToolsProvider.EXTERNAL_TOOLS_EP.point.registerExtension(
      DartAnalyzeInspectionDescriptorProvider(),
      testRootDisposable,
    )
    DartAnalyzeTestRunnerFactoryHolder.factory = createRunnerFactory()
    registerGlobalTool(DartAnalyzeTestGlobalInspection(), DartAnalyzeGlobalInspectionPresentation::class.java.name)
    GlobalOutputConsumer.EP_NAME.point.registerExtension(DartAnalyzeGlobalInspectionOutputConsumer(), testRootDisposable)
    InspectionExtensionsFactory.EP_NAME.point.registerExtension(
      DartAnalyzeGlobalInspectionContextFactory(),
      testRootDisposable,
    )
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
  }

  override fun createTestStructure() {
    val projectRoot = Path.of(project.basePath!!)
    Files.createDirectories(projectRoot)
    val sourceRoot = testData.resolve(testDataBasePath).resolve("sources")
    HeavyTestHelper.createTestProjectStructure(myModule, sourceRoot.toString(), projectRoot, true)
    createDartModule("test-module")
    PsiDocumentManager.getInstance(myProject).commitAllDocuments()
  }

  @Test
  fun `ignores Dart files in repository root without pubspec`() {
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named("qodana.starter")) }
    runAnalysis()

    assertEmptySarifResults()
  }

  @Test
  fun `reports diagnostics from a Dart file`() {
    val module = createDartModule("test-module")
    enableCoreLint(module)

    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile()) }
    runAnalysis()

    val result = manager.sarifRun.results.single()
    assertEquals(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, result.ruleId)
    assertEquals(Level.ERROR, result.level)
    assertTrue(result.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 1) != null)
    assertTrue(result.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 2) != null)
    assertEquals(
      "[undefined_identifier] Undefined name 'missing'. Try correcting the name to one that is defined, or defining the name.",
      result.message.text,
    )
    assertEquals(
      listOf("test-module/lib/main.dart"),
      listOf(result.locations.single().physicalLocation.artifactLocation.uri),
    )
    assertEquals(1, result.locations.single().physicalLocation.region.startLine)
    assertEquals(15, result.locations.single().physicalLocation.region.startColumn)
  }

  @Test
  fun `keeps fingerprints stable on retrigger and separates inspections`() {
    val module = createDartModule("test-module")
    enableCoreLint(module)
    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile()) }
    runAnalysis()
    val initialResult = manager.sarifRun.results.single()
    assertEquals(
      listOf(
        DART_ANALYZE_UNCATEGORIZED_SHORT_NAME,
        Level.ERROR,
        "[undefined_identifier] Undefined name 'missing'. Try correcting the name to one that is defined, or defining the name.",
        "test-module/lib/main.dart",
        1,
        15,
        7,
      ),
      resultSignature(initialResult),
    )
    val initialFingerprints = fingerprints(initialResult)

    runAnalysis()
    val retriggeredResult = manager.sarifRun.results.single()
    assertEquals(resultSignature(initialResult), resultSignature(retriggeredResult))
    assertEquals(initialFingerprints, fingerprints(retriggeredResult))

    Files.writeString(module.resolve("lib/main.dart"), "Future<void> main() async { await 1; }")
    LocalFileSystem.getInstance().refreshAndFindFileByNioFile(module.resolve("lib/main.dart"))
    analyzeResponses["test-module"] = readDartAnalyzeTestData("single-categorized.json")
    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile().copy(
          inspections = listOf(QodanaProfileYamlConfig.InspectionConfig("DartAnalyze.await_only_futures", enabled = true)),
        ),
      )
    }
    runAnalysis()

    val differentInspectionResult = manager.sarifRun.results.single()
    assertEquals(
      listOf(
        "DartAnalyze.await_only_futures",
        Level.WARNING,
        "[await_only_futures] Uses 'await' on an instance of 'int', which is not a subtype of 'Future'. Try removing the 'await' or changing the expression.",
        "test-module/lib/main.dart",
        1,
        1,
        1,
      ),
      resultSignature(differentInspectionResult),
    )
    val differentInspectionFingerprints = fingerprints(differentInspectionResult)
    assertNotEquals(initialFingerprints.first, differentInspectionFingerprints.first)
    assertNotEquals(initialFingerprints.second, differentInspectionFingerprints.second)
  }

  @Test
  fun `does not report diagnostics from files excluded by Qodana`() {
    val module = createDartModule("test-module")
    enableCoreLint(module)

    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile(),
        exclude = listOf(InspectScope(name = "All", paths = listOf("test-module/lib/main.dart"))),
      )
    }
    runAnalysis()

    assertEmptySarifResults()
  }

  @Test
  fun `reports diagnostics only from files included by Qodana`() {
    createModuleWithTwoDiagnostics()

    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME"),
        include = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib/included.dart"))),
      )
    }
    runAnalysis()

    assertEquals(
      listOf("test-module/lib/included.dart"),
      manager.sarifRun.results.map { it.locations.single().physicalLocation.artifactLocation.uri },
    )
  }

  @Test
  fun `includes an inspection everywhere when no paths are configured`() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME"),
        include = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME)),
      )
    }
    runAnalysis()

    assertEquals(listOf("test-module/lib/main.dart"), sarifResultPaths())
  }

  @Test
  fun `keeps an enabled inspection active outside included paths`() {
    createModuleWithTwoDiagnostics()

    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile(),
        include = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib/included.dart"))),
      )
    }
    runAnalysis()

    assertEquals(listOf("test-module/lib/included.dart", "test-module/lib/main.dart"), sarifResultPaths())
  }

  @Test
  fun `excludes an inspection only from configured files`() {
    createModuleWithTwoDiagnostics()

    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile(),
        exclude = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib/main.dart"))),
      )
    }
    runAnalysis()

    assertEquals(listOf("test-module/lib/included.dart"), sarifResultPaths())
  }

  @Test
  fun `excludes an inspection everywhere when no paths are configured`() {
    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile(),
        exclude = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME)),
      )
    }
    runAnalysis()

    assertEmptySarifResults()
  }

  @Test
  fun `include wins when include and exclude overlap`() {
    createModuleWithTwoDiagnostics()

    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME"),
        include = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib/included.dart"))),
        exclude = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib/included.dart"))),
      )
    }
    runAnalysis()

    assertEquals(listOf("test-module/lib/included.dart"), sarifResultPaths())
  }

  private fun enableCoreLint(module: Path) {
    Files.writeString(
      module.resolve("analysis_options.yaml"),
      """
      linter:
        rules:
          await_only_futures: true
      """.trimIndent(),
    )
  }

  @Test
  fun `reports diagnostics from all modules under the project root`() {
    copyMiniMonorepoIntoProject()
    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile()) }
    runAnalysis()

    val results = manager.sarifRun.results
    assertEquals(
      listOf(
        "apps/app/lib/main.dart",
        "packages/core/lib/main.dart",
        "packages/core/nested_flutter_app/lib/main.dart",
        "packages/flutter_app/lib/main.dart",
      ).sorted(),
      results.map { it.locations.single().physicalLocation.artifactLocation.uri }.sorted(),
    )
    assertTrue(results.all { it.ruleId == DART_ANALYZE_UNCATEGORIZED_SHORT_NAME })
    assertTrue(results.all { it.level == Level.ERROR })
  }

  @Test
  fun `does not reduce analyzed modules when an inspection is excluded from a file`() {
    copyMiniMonorepoIntoProject()
    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile(),
        exclude = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("packages/core/lib/main.dart"))),
      )
    }
    runAnalysis()

    assertEquals(
      listOf(
        "apps/app/lib/main.dart",
        "packages/core/nested_flutter_app/lib/main.dart",
        "packages/flutter_app/lib/main.dart",
      ),
      sarifResultPaths(),
    )
    assertRecordedAnalyzedModules(
      "apps/app",
      "packages/core",
      "packages/core/nested_flutter_app",
      "packages/flutter_app",
    )
  }

  @Test
  fun `analyzes modules intersecting only directory scopes`() {
    copyMiniMonorepoIntoProject()
    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile(), onlyDirectory = Path.of("packages/core")) }
    runAnalysis()

    assertEquals(
      listOf("packages/core/lib/main.dart", "packages/core/nested_flutter_app/lib/main.dart"),
      sarifResultPaths(),
    )
    assertRecordedAnalyzedModules("packages/core", "packages/core/nested_flutter_app")
  }

  @Test
  fun `analyzes a module when only directory selects its source directory or file`() {
    copyMiniMonorepoIntoProject()
    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile(), onlyDirectory = Path.of("packages/core/lib")) }
    runAnalysis()

    assertEquals(listOf("packages/core/lib/main.dart"), sarifResultPaths())
    assertRecordedAnalyzedModules("packages/core")

    recordedExecutions.clear()
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("packages/core/lib/main.dart")) }
    runAnalysis()

    assertEquals(listOf("packages/core/lib/main.dart"), sarifResultPaths())
    assertRecordedAnalyzedModules("packages/core")
  }

  @Test
  fun `does not analyze parent modules when only directory selects nested Dart or Flutter modules`() {
    createDartModule("test-module/example")
    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile(), onlyDirectory = Path.of("test-module/example/lib/main.dart")) }
    runAnalysis()

    assertEquals(listOf("test-module/example/lib/main.dart"), sarifResultPaths())
    assertRecordedAnalyzedModules("test-module/example")

    recordedExecutions.clear()
    copyMiniMonorepoIntoProject()
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("packages/core/nested_flutter_app")) }
    runAnalysis()

    assertEquals(listOf("packages/core/nested_flutter_app/lib/main.dart"), sarifResultPaths())
    assertRecordedAnalyzedModules("packages/core/nested_flutter_app")
  }

  @Test
  fun `deduplicates diagnostics reported by a parent and an example subproject`() {
    analyzeResponses["test-module"] = readDartAnalyzeTestData("dedup-root.json")
    Files.writeString(Path.of(project.basePath!!, "test-module/lib/main.dart"), "void main() {}")
    LocalFileSystem.getInstance().refreshAndFindFileByNioFile(Path.of(project.basePath!!, "test-module/lib/main.dart"))
    createDartModule("test-module/example")
    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile()) }
    runAnalysis()

    val results = manager.sarifRun.results
    assertEquals(1, results.size)
    assertEquals(
      setOf(
        "[undefined_identifier] Undefined name 'missing'. Try correcting the name to one that is defined, or defining the name.",
      ),
      results.map { it.message.text }.toSet(),
    )
    assertEquals(
      setOf("test-module/example/lib/main.dart"),
      results.map { it.locations.single().physicalLocation.artifactLocation.uri }.toSet(),
    )
    assertEquals(setOf(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME), results.map { it.ruleId }.toSet())
    assertEquals(Level.ERROR, results.single().level)
    assertEquals(1, results.single().locations.single().physicalLocation.region.startLine)
    assertEquals(15, results.single().locations.single().physicalLocation.region.startColumn)
  }

  @Test
  fun `ignores Dart modules under metadata directories`() {
    copyIgnoredModulesIntoProject()
    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile()) }
    runAnalysis()

    val result = manager.sarifRun.results.single()
    assertEquals(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, result.ruleId)
    assertEquals(Level.ERROR, result.level)
    assertEquals("test-module/lib/main.dart", result.locations.single().physicalLocation.artifactLocation.uri)
  }

  @Test
  fun `reports a pub get failure without a file location through SARIF`() {
    createDartModule("legacy-module", sdkConstraint = ">=2.7.0 <3.0.0")
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME")) }
    runAnalysis()

    val result = manager.sarifRun.results.single()
    assertEquals(DART_PROJECT_SETUP_SHORT_NAME, result.ruleId)
    assertEquals(Level.ERROR, result.level)
    assertTrue(result.message.text.contains("must be 2.12.0"))
    assertTrue(result.message.text.contains("legacy-module"))
    assertTrue(result.locations.all { it.physicalLocation == null })
  }

  @Test
  fun `reports setup failures for each failed module without file locations`() {
    createDartModule("legacy-module", sdkConstraint = ">=2.7.0 <3.0.0")
    createDartModule("legacy-module-2", sdkConstraint = ">=2.7.0 <3.0.0")
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME")) }
    runAnalysis()

    val results = manager.sarifRun.results.filter { it.ruleId == DART_PROJECT_SETUP_SHORT_NAME }
    assertEquals(2, results.size)
    assertEquals(setOf("legacy-module", "legacy-module-2"), results.map { result ->
      when {
        result.message.text.contains("legacy-module-2") -> "legacy-module-2"
        result.message.text.contains("legacy-module") -> "legacy-module"
        else -> error("Missing module path in '${result.message.text}'")
      }
    }.toSet())
    assertTrue(results.all { result -> result.locations.all { it.physicalLocation == null } })
  }

  private fun createModuleWithTwoDiagnostics() {
    val module = createDartModule("test-module")
    val includedFile = module.resolve("lib/included.dart")
    Files.writeString(includedFile, "void included() { missing; }")
    LocalFileSystem.getInstance().refreshAndFindFileByNioFile(includedFile)
    analyzeResponses["test-module"] = readDartAnalyzeTestData("two-uncategorized.json")
  }

  private fun sarifResultPaths(): List<String> =
    manager.sarifRun.results.map { it.locations.single().physicalLocation.artifactLocation.uri }.sorted()

  private fun assertRecordedAnalyzedModules(vararg modules: String) {
    if (recordedExecutions.isNotEmpty()) {
      assertEquals(modules.toList().sorted(), recordedExecutions.filter { it.first == "analyze" }.map { it.second }.sorted())
    }
  }

  private fun createDartModule(moduleName: String, isFlutter: Boolean = false, sdkConstraint: String = "^3.4.0"): Path {
    val moduleDirectory = Path.of(project.basePath!!, moduleName)
    Files.createDirectories(moduleDirectory)
    Files.writeString(
      moduleDirectory.resolve("pubspec.yaml"),
      if (isFlutter) {
        """
        name: flutter_module
        environment:
          sdk: '>=3.4.0 <4.0.0'
        dependencies:
          flutter:
            sdk: flutter
        """.trimIndent()
      }
      else {
        """
        name: dart_module
        environment:
          sdk: '$sdkConstraint'
        """.trimIndent()
      },
    )
    val file = moduleDirectory.resolve("lib/main.dart")
    Files.createDirectories(file.parent)
    Files.writeString(file, """void main() { missing; }""")
    LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file)
    LocalFileSystem.getInstance().refreshAndFindFileByNioFile(moduleDirectory.resolve("pubspec.yaml"))
    return moduleDirectory
  }

  private fun copyMiniMonorepoIntoProject() {
    val projectRoot = Path.of(project.basePath!!)
    Files.delete(projectRoot.resolve("test-module/pubspec.yaml"))
    val source = testData.resolve("dart-analyze/mini-monorepo")
    NioFiles.copyRecursively(source, projectRoot)
    Files.move(projectRoot.resolve("git"), projectRoot.resolve(".git"))
    findDartModuleDirectories(projectRoot).forEach { module ->
      LocalFileSystem.getInstance().refreshAndFindFileByNioFile(module.resolve("lib/main.dart"))
    }
  }

  private fun copyIgnoredModulesIntoProject() {
    val projectRoot = Path.of(project.basePath!!)
    val source = testData.resolve("dart-analyze/mini-monorepo")
    NioFiles.copyRecursively(source.resolve("git"), projectRoot.resolve(".git"))
    Files.createDirectories(projectRoot.resolve("apps/app"))
    NioFiles.copyRecursively(source.resolve("apps/app/.dart_tool"), projectRoot.resolve("apps/app/.dart_tool"))
  }

  @Test
  fun `reports an uncategorized diagnostic with its SARIF location`() {
    updateQodanaConfig { it.copy(profile = dartAnalyzeProfile()) }
    runAnalysis()

    val result = manager.sarifRun.results.single()
    assertEquals(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, result.ruleId)
    assertEquals(Level.ERROR, result.level)
    val location = result.locations.single().physicalLocation
    assertEquals("test-module/lib/main.dart", location.artifactLocation.uri)
    assertEquals(1, location.region.startLine)
    assertEquals(15, location.region.startColumn)
    assertEquals(
      "[undefined_identifier] Undefined name 'missing'. Try correcting the name to one that is defined, or defining the name.",
      result.message.text,
    )
  }

  @Test
  fun `does not report uncategorized diagnostics when the inspection is disabled`() {
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME")) }
    runAnalysis()

    assertEmptySarifResults()
  }

  @Test
  fun `reports uncategorized diagnostics when the inspection is enabled`() {
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named("qodana.single:DartAnalyzeUncategorized")) }
    runAnalysis()

    val result = manager.sarifRun.results.single { it.message.text.contains("[undefined_identifier]") }
    assertEquals(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, result.ruleId)
    assertTrue(result.message.text.contains("[undefined_identifier]"))
  }

  private fun createRunnerFactory(): DartAnalyzeRunnerFactory {
    recordedExecutions = mutableListOf()
    analyzeResponses = mutableMapOf(
      "test-module" to readDartAnalyzeTestData("single-uncategorized.json"),
      "apps/app" to readDartAnalyzeTestData("single-app-uncategorized.json"),
      "packages/core" to readDartAnalyzeTestData("single-core-uncategorized.json"),
      "packages/core/nested_flutter_app" to readDartAnalyzeTestData("single-nested-flutter-uncategorized.json"),
      "packages/flutter_app" to readDartAnalyzeTestData("single-flutter-uncategorized.json"),
    )
    analyzeResponses["test-module/example"] = readDartAnalyzeTestData("dedup-example.json")
    return DartAnalyzeTestRunnerFactory(
      Path.of(project.basePath!!),
      analyzeResponses,
      preparationFailures = mapOf(
        "legacy-module" to "The lower bound of sdk must be 2.12.0 or higher to enable null safety.",
        "legacy-module-2" to "The lower bound of sdk must be 2.12.0 or higher to enable null safety.",
      ),
      executions = recordedExecutions,
    ).create()
  }

  private fun dartAnalyzeProfile(): QodanaProfileConfig = QodanaProfileConfig.named("qodana.single:DartAnalyzeUncategorized")

  private fun fingerprints(result: Result): Pair<String, String> {
    val partialFingerprints = requireNotNull(result.partialFingerprints)
    return requireNotNull(partialFingerprints.get(BaselineCalculation.EQUAL_INDICATOR, 1)) to
           requireNotNull(partialFingerprints.get(BaselineCalculation.EQUAL_INDICATOR, 2))
  }

  private fun resultSignature(result: Result): List<Any?> {
    val location = result.locations.single().physicalLocation
    val region = location.region
    return listOf(result.ruleId, result.level, result.message.text, location.artifactLocation.uri,
                  region.startLine, region.startColumn, region.charLength)
  }
}
