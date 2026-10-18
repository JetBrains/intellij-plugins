package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.ParagraphMarkerBlock
import org.intellij.markdown.parser.markerblocks.providers.CodeBlockProvider
import org.intellij.markdown.parser.markerblocks.providers.HtmlBlockProvider
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.impl.InlineLinkParser
import org.intellij.plugins.markdown.lang.parser.MarkdownDefaultMarkerProcessor
import org.intellij.plugins.markdown.lang.parser.blocks.CodeFenceMarkerProvider
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
        add(MdxInlineElementParser())
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
    return MdxMarkerProcessor(productionHolder, MdxMarkdownConstraints.BASE)
  }
}

private class MdxMarkerProcessor(
  productionHolder: ProductionHolder,
  constraints: MarkdownConstraints,
) :
  MarkdownDefaultMarkerProcessor(productionHolder, constraints) {
  private val ownership = MdxMarkdownOwnership(::getMarkerBlockProviders)
  private val providers by lazy {
    buildList {
      add(MdxBlockProvider(ownership))
      add(MdxCodeFenceProvider())
      addAll(super.getMarkerBlockProviders())
      removeIf { it is HtmlBlockProvider }
      removeIf { it is CodeBlockProvider }
      removeIf { it is CodeFenceMarkerProvider && it !is MdxCodeFenceProvider }
    }
  }

  override fun updateStateInfo(pos: LookaheadText.Position) {
    super.updateStateInfo(pos)
    if (pos.offsetInCurrentLine > 0) return
    val constraints = if (pos.offsetInCurrentLine == -1) stateInfo.nextConstraints else stateInfo.currentConstraints
    val fenceOpener = ownership.observeLine(pos, constraints) ?: return
    for (marker in markersStack) {
      if (marker is MdxJsxBlockMarkerBlock) marker.addMarkdownOpacity(fenceOpener)
    }
  }

  override fun createNewMarkerBlocks(pos: LookaheadText.Position, productionHolder: ProductionHolder): List<MarkerBlock> {
    val owner = markersStack.lastOrNull() as? MdxJsxBlockMarkerBlock
    val openingStart = owner?.takeOpeningParagraphStart()
    if (openingStart != null) {
      return listOf(createOpeningParagraph(owner, openingStart, pos))
    }
    val blocks = super.createNewMarkerBlocks(pos, productionHolder)
    val block = blocks.singleOrNull()
    if (block is MdxJsxBlockMarkerBlock) {
      val start = block.takeOpeningParagraphStart() ?: return blocks
      return blocks + createOpeningParagraph(block, start, pos)
    }
    if (block is ParagraphMarkerBlock) {
      val paragraphOwner = markersStack.filterIsInstance<MdxJsxBlockMarkerBlock>().lastOrNull() ?: return blocks
      return listOf(paragraphOwner.createParagraph(
        pos.offset, pos.nextLineOrEofOffset, block.getBlockConstraints(), block.interruptsParagraph,
        paragraphObserver(paragraphOwner),
      ))
    }
    return blocks
  }

  private fun createOpeningParagraph(owner: MdxJsxBlockMarkerBlock, start: Int, pos: LookaheadText.Position): MarkerBlock {
    val constraints = owner.getBlockConstraints()
    return owner.createParagraph(start, pos.nextLineOrEofOffset, constraints, { next, nextConstraints ->
      nextConstraints.types.size <= constraints.types.lastIndexOf('>') ||
      providers.any { it.interruptsParagraph(next, nextConstraints) }
    }, paragraphObserver(owner))
  }

  private fun paragraphObserver(owner: MdxJsxBlockMarkerBlock): (LookaheadText.Position) -> Unit {
    val owners = markersStack.filterIsInstance<MdxJsxBlockMarkerBlock>().toMutableList()
    if (owners.lastOrNull() !== owner) owners.add(owner)
    return { pos ->
      for (index in owners.indices.reversed()) owners[index].observeParagraphLine(pos)
    }
  }

  override fun populateConstraintsTokens(
    pos: LookaheadText.Position,
    constraints: MarkdownConstraints,
    productionHolder: ProductionHolder,
  ) {
    super.populateConstraintsTokens(pos, toGfmConstraints(constraints), productionHolder)
  }

  override fun populateContinuedConstraintsTokens(
    pos: LookaheadText.Position,
    constraints: MarkdownConstraints,
    productionHolder: ProductionHolder,
  ) {
    super.populateContinuedConstraintsTokens(pos, toGfmConstraints(constraints), productionHolder)
  }

  private fun toGfmConstraints(constraints: MarkdownConstraints): MarkdownConstraints {
    val markdown = if (constraints is MdxJsxMarkdownConstraints) constraints.markdownConstraints() else constraints
    return if (markdown is MdxMarkdownConstraints) markdown.asGfmConstraints() else markdown
  }

  override fun getMarkerBlockProviders(): List<MarkerBlockProvider<StateInfo>> = providers
}
