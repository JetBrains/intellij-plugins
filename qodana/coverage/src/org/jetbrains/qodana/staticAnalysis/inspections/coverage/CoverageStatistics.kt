package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.LineCoverage
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.report.XMLProjectData
import com.intellij.rt.coverage.report.XMLProjectData.FileInfo
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData

@Deprecated("Only for API compatibility. QodanaGlobalInspectionContext.coverageStatisticsData should be used directly instead")
@Suppress("unused") // used in Scala plugin
class CoverageStatistics(private val statData: CoverageStatisticsData) {
  companion object {
    val stats = Key.create<CoverageStatistics>("qodana.coverage.stats")
  }

  fun processReportXmlData(fileInfo: FileInfo) {
  }
}

fun CoverageStatisticsData.processReportClassData(data: ClassData) {
  for (line in data.lines) {
    val lineData = (line as? LineData) ?: continue
    incrementReportStats(lineData.status.toByte())
  }
}

fun CoverageStatisticsData.processReportXmlData(fileInfo: FileInfo) {
  for (line in fileInfo.lines) {
    incrementReportStats(computeXmlLineStatus(line))
  }
}

fun CoverageStatisticsData.loadClassData(data: ClassData, virtualFile: VirtualFile) {
  for (line in data.lines) {
    val lineData = (line as? LineData) ?: continue
    incrementStats(lineData.status.toByte(), lineData.lineNumber, virtualFile)
  }
}

fun CoverageStatisticsData.loadXmlLineData(fileInfo: FileInfo, virtualFile: VirtualFile) {
  for (line in fileInfo.lines) {
    incrementStats(computeXmlLineStatus(line), line.lineNumber, virtualFile)
  }
}

fun CoverageStatisticsData.loadMissingData(document: Document, textRange: TextRange, virtualFile: VirtualFile) {
  val startOffset = textRange.startOffset
  val endOffset = textRange.endOffset
  val startLineNumber = document.getLineNumber(startOffset) + 1
  val endLineNumber = document.getLineNumber(endOffset) + 1
  for (lineNumber in startLineNumber..endLineNumber) {
    incrementTotalLines()
    val changedInfo = getChangedRanges(virtualFile.url)
    if (changedInfo != null) {
      if (changedInfo.contains(lineNumber)) {
        incrementFreshLines()
      }
    }
  }
}

private fun CoverageStatisticsData.incrementStats(
  status: Byte,
  lineNumber: Int,
  virtualFile: VirtualFile,
) {
  val covered = status != LineCoverage.NONE
  incrementTotalLines()
  if (covered) {
    incrementCoveredLines()
  }
  val changedInfo = getChangedRanges(virtualFile.url)
  if (changedInfo != null) {
    if (changedInfo.contains(lineNumber)) {
      incrementFreshLines()
      if (covered) {
        incrementFreshCoveredLines()
      }
    }
  }
}

private fun CoverageStatisticsData.computeXmlLineStatus(line: XMLProjectData.LineInfo): Byte {
  var status = LineCoverage.NONE
  if (line.missedBranches == 0 && line.missedInstructions == 0 && (line.coveredInstructions != 0 || line.coveredBranches != 0)) {
    status = LineCoverage.FULL
  }
  else if (line.coveredBranches != 0 || line.coveredInstructions != 0) {
    status = LineCoverage.PARTIAL
  }
  return status
}

private fun CoverageStatisticsData.incrementReportStats(status: Byte) {
  val covered = status != LineCoverage.NONE
  incrementReportTotalLines()
  if (covered) {
    incrementReportCoveredLines()
  }
}
