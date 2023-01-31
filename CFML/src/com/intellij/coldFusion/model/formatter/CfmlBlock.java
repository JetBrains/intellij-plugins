// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.formatter;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.stubs.CfmlStubElementTypes;
import com.intellij.formatting.*;
import com.intellij.formatting.templateLanguages.BlockWithParent;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory;
import com.intellij.lang.ASTNode;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

public class CfmlBlock extends TemplateLanguageBlock {
  private final Indent myIndent;
  private Wrap myChildWrap;
  private final CommonCodeStyleSettings mySettings;
  private final CodeStyleSettings superSettings;
  private final CfmlIndentProcessor myIndentProcessor;
  private final CfmlWrappingProcessor myWrappingProcessor;
  private final CfmlSpacingProcessor mySpacingProcessor;
  private final CfmlAlignmentProcessor myAlignmentProcessor;
  private final TextRange myTextRange;

  public CfmlBlock(ASTNode node,
                   Wrap wrap,
                   Alignment alignment,
                   CodeStyleSettings settings,
                   @NotNull TemplateLanguageBlockFactory blockFactory,
                   @Nullable List<DataLanguageBlockWrapper> foreignChildren) {
    super(node, wrap, alignment, blockFactory, settings, foreignChildren);
    superSettings = settings;
    mySettings = getSettings().getCommonSettings(CfmlLanguage.INSTANCE);
    CfmlCodeStyleSettings cfmlSettings = getSettings().getCustomSettings(CfmlCodeStyleSettings.class);
    myIndentProcessor = new CfmlIndentProcessor(mySettings, settings.getIndentSize(CfmlFileType.INSTANCE));
    myWrappingProcessor = new CfmlWrappingProcessor(node, mySettings);
    mySpacingProcessor = new CfmlSpacingProcessor(node, mySettings, cfmlSettings);
    myAlignmentProcessor = new CfmlAlignmentProcessor(node, mySettings);
    myIndent = getChildIndent();
    myTextRange = trimRangeToNonWhiteSpaceIfNeeded();
  }

  @Override
  protected IElementType getTemplateTextElementType() {
    return null;
  }

  @Nullable
  @Override
  protected Indent getChildIndent() {
    return myIndentProcessor.getChildIndent(myNode);
  }


  @Override
  protected List<Block> buildChildren() {
    return super.buildChildren();
  }

  private static final IElementType[] WHITESPACE = new IElementType[]{XmlTokenType.XML_WHITE_SPACE, CfmlTokenTypes.WHITE_SPACE};

  // TODO this is a hack to be removed when template blocks are implemented properly
  // for template text block we look for corresponding elements in HTML PSI tree and use contiguous range of non-whitespace elements
  private TextRange trimRangeToNonWhiteSpaceIfNeeded() {
    TextRange defaultRange = myNode.getTextRange();
    if (myNode.getElementType() != CfmlElementTypes.TEMPLATE_TEXT) {
      return defaultRange;
    }

    PsiFile htmlFile = myNode.getPsi().getContainingFile().getViewProvider().getPsi(HTMLLanguage.INSTANCE);
    if (htmlFile == null) {
      return defaultRange;
    }

    @Nullable TextRange nonWhitespace = null;
    final PsiElement commonHtmlParent =
      findCommonHtmlParent(htmlFile.getViewProvider().findElementAt(defaultRange.getStartOffset(), HTMLLanguage.INSTANCE),
                           htmlFile.getViewProvider().findElementAt(defaultRange.getEndOffset() - 1, HTMLLanguage.INSTANCE));
    if (commonHtmlParent == null) {
      return defaultRange;
    }

    ArrayDeque<PsiElement> elements = new ArrayDeque<>(Arrays.asList(commonHtmlParent.getChildren()));
    while (!elements.isEmpty()) {
      PsiElement e = elements.remove();
      if (!ArrayUtil.contains(e.getNode().getElementType(), WHITESPACE)) {
        if (defaultRange.contains(e.getTextRange())) {
          nonWhitespace = nonWhitespace == null ? e.getTextRange() : nonWhitespace.union(e.getTextRange());
        }
        else if (defaultRange.intersects(e.getTextRange())) {
          elements.addAll(Arrays.asList(e.getChildren()));
        }
      }
    }

    if (nonWhitespace != null) {
      assert defaultRange.intersects(nonWhitespace);
      return defaultRange.intersection(nonWhitespace);
    }
    return defaultRange;
  }

  @Nullable
  private static PsiElement findCommonHtmlParent(@Nullable PsiElement start, @Nullable PsiElement end) {
    if (start == null || end == null || start == end) {
      return start;
    }
    final TextRange endRange = end.getTextRange();
    PsiElement parent = start.getParent();
    while (parent != null && !parent.getTextRange().contains(endRange)) {
      parent = parent.getParent();
    }
    return parent;
  }

  @Override
  @NotNull
  public TextRange getTextRange() {
    return myTextRange;
  }

  @Override
  public Indent getIndent() {
    return myIndent;
  }

  public Wrap getChildWrap() {
    return myChildWrap;
  }

  @Override
  public Spacing getSpacing(Block child1, @NotNull Block child2) {
    return mySpacingProcessor.getSpacing(child1, child2);
  }

  @Override
  public Wrap createChildWrap(ASTNode child) {
    Wrap defaultWrap = super.createChildWrap(child);
    IElementType childType = child.getElementType();
    BlockWithParent parent = getParent();
    Wrap childWrap = parent instanceof CfmlBlock ? ((CfmlBlock)parent).getChildWrap() : null;
    Wrap wrap = myWrappingProcessor.createChildWrap(child, defaultWrap, childWrap);

    if (CfmlFormatterUtil.isAssignmentOperator(childType)) {
      myChildWrap = wrap;
    }
    return wrap;
  }

  @Override
  @Nullable
  protected Alignment createChildAlignment(ASTNode child) {
    if (child.getElementType() != CfscriptTokenTypes.FOR_KEYWORD &&
        child.getElementType() != CfscriptTokenTypes.L_BRACKET && child.getElementType() != CfmlElementTypes.BLOCK_OF_STATEMENTS) {
      return myAlignmentProcessor.createChildAlignment();
    }
    return null;
  }

  @NotNull
  @Override
  public ChildAttributes getChildAttributes(int newChildIndex) {
    List<Block> childBlockList = getSubBlocks();
    if (newChildIndex > 0 && newChildIndex - 1 < childBlockList.size()) {
      ASTBlock prevBlock = (ASTBlock)childBlockList.get(newChildIndex - 1);
      if (prevBlock != null) {
        Indent indent;
        Alignment alignment = myAlignmentProcessor.createChildAlignment();
        ASTNode prevNode = prevBlock.getNode();
        if (prevNode != null) {
          PsiElement prevTreePsiElement = prevNode.getTreePrev() != null ? prevNode.getTreePrev().getPsi() : null;
          IElementType prevBlockType = prevNode.getElementType();
          if (prevBlockType == CfscriptTokenTypes.L_CURLYBRACKET) {
            if (myNode.getElementType() == CfmlElementTypes.FUNCTION_DEFINITION &&
                mySettings.METHOD_BRACE_STYLE == CommonCodeStyleSettings.NEXT_LINE_SHIFTED2 ||
                mySettings.BRACE_STYLE == CommonCodeStyleSettings.NEXT_LINE_SHIFTED2) {
              indent = Indent.getSpaceIndent(superSettings.getIndentSize(CfmlFileType.INSTANCE) * 2);
            }
            else {
              indent = Indent.getNormalIndent();
            }
          }
          else if (myNode.getElementType() == CfmlStubElementTypes.CFML_FILE) {
            indent = Indent.getNoneIndent();
          }
          else if ((prevBlockType == CfmlTokenTypes.R_ANGLEBRACKET) && prevTreePsiElement != null &&
                   !prevTreePsiElement.getText().equalsIgnoreCase("cfscript")) {
            indent = Indent.getNormalIndent();
          }
          else if ((prevBlockType == CfmlTokenTypes.CLOSER) && prevTreePsiElement != null &&
                   !prevTreePsiElement.getText().equalsIgnoreCase("cfscript")) {
            indent = null;
          }
          else if (prevBlockType == CfmlElementTypes.TAG || prevBlockType == CfmlElementTypes.ARGUMENT_TAG
                   ||
                   prevBlockType == CfmlElementTypes.COMPONENT_TAG ||
                   prevBlockType == CfmlElementTypes.FUNCTION_TAG ||
                   prevBlockType == CfmlElementTypes.SCRIPT_TAG) {
            indent = Indent.getNormalIndent();
          }
          else if (prevBlockType == CfmlTokenTypes.COMMENT || prevBlockType == CfmlElementTypes.TEMPLATE_TEXT) {
            indent = prevBlock.getIndent();//getChildAttributes(newChildIndex - 1).getChildIndent();
          }
          else {
            indent = Indent.getNormalIndent();
          }
        }
        else {
          indent = ((DataLanguageBlockWrapper)prevBlock).getOriginal().getIndent();
        }
        return new ChildAttributes(indent, alignment);
      }
    }
    return super.getChildAttributes(newChildIndex);
  }
}
