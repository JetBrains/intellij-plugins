package org.jetbrains.qodana.staticAnalysis.inspections.coverageData

import com.intellij.codeInsight.actions.VcsFacade
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.ChangedRangesInfo
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.annotations.Nls
import org.jetbrains.qodana.QodanaBundle
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

private const val COV_FIELD_NAME = "coverage"
const val COVERAGE_DATA = "qodana.coverage.input"
val precomputedCoverageFiles = Key.create<List<File>>("qodana.coverage.files")

@Suppress("UNCHECKED_CAST")
var Run.coverageStats: Map<String, Int>
  set(value) {
    properties = (properties ?: PropertyBag()).also {
      it[COV_FIELD_NAME] = value
    }
  }
  get() = (properties?.get(COV_FIELD_NAME) as? Map<String, Int>?).orEmpty()

enum class CoverageData(val prop: String, @Nls val title: String, @Nls val dim: String) {
  TOTAL_COV("totalCoverage", QodanaBundle.message("cli.coverage.total.title"), QodanaBundle.message("cli.coverage.percent")),
  TOTAL_LINES("totalLines", QodanaBundle.message("cli.coverage.lines.title"), QodanaBundle.message("cli.coverage.lines")),
  TOTAL_COV_LINES("totalCoveredLines", QodanaBundle.message("cli.coverage.covered.title"), QodanaBundle.message("cli.coverage.lines")),
  FRESH_COV("freshCoverage", QodanaBundle.message("cli.coverage.fresh.title"), QodanaBundle.message("cli.coverage.percent")),
  FRESH_LINES("freshLines", QodanaBundle.message("cli.coverage.lines.title"), QodanaBundle.message("cli.coverage.lines")),
  FRESH_COV_LINES("freshCoveredLines", QodanaBundle.message("cli.coverage.covered.title"), QodanaBundle.message("cli.coverage.lines")),
}

/*
  Responsible for coverage statistics based on the Coverage global tools input.
  Lifetime is bound to Qodana context lifetime.
 */
class CoverageStatisticsData(val coverageComputationState: QodanaCoverageComputationState, project: Project, changedRanges: Map<String, Set<Int>>? = null) {
  private val totalLines = AtomicInteger()
  private val coveredLines = AtomicInteger()
  private val freshLines = AtomicInteger()
  private val freshCoveredLines = AtomicInteger()
  private val reportTotalLines = AtomicInteger()
  private val reportCoveredLines = AtomicInteger()
  private val isIncremental = coverageComputationState.isIncrementalAnalysis()
  private val changedRanges by lazy { changedRanges ?: computeChangedRanges(project) } // immutable

  fun computeStat(): Map<CoverageData, Int>? {
    if (!isIncremental && totalLines.get() == 0 || isIncremental && reportTotalLines.get() == 0) return null
    if (!isIncremental) {
      val totalCoverage = coveredLines.get() * 100 / totalLines.get()
      return mapOf(CoverageData.TOTAL_COV to totalCoverage, CoverageData.TOTAL_LINES to totalLines.get(), CoverageData.TOTAL_COV_LINES to coveredLines.get())
    } else {
      val reportTotalCoverage = reportCoveredLines.get() * 100 / reportTotalLines.get()
      val totalCovData = mapOf(CoverageData.TOTAL_COV to reportTotalCoverage, CoverageData.TOTAL_LINES to reportTotalLines.get(), CoverageData.TOTAL_COV_LINES to reportCoveredLines.get())
      val freshCodeCoverage = if (freshLines.get() != 0) freshCoveredLines.get() * 100 / freshLines.get() else 100
      return totalCovData + mapOf(CoverageData.FRESH_COV to freshCodeCoverage, CoverageData.FRESH_LINES to freshLines.get(), CoverageData.FRESH_COV_LINES to freshCoveredLines.get())
    }
  }

  private fun computeChangedRanges(project: Project): Map<String, Set<Int>> {
    val changedLinesDataMap = mutableMapOf<String, Set<Int>>()
    val changeListManager = ChangeListManager.getInstance(project)
    val psiManager = PsiManager.getInstance(project)
    val changedFiles = ReadAction.compute<List<PsiFile>, Throwable> { changeListManager.allChanges
      .filter { it.afterRevision != null && it.virtualFile != null }
      .mapNotNull { psiManager.findFile(it.virtualFile!!) } }
    for (changedFile in changedFiles) {
      val lines = mutableSetOf<Int>()
      val ranges = ReadAction.compute<ChangedRangesInfo?, Throwable> { VcsFacade.getInstance().getChangedRangesInfo(changedFile) } ?: continue
      val document = PsiDocumentManager.getInstance(project).getDocument(changedFile) ?: continue
      for (range in ranges.allChangedRanges) {
        val startLine = document.getLineNumber(range.startOffset) + 1
        val endLine = document.getLineNumber(range.endOffset) + 1
        for (line in startLine .. endLine) {
          lines.add(line)
        }
      }
      changedLinesDataMap[changedFile.virtualFile.url] = Collections.unmodifiableSet(lines)
    }

    return Collections.unmodifiableMap(changedLinesDataMap)
  }

  fun incrementTotalLines() {
    totalLines.incrementAndGet()
  }

  fun incrementCoveredLines() {
    coveredLines.incrementAndGet()
  }

  fun incrementFreshLines() {
    freshLines.incrementAndGet()
  }

  fun incrementFreshCoveredLines() {
    freshCoveredLines.incrementAndGet()
  }

  fun incrementReportTotalLines() {
    reportTotalLines.incrementAndGet()
  }

  fun incrementReportCoveredLines() {
    reportCoveredLines.incrementAndGet()
  }

  fun getChangedRanges(virtualFileUrl: String) = changedRanges.getOrDefault(virtualFileUrl, null)
}

enum class QodanaCoverageComputationState {
  SKIP_COMPUTE,
  SKIP_REPORT,
  DEFAULT;

  fun isIncrementalAnalysis() = this != DEFAULT

  fun isFirstStage() = this == SKIP_COMPUTE
}