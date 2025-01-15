package org.intellij.prisma.ide.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.lang.documentation.QuickDocHighlightingHelper
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.prisma.lang.PrismaLanguage
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.psi.PrismaElementTypes.TRIPLE_COMMENT
import java.net.URI

internal const val NO_WRAP = "white-space: nowrap"
internal const val SECTION_START_NO_WRAP = "<td valign='top' style='$NO_WRAP'>"

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
  append(DocumentationMarkup.SECTION_END)
  block()
  if (!endsWith(DocumentationMarkup.SECTION_END)) {
    append(DocumentationMarkup.SECTION_END)
  }
  append("</tr>")
  append("\n")
}

internal fun StringBuilder.cell(noWrap: Boolean = true, block: StringBuilder.() -> Unit) {
  append(if (noWrap) SECTION_START_NO_WRAP else DocumentationMarkup.SECTION_START)
  block()
  if (!endsWith(DocumentationMarkup.SECTION_END)) {
    append(DocumentationMarkup.SECTION_END)
  }
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
    QuickDocHighlightingHelper.getStyledCodeFragment(project, PrismaLanguage, text.replace("\t", "  "))
  }
  catch (e: ProcessCanceledException) {
    throw e
  }
  catch (_: Exception) {
    text
  }
}

@NlsSafe
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
  ),
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

val PsiElement.isDocComment: Boolean
  get() = elementType == TRIPLE_COMMENT || elementType == DOC_COMMENT

/**
 * Returns a comment block of triple comments only, e.g.,
 *
 * ```
 * /// It's a multiline
 * /// triple
 * /// comment
 * model User {
 *   ...
 * }
 * ```
 */
fun PsiElement.collectPrecedingInlineDocComments(strict: Boolean = true): List<PsiComment> {
  val initialComment = if (strict) prevDocComment() else this
  if (initialComment.elementType != TRIPLE_COMMENT) {
    return emptyList()
  }

  return generateSequence(initialComment) { it.prevDocComment().takeIf { it.elementType == TRIPLE_COMMENT } }
    .filterIsInstance<PsiComment>()
    .toList()
    .asReversed()
}

fun getMultilineDocCommentText(docComment: PrismaDocComment): String {
  val sb = StringBuilder()

  for (child in docComment.firstChild.siblings()) {
    val elementType = child.node.elementType
    val text = child.text

    if (elementType != DOC_COMMENT_START &&
        elementType != DOC_COMMENT_LEADING_ASTERISK &&
        elementType != DOC_COMMENT_END) {
      val newLinesCount = if (child is PsiWhiteSpace) StringUtil.countNewLines(text) else 0
      if (newLinesCount > 0) {
        sb.append(StringUtil.repeatSymbol('\n', newLinesCount))
      }
      else {
        sb.append(text)
      }
    }
  }

  return sb.toString().trim(' ', '\r', '\n', '\t')
}

fun buildDocComments(element: PsiElement): List<PrismaDocComment> {
  return PsiTreeUtil
    .findChildrenOfType(element, PsiComment::class.java)
    .filter { it.isDocComment && !it.isTrailingComment }
    .let { groupDocComments(it) }
}

fun groupDocComments(comments: List<PsiComment>): List<PrismaDocComment> {
  if (comments.isEmpty()) {
    return emptyList()
  }

  val docComments = mutableListOf<PrismaDocComment>()
  var i = comments.lastIndex
  while (i >= 0) {
    val comment = comments[i]
    if (comment is PrismaDocComment && comment.elementType == DOC_COMMENT) {
      docComments.add(comment)
      i--
      continue
    }

    val block = comment.collectPrecedingInlineDocComments(false)
    if (block.isNotEmpty()) {
      docComments.add(PrismaCompoundInlineDocumentationComment(block))
      i = comments.indexOf(block.first()) - 1
    }
    else {
      i--
    }
  }
  return docComments
}