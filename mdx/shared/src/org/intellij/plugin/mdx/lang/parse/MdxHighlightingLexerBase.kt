package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMConstraints
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.providers.CodeBlockProvider
import org.intellij.markdown.parser.markerblocks.providers.HtmlBlockProvider
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.impl.InlineLinkParser
import org.intellij.plugins.markdown.lang.parser.MarkdownDefaultMarkerProcessor
import java.net.URI

object MdxFlavourDescriptor : CommonMarkFlavourDescriptor() {
  private val myGfmFlavourDescriptor: GFMFlavourDescriptor = GFMFlavourDescriptor()

  override val markerProcessorFactory: MarkerProcessorFactory get() = MdxProcessFactory

  override val sequentialParserManager: SequentialParserManager = object : SequentialParserManager() {
    override fun getParserSequence(): List<SequentialParser> {
      val parsers = myGfmFlavourDescriptor.sequentialParserManager.getParserSequence()
      val inlineLinkIndex = parsers.indexOfFirst { it is InlineLinkParser }
      val insertionIndex = if (inlineLinkIndex == -1) parsers.size else inlineLinkIndex
      return buildList(parsers.size + 1) {
        addAll(parsers.subList(0, insertionIndex))
        add(MdxInlineJsxParser())
        addAll(parsers.subList(insertionIndex, parsers.size))
      }
    }
  }

  override fun createHtmlGeneratingProviders(linkMap: LinkMap, baseURI: URI?): Map<IElementType, GeneratingProvider> {
    return myGfmFlavourDescriptor.createHtmlGeneratingProviders(linkMap, baseURI)
  }

  override fun createInlinesLexer(): MarkdownLexer {
    return myGfmFlavourDescriptor.createInlinesLexer()
  }
}

private object MdxProcessFactory : MarkerProcessorFactory {
  override fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*> {
    return MdxMarkerProcessor(productionHolder, GFMConstraints.BASE)
  }
}

private class MdxMarkerProcessor(
  productionHolder: ProductionHolder,
  constraints: MarkdownConstraints,
) :
  MarkdownDefaultMarkerProcessor(productionHolder, constraints) {

  override fun getMarkerBlockProviders(): List<MarkerBlockProvider<StateInfo>> =
    buildList {
      add(JsxBlockProvider())
      addAll(super.getMarkerBlockProviders())
      removeIf { it is HtmlBlockProvider }
      removeIf { it is CodeBlockProvider }
    }
}
