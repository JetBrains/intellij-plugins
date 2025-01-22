package org.angular2.lang.expr.service.tcb

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.util.containers.MultiMap
import org.angular2.entities.Angular2Directive
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.SourceMappingFlag
import java.util.*

internal class Expression(builder: ExpressionBuilder.() -> Unit) {

  constructor(code: String)
    : this({ append(code) })

  constructor(
    code: String,
    originalRange: TextRange?,
    types: Boolean,
    diagnosticsRange: TextRange? = originalRange,
  )
    : this({ append(code, originalRange, types, diagnosticsRange) })

  constructor(id: Identifier)
    : this({ append(id) })

  private val code: StringBuilder

  private val sourceMappings: Set<SourceMappingData>
  private val contextVarMappings: Set<ContextVarMappingData>
  private val directiveVarMappings: Set<DirectiveVarMappingData>
  private val nameMappings: List<Pair<Int, Map<String, String>>>

  init {
    val expression = ExpressionBuilderImpl().apply(builder)
    code = expression.code
    sourceMappings = expression.sourceMappings
    contextVarMappings = expression.contextVarMappings
    directiveVarMappings = expression.directiveVarMappings
    nameMappings = expression.nameMappings
  }

  override fun toString(): String =
    code.toString()

  fun asTranspiledTemplate(
    templateFile: Angular2HtmlFile,
    diagnostics: Set<Angular2TemplateTranspiler.Diagnostic>,
  ): Angular2TemplateTranspiler.TranspiledTemplate =
    object : Angular2TemplateTranspiler.TranspiledTemplate {
      override val templateFile: Angular2HtmlFile = templateFile
      override val generatedCode: String = code.toString()
      override val sourceMappings: List<Angular2TemplateTranspiler.SourceMapping> = this@Expression.sourceMappings.toList()
      override val contextVarMappings: List<Angular2TemplateTranspiler.ContextVarMapping> = this@Expression.contextVarMappings.toList()
      override val directiveVarMappings: List<Angular2TemplateTranspiler.DirectiveVarMapping> = this@Expression.directiveVarMappings.toList()
      override val diagnostics: Set<Angular2TemplateTranspiler.Diagnostic> = diagnostics
      override val nameMappings: List<Pair<Int, Map<String, String>>> = this@Expression.nameMappings
    }

  fun asTranspiledHostBindings(
    cls: TypeScriptClass,
    inlineCodeRanges: List<TextRange>,
    diagnostics: Set<Angular2TemplateTranspiler.Diagnostic>,
  ): Angular2TemplateTranspiler.TranspiledHostBindings =
    object : Angular2TemplateTranspiler.TranspiledHostBindings {
      override val cls: TypeScriptClass = cls
      override val inlineCodeRanges: List<TextRange> = inlineCodeRanges
      override val generatedCode: String = code.toString()
      override val sourceMappings: List<Angular2TemplateTranspiler.SourceMapping> = this@Expression.sourceMappings.toList()
      override val contextVarMappings: List<Angular2TemplateTranspiler.ContextVarMapping> = this@Expression.contextVarMappings.toList()
      override val directiveVarMappings: List<Angular2TemplateTranspiler.DirectiveVarMapping> = this@Expression.directiveVarMappings.toList()
      override val diagnostics: Set<Angular2TemplateTranspiler.Diagnostic> = diagnostics
      override val nameMappings: List<Pair<Int, Map<String, String>>> = this@Expression.nameMappings
    }


  interface ExpressionBuilder {
    val isIgnoreDiagnostics: Boolean

    fun append(code: String): ExpressionBuilder

    fun append(id: Identifier): ExpressionBuilder

    fun append(
      code: String,
      originalRange: TextRange?,
      supportTypes: Boolean = false,
      diagnosticsRange: TextRange? = originalRange,
      supportSemanticHighlighting: Boolean = diagnosticsRange != null,
      supportReversedTypes: Boolean = supportTypes,
      contextVar: Boolean = false,
      varOfDirective: Angular2Directive? = null,
      nameMap: Map<String, String>? = null,
    ): ExpressionBuilder

    fun append(
      id: Identifier,
      originalRange: TextRange?,
      supportTypes: Boolean = false,
      diagnosticsRange: TextRange? = originalRange,
      supportSemanticHighlighting: Boolean = diagnosticsRange != null,
      supportReversedTypes: Boolean = supportTypes,
      contextVar: Boolean = false,
      varOfDirective: Angular2Directive? = null,
    ): ExpressionBuilder

    fun append(expression: Expression): ExpressionBuilder

    fun append(expression: ExpressionBuilder.() -> Unit): ExpressionBuilder

    fun withSourceSpan(
      originalRange: TextRange?,
      supportTypes: Boolean = false,
      diagnosticsRange: TextRange? = originalRange,
      supportSemanticHighlighting: Boolean = diagnosticsRange != null,
      supportReversedTypes: Boolean = supportTypes,
      builder: ExpressionBuilder.() -> Unit,
    )

    fun withMappingsOffset(offset: Int, builder: ExpressionBuilder.() -> Unit)

    fun withSupportReverseTypes(builder: ExpressionBuilder.() -> Unit)

    fun withIgnoreMappings(builder: ExpressionBuilder.() -> Unit)

    fun codeBlock(builder: BlockBuilder.() -> Unit)

    fun statements(builder: BlockBuilder.() -> Unit)

    fun newLine()

    fun removeMappings(range: TextRange)
  }

  interface BlockBuilder : ExpressionBuilder {

    fun appendStatement(statement: Statement): BlockBuilder

    fun appendStatement(expression: ExpressionBuilder.() -> Unit): BlockBuilder

  }

  private class ExpressionBuilderImpl : ExpressionBuilder, BlockBuilder {
    val code = StringBuilder()

    val sourceMappings = mutableSetOf<SourceMappingData>()
    val contextVarMappings = mutableSetOf<ContextVarMappingData>()
    val directiveVarMappings = mutableSetOf<DirectiveVarMappingData>()
    val nameMappings = mutableListOf<Pair<Int, Map<String, String>>>()

    private var ignoreMappings = false

    private var supportReversedTypes = false

    override val isIgnoreDiagnostics: Boolean
      get() = ignoreMappings

    override fun removeMappings(range: TextRange) {
      sourceMappings.removeIf {
        it.sourceOffset == range.startOffset
        && it.sourceOffset + it.sourceLength == range.endOffset
      }
    }

    override fun append(code: String): ExpressionBuilder {
      this.code.append(code)
      return this
    }

    override fun append(id: Identifier): ExpressionBuilder {
      this.code.append(id.toString())
      return this
    }

    override fun append(
      code: String,
      originalRange: TextRange?,
      supportTypes: Boolean,
      diagnosticsRange: TextRange?,
      supportSemanticHighlighting: Boolean,
      supportReversedTypes: Boolean,
      contextVar: Boolean,
      varOfDirective: Angular2Directive?,
      nameMap: Map<String, String>?,
    ): ExpressionBuilder {
      if (originalRange != null) {
        val sourceOffset = originalRange.startOffset
        val sourceLength = originalRange.length
        val generatedOffset = this.code.length
        val generatedLength = code.length
        sourceMappings.add(SourceMappingData(
          sourceOffset = sourceOffset,
          sourceLength = sourceLength,
          generatedOffset = generatedOffset,
          generatedLength = generatedLength,
          diagnosticsOffset = diagnosticsRange?.startOffset?.takeIf { !ignoreMappings },
          diagnosticsLength = diagnosticsRange?.length?.takeIf { !ignoreMappings },
          flags = buildMappingFlags(ignoreMappings, supportTypes, supportSemanticHighlighting, supportReversedTypes || this.supportReversedTypes)
        ))
        if (!ignoreMappings) {
          if (contextVar) {
            contextVarMappings.add(ContextVarMappingData(
              elementNameOffset = sourceOffset,
              elementNameLength = sourceLength,
              generatedOffset = generatedOffset,
              generatedLength = generatedLength
            ))
          }
          if (varOfDirective != null) {
            directiveVarMappings.add(DirectiveVarMappingData(
              elementNameOffset = sourceOffset,
              elementNameLength = sourceLength,
              directive = varOfDirective,
              generatedOffset = generatedOffset,
              generatedLength = generatedLength
            ))
          }
          if (nameMap != null) {
            nameMappings.add(Pair(originalRange.startOffset, nameMap))
          }
        }
      }
      this.code.append(code)
      return this
    }

    override fun append(
      id: Identifier,
      originalRange: TextRange?,
      supportTypes: Boolean,
      diagnosticsRange: TextRange?,
      supportSemanticHighlighting: Boolean,
      supportReversedTypes: Boolean,
      contextVar: Boolean,
      varOfDirective: Angular2Directive?,
    ): ExpressionBuilder =
      append(id.toString(), originalRange, supportTypes, diagnosticsRange, supportSemanticHighlighting, contextVar = contextVar, varOfDirective = varOfDirective,
             nameMap = id.sourceName?.let { mapOf(Pair(id.name, it)) })

    override fun append(expression: Expression): ExpressionBuilder {
      val offset = this.code.length
      expression.sourceMappings.mapTo(this.sourceMappings) { sourceMapping ->
        sourceMapping.copy(
          generatedOffset = sourceMapping.generatedOffset + offset,
          diagnosticsOffset = sourceMapping.diagnosticsOffset?.takeIf { !ignoreMappings },
          diagnosticsLength = sourceMapping.diagnosticsLength?.takeIf { !ignoreMappings },
          flags = sourceMapping.flags.applyOverrideFlags()
        )
      }
      if (!ignoreMappings) {
        expression.contextVarMappings.mapTo(this.contextVarMappings) { contextVarMapping ->
          contextVarMapping.copy(
            generatedOffset = contextVarMapping.generatedOffset + offset,
          )
        }
        expression.directiveVarMappings.mapTo(this.directiveVarMappings) { directiveVarMapping ->
          directiveVarMapping.copy(
            generatedOffset = directiveVarMapping.generatedOffset + offset,
          )
        }
        nameMappings.addAll(expression.nameMappings)
      }
      this.code.append(expression.code)
      return this
    }

    override fun append(expression: ExpressionBuilder.() -> Unit): ExpressionBuilder {
      append(Expression(expression))
      return this
    }

    override fun withSourceSpan(
      originalRange: TextRange?,
      supportTypes: Boolean,
      diagnosticsRange: TextRange?,
      supportSemanticHighlighting: Boolean,
      supportReversedTypes: Boolean,
      builder: ExpressionBuilder.() -> Unit,
    ) {
      if (originalRange != null) {
        val offset = this.code.length
        this.builder()
        sourceMappings.add(SourceMappingData(
          sourceOffset = originalRange.startOffset,
          sourceLength = originalRange.length,
          generatedOffset = offset,
          generatedLength = this.code.length - offset,
          diagnosticsOffset = diagnosticsRange?.startOffset?.takeIf { !ignoreMappings },
          diagnosticsLength = diagnosticsRange?.length?.takeIf { !ignoreMappings },
          flags = buildMappingFlags(ignoreMappings, supportTypes, supportSemanticHighlighting, supportReversedTypes || this.supportReversedTypes),
        ))
      }
      else {
        this.builder()
      }
    }

    private fun buildMappingFlags(
      ignoreMappings: Boolean,
      supportTypes: Boolean,
      supportSemanticHighlighting: Boolean,
      supportReversedTypes: Boolean,
    ): EnumSet<SourceMappingFlag> {
      if (ignoreMappings || (!supportTypes && !supportSemanticHighlighting))
        return EnumSet.noneOf(SourceMappingFlag::class.java).apply {
          if (supportReversedTypes) add(SourceMappingFlag.REVERSE_TYPES)
        }
      return EnumSet.allOf(SourceMappingFlag::class.java).apply {
        if (!supportTypes) remove(SourceMappingFlag.TYPES)
        if (!supportSemanticHighlighting) remove(SourceMappingFlag.SEMANTIC)
        if (!supportReversedTypes) remove(SourceMappingFlag.REVERSE_TYPES)
      }
    }

    private fun EnumSet<SourceMappingFlag>.applyOverrideFlags(): EnumSet<SourceMappingFlag> =
      if (ignoreMappings)
        EnumSet.noneOf(SourceMappingFlag::class.java).apply {
          if (this.contains(SourceMappingFlag.REVERSE_TYPES) || supportReversedTypes) add(SourceMappingFlag.REVERSE_TYPES)
        }
      else
        this.also {
          if (supportReversedTypes) add(SourceMappingFlag.REVERSE_TYPES)
        }

    override fun withMappingsOffset(offset: Int, builder: ExpressionBuilder.() -> Unit) {
      if (offset == 0) {
        this.builder()
        return
      }
      val subBuilder = ExpressionBuilderImpl()
      subBuilder.ignoreMappings = ignoreMappings
      subBuilder.supportReversedTypes = supportReversedTypes
      subBuilder.builder()
      val generatedOffset = this.code.length
      subBuilder.sourceMappings.mapTo(this.sourceMappings) { sourceMapping ->
        sourceMapping.copy(
          sourceOffset = sourceMapping.sourceOffset + offset,
          generatedOffset = sourceMapping.generatedOffset + generatedOffset,
          diagnosticsOffset = sourceMapping.diagnosticsOffset?.let { it + offset },
        )
      }
      subBuilder.contextVarMappings.mapTo(this.contextVarMappings) { contextVarMapping ->
        contextVarMapping.copy(
          elementNameOffset = contextVarMapping.elementNameOffset + offset,
          generatedOffset = contextVarMapping.generatedOffset + generatedOffset,
        )
      }
      subBuilder.directiveVarMappings.mapTo(this.directiveVarMappings) { directiveVarMapping ->
        directiveVarMapping.copy(
          elementNameOffset = directiveVarMapping.elementNameOffset + offset,
          generatedOffset = directiveVarMapping.generatedOffset + generatedOffset,
        )
      }
      subBuilder.nameMappings.mapTo(this.nameMappings) { nameMapping ->
        Pair(nameMapping.first + offset, nameMapping.second)
      }
      nameMappings.addAll(subBuilder.nameMappings)
      this.code.append(subBuilder.code)
    }

    override fun withIgnoreMappings(builder: ExpressionBuilder.() -> Unit) {
      ignoreMappings = true
      this.builder()
      ignoreMappings = false
    }

    override fun withSupportReverseTypes(builder: ExpressionBuilder.() -> Unit) {
      supportReversedTypes = true
      this.builder()
      supportReversedTypes = false
    }

    override fun codeBlock(builder: BlockBuilder.() -> Unit) {
      append("{\n")
      this.builder()
      append("}")
    }

    override fun statements(builder: BlockBuilder.() -> Unit) {
      this.builder()
    }

    override fun newLine() {
      append("\n")
    }

    override fun appendStatement(statement: Statement): BlockBuilder {
      append(statement.expression).newLine()
      return this
    }

    override fun appendStatement(expression: ExpressionBuilder.() -> Unit): BlockBuilder {
      appendStatement(Statement(expression))
      return this
    }

    override fun toString(): String =
      code.toString()

  }
}

internal class Identifier(
  /** Name of the identifier as used in the generated file within TCB */
  val name: String,
  /** name of the identifier as in the source HTML file */
  val sourceName: String?,
  /** text range of the original variable within the source HTML file */
  val sourceSpan: TextRange? = null,
) {
  override fun toString(): String = name
}

internal class Statement private constructor(val expression: Expression) {
  constructor(builder: Expression.ExpressionBuilder.() -> Unit) : this(Expression(builder))
}

internal data class SourceMappingData(
  override val sourceOffset: Int,
  override val sourceLength: Int,
  override val generatedOffset: Int,
  override val generatedLength: Int,
  override val diagnosticsOffset: Int?,
  override val diagnosticsLength: Int?,
  override val flags: EnumSet<SourceMappingFlag>,
) : Angular2TemplateTranspiler.SourceMapping {
  override fun offsetBy(generatedOffset: Int, sourceOffset: Int): Angular2TemplateTranspiler.SourceMapping =
    copy(sourceOffset = this.sourceOffset + sourceOffset,
         generatedOffset = this.generatedOffset + generatedOffset,
         diagnosticsOffset = this.diagnosticsOffset?.let { it + sourceOffset })
}

internal data class ContextVarMappingData(
  override val elementNameOffset: Int,
  override val elementNameLength: Int,
  override val generatedOffset: Int,
  override val generatedLength: Int,
) : Angular2TemplateTranspiler.ContextVarMapping

internal data class DirectiveVarMappingData(
  override val elementNameOffset: Int,
  override val elementNameLength: Int,
  override val directive: Angular2Directive,
  override val generatedOffset: Int,
  override val generatedLength: Int,
) : Angular2TemplateTranspiler.DirectiveVarMapping

private fun rangeToText(text: String, range: TextRange) =
  "«${text.substring(range.startOffset, range.endOffset)}» [${range.startOffset}]"

fun Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile.verifyMappings() {

  // check for unique mapping for types
  val typeMappings = MultiMap<Pair<PsiFile, TextRange>, TextRange>()

  fileMappings.forEach { (_, fileMappings) ->
    fileMappings.sourceMappings.forEach { mapping ->
      if (mapping.flags.contains(SourceMappingFlag.TYPES)) {
        val key = Pair(fileMappings.sourceFile, TextRange.create(mapping.sourceOffset, mapping.sourceOffset + mapping.sourceLength))
        typeMappings.putValue(key, TextRange.create(mapping.generatedOffset, mapping.generatedOffset + mapping.generatedLength))
      }
    }
  }

  val errors = mutableListOf<String>()

  typeMappings.entrySet().forEach { (key, generatedRanges) ->
    if (generatedRanges.size > 1) {
      errors.add("Duplicated mapping from source file ${key.first.name}: " + rangeToText(key.first.text, key.second) + " to generated file: " +
                 generatedRanges.joinToString { rangeToText(generatedCode, it) })
    }
  }

  if (errors.isNotEmpty()) {
    throw IllegalStateException(errors.joinToString("\n"))
  }
}
