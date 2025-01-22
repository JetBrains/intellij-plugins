package org.angular2.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ex.ProblemDescriptorImpl
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.startOffset
import org.angular2.lang.expr.service.Angular2TypeScriptService
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.DiagnosticKind
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder

abstract class AngularTcbOutOfBandInspectionBase(private val kind: DiagnosticKind) : LocalInspectionTool() {

  override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
    if (file.language !is Angular2HtmlDialect
        || TypeScriptServiceHolder.getForElement(file)?.service !is Angular2TypeScriptService)
      return null

    val (transpiledFile, topLevelTemplateFile) = Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveAndTopLevelSourceFile(file)
                                                 ?: return null

    val diagnostics = transpiledFile.diagnostics[topLevelTemplateFile]
                        ?.filter { it.kind == kind }
                        ?.takeIf { it.isNotEmpty() }
                      ?: return null

    val fileRange = TextRange(0, file.textLength).let {
      if (file != topLevelTemplateFile)
        InjectedLanguageManager.getInstance(file.project).injectedToHost(file, it)
      else
        it
    }

    return diagnostics.filter { fileRange.contains(it.startOffset) }.mapNotNull {
      val startOffset = it.startOffset - fileRange.startOffset

      val commonElement = PsiTreeUtil.findElementOfClassAtRange(file, startOffset, startOffset + it.length, PsiElement::class.java)
                          ?: return@mapNotNull null
      val startOffsetInTheElement = startOffset - commonElement.startOffset
      ProblemDescriptorImpl(commonElement, commonElement, it.message, it.quickFixes,
                            it.highlightType ?: ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            false, TextRange(startOffsetInTheElement, startOffsetInTheElement + it.length), isOnTheFly)
    }.toTypedArray()
  }

}
