package org.angular2.lang.expr.service.tcb

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.service.withServiceTraceSpan
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import org.angular2.codeInsight.config.Angular2Compiler
import org.angular2.entities.Angular2ClassBasedComponent
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2Directive
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.lang.expr.service.tcb.Expression.ExpressionBuilder
import java.util.*

object Angular2TemplateTranspiler {

  internal fun createFileContext(file: PsiFile): Environment {
    return Environment(Angular2Compiler.getTypeCheckingConfig(file), file)
  }

  internal fun transpileTemplate(
    fileContext: FileContext,
    component: Angular2Component,
    tcbId: String,
  ): TranspiledTemplate? = withServiceTraceSpan("transpileTemplate") {
    val boundTarget = BoundTarget(component)
    if (boundTarget.templateFile == null) return@withServiceTraceSpan null

    val context = Context(
      fileContext as Environment,
      OutOfBandDiagnosticRecorder(), tcbId,
      boundTarget,
    )

    val scope = Scope.forNodes(context, null, null, boundTarget.templateRoots, null)
    val statements = scope.render()

    return@withServiceTraceSpan Expression {
      append("function _tcb${context.id}")

      val cls = (component as? Angular2ClassBasedComponent)?.typeScriptClass
      if (cls != null) {
        emitMethodDeclarationWithParametrizedThis(cls)
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

  internal fun transpileHostBindings(
    fileContext: FileContext,
    cls: TypeScriptClass,
    tcbId: String,
  ): TranspiledHostBindings? = withServiceTraceSpan("transpileHostBindings") {
    val boundTarget = BoundTarget(null)
    val context = Context(
      fileContext as Environment,
      OutOfBandDiagnosticRecorder(), tcbId,
      boundTarget,
    )
    val (hostBindings, inlineCodeRanges) = buildHostBindingsAst(cls)
                       ?: return@withServiceTraceSpan null
    val scope = Scope.forNodes(context, null, null, listOf(hostBindings), null)
    val statements = scope.render()

    return@withServiceTraceSpan Expression {
      append("function _tcb_host_${context.id}")
      emitMethodDeclarationWithParametrizedThis(cls)
      codeBlock {
        for (it in statements) {
          appendStatement(it)
        }
      }
    }.asTranspiledHostBindings(cls, inlineCodeRanges, context.oobRecorder.getDiagnostics())
  }

  private fun ExpressionBuilder.emitMethodDeclarationWithParametrizedThis(cls: TypeScriptClass) {
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

  interface FileContext {
    fun getCommonCode(): String
  }

  interface TranspiledCode {
    val generatedCode: String
    val sourceMappings: List<SourceMapping>
    val contextVarMappings: List<ContextVarMapping>
    val directiveVarMappings: List<DirectiveVarMapping>
    val diagnostics: Set<Diagnostic>
    val nameMappings: List<Pair<Int, Map<String, String>>>
  }

  interface TranspiledTemplate : TranspiledCode {
    val templateFile: Angular2HtmlFile
  }

  interface TranspiledHostBindings : TranspiledCode {
    val cls: TypeScriptClass
    val inlineCodeRanges: List<TextRange>
  }

  @Suppress("unused")
  enum class SourceMappingFlag {
    FORMAT,
    COMPLETION,
    NAVIGATION,
    SEMANTIC,
    STRUCTURE,
    TYPES,
    REVERSE_TYPES,
  }

  interface SourceMapping {
    val sourceOffset: Int
    val sourceLength: Int
    val generatedOffset: Int
    val generatedLength: Int
    val diagnosticsOffset: Int?
    val diagnosticsLength: Int?
    val flags: EnumSet<SourceMappingFlag>

    fun offsetBy(generatedOffset: Int = 0, sourceOffset: Int = 0): SourceMapping

    val ignored: Boolean get() = flags.isEmpty() && diagnosticsOffset == null
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
    val quickFixes: Array<LocalQuickFix>?

    fun offsetBy(offset: Int): Diagnostic
  }

}
