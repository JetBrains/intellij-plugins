package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.jetbrains.qodana.sarif.model.*
import com.jetbrains.qodana.sarif.model.Result.BaselineState
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertLinesMatch
import kotlin.random.Random

private const val MAIN_RESULTS_TITLE = "Qodana - Test summary"

class CommandLineResultsPrinterTest {

  @Test
  fun testPrintMainResults() = doTest(
    """
        
        $MAIN_RESULTS_TITLE
        Analysis results: 16 problems detected
        By severity: Critical - 3, High - 3, Moderate - 2, Low - 3, Info - 5
        ----------------------------------------------------------------------------
        Name                                                Severity  Problems count
        ----------------------------------------------------------------------------
        inspection1                                         Critical               2
        inspection2                                         Critical               1
        inspection3                                             High               3
        inspection4                                         Moderate               1
        inspection5                                         Moderate               1
        inspection6                                              Low               2
        inspection7                                              Low               1
        inspection8                                             Info               5
        ----------------------------------------------------------------------------


      """.trimIndent(),
    listOf(
      result("inspection1", QodanaSeverity.CRITICAL),
      result("inspection1", QodanaSeverity.CRITICAL),
      result("inspection2", QodanaSeverity.CRITICAL),

      result("inspection3", QodanaSeverity.HIGH),
      result("inspection3", QodanaSeverity.HIGH),
      result("inspection3", QodanaSeverity.HIGH),

      result("inspection4", QodanaSeverity.MODERATE),
      result("inspection5", QodanaSeverity.MODERATE),

      result("inspection6", QodanaSeverity.LOW),
      result("inspection6", QodanaSeverity.LOW),
      result("inspection7", QodanaSeverity.LOW),

      result("inspection8", QodanaSeverity.INFO),
      result("inspection8", QodanaSeverity.INFO),
      result("inspection8", QodanaSeverity.INFO),
      result("inspection8", QodanaSeverity.INFO),
      result("inspection8", QodanaSeverity.INFO),
    )
  ) { results -> printResults(results, MAIN_RESULTS_TITLE) }

  @Test
  fun testPrintMainResultsCustomMessage() {
    val groupingMessage = "Test grouping message"
    doTest(
      """
        
        $MAIN_RESULTS_TITLE
        Analysis results: 16 problems detected
        $groupingMessage
        ----------------------------------------------------------------------------
        Name                                                Severity  Problems count
        ----------------------------------------------------------------------------
        inspection1                                         Critical               2
        inspection2                                         Critical               1
        inspection3                                             High               3
        inspection4                                         Moderate               1
        inspection5                                         Moderate               1
        inspection6                                              Low               2
        inspection7                                              Low               1
        inspection8                                             Info               5
        ----------------------------------------------------------------------------


      """.trimIndent(),
      listOf(
        result("inspection1", QodanaSeverity.CRITICAL),
        result("inspection1", QodanaSeverity.CRITICAL),
        result("inspection2", QodanaSeverity.CRITICAL),

        result("inspection3", QodanaSeverity.HIGH),
        result("inspection3", QodanaSeverity.HIGH),
        result("inspection3", QodanaSeverity.HIGH),

        result("inspection4", QodanaSeverity.MODERATE),
        result("inspection5", QodanaSeverity.MODERATE),

        result("inspection6", QodanaSeverity.LOW),
        result("inspection6", QodanaSeverity.LOW),
        result("inspection7", QodanaSeverity.LOW),

        result("inspection8", QodanaSeverity.INFO),
        result("inspection8", QodanaSeverity.INFO),
        result("inspection8", QodanaSeverity.INFO),
        result("inspection8", QodanaSeverity.INFO),
        result("inspection8", QodanaSeverity.INFO),
      )
    ) { results -> printResults(results, MAIN_RESULTS_TITLE, groupingMessage) }
  }

  @Test
  fun testPrintMainResultsSingleResult() = doTest(
    """
      
      $MAIN_RESULTS_TITLE
      Analysis results: 1 problem detected
      By severity: Critical - 1
      ----------------------------------------------------------------------------
      Name                                                Severity  Problems count
      ----------------------------------------------------------------------------
      inspection                                          Critical               1
      ----------------------------------------------------------------------------


    """.trimIndent(),
    listOf(
      result("inspection", QodanaSeverity.CRITICAL)
    )
  ) { results -> printResults(results, MAIN_RESULTS_TITLE) }

  @Test
  fun testPrintMainResultsLongInspectionNames() = doTest(
    """
      
      Qodana - Test summary
      Analysis results: 3 problems detected
      By severity: Critical - 1, High - 1, Low - 1
      ----------------------------------------------------------------------------
      Name                                                Severity  Problems count
      ----------------------------------------------------------------------------
      inspection1 long__________________________________  Critical               1
      _____________name                                                           
      inspection2                                             High               1
      inspection3 long__________________________________       Low               1
      _____________name                                                           
      ----------------------------------------------------------------------------


    """.trimIndent(),
    listOf(
      result("inspection1 long_______________________________________________name", QodanaSeverity.CRITICAL),
      result("inspection2", QodanaSeverity.HIGH),
      result("inspection3 long_______________________________________________name", QodanaSeverity.LOW)
    )
  ) { results -> printResults(results, MAIN_RESULTS_TITLE) }

  @Test
  fun testPrintResultsWithBaselineStateIncludeAbsent() = doTest(
    """
      
      Qodana - Baseline summary
      Analysis results: 13 problems detected
      Grouping problems according to baseline: UNCHANGED: 5, NEW: 5, ABSENT: 3
      ---------------------------------------------------------------------------------------
      Name                                                 Baseline  Severity  Problems count
      ---------------------------------------------------------------------------------------
      inspection1                                               NEW  Critical               2
      inspection2                                               NEW      High               2
      inspection3                                               NEW       Low               1
      inspection4                                            ABSENT  Moderate               1
      inspection5                                            ABSENT      Info               2
      inspection8                                         UNCHANGED  Critical               1
      inspection9                                         UNCHANGED       Low               3
      inspection10                                        UNCHANGED      Info               1
      ---------------------------------------------------------------------------------------


    """.trimIndent(),
    listOf(
      result("inspection1", QodanaSeverity.CRITICAL, BaselineState.NEW),
      result("inspection1", QodanaSeverity.CRITICAL, BaselineState.NEW),
      result("inspection2", QodanaSeverity.HIGH, BaselineState.NEW),
      result("inspection2", QodanaSeverity.HIGH, BaselineState.NEW),
      result("inspection3", QodanaSeverity.LOW, BaselineState.NEW),

      result("inspection4", QodanaSeverity.MODERATE, BaselineState.ABSENT),
      result("inspection5", QodanaSeverity.INFO, BaselineState.ABSENT),
      result("inspection5", QodanaSeverity.INFO, BaselineState.ABSENT),

      result("inspection8", QodanaSeverity.CRITICAL, BaselineState.UNCHANGED),
      result("inspection9", QodanaSeverity.LOW, BaselineState.UNCHANGED),
      result("inspection9", QodanaSeverity.LOW, BaselineState.UNCHANGED),
      result("inspection9", QodanaSeverity.LOW, BaselineState.UNCHANGED),
      result("inspection10", QodanaSeverity.INFO, BaselineState.UNCHANGED)
    )
  ) { results -> printResultsWithBaselineState(results, includeAbsent = true) }

  @Test
  fun testPrintResultsWithBaselineStateNoIncludeAbsent() = doTest(
    """
      
      Qodana - Baseline summary
      Analysis results: 10 problems detected
      Grouping problems according to baseline: UNCHANGED: 5, NEW: 5
      ---------------------------------------------------------------------------------------
      Name                                                 Baseline  Severity  Problems count
      ---------------------------------------------------------------------------------------
      inspection1                                               NEW  Critical               2
      inspection2                                               NEW      High               2
      inspection3                                               NEW       Low               1
      inspection8                                         UNCHANGED  Critical               1
      inspection9                                         UNCHANGED       Low               3
      inspection10                                        UNCHANGED      Info               1
      ---------------------------------------------------------------------------------------


    """.trimIndent(),
    listOf(
      result("inspection1", QodanaSeverity.CRITICAL, BaselineState.NEW),
      result("inspection1", QodanaSeverity.CRITICAL, BaselineState.NEW),
      result("inspection2", QodanaSeverity.HIGH, BaselineState.NEW),
      result("inspection2", QodanaSeverity.HIGH, BaselineState.NEW),
      result("inspection3", QodanaSeverity.LOW, BaselineState.NEW),

      result("inspection4", QodanaSeverity.MODERATE, BaselineState.ABSENT),
      result("inspection5", QodanaSeverity.INFO, BaselineState.ABSENT),
      result("inspection5", QodanaSeverity.INFO, BaselineState.ABSENT),

      result("inspection8", QodanaSeverity.CRITICAL, BaselineState.UNCHANGED),
      result("inspection9", QodanaSeverity.LOW, BaselineState.UNCHANGED),
      result("inspection9", QodanaSeverity.LOW, BaselineState.UNCHANGED),
      result("inspection9", QodanaSeverity.LOW, BaselineState.UNCHANGED),
      result("inspection10", QodanaSeverity.INFO, BaselineState.UNCHANGED)
    )
  ) { results -> printResultsWithBaselineState(results, includeAbsent = false) }

  @Test
  fun testPrintSanityResults() = doTest(
    """
      
      Qodana - Sanity summary
      Analysis results: 20 problems detected
      20 suspicious problems were detected during Sanity Check. Probably there are some troubles with corresponding inspections. You may fix the problems or exclude the files containing them from the analysis
      ------------------------------------------------------------------------------------
      File                                            Inspection  Severity  Problems count
      ------------------------------------------------------------------------------------
      A.java                                         inspection1  Critical               2
      A.java                                         inspection2  Critical               1
      B.java                                         inspection1  Critical               1
      B.java                                         inspection2  Critical               1
      C.java                                         inspection1  Critical               1
      A.java                                         inspection3      High               1
      A.java                                         inspection4      High               1
      B.java                                         inspection3      High               1
      B.java                                         inspection4      High               1
      C.java                                         inspection3      High               1
      A.java                                         inspection5  Moderate               1
      B.java                                         inspection5  Moderate               1
      A.java                                         inspection6       Low               1
      A.java                                         inspection7       Low               1
      B.java                                         inspection6       Low               1
      A.java                                         inspection8      Info               2
      A.java                                         inspection9      Info               1
      B.java                                         inspection8      Info               1
      ------------------------------------------------------------------------------------


    """.trimIndent(),
    listOf(
      result("inspection1", QodanaSeverity.CRITICAL, fileUri = "dir/A.java"),
      result("inspection1", QodanaSeverity.CRITICAL, fileUri = "dir/A.java"),
      result("inspection1", QodanaSeverity.CRITICAL, fileUri = "dir/B.java"),
      result("inspection1", QodanaSeverity.CRITICAL, fileUri = "dir/C.java"),
      result("inspection2", QodanaSeverity.CRITICAL, fileUri = "dir/A.java"),
      result("inspection2", QodanaSeverity.CRITICAL, fileUri = "dir/B.java"),

      result("inspection3", QodanaSeverity.HIGH, fileUri = "dir/A.java"),
      result("inspection3", QodanaSeverity.HIGH, fileUri = "dir/B.java"),
      result("inspection3", QodanaSeverity.HIGH, fileUri = "dir/C.java"),
      result("inspection4", QodanaSeverity.HIGH, fileUri = "dir/A.java"),
      result("inspection4", QodanaSeverity.HIGH, fileUri = "dir/B.java"),

      result("inspection5", QodanaSeverity.MODERATE, fileUri = "dir/A.java"),
      result("inspection5", QodanaSeverity.MODERATE, fileUri = "dir/B.java"),

      result("inspection6", QodanaSeverity.LOW, fileUri = "dir/A.java"),
      result("inspection6", QodanaSeverity.LOW, fileUri = "dir/B.java"),
      result("inspection7", QodanaSeverity.LOW, fileUri = "dir/A.java"),

      result("inspection8", QodanaSeverity.INFO, fileUri = "dir/A.java"),
      result("inspection8", QodanaSeverity.INFO, fileUri = "dir/A.java"),
      result("inspection8", QodanaSeverity.INFO, fileUri = "dir/B.java"),
      result("inspection9", QodanaSeverity.INFO, fileUri = "dir/A.java")
    )
  ) { results -> printSanityResults(results) }

  @Test
  fun testPrintSanityResultsDifferentFilesWithSameName() = doTest(
    """
      
      Qodana - Sanity summary
      Analysis results: 2 problems detected
      2 suspicious problems were detected during Sanity Check. Probably there are some troubles with corresponding inspections. You may fix the problems or exclude the files containing them from the analysis
      ------------------------------------------------------------------------------------
      File                                            Inspection  Severity  Problems count
      ------------------------------------------------------------------------------------
      A.java                                          inspection  Critical               1
      A.java                                          inspection  Critical               1
      ------------------------------------------------------------------------------------


    """.trimIndent(),
    listOf(
      result("inspection", QodanaSeverity.CRITICAL, fileUri = "dir1/A.java"),
      result("inspection", QodanaSeverity.CRITICAL, fileUri = "dir2/A.java")
    )
  ) { results -> printSanityResults(results) }

  @Test
  fun testPrintSanityResultsSingleResult() = doTest(
    """
      
      Qodana - Sanity summary
      Analysis results: 1 problem detected
      1 suspicious problem was detected during Sanity Check. Probably there are some troubles with corresponding inspection. You may fix the problem or exclude the files containing it from the analysis
      ------------------------------------------------------------------------------------
      File                                            Inspection  Severity  Problems count
      ------------------------------------------------------------------------------------
      A.java                                          inspection  Critical               1
      ------------------------------------------------------------------------------------


    """.trimIndent(),
    listOf(
      result("inspection", QodanaSeverity.CRITICAL, fileUri = "A.java")
    )
  ) { results -> printSanityResults(results) }

  private fun doTest(
    expected: String,
    results: List<Result>,
    printerTestAction: CommandLineResultsPrinter.(List<Result>) -> Unit
  ) {
    val commandLineResultsPrinter = CommandLineResultsPrinter(
      inspectionIdToName = { it },
      cliPrinter = { assertLinesMatch(it.lines(), expected.lines()) }
    )
    commandLineResultsPrinter.printerTestAction(results.shuffled(Random.Default))
  }

  private fun result(
    inspectionId: String,
    severity: QodanaSeverity,
    baselineState: BaselineState? = null,
    fileUri: String? = null
  ): Result {
    return Result().apply {
      this.baselineState = baselineState
      this.ruleId = inspectionId
      this.properties = PropertyBag().apply {
        this["qodanaSeverity"] = severity.toString()
      }

      if (fileUri != null) {
        this.locations = listOf(Location().withPhysicalLocation(
          PhysicalLocation().withArtifactLocation(
            ArtifactLocation().withUri(fileUri)
          )
        ))
      }
    }
  }
}