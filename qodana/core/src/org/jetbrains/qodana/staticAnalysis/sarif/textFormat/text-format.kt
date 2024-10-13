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
  return htmlToMarkdownConverter.convert(html, -1)
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