package org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.cyclomaticComplexity

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.MetricFileData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.rowData.CyclomaticComplexityMetricTableRowData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.CyclomaticComplexityMetricTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.database.tables.MetricTable
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.checkLanguage
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.problemDescriptors.MetricCodeDescriptor

class CyclomaticComplexityMetricInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    val file: PsiFile = session.file
    val fileLanguageId: String = file.language.id
    val filePath: String = file.virtualFile.path

    val visitor: CyclomaticComplexityMetricFileVisitor = CyclomaticComplexityMetricFileVisitor.EP.extensionList.find {
      checkLanguage(fileLanguageId, it.language)
    } ?: return PsiElementVisitor.EMPTY_VISITOR

    val metricTable: CyclomaticComplexityMetricTable =
      MetricTable.EP.findExtension(CyclomaticComplexityMetricTable::class.java) ?: return PsiElementVisitor.EMPTY_VISITOR

    return object : PsiElementVisitor() {
      override fun visitFile(file: PsiFile) {
        val cyclomaticComplexityMethodData: List<CyclomaticComplexityMethodData> = visitor.visit(file)
        val tableDataRows: List<CyclomaticComplexityMetricTableRowData> = cyclomaticComplexityMethodData.map { methodData ->
          CyclomaticComplexityMetricTableRowData(
            filePath = filePath,
            methodName = methodData.methodName,
            cyclomaticComplexityValue = methodData.value,
            methodFileOffset = methodData.methodFileOffset
          )
        }
        val metricFileData = MetricFileData(
          filePath, metricTable, tableDataRows
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