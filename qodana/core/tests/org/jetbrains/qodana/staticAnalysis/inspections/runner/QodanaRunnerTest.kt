package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiMethod
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.replaceService
import com.intellij.util.application
import com.jetbrains.qodana.sarif.model.*
import com.jetbrains.qodana.sarif.model.Result.BaselineState
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProvider
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProviderTestImpl
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.inspectionKts.FORCE_DISABLE_INSPECTION_KTS
import org.jetbrains.qodana.respond200PublishReport
import org.jetbrains.qodana.staticAnalysis.QodanaEnvEmpty
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase.Companion.runTest
import org.jetbrains.qodana.staticAnalysis.addQodanaEnvMock
import org.jetbrains.qodana.staticAnalysis.inspections.config.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.markGenFolderAsGeneratedSources
import org.jetbrains.qodana.staticAnalysis.profile.QODANA_PROMO_ANALYZE_EACH_N_FILE_KEY
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.staticAnalysis.sarif.configProfile
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.TEAMCITY_CHANGES_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.scoped.COVERAGE_SKIP_COMPUTATION_PROPERTY
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_BASELINE_PROPERTY
import org.jetbrains.qodana.staticAnalysis.stat.InspectionDurationsAggregatorService
import org.jetbrains.qodana.staticAnalysis.withSystemProperty
import org.junit.Ignore
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertLinesMatch
import org.junit.jupiter.api.assertThrows
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * See [QodanaRunnerTestCase] for basic usage.
 *
 * To regenerate the baseline tests, see `regenerate-baseline-tests.sh`.
 */
@TestDataPath("\$CONTENT_ROOT/testData/QodanaRunnerTest")
class QodanaRunnerTest : QodanaRunnerTestCase() {

  override fun setUp() {
    super.setUp()
    application.replaceService(IjQDCloudClientProvider::class.java, IjQDCloudClientProviderTestImpl(), testRootDisposable)
    manager.registerEmbeddedProfilesTestProvider()
  }

  @Test
  fun `testInspection IgnoreResultOfCall`(): Unit = runBlocking {

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(0)
  }

  @Test
  fun `testEmpty profile`(): Unit = runBlocking {

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules(/* none */)
    assertSarifExitCode(0)
  }

  @Test
  fun testStopThreshold(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        stopThreshold = 1,
      )
    }

    assertThrows<ProcessCanceledException> {
      runAnalysis()
    }

    assertNoSarifResults()
    assertSarifExitCode(1)
  }

  @Test
  fun `testFailThreshold surpassed`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(failureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = 1)))
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("ConstantValue", "IgnoreResultOfCall")
    assertSarifExitCode(255)
  }

  @Test
  fun `testBaseline, results unchanged`(): Unit = runBlocking {
    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(0)
  }

  /**
   * The baseline run reported 2 problems.
   * Since then, all problems have been fixed in the code.
   * Since `includeAbsent` is false, the fixed problems are not reported.
   * This leaves the final report completely empty.
   */
  @Test
  fun `testBaseline, all results absent`(): Unit = runBlocking {
    assertEquals(false, qodanaConfig.includeAbsent)

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(0)
  }

  @Test
  fun `testBaseline, new results`(): Unit = runBlocking {

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall", "ConstantValue")
    assertSarifExitCode(0)
  }

  @Test
  fun `testBaseline, SARIF only`(): Unit = runBlocking {
    // Since 2022-07-06, the default output format is "SARIF + project structure",
    // which makes setting the system property redundant.
    assertEquals(OutputFormat.SARIF_AND_PROJECT_STRUCTURE, qodanaConfig.outputFormat)

    withSystemProperty(QODANA_FORMAT, "SARIF_AND_PROJECT_STRUCTURE") {
      assertEquals(OutputFormat.SARIF_AND_PROJECT_STRUCTURE, qodanaConfig.outputFormat)

      runAnalysis()

      assertSarifResults()
      assertSarifEnabledRules("IgnoreResultOfCall", "ConstantValue")
      assertFalse(qodanaConfig.resultsStorage.exists())
      assertInspectionFilesInOutPath(/* none */)
    }
  }

  /** Compared to the baseline, 1 problem has been fixed in the code. */
  @Test
  fun `testBaseline, one result removed`(): Unit = runBlocking {
    assertEquals(qodanaConfig.includeAbsent, false)

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(0)
  }

  @Test
  fun `inspection names from baseline`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        includeAbsent = true,
      )
    }
    val expected =
      """

        Qodana - Baseline summary
        Analysis results: 1 problem detected
        Grouping problems according to baseline: UNCHANGED: 0, NEW: 0, ABSENT: 1
        --------------------------------------------------------------------------------------
        Name                                                Baseline  Severity  Problems count
        --------------------------------------------------------------------------------------
        Not existing inspection                               ABSENT  Critical               1
        --------------------------------------------------------------------------------------
  
  
      """.trimIndent()
    val commandLinePrinter = CommandLineResultsPrinter(
      inspectionIdToName = QodanaRunner.getInspectionIdToNameMap(mapOf(), qodanaConfig),
      cliPrinter = { assertLinesMatch(it.lines(), expected.lines())  }
    )

    val results = listOf(
      result("NotExistingId", QodanaSeverity.CRITICAL, BaselineState.ABSENT)
    )

    commandLinePrinter.printResultsWithBaselineState(results, true)
  }

  private fun result(
    inspectionId: String,
    severity: QodanaSeverity,
    baselineState: BaselineState? = null
  ): Result {
    return Result().apply {
      this.baselineState = baselineState
      this.ruleId = inspectionId
      this.properties = PropertyBag().apply {
        this["qodanaSeverity"] = severity.toString()
      }
    }
  }

  @Test
  fun `testBaseline, includeAbsent`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        includeAbsent = true,
      )
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(0)
  }

  @Test
  fun `testOld baseline, includeAbsent`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        includeAbsent = true,
      )
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(0)
  }

  /** Ensure that the duration of inspections is aggregated by inspection. */
  @Test
  fun testAggregation(): Unit = runBlocking {

    runAnalysis()

    assertSarifResults()
    val aggregator = project.getService(InspectionDurationsAggregatorService::class.java)
    assertEquals("files: 1, problems: 2", aggregator.getSummary("CanBeFinal"))
  }

  @Test
  fun `testScoped-script`() {
    val scope = qodanaConfig.projectPath.resolve("scope")

    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to scope.toString())),
        profile = QodanaProfileConfig(name = "qodana.single:ConstantValue"),
      )
    }

    scope.writeText("""
      {
        "files" : [ {
          "path" : "test-module/A.java",
          "added" : [ ],
          "deleted" : [ ]
        },
        {
          "path" : "test-module/C.java",
          "added" : [ ],
          "deleted" : [ ]
        } ]
      }
    """.trimIndent())

    try {
      System.setProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY, "true")
      runAnalysis()
      assertSarifResults()
    } finally {
      System.clearProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY)
    }
  }

  @Test
  fun `testScoped-script-with-baseline`() {
    val scope = qodanaConfig.projectPath.resolve("scope")

    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to scope.toString())),
        profile = QodanaProfileConfig(name = "qodana.single:ConstantValue"),
        baseline = "test-module/baseline.sarif.json",
        includeAbsent = true
      )
    }

    scope.writeText("""
      {
        "files" : [ {
          "path" : "test-module/A.java",
          "added" : [ ],
          "deleted" : [ ]
        },
        {
          "path" : "test-module/C.java",
          "added" : [ ],
          "deleted" : [ ]
        } ]
      }
    """.trimIndent())

    try {
      System.setProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY, "true")
      runAnalysis()
      assertSarifResults()
    } finally {
      System.clearProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY)
    }
  }

  @Test
  fun `testScoped-script-second-stage`() {
    val scope = qodanaConfig.projectPath.resolve("scope")

    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to scope.toString())),
        profile = QodanaProfileConfig(name = "qodana.single:ConstantValue"),
        includeAbsent = true
      )
    }

    scope.writeText("""
      {
        "files" : [ {
          "path" : "test-module/A.java",
          "added" : [ ],
          "deleted" : [ ]
        },
        {
          "path" : "test-module/C.java",
          "added" : [ ],
          "deleted" : [ ]
        } ]
      }
    """.trimIndent())

    try {
      System.setProperty(SCOPED_BASELINE_PROPERTY, "test-module/baseline.sarif.json")
      System.setProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY, "true")
      runAnalysis()
      assertSarifResults()
    } finally {
      System.clearProperty(SCOPED_BASELINE_PROPERTY)
      System.clearProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY)
    }
  }

  @Test
  fun `testTeamcity-changes`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(TEAMCITY_CHANGES_SCRIPT_NAME),
        profile = QodanaProfileConfig(name = "qodana.single:ConstantValue"),
      )
    }

    qodanaConfig.projectPath.resolve("teamcity-changes.txt").writeText("""
      test-module/A.java:CHANGED:hash
      test-module/C.java:REMOVED:hash
    """.trimIndent())

    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testTeamcity-changes-with-baseline`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(TEAMCITY_CHANGES_SCRIPT_NAME),
        profile = QodanaProfileConfig(name = "qodana.single:ConstantValue"),
        baseline = "test-module/baseline.sarif.json",
        includeAbsent = true
      )
    }

    qodanaConfig.projectPath.resolve("teamcity-changes.txt").writeText("""
      test-module/A.java:CHANGED:hash
      test-module/C.java:REMOVED:hash
    """.trimIndent())

    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testTeamcity-changes-with-baseline-2`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(TEAMCITY_CHANGES_SCRIPT_NAME),
        profile = QodanaProfileConfig(name = "qodana.single:ConstantValue"),
        baseline = "test-module/baseline.sarif.json",
        includeAbsent = true
      )
    }

    qodanaConfig.projectPath.resolve("teamcity-changes.txt").writeText("""
      test-module/A.java:CHANGED:hash
      test-module/C.java:REMOVED:hash
      test-module/D.java:CHANGED:hash
    """.trimIndent())

    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testTeamcity-changes, custom location`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(TEAMCITY_CHANGES_SCRIPT_NAME, mapOf("path" to "test-module/teamcity-changes.txt")),
        profile = QodanaProfileConfig(name = "qodana.single:ConstantValue"),
      )
    }

    runAnalysis()

    assertSarifResults()
  }

  @Test
  fun `testBaseline, failThreshold reached`(): Unit = runBlocking {
    updateQodanaConfig {
      assertEquals(false, it.includeAbsent)
      it.copy(failureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = 1)))
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(0) // 1 new problem, the threshold is 1
  }

  @Test
  fun `testBaseline, failThreshold surpassed`(): Unit = runBlocking {
    updateQodanaConfig {
      assertEquals(false, it.includeAbsent)
      it.copy(failureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = 1)))
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(255) // 2 new problems, the threshold is 1
  }

  @Test
  fun `testBaseline, includeAbsent, failThreshold reached`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        failureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = 1)),
        includeAbsent = true,
      )
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(0) // 1 absent and 2 new problems, the threshold is thus reached
  }

  @Test
  fun `testBaseline, includeAbsent, failThreshold surpassed`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        failureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = 1)),
        includeAbsent = true,
      )
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertSarifExitCode(255) // 1 absent and 2 new problems, the threshold is thus surpassed
  }

  @Test
  fun `testInclude inspection everywhere`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        include = listOf(InspectScope("ConstantValue")),
      )
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("ConstantValue", "IgnoreResultOfCall")
    assertSarifExitCode(0)
  }

  @Test
  fun `testExclude inspection everywhere`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        exclude = listOf(InspectScope("ConstantValue")),
      )
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
  }

  @Test
  fun `testInclude inspection for single file`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        include = listOf(InspectScope("ConstantValue", paths = listOf("test-module/B.java"))),
      )
    }

    runAnalysis()

    assertSarifResults()
    assertSarifSummary(
      // The ConstantCondition is only reported for B.java, not for A.java.
      "test-module/A.java:7:5: Result of 'A.unusedResult()' is ignored",
      "test-module/A.java:8:5: Result of 'A.unusedResult()' is ignored",
      "test-module/B.java:4:9: Condition '1 == 1' is always 'true'",
      "test-module/B.java:7:5: Result of 'B.unusedResult()' is ignored",
      "test-module/B.java:8:5: Result of 'B.unusedResult()' is ignored",
    )
    assertSarifEnabledRules("ConstantValue", "IgnoreResultOfCall")
    assertSarifExitCode(0)
  }

  @Test
  fun `testExclude inspection for single file`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(exclude = listOf(InspectScope("IgnoreResultOfCall", paths = listOf("test-module/B.java"))))
    }

    runAnalysis()

    assertSarifResults()
    assertSarifSummary(
      "test-module/A.java:4:9: Condition '1 == 1' is always 'true'",
      "test-module/A.java:7:5: Result of 'A.unusedResult()' is ignored",
      "test-module/A.java:8:5: Result of 'A.unusedResult()' is ignored",
      "test-module/B.java:4:9: Condition '1 == 1' is always 'true'",
      // No IgnoreResultOfCall for B.java.
    )
    assertSarifEnabledRules("ConstantValue", "IgnoreResultOfCall")
    assertSarifExitCode(0)
  }

  @Test
  fun `testInspect_sh output format`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        exclude = listOf(InspectScope("ConstantValue")),
        resultsStorage = outputBasePath,
        outputFormat = OutputFormat.INSPECT_SH_FORMAT
      )
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
  }

  @Test
  fun `testDefault output format`(): Unit = runBlocking {
    updateQodanaConfig {
      assertEquals(OutputFormat.SARIF_AND_PROJECT_STRUCTURE, it.outputFormat)
      it.copy(
        exclude = listOf(InspectScope("ConstantValue")),
      )
    }

    runAnalysis()

    assertSarifResults()
    assertSarifEnabledRules("IgnoreResultOfCall")
    assertInspectionFilesInOutPath(/* none */)
  }

  @Test
  @Ignore("Memory leak due to keeping ref from Registrar to SSbasedInspection")
  fun `testSingle SSbasedInspection`(): Unit = runBlocking {
    runAnalysis()

    assertSarifResults()
  }

  @Test
  fun `profile name reporting into sarif`(): Unit = runTest {
    val testData = listOf(
      Pair("empty", "empty"),
      Pair("qodana.starter", "starter"),
      Pair("qodana.recommended", "recommended"),
      Pair("qodana.single:ConstantValue", "single"),
      Pair("", "absent"),
      Pair("foobar", "other"),
    )
    for ((profileName, expectedName) in testData) {
      val profile = LoadedProfile(
        QodanaInspectionProfile.newWithEnabledByDefaultTools(profileName, QodanaInspectionProfileManager.getInstance(project)),
        profileName,
        "")
      assertEquals(
        expectedName,
        configProfile(profile).second
      )
    }
  }

  @Test
  fun `profile path reporting into sarif`(): Unit = runTest {
    val testData = listOf(
      Pair("", "absent"),
      Pair("/foo/bar", "path"),
    )
    for ((profilePath, expectedName) in testData) {
      val inspectionProfile = QodanaInspectionProfile.newWithEnabledByDefaultTools("", QodanaInspectionProfileManager.getInstance(project))
      val profile = LoadedProfile(inspectionProfile, "", profilePath)
      assertEquals(
        expectedName,
        configProfile(profile).second
      )
    }
  }

  @Test
  fun `testEmbedded problem`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:CssInvalidHtmlTagReference"),
      )
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testSanity same inspections in main and sanity profile`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        disableSanityInspections = false
      )
    }

    runAnalysis()

    assertSarifResults()
  }

  @Test
  fun `testSanity one inspection is present only in sanity profile`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        disableSanityInspections = false
      )
    }

    runAnalysis()

    assertSarifResults()
  }

  @Test
  fun `testSanity should not trigger failOnError`() {
    updateQodanaConfig {
      it.copy(
        disableSanityInspections = false,
        failOnErrorNotification = true,
        moduleSuspendThreshold = 2
      )
    }

    runAnalysis()

    val run = qodanaRunner().sarifRun
    assertEquals(ExitStatus(0, null, true), run.firstExitStatus)
    val notifications = run.invocations.orEmpty()
      .flatMap { it.toolExecutionNotifications.orEmpty() }
    assertEquals(1, notifications.size)
  }

  @Test
  fun `testSanity no notification if sanity threshold not reached`() {
    updateQodanaConfig {
      it.copy(
        disableSanityInspections = false,
        failOnErrorNotification = true
      )
    }

    runAnalysis()

    val notifications = qodanaRunner().sarifRun.invocations.orEmpty()
      .flatMap { it.toolExecutionNotifications.orEmpty() }
    assertEquals(0, notifications.size)
  }

  @Test
  fun `testPromo same inspections in main and promo profile`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        runPromoInspections = true,
      )
    }

    runAnalysis()

    assertSarifResults()
  }

  @Test
  fun `testPromo one inspection is present only in promo profile`(): Unit = runBlocking {
    val messageReporter = object : QodanaMessageReporter by QodanaMessageReporter.EMPTY {
      var nameFound = false
      var idFound = false

      override fun reportMessage(minVerboseLevel: Int, message: String?) = when {
        message == null -> Unit
        "Result of method call ignored" in message -> nameFound = true
        "IgnoreResultOfCall" in message -> idFound = true
        else -> Unit
      }
    }
    updateQodanaConfig {
      it.copy(
        runPromoInspections = true,
      )
    }

    PlatformTestUtil.withSystemProperty<Nothing>(QODANA_PROMO_ANALYZE_EACH_N_FILE_KEY, "1") {
      manager.runAnalysis(project, messageReporter)
    }

    assertSarifResults()
    Assertions.assertTrue(messageReporter.nameFound, "Inspection Name should have been printed")
    Assertions.assertFalse(messageReporter.idFound, "Inspection ID should NOT have been printed")
  }

  @Test
  fun `testExclude folders in project model`(): Unit = runBlocking {
    val rootManager = ModuleRootManager.getInstance(module).modifiableModel
    val contentRoot = rootManager.contentEntries[0]
    contentRoot.addExcludeFolder(contentRoot.url + "/dir")
    writeAction { rootManager.commit() }

    runAnalysis()

    assertSarifResults()
  }

  @Test
  fun testIncludeExcludeWithNonEmptyProfile(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        exclude = listOf(InspectScope("All")),
        include = listOf(InspectScope("ConstantValue", paths = listOf("test-module/A.java")))
      )
    }

    runAnalysis()

    assertSarifResults()
  }

  @Test
  fun `testSeverityThreshold reached`() {
    updateQodanaConfig {

      it.copy(failureConditions = FailureConditions(FailureConditions.SeverityThresholds(high = 3)))
    }

    runAnalysis()

    assertSarifResults()
    assertSarifExitCode(0)
  }

  @Test
  fun `testSeverityThreshold surpassed`() {
    updateQodanaConfig {
      it.copy(failureConditions = FailureConditions(FailureConditions.SeverityThresholds(high = 2)))
    }

    runAnalysis()

    assertSarifResults()
    assertSarifExitCode(255)
  }

  @Test
  fun `testDo not analyze generated code`(): Unit = runBlocking {
    markGenFolderAsGeneratedSources(module)

    runAnalysis()

    assertSarifResults()
  }

  @Test
  fun `testOpenInIde json generated after publishing`(): Unit = runBlocking {
    addQodanaEnvMock(testRootDisposable, object : QodanaEnvEmpty() {
      override val QODANA_ENDPOINT by value("https://host-url")
      override val QODANA_TOKEN by value("token")

      override val QODANA_BRANCH by value("branch")
      override val QODANA_REVISION by value("revision")
      override val QODANA_REMOTE_URL by value("https://remote-url")
      override val QODANA_REPO_URL by value("https://repo-url")
    })
    mockQDCloudHttpClient.apply {
      respond200PublishReport(
        host = "https://host-url",
        projectId = "TEST_PROJECT_ID",
        reportId = "TEST_REPORT_ID"
      )
      respond("projects") {
        @Language("JSON")
        val response = """
          {
            "id": "TEST_PROJECT_ID",
            "organizationId": "TEST_ORGANIZATION_ID",
            "name": "test-project"
          }
        """.trimIndent()
        qodanaCloudResponse {
          response
        }
      }
    }

    val projectApi = obtainQodanaCloudProjectApi()?.asSuccess()
    assertNotNull(projectApi)

    manager.setQodanaCloudProjectApi(projectApi)

    runAnalysis()

    assertSarifResults()
    assertOpenInIdeJson()
  }

  @Test
  fun `testOpenInIde json generated after publishing no endpoint`(): Unit = runBlocking {
    addQodanaEnvMock(testRootDisposable, object : QodanaEnvEmpty() {
      override val QODANA_TOKEN by value("token")

      override val QODANA_BRANCH by value("branch")
      override val QODANA_REVISION by value("revision")
      override val QODANA_REMOTE_URL by value("https://remote-url")
      override val QODANA_REPO_URL by value("https://repo-url")
    })
    mockQDCloudHttpClient.apply {
      respond200PublishReport(
        host = "https://qodana.cloud",
        projectId = "TEST_PROJECT_ID",
        reportId = "TEST_REPORT_ID"
      )
      respond("projects") {
        @Language("JSON")
        val response = """
          {
            "id": "TEST_PROJECT_ID",
            "organizationId": "TEST_ORGANIZATION_ID",
            "name": "test-project"
          }
        """.trimIndent()
        qodanaCloudResponse {
          response
        }
      }
    }

    val projectApi = obtainQodanaCloudProjectApi()?.asSuccess()
    assertNotNull(projectApi)

    manager.setQodanaCloudProjectApi(projectApi)

    runAnalysis()

    assertSarifResults()
    assertOpenInIdeJson()
  }

  // Why? - in case of empty range the editor artificially sets the range to 1, so we should do it too, see QD-8624
  @Test
  fun `testInspection with zero range not at file start has length in sarif`(): Unit = runBlocking {
    val tool = EmptyRangeNotAtStartTool()
    registerTool(tool)
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:${tool.shortName}"),
      )
    }

    runAnalysis()

    assertSarifResults()
  }

  // Why? - to register "whole file" problem one can either register problem with range == text.length or with a zero range at the start
  // Why the second option exists if the first one is available? Because sometimes PsiFile.range < text.length :c, so you can't rely on first one
  @Test
  fun `testInspection with zero range at file start no region in sarif`(): Unit = runBlocking {
    val tool = EmptyRangeAtStartTool()
    registerTool(tool)
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:${tool.shortName}"),
      )
    }

    runAnalysis()

    assertSarifResults()
  }

  // community linters
  @Test
  fun `testDisabled flexInspect doesnt hang`(): Unit = runBlocking {
    withSystemProperty(FORCE_DISABLE_INSPECTION_KTS, "true") {
      runAnalysis()
    }
  }

  @Test
  @Ignore("TODO: What exactly should be tested here?")
  fun `testVersion control provenance`(): Unit = runBlocking {
    // The SARIF result includes a node named versionControlProvenance.
    // Ensure that this is correctly copied from the actual version control system.
    // This requires configuring Git (user.name, user.email) and running 'git init' in the temporary project directory.
  }
}

private class EmptyRangeNotAtStartTool : LocalInspectionTool() {
  override fun getGroupDisplayName(): String = "TestGroup"

  override fun getShortName(): String = "QdTestEmptyRangeNotAtStart"

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JavaElementVisitor() {
      override fun visitMethod(method: PsiMethod) {
        holder.registerProblem(method, TextRange(0, 0), "Empty range")
      }
    }
}

private class EmptyRangeAtStartTool : LocalInspectionTool() {
  override fun getGroupDisplayName(): String = "TestGroup"

  override fun getShortName(): String = "QdTestEmptyRangeAtStart"

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JavaElementVisitor() {
      override fun visitClass(aClass: PsiClass) {
        holder.registerProblem(aClass, TextRange(0, 0), "Empty range")
      }
    }
}
