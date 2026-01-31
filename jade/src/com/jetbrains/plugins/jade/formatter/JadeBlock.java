// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.formatter;

import com.intellij.embedding.TemplateMasqueradingLexer;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.BlockEx;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageFormatting;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.formatter.common.InjectedLanguageBlockWrapper;
import com.intellij.psi.formatter.xml.AbstractXmlBlock;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.impl.source.xml.XmlTokenImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.lexer.IndentUtil;
import com.jetbrains.plugins.jade.lexer.JadeEmbeddedTokenTypesWrapper;
import com.jetbrains.plugins.jade.lexer.JadeEmbeddedTokenTypesWrapperForCssStylesheet;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import com.jetbrains.plugins.jade.psi.impl.JadeBlockImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeCaseStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeCommentImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeConditionalElseImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeConditionalStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeFilterImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeForStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeIncludeStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeJSCodeLineImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeJSStatementImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeTagImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeWhenStatementImpl;
import com.jetbrains.plugins.jade.psi.stubs.JadeStubElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JadeBlock extends TemplateLanguageBlock implements BlockEx {
  private static final Set<String> ourTagNamesWithChildIndentByEnter = Set.of(
    "div", "ol", "ul", "script", "style", "body", "head", "table", "tbody", "form", "block", "html"
  );

  private final @NotNull TemplateLanguageBlockFactory myBlockFactory;
  protected final @NotNull CodeStyleSettings mySettings;
  private final Indent myIndent;

  protected JadeBlock(@NotNull ASTNode node,
                      @Nullable Wrap wrap,
                      @Nullable Alignment alignment,
                      @NotNull TemplateLanguageBlockFactory blockFactory,
                      @NotNull CodeStyleSettings settings,
                      @Nullable List<DataLanguageBlockWrapper> foreignChildren, Indent indent) {
    super(node, wrap, alignment, blockFactory, settings, foreignChildren);
    myBlockFactory = blockFactory;
    mySettings = settings;
    myIndent = indent;
  }

  @Override
  protected IElementType getTemplateTextElementType() {
    return JadeTokenTypes.JS_CODE_BLOCK;
  }

  @Override
  public @Nullable Language getLanguage() {
    PsiElement psi = myNode.getPsi();
    if (psi instanceof LeafPsiElement) {
      psi = psi.getParent();
    }
    return psi.getLanguage();
  }

  @Override
  protected List<Block> buildChildren() {
    if (JadeTokenTypes.COMMENTS.contains(myNode.getElementType()) || JadeTokenTypes.TEXT_SET.contains(myNode.getElementType())) {
      return buildTextChildrenBlocks();
    }

    final ArrayList<Block> result = new ArrayList<>();

    ASTNode child = myNode.getFirstChildNode();
    while (child != null) {
      Block blockForAChild = getBlockForAChild(child);
      if (blockForAChild != null) {
        result.add(blockForAChild);
      }

      child = child.getTreeNext();
    }

    return result;
  }

  protected @Nullable Block getBlockForAChild(@NotNull ASTNode child) {
    if (AbstractXmlBlock.containsWhiteSpacesOnly(child) || child.getTextLength() <= 0) {
      return null;
    }

    final IElementType childType = child.getElementType();

    Language injectedLang = getInjectedLanguageForElementType(childType);
    if (injectedLang != null) {
      final FormattingModelBuilder builder = LanguageFormatting.INSTANCE.forContext(injectedLang, child.getPsi());
      if (builder != null) {
        final FormattingModel childModel = builder.createModel(FormattingContext.create(child.getPsi(), getSettings()));

        return new InjectedLanguageBlockWrapper(childModel.getRootBlock(), 0, null, getIndentForChildScript());
      }
      else {
        return new JadeBlock(child, null, null, myBlockFactory, mySettings, null, Indent.getNormalIndent());
      }
    }

    Indent childIndent = getChildIndentByElementType(child, childType);

    if (childType == JadeTokenTypes.JS_META_CODE) {
      return new JadeMetaJsBlock(child, null, null, myBlockFactory, mySettings, null, childIndent);
    }

    return new JadeBlock(child, null, null, myBlockFactory, mySettings, null, childIndent);
  }

  protected Indent getChildIndentByElementType(@NotNull ASTNode child, IElementType childType) {
    Indent childIndent;
    if (shouldIndentChildren(myNode) &&
        (childType == JadeElementTypes.TAG ||
         childType == JadeElementTypes.MIXIN ||
         childType == JadeStubElementTypes.MIXIN_DECLARATION ||
         childType == JadeElementTypes.CASE_STATEMENT ||
         childType == JadeElementTypes.CONDITIONAL_STATEMENT ||
         childType == JadeElementTypes.CONDITIONAL_BODY ||
         childType == JadeElementTypes.FOR_STATEMENT ||
         childType == JadeElementTypes.FOR_BODY ||
         childType == JadeElementTypes.WHEN_STATEMENT ||
         childType == JadeElementTypes.FILTER ||
         childType == JadeElementTypes.INCLUDE_STATEMENT ||
         childType == JadeElementTypes.JS_CODE_BLOCK_PATCHED ||
         childType == JadeElementTypes.JS_CODE_BLOCK ||
         childType == JadeElementTypes.JS_EXPR ||
         childType == JadeElementTypes.BLOCK ||
         childType == JadeElementTypes.YIELD_STATEMENT ||
         childType == JadeElementTypes.PIPED_TEXT ||
         childType == JadeTokenTypes.FILTER_CODE ||
         childType == JadeTokenTypes.JS_META_CODE ||
         (childType == XmlElementType.XML_ATTRIBUTE && child != child.getTreeParent().getFirstChildNode()) ||
         childType == XmlElementType.XML_TEXT ||
         childType == XmlElementType.HTML_RAW_TEXT ||
         childType instanceof JadeEmbeddedTokenTypesWrapper ||
         child instanceof JadeCommentImpl ||
         isJsCodeBlock(myNode, child))) {
      childIndent = Indent.getNormalIndent();
    }
    else {
      childIndent = Indent.getNoneIndent();
    }
    return childIndent;
  }


  private List<Block> buildTextChildrenBlocks() {
    final ArrayList<Block> result = new ArrayList<>();

    CommonCodeStyleSettings.IndentOptions indentOptions = mySettings.getCommonSettings(JadeLanguage.INSTANCE).getIndentOptions();
    int blockIndent = calcCurrentAbsoluteIndent(myNode, indentOptions.TAB_SIZE);
    String text = myNode.getText();
    int lineStart = 0;
    while (lineStart < text.length()) {
      int nonWs = lineStart;
      char c;
      while ((c = text.charAt(nonWs)) != '\n' && Character.isWhitespace(c)) {
        nonWs++;
        if (nonWs == text.length()) {
          break; // last line ends with whitespace
        }
      }
      if (c != '\n') {
        int currentIndentInSpaces = IndentUtil.calcIndent(text.substring(lineStart, nonWs), 0, indentOptions.TAB_SIZE);
        int eol = text.indexOf('\n', nonWs);
        int blockEnd = eol != -1 ? eol : text.length();
        if (blockEnd > nonWs) {
          result.add(new JadeCommentBlock(myNode, nonWs, blockEnd,
                                          calcIndentForSpaces(currentIndentInSpaces - blockIndent, indentOptions)));
        }
        if (eol != -1) {
          lineStart = eol + 1;
        }
        else {
          break;
        }
      }
      else {
        lineStart = nonWs + 1;
      }
    }

    return result;
  }

  private static int calcCurrentAbsoluteIndent(ASTNode node, final int tabSize) {
    int startOffset = node.getStartOffset();
    String text = TreeUtil.getFileElement((TreeElement)node).getText();
    int wsEnd = startOffset;
    while (wsEnd > 0 && !Character.isWhitespace(text.charAt(wsEnd))) {
      wsEnd--;
    }
    int wsStart = wsEnd;
    while (wsStart > 0 && Character.isWhitespace(text.charAt(wsEnd)) && text.charAt(wsStart) != '\n') {
      wsStart--;
    }
    return wsEnd > wsStart ? IndentUtil.calcIndent(text.substring(wsStart, wsEnd + 1), 0, tabSize) : 0;
  }

  private static boolean shouldIndentChildren(ASTNode node) {
    return node instanceof JadeTagImpl
      || node instanceof JadeConditionalStatementImpl
      || node instanceof JadeForStatementImpl
      || node instanceof JadeFilterImpl
      || node instanceof JadeIncludeStatementImpl
      || node instanceof JadeConditionalElseImpl
      || node.getElementType() == JadeStubElementTypes.MIXIN_DECLARATION
      || node instanceof JadeCaseStatementImpl
      || node instanceof JadeWhenStatementImpl
      || node instanceof JadeJSCodeLineImpl
      || (node instanceof JadeJSStatementImpl && ((JadeJSStatementImpl)node).hasJadeBlock())
      || node.getElementType() == JadeTokenTypes.JS_META_CODE;
  }

  private static boolean shouldIndentChildrenAfterEnter(ASTNode node) {
    if (!shouldIndentChildren(node)) {
      return false;
    }

    if (node instanceof JadeIncludeStatementImpl) {
      return ((JadeIncludeStatementImpl)node).getFirstChildNode().getElementType() == JadeTokenTypes.INCLUDE_KEYWORD;
    }
    else if (node instanceof JadeTagImpl) {
      final String name = ((JadeTagImpl)node).getName();
      return ourTagNamesWithChildIndentByEnter.contains(name);
    }
    else {
      return true;
    }
  }

  private static Indent calcIndentForSpaces(int spaces, CommonCodeStyleSettings.IndentOptions indentOptions) {
    if (spaces <= 0) {
      return Indent.getNoneIndent();
    }
    if (indentOptions.USE_TAB_CHARACTER) {
      // we have to change spaces number so whitespace can be represented by tabs with no misfit (so formatter can put tabs instead of spaces)
      // let's use the simplest heuristics for now
      spaces = Math.round((float)spaces / indentOptions.TAB_SIZE) * indentOptions.TAB_SIZE;
    }
    return Indent.getSpaceIndent(spaces, false);
  }

  @Override
  public Indent getIndent() {
    return myIndent;
  }

  @Override
  public @Nullable Spacing getSpacing(final @Nullable Block child1, final @NotNull Block child2) {
    return createSpacingBuilder().getSpacing(this, child1, child2);
  }

  @Override
  public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
    Block block = getPrevChildBlock(newChildIndex);
    if (block instanceof AbstractBlock) {
      ASTNode prevChild = ((AbstractBlock)block).getNode();
      if ((shouldIndentChildren(prevChild) || prevChild instanceof JadeCommentImpl)) {
        return ChildAttributes.DELEGATE_TO_PREV_CHILD;
      }
    }
    else if (block instanceof InjectedLanguageBlockWrapper
             && (myNode instanceof JadeBlockImpl || ((InjectedLanguageBlockWrapper)block).getLanguage() == CSSLanguage.INSTANCE)) {
      return ChildAttributes.DELEGATE_TO_PREV_CHILD;
    }

    return super.getChildAttributes(newChildIndex);
  }

  private Block getPrevChildBlock(int newChildIndex) {
    if (newChildIndex == 0) {
      return null;
    }
    else {
      return getSubBlocks().get(newChildIndex - 1);
    }
  }

  @Override
  protected @Nullable Indent getChildIndent() {
    // For the enter handling
    return shouldIndentChildren(myNode) && shouldIndentChildrenAfterEnter(myNode)
           || myNode instanceof JadeCommentImpl ? Indent.getNormalIndent() : Indent.getNoneIndent();
  }

  private SpacingBuilder createSpacingBuilder() {
    return new SpacingBuilder(getSettings(), JadeLanguage.INSTANCE)
      .between(JadeTokenTypes.PIPE, JadeTokenTypes.TEXT_SET).spacing(1, Integer.MAX_VALUE, 0, false, 0);
  }

  private static @Nullable Language getInjectedLanguageForElementType(@NotNull IElementType elementType) {
    if (elementType instanceof JadeEmbeddedTokenTypesWrapper) {
      return getInjectedLanguageForElementType(((JadeEmbeddedTokenTypesWrapper)elementType).getDelegate());
    }
    if (elementType instanceof JadeEmbeddedTokenTypesWrapperForCssStylesheet) {
      return elementType.getLanguage();
    }

    if (elementType == JadeTokenTypes.STYLE_BLOCK) {
      return CSSLanguage.INSTANCE;
    }
    if (elementType == JadeTokenTypes.JS_CODE_BLOCK ||
        elementType == JadeTokenTypes.JS_EXPR) {
      return JavascriptLanguage.INSTANCE;
    }
    if (elementType == JadeTokenTypes.JS_CODE_BLOCK_PATCHED) {
      //return JavaScriptInJadeLanguageDialect.INSTANCE;
      return null;
    }

    for (EmbeddedTokenTypesProvider provider : EmbeddedTokenTypesProvider.getProviders()) {
      if (provider.getElementType() == elementType) {
        return provider.getElementType().getLanguage();
      }
    }

    return null;
  }

  private @NotNull Indent getIndentForChildScript() {
    if (!(myNode instanceof JadeTagImpl)) {
      return Indent.getNoneIndent();
    }

    PsiElement firstChild = ((JadeTagImpl)myNode).getFirstChild();
    if (!(firstChild instanceof XmlTokenImpl)) {
      return Indent.getNoneIndent();
    }

    String name = firstChild.getText();
    if (HtmlUtil.SCRIPT_TAG_NAME.equals(name) || HtmlUtil.STYLE_TAG_NAME.equals(name)) {
      return Indent.getNormalIndent();
    }
    else {
      return Indent.getNoneIndent();
    }
  }

  protected static boolean isJsCodeBlock(@NotNull ASTNode parent, @NotNull ASTNode child) {
    if (parent.getElementType() != JadeTokenTypes.JS_META_CODE) {
      return false;
    }
    if (!(child.getPsi() instanceof JSElement)) {
      return false;
    }

    final ASTNode minus = parent.findChildByType(TemplateMasqueradingLexer.MINUS_TYPE);
    if (minus == null) {
      return false;
    }
    final ASTNode next = minus.getTreeNext();
    return next != null && next.getElementType() == JadeTokenTypes.INDENT;
  }
}
