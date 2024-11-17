package org.jetbrains.qodana.jvm.java.metrics.linesOfCode

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.jvm.java.metrics.getNumberOfLinesWhereOnlyElementOnALine
import org.jetbrains.qodana.jvm.java.metrics.hasFirstAndLastChildOnTheSameLine
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.MetricFileData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData.LinesOfCodeMetricTableRowData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData.MetricTableRowData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.iterateFileContents
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.problemDescriptors.MetricCodeDescriptor


class JvmLinesOfCodeInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : PsiElementVisitor() {
      override fun visitFile(file: PsiFile) {
        val document: Document? = PsiDocumentManager.getInstance(file.project).getDocument(file)
        if (document == null) {
          return
        }

        val numberOfLinesInDocument: Int = document.lineCount
        val locWhitespaceVisitor = LocWhitespaceVisitor(document)
        var commentLines = 0

        file.iterateFileContents(
          visitor = locWhitespaceVisitor,
          returnCondition = { element ->
            // No need to visit children of an element that has type PsiComment
            if (element is PsiComment) {
              true
            }
            else {
              // If the first and last child of the PsiElement are on the same line, there is no need to visit them.
              element.hasFirstAndLastChildOnTheSameLine(document)
            }
          },
          beforeReturnCallback = { element ->
            if (element is PsiComment) {
              commentLines += element.getNumberOfLinesWhereOnlyElementOnALine(document, ignoreWhitespace = true)
            }
          }
        )

        val whitespaceLines: Int = locWhitespaceVisitor.whitespaceLines
        val totalLines = numberOfLinesInDocument - commentLines - whitespaceLines
        val filePath: String = file.virtualFile.path

        val tableRowData: MetricTableRowData = LinesOfCodeMetricTableRowData(
          filePath = filePath, numberOfLines = totalLines
        )
        val metricFileData = MetricFileData(
          filePath = filePath,
          tableRows = listOf(tableRowData),
          metricTable = tableRowData.metricTable
        )
        holder.registerProblem(
          MetricCodeDescriptor(
            element = file,
            fileData = metricFileData
          )
        )
      }
    }
  }
}