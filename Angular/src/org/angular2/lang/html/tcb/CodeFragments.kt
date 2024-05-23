package org.angular2.lang.html.tcb

import com.intellij.openapi.util.TextRange


internal class Expression(builder: ExpressionBuilder.() -> Unit) {

  constructor(code: String, originalRange: TextRange? = null)
    : this({ append(code, originalRange) })

  constructor(id: Identifier)
    : this({ append(id) })

  constructor(originalRange: TextRange?, builder: ExpressionBuilder.() -> Unit)
    : this({ withSourceSpan(originalRange, builder) })

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
    sourceCode: String,
    imports: List<Angular2TemplateTranspiler.TypeScriptImport>,
  ): Angular2TemplateTranspiler.TranspiledTemplate =
    object : Angular2TemplateTranspiler.TranspiledTemplate {
      override val sourceCode: String = sourceCode
      override val generatedCode: String = code.toString()
      override val sourceMappings: List<Angular2TemplateTranspiler.SourceMapping> = this@Expression.sourceMappings
      override val imports: List<Angular2TemplateTranspiler.TypeScriptImport> = imports
    }

  interface ExpressionBuilder {
    fun append(code: String, originalRange: TextRange? = null): ExpressionBuilder

    fun append(id: Identifier, originalRange: TextRange? = null): ExpressionBuilder

    fun append(expression: Expression): ExpressionBuilder

    fun append(expression: ExpressionBuilder.() -> Unit): ExpressionBuilder

    fun withSourceSpan(originalRange: TextRange?, builder: ExpressionBuilder.() -> Unit)

    fun withIgnoreDiagnostics(builder: ExpressionBuilder.() -> Unit)

    fun codeBlock(builder: BlockBuilder.() -> Unit)

    fun newLine()
  }

  interface BlockBuilder : ExpressionBuilder {

    fun appendStatement(statement: Statement): BlockBuilder

    fun appendStatement(expression: ExpressionBuilder.() -> Unit): BlockBuilder

  }

  private class ExpressionBuilderImpl : ExpressionBuilder, BlockBuilder {
    val code = StringBuilder()

    val sourceMappings = mutableListOf<SourceMappingData>()

    override fun append(code: String, originalRange: TextRange?): ExpressionBuilder {
      if (originalRange != null) {
        sourceMappings.add(SourceMappingData(originalRange.startOffset, originalRange.length,
                                             this.code.length, code.length))
      }
      this.code.append(code)
      return this
    }

    override fun append(id: Identifier, originalRange: TextRange?): ExpressionBuilder =
      append(id.toString(), originalRange)

    override fun append(expression: Expression): ExpressionBuilder {
      val offset = this.code.length
      expression.sourceMappings.mapTo(this.sourceMappings) { sourceMapping ->
        sourceMapping.withOffset(offset)
      }
      this.code.append(expression.code)
      return this
    }

    override fun append(expression: ExpressionBuilder.() -> Unit): ExpressionBuilder {
      append(Expression(expression))
      return this
    }

    override fun withSourceSpan(originalRange: TextRange?, builder: ExpressionBuilder.() -> Unit) {
      // TODO - ignore for now, determine whether it makes sense to register the range
      this.builder()
    }

    override fun withIgnoreDiagnostics(builder: ExpressionBuilder.() -> Unit) {
      // TODO - ignore for now, add a filter later on
      this.builder()
    }

    override fun codeBlock(builder: BlockBuilder.() -> Unit) {
      append("{\n")
      this.builder()
      append("}")
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

internal class Identifier constructor(val name: String, val sourceSpan: TextRange? = null, val kind: ExpressionIdentifier? = null) {
  override fun toString(): String = name
}

internal class Statement private constructor(val expression: Expression) {
  constructor(builder: Expression.ExpressionBuilder.() -> Unit) : this(Expression(builder))
}

private data class SourceMappingData(
  override val sourceOffset: Int,
  override val sourceLength: Int,
  override val generatedOffset: Int,
  override val generatedLength: Int,
) : Angular2TemplateTranspiler.SourceMapping {
  fun withOffset(offset: Int) =
    copy(generatedOffset = generatedOffset + offset)
}
