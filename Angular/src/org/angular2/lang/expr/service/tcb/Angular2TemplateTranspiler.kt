package org.angular2.lang.expr.service.tcb

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.service.withServiceTraceSpan
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import org.angular2.codeInsight.config.Angular2Compiler
import org.angular2.entities.Angular2ClassBasedComponent
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.index.getFunctionNameFromIndex
import org.angular2.lang.expr.service.tcb.Expression.ExpressionBuilder
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.web.scopes.BINDINGS_PROP
import org.angular2.web.scopes.INPUT_BINDING_FUN
import org.angular2.web.scopes.OUTPUT_BINDING_FUN
import org.angular2.web.scopes.TWO_WAY_BINDING_FUN
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

  internal fun transpileCreateComponentBindings(
    fileContext: FileContext,
    call: JSCallExpression,
    tcbId: String,
  ): TranspiledCreateComponentBindings? = withServiceTraceSpan("transpileCreateComponentBindings") {
    val boundTarget = BoundTarget(null)
    val context = Context(
      fileContext as Environment,
      OutOfBandDiagnosticRecorder(), tcbId,
      boundTarget,
    )
    val inlineCodeRanges = mutableListOf<TextRange>()
    val component =
      call.arguments.firstOrNull()
        ?.let { JSResolveUtil.getElementJSType(it) }
        ?.substitute()
        ?.sourceElement
        ?.let { Angular2EntitiesProvider.getComponent(it) }

    val initializer = call.arguments.getOrNull(1)?.asSafely<JSObjectLiteralExpression>()
                      ?: return@withServiceTraceSpan null

    val dynamicBindings = SmartList<DynamicDirectiveBinding>()

    dynamicBindings.addAll(analyzeDirectiveBindings(component, initializer, inlineCodeRanges))

    initializer.findProperty("directives")
      ?.value?.asSafely<JSArrayLiteralExpression>()
      ?.expressions
      ?.filterIsInstance<JSObjectLiteralExpression>()
      ?.forEach { objectLiteral ->
        val directive = objectLiteral.findProperty("type")?.value
          ?.let { JSResolveUtil.getElementJSType(it) }?.substitute()?.sourceElement
          ?.let { Angular2EntitiesProvider.getDirective(it) }
        dynamicBindings.addAll(analyzeDirectiveBindings(directive, objectLiteral, inlineCodeRanges))
      }

    if (dynamicBindings.isEmpty()) return@withServiceTraceSpan null

    val scope = Scope.forDynamicBindings(context, dynamicBindings)
    val statements = scope.render()

    return@withServiceTraceSpan Expression {
      append("function _tcb_createComponent_${context.id}()")
      codeBlock {
        for (it in statements) {
          appendStatement(it)
        }
      }
      append(";")
    }.asTranspiledCreateComponentBindings( call, inlineCodeRanges, context.oobRecorder.getDiagnostics())
  }

  private fun analyzeDirectiveBindings(directive: Angular2Directive?, initializer: JSObjectLiteralExpression, inlineCodeRanges: MutableList<TextRange>): List<DynamicDirectiveBinding> =
    initializer.findProperty(BINDINGS_PROP)
      ?.value?.asSafely<JSArrayLiteralExpression>()
      ?.expressions
      ?.filterIsInstance<JSCallExpression>()
      ?.mapNotNull {
        val kind = getFunctionNameFromIndex(it)?.takeIf { it == INPUT_BINDING_FUN || it == OUTPUT_BINDING_FUN || it == TWO_WAY_BINDING_FUN }
                   ?: return@mapNotNull null
        val name = it.arguments.firstOrNull()?.asSafely<JSLiteralExpression>()?.takeIf { it.isQuotedLiteral }
                   ?: return@mapNotNull null
        val value = if (kind == OUTPUT_BINDING_FUN)
          it.typeArguments.firstOrNull() ?: return@mapNotNull null
        else
          it.arguments.getOrNull(1) ?: return@mapNotNull null
        inlineCodeRanges.add(name.textRange.let { TextRange(it.startOffset + 1, it.endOffset - 1) })
        DynamicDirectiveBinding(directive, name, value, kind)
      } ?: emptyList()

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

  interface TranspiledCreateComponentBindings : TranspiledCode {
    val call: JSCallExpression
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
    val sourceFile: PsiFile?

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
    UnresolvedPipe,
    IllegalForLoopTrackAccess
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
