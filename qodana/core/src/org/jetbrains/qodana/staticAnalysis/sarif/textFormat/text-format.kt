package org.jetbrains.qodana.staticAnalysis.sarif.textFormat

import com.intellij.openapi.util.text.StringUtil
import com.intellij.xml.util.XmlStringUtil
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.html2md.converter.HtmlNodeRenderer
import com.vladsch.flexmark.html2md.converter.HtmlNodeRendererHandler
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

private val htmlToMarkdownConverter by lazy { createHtmlToMarkdownConverter() }

private val markdownToHtmlParser by lazy { Parser.builder().build() }
private val markdownToHtmlRenderer by lazy { HtmlRenderer.builder().build() }

private fun createHtmlToMarkdownConverter(): FlexmarkHtmlConverter {
  val options = MutableDataSet().set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false)
  val preElementHtmlRenderer = PreElementHtmlRenderer(options)
  return FlexmarkHtmlConverter.builder(options)
    .htmlNodeRendererFactory {
      HtmlNodeRenderer {
        setOf(HtmlNodeRendererHandler(FlexmarkHtmlConverter.PRE_NODE, Element::class.java, preElementHtmlRenderer))
      }
    }
    .build()
}

fun htmlToMarkdown(html: String): String {
  return htmlToMarkdownConverter.convert(unwrapTables(html), -1)
}

private fun unwrapTables(html: String): String {
  if (!html.contains("<table", ignoreCase = true)) return html

  val document = Jsoup.parse(html)
  document.outputSettings().prettyPrint(false)
  // Reverse document order unwraps a nested table before the table that holds it.
  document.select("table").asReversed().forEach(::unwrapTable)
  return document.html()
}

private fun unwrapTable(table: Element) {
  val parent = table.parent() ?: return
  parent.insertChildren(table.siblingIndex(), flattenRows(table))
  table.remove()
}

private fun flattenRows(table: Element): List<Node> = buildList {
  // A table is a block element, so the first row must not join the text before the table.
  if (startsAfterText(table)) add(Element("br"))
  table.select("tr").forEach { row ->
    row.children().forEachIndexed { index, cell ->
      if (index > 0) add(TextNode(" "))
      addAll(cell.childNodes())
    }
    add(Element("br"))
  }
}

/** Tells if text comes directly before the table, without a line break between them. */
private fun startsAfterText(table: Element): Boolean {
  val previous = table.previousSibling()
  return previous is TextNode && !previous.isBlank
}

fun htmlToPlainText(html: String): String {
  val htmlElement = Jsoup.parse(html)

  val codes = htmlElement.select("code")
  codes.forEach {
    val text = it.text()
    it.text("'$text'")
  }

  val paragraphs = htmlElement.select("p")
  paragraphs.forEach {
    val text = it.text()
    it.text(System.lineSeparator() + text + System.lineSeparator())
  }

  return htmlElement.text()
}

fun markdownToHtml(markdown: String): String {
  val document = markdownToHtmlParser.parse(markdown)
  val html = markdownToHtmlRenderer.render(document)
  val formattedHtml = Jsoup.parse(html).apply {
    outputSettings().indentAmount(0)
  }.html()
  return formattedHtml
}

internal fun escapeExceptTag(input: String, tagName: String): String {
  val spanPattern =  Regex("(<$tagName[^>]*>|</$tagName>)")

  val builder = StringBuilder()
  val matches = spanPattern.findAll(input)

  var lastEnd = 0
  matches.forEach { matchResult ->
    if(matchResult.range.first != lastEnd) {
      val originalText = input.substring(lastEnd, matchResult.range.first)
      builder.append(XmlStringUtil.escapeString(unescape(originalText)))
    }
    builder.append(matchResult.value)

    lastEnd = matchResult.range.last + 1
  }
  if(lastEnd < input.length) {
    val originalText = input.substring(lastEnd, input.length)
    builder.append(XmlStringUtil.escapeString(unescape(originalText)))
  }

  return builder.toString()
}

internal fun escapeContentInTag(data: String, tagName: String): String {
  val pattern = Regex("<$tagName>(.*?)</$tagName>")
  return data.replace(pattern) { matchResult ->
    "<$tagName>${escapeExceptTag(matchResult.groupValues[1], "span")}</$tagName>"
  }
}

private fun unescape(text: String): String {
  return StringUtil.unescapeXmlEntities(text).replace("&nbsp;|&#32;".toRegex(), " ")
}