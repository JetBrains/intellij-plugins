package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.jetbrains.qodana.sarif.model.Result
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.CodeQualityMetrics
import org.jetbrains.qodana.staticAnalysis.inspections.runner.CommandLineTable.Companion.DEFAULT_COLUMN_SIZE
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.staticAnalysis.sarif.qodanaSeverity

class CommandLineResultsPrinter(
  private val inspectionIdToName: (inspectionId: String) -> String,
  private val cliPrinter: (result: String) -> Unit,
) {
  fun printResultsWithBaselineState(results: List<Result>, includeAbsent: Boolean) {
    val resultsCountByBaselineState = results.groupingBy { it.baselineState }.eachCount()

    val groupingMessage = QodanaBundle.message(
      "cli.baseline.results.grouping.message",
      resultsCountByBaselineState[Result.BaselineState.UNCHANGED] ?: 0,
      resultsCountByBaselineState[Result.BaselineState.NEW] ?: 0,
      if (includeAbsent) 1 else 0, resultsCountByBaselineState[Result.BaselineState.ABSENT] ?: 0
    )

    val countedProblems = (
      results
        .takeIf { !includeAbsent }?.filter { it.baselineState != Result.BaselineState.ABSENT }
      ?: results)
      .groupingBy {
        Triple(
          inspectionIdToName.invoke(it.ruleId),
          it.baselineState,
          it.qodanaSeverity
        )
      }.eachCount()

    printProblemsCountTable(
      QodanaBundle.message("cli.baseline.results.title"),
      groupingMessage,
      listOf(
        QodanaBundle.message("cli.results.table.header.inspection.name"),
        QodanaBundle.message("cli.results.table.header.baseline"),
        QodanaBundle.message("cli.results.table.header.severity")
      ),
      listOf(CommandLineTable.DEFAULT_COLUMN_SIZE, 0, 0),
      countedProblems,
      compareByDescending<Map.Entry<Triple<String, Result.BaselineState, QodanaSeverity>, Int>> { it.key.second.order }
        .thenByDescending { it.key.third }.thenByDescending { it.value }.thenBy { it.key.first }
    ) {
      listOf(it.first, it.second.value().uppercase(), it.third.toString())
    }
  }

  private val Result.BaselineState.order: Int
    get() = when (this) {
      Result.BaselineState.UNCHANGED -> 0
      Result.BaselineState.UPDATED -> 1
      Result.BaselineState.ABSENT -> 2
      Result.BaselineState.NEW -> 3
    }

  fun printCoverage(coverage: Map<CoverageData, Int>?, sectionTitle: String) {
    val result = StringBuilder()
    result.appendLine(sectionTitle)
    if (coverage == null) {
      result.appendLine(QodanaBundle.message("cli.coverage.no.coverage"))
    } else {
      if (coverage.containsKey(CoverageData.TOTAL_COV)) {
        result.appendLine("${CoverageData.TOTAL_COV.title}:")
        result.appendLine(generateProgressBar(coverage[CoverageData.TOTAL_COV]!!))
        result.append("${coverage[CoverageData.TOTAL_LINES]} ${CoverageData.TOTAL_LINES.title}, ")
        result.appendLine("${coverage[CoverageData.TOTAL_COV_LINES]} ${CoverageData.TOTAL_COV_LINES.title}")
      }
      if (coverage.containsKey(CoverageData.FRESH_COV)) {
        result.appendLine("${CoverageData.FRESH_COV.title}:")
        result.appendLine(generateProgressBar(coverage[CoverageData.FRESH_COV]!!))
        result.append("${coverage[CoverageData.FRESH_LINES]} ${CoverageData.FRESH_LINES.title}, ")
        result.append("${coverage[CoverageData.FRESH_COV_LINES]} ${CoverageData.FRESH_COV_LINES.title}")
      }
    }
    cliPrinter.invoke(result.toString())
  }

  fun printCodeQualityMetrics(metricsData: Map<CodeQualityMetrics, Any>?, sectionTitle: String) {
    val result = StringBuilder()
    result.appendLine(sectionTitle)

    metricsData?.filter { it.key.printable }?.forEach { (key, value) ->
      result.appendLine("${key.title}:")
      result.append("$value ${key.dim}")
    } ?: result.appendLine(QodanaBundle.message("cli.metrics.no.metrics"))

    cliPrinter.invoke(result.toString())
  }

  fun printResults(results: List<Result>, sectionTitle: String, message: String? = null) {
    val countedByLevels = results.groupingBy { it.qodanaSeverity }.eachCount().toSortedMap(compareByDescending { it })
    val groupingMessage = message ?: QodanaBundle.message(
      "cli.main.results.grouping.message.by.severity",
      countedByLevels.map { "${it.key} - ${it.value}" }.joinToString(", ")
    )

    val countedProblems = results.groupingBy { inspectionIdToName(it.ruleId) to it.qodanaSeverity }.eachCount()

    printProblemsCountTable(
      sectionTitle,
      groupingMessage,
      listOf(
        QodanaBundle.message("cli.results.table.header.inspection.name"),
        QodanaBundle.message("cli.results.table.header.severity")
      ),
      listOf(CommandLineTable.DEFAULT_COLUMN_SIZE, 0),
      countedProblems,
      compareByDescending<Map.Entry<Pair<String, QodanaSeverity>, Int>> { it.key.second }
        .thenByDescending { it.value }.thenBy { it.key.first }
    ) {
      listOf(it.first, it.second.toString())
    }
  }

  fun printSanityResults(sanityResults: List<Result>) {
    val groupingMessage = QodanaBundle.message("cli.sanity.results.grouping.message", sanityResults.size)

    val problems = sanityResults.flatMap {
      it.locations.map { location ->
        (location.physicalLocation?.artifactLocation?.uri ?: "") to it
      }
    }.groupingBy {
      Triple(
        it.first,
        inspectionIdToName.invoke(it.second.ruleId),
        it.second.qodanaSeverity
      )
    }.eachCount()

    printProblemsCountTable(
      QodanaBundle.message("cli.sanity.results.title"),
      groupingMessage,
      listOf(
        QodanaBundle.message("cli.sanity.results.table.header.file"),
        QodanaBundle.message("cli.sanity.results.table.header.inspection.name"),
        QodanaBundle.message("cli.results.table.header.severity")
      ),
      listOf(0, CommandLineTable.DEFAULT_COLUMN_SIZE, 0),
      problems,
      compareByDescending<Map.Entry<Triple<String, String, QodanaSeverity>, Int>> { it.key.third }
        .thenByDescending { it.value }.thenBy { it.key.first }.thenBy { it.key.second }
    ) {
      val fileName = it.first.split("/").last()
      listOf(fileName, it.second, it.third.toString())
    }
  }

  private fun <T> printProblemsCountTable(
    sectionTitle: String,
    groupingMessage: String,
    title: List<String>,
    columnSizes: List<Int>,
    countedProblems: Map<T, Int>,
    comparator: Comparator<Map.Entry<T, Int>>,
    tableRowSelector: (T) -> List<String>
  ) {
    val problemsCount = countedProblems.values.sum()

    val result = StringBuilder()
    result.appendLine(System.lineSeparator() + sectionTitle)
    result.appendLine(QodanaBundle.message("cli.results.problems.count", problemsCount))

    if (problemsCount == 0) {
      cliPrinter.invoke(result.toString())
      return
    }

    val rows = countedProblems.entries.sortedWith(comparator).map { tableRowSelector.invoke(it.key) + it.value.toString() }
    val commandLineTable = CommandLineTable(
      title + QodanaBundle.message("cli.results.table.header.problems.count"),
      rows,
      columnSizes + 0,
    )
    result.appendLine(groupingMessage)
    result.appendLine(commandLineTable.buildTable())
    cliPrinter.invoke(result.toString())
  }

  private fun generateProgressBar(percentage: Int, totalWidth: Int = DEFAULT_COLUMN_SIZE): String {
    val filled = if (percentage < 50) "\u001B[31m▓\u001B[0m" else "\u001B[32m▓\u001B[0m" // TODO: align with the QG, when implemented
    val empty = "░"

    val percentageString = String.format("%3d%%", percentage)
    val percentageWidth = percentageString.length
    val width = totalWidth - percentageWidth - 2

    val filledCount = (width * (percentage.toDouble() / 100)).toInt()
    val emptyCount = width - filledCount

    val progressBar = StringBuilder()

    for (i in 1..filledCount) {
      progressBar.append(filled)
    }

    for (i in 1..emptyCount) {
      progressBar.append(empty)
    }

    return "${progressBar} $percentageString"
  }
}

