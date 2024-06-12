package org.angular2.lang.html.tcb

import com.intellij.openapi.util.TextRange
import org.angular2.lang.html.Angular2HtmlFile

internal class Expression(builder: ExpressionBuilder.() -> Unit) {

  constructor(code: String, originalRange: TextRange? = null)
    : this({ append(code, originalRange) })

  constructor(id: Identifier)
    : this({ append(id) })

  private val code: StringBuilder

  private val sourceMappings: List<SourceMappingData>

  init {
    val expression = ExpressionBuilderImpl().apply(builder)
    code = expression.code
    sourceMappings = expression.sourceMappings
  }

  override fun toString(): String =
    code.toString()

  fun asTranspiledTemplate(
    templateFile: Angular2HtmlFile,
  ): Angular2TemplateTranspiler.TranspiledTemplate =
    object : Angular2TemplateTranspiler.TranspiledTemplate {
      override val templateFile: Angular2HtmlFile = templateFile
      override val generatedCode: String = code.toString()
      override val sourceMappings: List<Angular2TemplateTranspiler.SourceMapping> = this@Expression.sourceMappings
    }

  interface ExpressionBuilder {
    fun append(code: String, originalRange: TextRange? = null): ExpressionBuilder

    fun append(id: Identifier, originalRange: TextRange? = null): ExpressionBuilder

    fun append(expression: Expression): ExpressionBuilder

    fun append(expression: ExpressionBuilder.() -> Unit): ExpressionBuilder

    fun withSourceSpan(originalRange: TextRange?, builder: ExpressionBuilder.() -> Unit)

    fun withIgnoreDiagnostics(builder: ExpressionBuilder.() -> Unit)

    fun codeBlock(builder: BlockBuilder.() -> Unit)

    fun statements(builder: BlockBuilder.() -> Unit)

    fun newLine()
  }

  interface BlockBuilder : ExpressionBuilder {

    fun appendStatement(statement: Statement): BlockBuilder

    fun appendStatement(expression: ExpressionBuilder.() -> Unit): BlockBuilder

  }

  private class ExpressionBuilderImpl : ExpressionBuilder, BlockBuilder {
    val code = StringBuilder()

    val sourceMappings = mutableListOf<SourceMappingData>()

    private var ignoreDiagnostics = false

    override fun append(code: String, originalRange: TextRange?): ExpressionBuilder {
      if (originalRange != null) {
        sourceMappings.add(SourceMappingData(originalRange.startOffset, originalRange.length,
                                             this.code.length, code.length, ignoreDiagnostics))
      }
      this.code.append(code)
      return this
    }

    override fun append(id: Identifier, originalRange: TextRange?): ExpressionBuilder =
      append(id.toString(), originalRange)

    override fun append(expression: Expression): ExpressionBuilder {
      val offset = this.code.length
      expression.sourceMappings.mapTo(this.sourceMappings) { sourceMapping ->
        sourceMapping.copy(generatedOffset = sourceMapping.generatedOffset + offset,
                           ignoreDiagnostics = ignoreDiagnostics || sourceMapping.ignoreDiagnostics)
      }
      this.code.append(expression.code)
      return this
    }

    override fun append(expression: ExpressionBuilder.() -> Unit): ExpressionBuilder {
      append(Expression(expression))
      return this
    }

    override fun withSourceSpan(originalRange: TextRange?, builder: ExpressionBuilder.() -> Unit) {
      if (originalRange != null) {
        //val offset = this.code.length
        this.builder()
        // TODO this may be necessary only for error mapping
        //sourceMappings.add(SourceMappingData(originalRange.startOffset, originalRange.length, offset,
        //                                     this.code.length - offset, ignoreDiagnostics))
      }
      else {
        this.builder()
      }
    }

    override fun withIgnoreDiagnostics(builder: ExpressionBuilder.() -> Unit) {
      ignoreDiagnostics = true
      this.builder()
      ignoreDiagnostics = false
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

internal class Identifier(val name: String, val sourceSpan: TextRange? = null, val kind: ExpressionIdentifier? = null) {
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
  override val ignoreDiagnostics: Boolean,
) : Angular2TemplateTranspiler.SourceMapping {
  override fun offsetBy(generatedOffset: Int, sourceOffset: Int): Angular2TemplateTranspiler.SourceMapping =
    copy(sourceOffset = this.sourceOffset + sourceOffset, generatedOffset = this.generatedOffset + generatedOffset)
}
