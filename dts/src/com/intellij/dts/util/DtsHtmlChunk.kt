package com.intellij.dts.util

import com.intellij.dts.DtsBundle
import com.intellij.dts.api.DtsPath
import com.intellij.dts.highlighting.DtsHighlightAnnotator
import com.intellij.dts.highlighting.DtsTextAttributes
import com.intellij.dts.lang.DtsLanguage
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsPHandle
import com.intellij.dts.util.DtsHtmlChunk.node
import com.intellij.lang.documentation.QuickDocHighlightingHelper
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.editor.richcopy.SyntaxInfoBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.jetbrains.annotations.PropertyKey

object DtsHtmlChunk {
  private const val BINDING_HTML_TAG = "!!html"
  private const val BINDING_PARAGRAPH_HTML_TAG = "!!phtml"

  private val bindingEndOfLineRx = Regex("\\s*\\n\\s*")
  private val bindingLineBrakeRx = Regex("\\s*\\n\\s*\\n")

  private val highlightAnnotator = DtsHighlightAnnotator()

  private val dtsKeywords = listOf(
    "#include", "#define",
    "/include/", "/dts-v1/", "/plugin/", "/memreserve/", "/delete-node/", "/delete-property/", "/omit-if-no-ref/"
  )

  private fun styledSpan(attr: DtsTextAttributes, text: String): @NlsSafe String {
    return QuickDocHighlightingHelper.getStyledFragment(text, attr.attribute)
  }

  /**
   * Generates bold definition name.
   */
  fun definitionName(key: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String): HtmlChunk {
    return HtmlChunk.fragment(
      bundle(key),
      HtmlChunk.text(": "),
    ).bold()
  }

  /**
   * Generates the colored html for a property name.
   */
  fun propertyName(name: String): HtmlChunk {
    return HtmlChunk.raw(styledSpan(DtsTextAttributes.PROPERTY_NAME, name))
  }

  /**
   * Generates the colored html for a node name. If possible prefer [node]
   * which can handle more cases.
   */
  fun nodeName(nodeName: String): HtmlChunk {
    val (name, addr) = DtsUtil.splitName(nodeName)
    if (addr == null) return HtmlChunk.raw(styledSpan(DtsTextAttributes.NODE_NAME, name))

    return HtmlChunk.fragment(
      HtmlChunk.raw(styledSpan(DtsTextAttributes.NODE_NAME, name)),
      HtmlChunk.text("@"),
      HtmlChunk.raw(styledSpan(DtsTextAttributes.NODE_UNIT_ADDR, addr))
    )
  }

  private fun pHandle(handle: DtsPHandle): HtmlChunk {
    val builder = HtmlBuilder()
    builder.append("&")

    val label = handle.dtsPHandleLabel
    if (label != null) {
      builder.appendRaw(styledSpan(DtsTextAttributes.LABEL, label.text))
    }

    val path = handle.dtsPHandlePath
    if (path != null) {
      builder.append("{")

      val segments = path.text.split("/").filter { it.isNotEmpty() }
      for (segment in segments) {
        builder.append("/")
        builder.append(nodeName(segment))
      }

      if (segments.isEmpty()) {
        builder.append("/")
      }

      builder.append("}")
    }

    return builder.toFragment()
  }

  /**
   * Generates the colored html for a node name. Can also handle references
   * and root nodes.
   */
  fun node(element: DtsNode): HtmlChunk {
    return when (element) {
      is DtsNode.Root -> HtmlChunk.text("/")
      is DtsNode.Sub -> nodeName(element.dtsName)
      is DtsNode.Ref -> pHandle(element.dtsHandle)
    }
  }

  /**
   * Generates the colored html for the node path.
   */
  fun path(path: DtsPath): HtmlChunk {
    val builder = HtmlBuilder()
    for (segment in path.segments) {
      builder.append("/")
      builder.append(nodeName(segment))
    }

    if (path.segments.isEmpty()) {
      builder.append("/")
    }

    return builder.toFragment()
  }

  fun bundle(key: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String): HtmlChunk {
    return HtmlChunk.raw(DtsBundle.message(key))
  }

  fun string(text: @NlsSafe String): HtmlChunk {
    return HtmlChunk.raw(styledSpan(DtsTextAttributes.STRING, text))
  }

  fun int(text: @NlsSafe String): HtmlChunk {
    return HtmlChunk.raw(styledSpan(DtsTextAttributes.NUMBER, text))
  }

  private fun tryParseDtsToHtml(project: Project, text: String): @NlsSafe String? {
    val fakePsiFile = PsiFileFactory.getInstance(project).createFileFromText(
      "comment.dtsi",
      DtsLanguage, text.trim(),
      false,
      false,
    )

    val errors = SyntaxTraverser.psiTraverser(fakePsiFile).traverse().filter {
      if (it !is PsiErrorElement) return@filter false

      // Ignore ... sometimes used to skip missing properties and ignore
      // errors at the end of the text. Probably just a missing semicolon.
      it.text != "..." && it.startOffset != fakePsiFile.endOffset
    }
    if (errors.isNotEmpty) return null

    // reformat the file, because whitespace could be messed up after loaded
    // from binding
    CodeStyleManager.getInstance(project).reformat(fakePsiFile, true)

    val holder = DtsAnnotationHolder()

    fakePsiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
      override fun visitElement(element: PsiElement) {
        if (element !is CompositeElement) {
          highlightAnnotator.annotate(element, holder)
        }

        super.visitElement(element)
      }
    })

    val scheme = EditorColorsManager.getInstance().schemeForCurrentUITheme
    val content = HtmlSyntaxInfoUtil.getHtmlContent(
      fakePsiFile,
      fakePsiFile.text,
      DtsAnnotationHolderIterator(holder.annotations, scheme),
      scheme,
      0,
      fakePsiFile.text.length,
    ) ?: return null

    return "<code>$content</code>"
  }

  private fun bindingHtml(project: Project, text: String): @NlsSafe String {
    if (text.startsWith(BINDING_HTML_TAG)) {
      return text.removePrefix(BINDING_HTML_TAG).trim()
    }

    val paragraphs = text.trim().split(bindingLineBrakeRx)

    val html = paragraphs.map { paragraph ->
      if (paragraph.startsWith(BINDING_PARAGRAPH_HTML_TAG)) {
        return@map paragraph.removePrefix(BINDING_PARAGRAPH_HTML_TAG).trim().replace(bindingEndOfLineRx, " ")
      }

      val couldBeDtsCode = paragraph.contains(";") || dtsKeywords.any { paragraph.contains(it) }
      if (couldBeDtsCode) {
        val html = tryParseDtsToHtml(project, paragraph)
        if (html != null) return@map html
      }

      StringUtil.escapeXmlEntities(paragraph.replace(bindingEndOfLineRx, " "))
    }

    return html.joinToString("<br/><br/>")
  }

  /**
   * Generates html from text which was loaded from a zephyr binding. If the
   * text starts with "!!html" it will be loaded as raw html. Otherwise, the
   * text is split into consecutive paragraphs and separated by two line
   * breaks. If a paragraph can be successfully parsed by the dts parser, it
   * is considered as dts code and will be colored and formatted accordingly.
   * If a paragraph starts with "!!phtml" it will be loaded as raw html.
   */
  fun binding(project: Project, text: @NlsSafe String): HtmlChunk {
    return HtmlChunk.raw(bindingHtml(project, text))
  }
}

private data class DtsAnnotation(val range: TextRange, val attribute: TextAttributesKey) {
  val startOffset = range.startOffset

  val endOffset = range.endOffset
}

private class DtsAnnotationHolder : DtsHighlightAnnotator.Holder {
  val annotations = mutableListOf<DtsAnnotation>()

  override fun newAnnotation(range: TextRange, attr: DtsTextAttributes) {
    annotations.add(DtsAnnotation(range, attr.attribute))
  }
}

private class DtsAnnotationHolderIterator(holder: Iterable<DtsAnnotation>, val scheme: EditorColorsScheme) : SyntaxInfoBuilder.RangeIterator {
  private val iterator = holder.iterator()
  private var annotation: DtsAnnotation? = null

  private val requireAnnotation: DtsAnnotation
    get() = requireNotNull(annotation) { "no annotation, check atEnd first" }

  override fun advance() {
    if (iterator.hasNext()) {
      annotation = iterator.next()
    }
  }

  override fun atEnd(): Boolean = !iterator.hasNext()

  override fun getRangeStart(): Int = requireAnnotation.startOffset

  override fun getRangeEnd(): Int = requireAnnotation.endOffset

  override fun getTextAttributes(): TextAttributes = scheme.getAttributes(requireAnnotation.attribute)

  override fun dispose() {}
}
