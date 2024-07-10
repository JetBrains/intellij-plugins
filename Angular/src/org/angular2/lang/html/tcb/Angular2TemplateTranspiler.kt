package org.angular2.lang.html.tcb

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import org.angular2.codeInsight.config.Angular2Compiler
import org.angular2.entities.Angular2ClassBasedComponent
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2Directive
import org.angular2.lang.html.Angular2HtmlFile

object Angular2TemplateTranspiler {

  internal fun createFileContext(file: PsiFile): Environment {
    return Environment(Angular2Compiler.getTypeCheckingConfig(file), file)
  }

  internal fun transpileTemplate(
    fileContext: FileContext,
    component: Angular2Component,
    tcbId: String,
  ): TranspiledTemplate? {
    val boundTarget = BoundTarget(component)
    if (boundTarget.templateFile == null) return null

    val context = Context(
      fileContext as Environment,
      OutOfBandDiagnosticRecorder(), tcbId,
      boundTarget,
    )

    val scope = Scope.forNodes(context, null, null, boundTarget.templateRoots, null)
    val statements = scope.render()

    return Expression {
      append("function _tcb${context.id}")

      val cls = (component as? Angular2ClassBasedComponent)?.typeScriptClass
      if (cls != null) {
        val typeParameters = cls.typeParameters
        if (typeParameters.isNotEmpty()) {
          append("<")
          typeParameters.forEachIndexed { i, param ->
            if (i > 0) {
              append(", ")
            }
            append(param.text)
          }
          append(">")
        }
        append("(this: ").append(cls.name ?: "never")
        if (typeParameters.isNotEmpty()) {
          append("<")
          typeParameters.forEachIndexed { i, param ->
            if (i > 0) {
              append(", ")
            }
            append(param.name ?: "never")
          }
          append(">")
        }
        append(") ")
      }
      else {
        append("() ")
      }
      codeBlock {
        for (it in statements) {
          appendStatement(it)
        }
      }
    }.asTranspiledTemplate(boundTarget.templateFile, context.oobRecorder.getDiagnostics())
  }

  interface FileContext {
    fun getCommonCode(): String
  }

  interface TranspiledTemplate {
    val templateFile: Angular2HtmlFile
    val generatedCode: String
    val sourceMappings: List<SourceMapping>
    val contextVarMappings: List<ContextVarMapping>
    val directiveVarMappings: List<DirectiveVarMapping>
    val diagnostics: Set<Diagnostic>
    val nameMappings: List<Pair<Int, Map<String, String>>>
  }

  interface SourceMapping {
    val sourceOffset: Int
    val sourceLength: Int
    val generatedOffset: Int
    val generatedLength: Int
    val diagnosticsOffset: Int?
    val diagnosticsLength: Int?
    val types: Boolean

    fun offsetBy(generatedOffset: Int = 0, sourceOffset: Int = 0): SourceMapping

    val ignored get() = !types && diagnosticsOffset == null
  }

  interface ContextVarMapping {
    val elementNameOffset: Int
    val elementNameLength: Int
    val generatedOffset: Int
    val generatedLength: Int

    fun getElementNameRangeWithOffset(offset: Int): TextRange =
      TextRange(elementNameOffset + offset, elementNameOffset + offset + elementNameLength)

    fun getGeneratedRangeWithOffset(offset: Int): TextRange =
      TextRange(generatedOffset + offset, generatedOffset + offset + generatedLength)
  }

  interface DirectiveVarMapping {
    val elementNameOffset: Int
    val elementNameLength: Int
    val directive: Angular2Directive
    val generatedOffset: Int
    val generatedLength: Int

    fun getElementNameRangeWithOffset(offset: Int): TextRange =
      TextRange(elementNameOffset + offset, elementNameOffset + offset + elementNameLength)

    fun getGeneratedRangeWithOffset(offset: Int): TextRange =
      TextRange(generatedOffset + offset, generatedOffset + offset + generatedLength)
  }

  enum class DiagnosticKind {
    UnresolvedPipe
  }

  interface Diagnostic {
    val kind: DiagnosticKind
    val startOffset: Int
    val length: Int
    val message: @InspectionMessage String
    val category: String? /* JSAnnotationError.*_CATEGORY */
    val highlightType: ProblemHighlightType?

    fun offsetBy(offset: Int): Diagnostic
  }

}