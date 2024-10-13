package org.jetbrains.qodana.jvm.java.metrics.cyclomaticComplexity

import com.intellij.psi.*
import com.intellij.psi.util.parents
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.VisitorLanguage
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.cyclomaticComplexity.CyclomaticComplexityMethodData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.cyclomaticComplexity.CyclomaticComplexityMetricFileVisitor
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.iterateFileContents

class JavaCyclomaticComplexityMetricFileVisitor : CyclomaticComplexityMetricFileVisitor {
  override val language: String
    get() = VisitorLanguage.JAVA.id

  override fun visit(file: PsiFile): List<CyclomaticComplexityMethodData> {
    val fileVisitor = CyclomaticComplexityMetricFileVisitor()
    file.iterateFileContents(fileVisitor)
    return fileVisitor.methodData
  }

  private class CyclomaticComplexityMetricFileVisitor : JavaElementVisitor() {
    private val _methodData = mutableListOf<CyclomaticComplexityMethodData>()
    val methodData: List<CyclomaticComplexityMethodData> = _methodData

    private fun visitCallableElement(callableElement: PsiElement, name: String) {
      val methodVisitor = JavaCyclomaticComplexityMethodVisitor()
      callableElement.acceptChildren(methodVisitor)
      val cyclomaticComplexityValue: Int = methodVisitor.cyclomaticComplexityValue
      val methodData: CyclomaticComplexityMethodData = CyclomaticComplexityMethodData(
        methodName = name,
        value = cyclomaticComplexityValue,
        methodFileOffset = callableElement.textRange.startOffset
      )
      _methodData.add(methodData)
    }

    private fun resolveName(psiElement: PsiElement): String {
      return psiElement.parents(true).filter {
        it is PsiMethod || it is PsiClass || it is PsiVariable
      }.map { element ->
        val functionName: String? = (element as? PsiMethod)?.name
        val className: String? = (element as? PsiClass)?.name
        val variableName: String? = (element as? PsiVariable)?.name
        functionName ?: className ?: variableName
      }.toList().reversed().joinToString(separator = "$")
    }

    override fun visitMethod(method: PsiMethod) {
      val name = "function: ${resolveName(method)}"
      visitCallableElement(method, name)
    }

    override fun visitLambdaExpression(expression: PsiLambdaExpression) {
      val name = "lambda: ${resolveName(expression)}"
      visitCallableElement(expression, name)
    }
  }
}