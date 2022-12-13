package org.intellij.prisma.ide.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.prisma.lang.parser.PrismaParserDefinition
import org.intellij.prisma.lang.psi.PrismaDocumentationOwner
import org.intellij.prisma.lang.psi.PrismaElementFactory
import org.intellij.prisma.lang.psi.skipWhitespacesBackwardWithoutNewLines
import org.intellij.prisma.lang.psi.skipWhitespacesForwardWithoutNewLines
import java.net.URI

internal fun stripDocCommentBlockPrefixes(text: String): String {
  return text.trimMargin("///").trimIndent()
}

internal fun stripDocCommentLinePrefix(text: String): String {
  return text.removePrefix("///").trim()
}

internal fun StringBuilder.definition(block: StringBuilder.() -> Unit) {
  append(DocumentationMarkup.DEFINITION_START)
  block()
  append(DocumentationMarkup.DEFINITION_END)
  append("\n")
}

internal fun StringBuilder.sections(block: StringBuilder.() -> Unit) {
  append(DocumentationMarkup.SECTIONS_START)
  block()
  append(DocumentationMarkup.SECTIONS_END)
  append("\n")
}

internal fun StringBuilder.section(header: String, block: StringBuilder.() -> Unit) {
  append(DocumentationMarkup.SECTION_HEADER_START)
  append(header)
  append(DocumentationMarkup.SECTION_SEPARATOR)
  block()
  append(DocumentationMarkup.SECTION_END)
  append("\n")
}

internal fun StringBuilder.section(header: String, td: HtmlChunk, block: StringBuilder.() -> Unit) {
  append(DocumentationMarkup.SECTION_HEADER_START)
  append(header)
  append(DocumentationMarkup.SECTION_END)
  td.appendTo(this)
  block()
  append(DocumentationMarkup.SECTION_END)
  append("\n")
}

internal fun StringBuilder.content(block: StringBuilder.() -> Unit) {
  append(DocumentationMarkup.CONTENT_START)
  block()
  append(DocumentationMarkup.CONTENT_END)
  append("\n")
}

internal fun StringBuilder.documentationComment(element: PsiElement?) {
  val comment = (element as? PrismaDocumentationOwner)?.docComment ?: return
  val rendered = PrismaDocumentationRenderer(comment).render() ?: return
  content {
    append(rendered)
  }
}

internal fun toHtml(project: Project, text: String): String {
  return try {
    toHtml(project, text, EditorColorsManager.getInstance().globalScheme)
  }
  catch (e: ProcessCanceledException) {
    throw e
  }
  catch (e: Exception) {
    text
  }
}

internal fun toHtml(project: Project, text: String, scheme: EditorColorsScheme): String {
  val code = text.trimIndent().replace("\t", "  ")
  val file = PrismaElementFactory.createFile(project, code)
  return HtmlSyntaxInfoUtil
           .getHtmlContent(file, code, null, scheme, 0, code.length)?.toString() ?: text
}

fun documentationMarkdownToHtml(markdown: String?): String? {
  if (markdown.isNullOrBlank()) {
    return null
  }

  val flavour = PrismaMarkdownFlavourDescriptor()
  val root = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
  return HtmlGenerator(markdown, root, flavour).generateHtml()
}

private class PrismaMarkdownFlavourDescriptor(
  private val base: MarkdownFlavourDescriptor = GFMFlavourDescriptor(
    useSafeLinks = false,
    absolutizeAnchorLinks = true
  )
) : MarkdownFlavourDescriptor by base {

  override fun createHtmlGeneratingProviders(linkMap: LinkMap, baseURI: URI?): Map<IElementType, GeneratingProvider> {
    val generatingProviders = HashMap(base.createHtmlGeneratingProviders(linkMap, null))
    // Filter out MARKDOWN_FILE to avoid producing unnecessary <body> tags
    generatingProviders.remove(MarkdownElementTypes.MARKDOWN_FILE)
    return generatingProviders
  }
}

val PsiElement?.hasTrailingComment: Boolean
  get() = skipWhitespacesForwardWithoutNewLines() is PsiComment

val PsiElement?.isTrailingComment: Boolean
  get() {
    if (this !is PsiComment) {
      return false
    }
    val prev = skipWhitespacesBackwardWithoutNewLines()
    return prev != null && prev !is PsiWhiteSpace
  }

val PsiElement?.trailingDocComment: PsiComment?
  get() = (skipWhitespacesForwardWithoutNewLines() as? PsiComment)?.takeIf { it.isDocComment }

fun PsiElement.prevDocComment(includeTrailing: Boolean = false): PsiComment? {
  var prev = prevSibling
  if (prev is PsiWhiteSpace && StringUtil.countNewLines(prev.text) <= 1) prev = prev.prevSibling
  return prev?.takeIf { it.isDocComment && (includeTrailing || !it.isTrailingComment) } as? PsiComment
}

fun PsiElement.nextDocComment(includeTrailing: Boolean = false): PsiComment? {
  var next = nextSibling
  if (next is PsiWhiteSpace && StringUtil.countNewLines(next.text) <= 1) next = next.nextSibling
  return next?.takeIf { it.isDocComment && (includeTrailing || !it.isTrailingComment) } as? PsiComment
}

val PsiElement.isDocComment
  get() = elementType == PrismaParserDefinition.DOC_COMMENT

fun PsiElement.collectPrecedingDocComments(strict: Boolean = true): List<PsiComment> =
  generateSequence(if (strict) prevDocComment() else this) { it.prevDocComment() }
    .filterIsInstance<PsiComment>()
    .toList()
    .asReversed()