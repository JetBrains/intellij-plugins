package org.jetbrains.qodana.jvm.kotlin.metrics.cyclomaticComplexity

import com.intellij.psi.PsiFile
import com.intellij.psi.util.parents
import org.jetbrains.kotlin.psi.*
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.VisitorLanguage
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.cyclomaticComplexity.CyclomaticComplexityMethodData
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.cyclomaticComplexity.CyclomaticComplexityMetricFileVisitor
import org.jetbrains.qodana.staticAnalysis.inspections.metrics.iterateFileContents

class KotlinCyclomaticComplexityMetricFileVisitor : CyclomaticComplexityMetricFileVisitor {
  override val language: String
    get() = VisitorLanguage.KOTLIN.id

  override fun visit(file: PsiFile): List<CyclomaticComplexityMethodData> {
    val fileVisitor = CyclomaticComplexityMetricFileVisitor()
    file.iterateFileContents(fileVisitor)
    return fileVisitor.methodDataList
  }

  private class CyclomaticComplexityMetricFileVisitor : KtVisitorVoid() {
    private val _methodDataList = mutableListOf<CyclomaticComplexityMethodData>()
    val methodDataList: List<CyclomaticComplexityMethodData> = _methodDataList

    private fun visitCallableElement(callableElement: KtElement, name: String) {
      val methodVisitor = KotlinCyclomaticComplexityMethodVisitor()
      callableElement.acceptChildren(methodVisitor)
      val cyclomaticComplexityValue: Int = methodVisitor.cyclomaticComplexityValue
      val methodData: CyclomaticComplexityMethodData = CyclomaticComplexityMethodData(
        methodName = name,
        value = cyclomaticComplexityValue,
        methodFileOffset = callableElement.textRange.startOffset
      )
      _methodDataList.add(methodData)
    }

    private fun resolveName(ktElement: KtElement): String {
      return ktElement.parents(withSelf = true).filter {
        it is KtNamedFunction || it is KtClass || it is KtVariableDeclaration
      }.map { element ->
        val functionName: String? = (element as? KtNamedFunction)?.name
        val className: String? = (element as? KtClass)?.name
        val variableName: String? = (element as? KtVariableDeclaration)?.name
        functionName ?: className ?: variableName
      }.toList().reversed().joinToString(separator = "$")
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
      if (!function.hasBody()) return
      val name = "function: ${resolveName(function)}"
      visitCallableElement(function, name)
    }

    override fun visitClassInitializer(initializer: KtClassInitializer) {
      val name = "init: ${resolveName(initializer)}"
      visitCallableElement(initializer, name)
    }

    override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor) {
      val name = "secondary constructor: ${resolveName(constructor)}"
      visitCallableElement(constructor, name)
    }

    override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
      val name = "lambda: ${resolveName(lambdaExpression)}"
      visitCallableElement(lambdaExpression, name)
    }
  }
}