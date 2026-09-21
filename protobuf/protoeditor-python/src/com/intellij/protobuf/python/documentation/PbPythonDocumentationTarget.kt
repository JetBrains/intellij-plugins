package com.intellij.protobuf.python.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.markdown.utils.doc.DocMarkdownToHtmlConverter
import com.intellij.model.Pointer
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.protobuf.ide.highlighter.PbSyntaxHighlighter
import com.intellij.protobuf.lang.psi.PbCommentOwner
import com.intellij.protobuf.lang.psi.PbEnumDefinition
import com.intellij.protobuf.lang.psi.PbEnumValue
import com.intellij.protobuf.lang.psi.PbField
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbGroupDefinition
import com.intellij.protobuf.lang.psi.PbMapField
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.protobuf.lang.psi.PbOneofBody
import com.intellij.protobuf.lang.psi.PbOneofDefinition
import com.intellij.protobuf.lang.psi.PbSimpleField
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.lang.psi.PbTypeName
import com.intellij.protobuf.lang.psi.util.PbPsiUtil
import com.intellij.psi.createSmartPointer
import com.intellij.ui.ColorUtil
import com.jetbrains.python.documentation.PythonDocumentationProvider
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyTargetExpression
import java.awt.Font

/**
 * Quick Documentation for a Python element that refers to a symbol of a `.proto` file.
 *
 * Shows the Python documentation of the variable, when there is one, followed by the `.proto` declaration of [pbSymbol].
 */
internal class PbPythonDocumentationTarget(pbSymbol: PbSymbol, sourceElement: PyElement) : DocumentationTarget {

  private val symbolPtr = pbSymbol.createSmartPointer()
  private val sourcePtr = sourceElement.createSmartPointer()

  val pyAnchor: PyElement?
    get() = sourcePtr.element

  override fun createPointer(): Pointer<out DocumentationTarget> = Pointer {
    PbPythonDocumentationTarget(
      symbolPtr.element ?: return@Pointer null,
      sourcePtr.element ?: return@Pointer null
    )
  }

  override fun computePresentation(): TargetPresentation {
    @NlsSafe val name = symbolPtr.element?.name ?: ""
    return TargetPresentation.builder(name).presentation()
  }

  override val navigatable: Navigatable?
    get() = symbolPtr.element as? Navigatable

  override fun computeDocumentation(): DocumentationResult? {
    val element = symbolPtr.element ?: return null

    val pyDoc = findPythonVariable(sourcePtr.element)?.let {
      PythonDocumentationProvider().generateDoc(it, it)
    }
    val protoDoc = buildProtoHtml(element)

    @NlsSafe
    val result = if (!pyDoc.isNullOrBlank()) {
      "${pyDoc.substringBeforeLast("</body>")}<hr>$protoDoc</body></html>"
    }
    else {
      "<html><body>$protoDoc</body></html>"
    }

    return DocumentationResult.documentation(result)
  }

  /**
   * Finds the Python assignment target to document together with the `.proto` symbol.
   * Returns `null` for a field read, because the user code has no declaration for it.
   */
  private fun findPythonVariable(source: PyElement?): PyTargetExpression? = when (source) {
    is PyTargetExpression -> source
    is PyReferenceExpression if !source.isQualified ->
      source.reference.multiResolve(false)
        .mapNotNull { it.element as? PyTargetExpression }
        .lastOrNull()
    else -> null
  }

  private fun buildProtoHtml(symbol: PbSymbol): String = buildString {
    renderComments(this, symbol)

    @NlsSafe
    val contentHtml = buildString {
      when (symbol) {
        is PbMessageType -> renderMessage(this, symbol)
        is PbEnumDefinition -> renderEnum(this, symbol)
        is PbEnumValue -> renderEnumValue(this, symbol)
        is PbField -> renderField(this, symbol)
      }
    }

    if (contentHtml.isNotBlank()) {
      DocumentationMarkup.DEFINITION_ELEMENT
        .child(
          DocumentationMarkup.PRE_ELEMENT
            .style(getStyle(PbSyntaxHighlighter.IDENTIFIER))
            .addRaw(contentHtml)
        )
        .appendTo(this)
    }

    val pbFile = symbol.containingFile as? PbFile
    if (pbFile != null) {
      @NlsSafe val fileName = pbFile.name
      DocumentationMarkup.BOTTOM_ELEMENT
        .child(HtmlChunk.text(fileName).code())
        .appendTo(this)
    }
  }

  private fun renderComments(out: StringBuilder, element: PbSymbol) {
    if (element !is PbCommentOwner) return

    @NlsSafe
    val content = element.comments.joinToString("\n") { comment ->
      normalizeCommentText(comment.text)
    }

    if (content.isNotBlank()) {
      DocumentationMarkup.CONTENT_ELEMENT
        .addRaw(DocMarkdownToHtmlConverter.convert(element.project, content))
        .appendTo(out)
    }
  }

  private fun normalizeCommentText(text: String): String {
    val trimmed = text.trim()

    fun String.trimLines(): String =
      lines().joinToString("\n") { it.trim() }.trim()

    return when {
      trimmed.startsWith("//") -> trimmed.removePrefix("//").trimLines()
      trimmed.startsWith("/*") && trimmed.endsWith("*/") ->
        trimmed
          .removePrefix("/*")
          .removeSuffix("*/")
          .lines()
          .joinToString("\n") { it.trimStart().removePrefix("*").trim() }
          .trim()
      else -> trimmed.trimLines()
    }
  }

  private fun renderMessage(out: StringBuilder, message: PbMessageType, indent: Int = 0) {
    out.repeat(" ", indent)
    val keyword = if (message is PbGroupDefinition) "group" else "message"
    renderHighlighted(out, keyword, PbSyntaxHighlighter.KEYWORD)
    out.append(" ")
    out.append(message.name ?: "")

    // A oneof renders its own members
    val members = message.symbols.filterNot { PbPsiUtil.isGeneratedMapEntry(it) || it.parent is PbOneofBody }
    if (members.isEmpty()) {
      out.append(" {}")
      return
    }
    out.appendLine(" {")

    val nextIndent = indent + 2
    var previousWasNested = false

    for ((index, member) in members.withIndex()) {
      when (member) {
        is PbMessageType -> {
          if (index > 0) out.appendLine()
          renderMessage(out, member, nextIndent)
          previousWasNested = true
        }
        is PbEnumDefinition -> {
          if (index > 0) out.appendLine()
          renderEnum(out, member, nextIndent)
          previousWasNested = true
        }
        is PbOneofDefinition -> {
          if (index > 0) out.appendLine()
          renderOneof(out, member, nextIndent)
          previousWasNested = true
        }
        is PbField -> {
          if (previousWasNested) out.appendLine()
          out.repeat(" ", nextIndent)
          renderField(out, member)
          previousWasNested = false
        }
        else -> continue
      }
      out.appendLine()
    }
    out.repeat(" ", indent)
    out.append("}")
  }

  private fun renderEnum(out: StringBuilder, enum: PbEnumDefinition, indent: Int = 0) {
    out.repeat(" ", indent)
    renderHighlighted(out, "enum", PbSyntaxHighlighter.KEYWORD)
    out.append(" ")
    out.append(enum.name ?: "")

    val values = enum.enumValues
    if (values.isEmpty()) {
      out.append(" {}")
      return
    }
    out.appendLine(" {")

    val nextIndent = indent + 2
    for (value in values) {
      renderEnumValue(out, value, nextIndent)
      out.appendLine()
    }
    out.repeat(" ", indent)
    out.append("}")
  }

  private fun renderEnumValue(out: StringBuilder, enumValue: PbEnumValue, indent: Int = 0) {
    out.repeat(" ", indent)
    renderHighlighted(out, enumValue.name ?: "", PbSyntaxHighlighter.ENUM_VALUE)
    enumValue.numberValue?.let {
      out.append(" = ")
      renderHighlighted(out, it.text, PbSyntaxHighlighter.NUMBER)
    }
  }

  private fun renderOneof(out: StringBuilder, oneof: PbOneofDefinition, indent: Int = 0) {
    out.repeat(" ", indent)
    renderHighlighted(out, "oneof", PbSyntaxHighlighter.KEYWORD)
    out.append(" ")
    out.append(oneof.name ?: "")

    val members = oneof.body?.children?.filter { it is PbSimpleField || it is PbGroupDefinition }.orEmpty()
    if (members.isEmpty()) {
      out.append(" {}")
      return
    }
    out.appendLine(" {")

    val nextIndent = indent + 2
    for (member in members) {
      when (member) {
        is PbGroupDefinition -> renderMessage(out, member, nextIndent)
        is PbSimpleField -> {
          out.repeat(" ", nextIndent)
          renderField(out, member)
        }
      }
      out.appendLine()
    }
    out.repeat(" ", indent)
    out.append("}")
  }

  private fun renderField(out: StringBuilder, field: PbField) {
    field.declaredLabel?.let {
      renderHighlighted(out, it.text, PbSyntaxHighlighter.KEYWORD)
      out.append(" ")
    }
    when (field) {
      is PbSimpleField -> renderTypeName(out, field.typeName)
      is PbMapField -> {
        renderHighlighted(out, "map", PbSyntaxHighlighter.KEYWORD)
        out.append("<")
        renderTypeName(out, field.keyType)
        out.append(", ")
        renderTypeName(out, field.valueType)
        out.append(">")
      }
    }
    out.append(" ")
    out.append(field.name ?: "")
    field.fieldNumber?.let {
      out.append(" = ")
      renderHighlighted(out, it.text, PbSyntaxHighlighter.NUMBER)
    }
  }

  private fun renderTypeName(out: StringBuilder, typeName: PbTypeName?) {
    if (typeName == null) return

    @NlsSafe val shortName = typeName.shortName
    when (val resolved = typeName.symbolPath.reference?.resolve()) {
      // A scalar type such as `int32` does not resolve to a symbol
      null -> renderHighlighted(out, shortName, PbSyntaxHighlighter.KEYWORD)
      is PbSymbol -> {
        val qualifiedName = resolved.qualifiedName
        if (qualifiedName == null || resolved == symbolPtr.element) {
          out.append(shortName)
          return
        }
        val fileUrl = resolved.containingFile?.virtualFile?.url
        val link = if (fileUrl != null) "$fileUrl:$qualifiedName" else qualifiedName.toString()
        HtmlChunk.link("psi_element://$link", HtmlChunk.text(shortName)).appendTo(out)
      }
      else -> out.append(shortName)
    }
  }

  private fun getStyle(attributesKey: TextAttributesKey): String {
    val textAttributes = EditorColorsManager.getInstance().globalScheme.getAttributes(attributesKey)
    val styles = buildList {
      textAttributes.foregroundColor?.let {
        add("color: ${ColorUtil.toHtmlColor(it)}")
      }
      if (textAttributes.fontType and Font.BOLD != 0) {
        add("font-weight: bold")
      }
    }
    return styles.joinToString("; ")
  }

  private fun renderHighlighted(out: StringBuilder, @NlsSafe text: String, attributesKey: TextAttributesKey) {
    HtmlChunk.span(getStyle(attributesKey))
      .addText(text)
      .appendTo(out)
  }
}
