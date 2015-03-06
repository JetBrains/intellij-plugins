package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.*;
import com.intellij.formatting.templateLanguages.BlockWithParent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.jetbrains.lang.dart.DartTokenTypes.*;

public class DartBlock extends AbstractBlock implements BlockWithParent {

  private static final TokenSet STATEMENTS_WITH_OPTIONAL_BRACES = TokenSet.create(
    IF_STATEMENT, WHILE_STATEMENT, FOR_STATEMENT
  );

  private static final TokenSet LAST_TOKENS_IN_SWITCH_CASE = TokenSet.create(
    BREAK_STATEMENT, CONTINUE_STATEMENT, RETURN_STATEMENT, THROW_STATEMENT
  );

  private final DartIndentProcessor myIndentProcessor;
  private final DartSpacingProcessor mySpacingProcessor;
  private final DartWrappingProcessor myWrappingProcessor;
  private final DartAlignmentProcessor myAlignmentProcessor;
  private final CodeStyleSettings mySettings;
  private Wrap myChildWrap = null;
  private final Indent myIndent;
  private BlockWithParent myParent;

  protected DartBlock(ASTNode node,
                      Wrap wrap,
                      Alignment alignment,
                      CodeStyleSettings settings) {
    super(node, wrap, alignment);
    mySettings = settings;
    myIndentProcessor = new DartIndentProcessor(mySettings.getCommonSettings(DartLanguage.INSTANCE));
    mySpacingProcessor = new DartSpacingProcessor(node, mySettings.getCommonSettings(DartLanguage.INSTANCE));
    myWrappingProcessor = new DartWrappingProcessor(node, mySettings.getCommonSettings(DartLanguage.INSTANCE));
    myAlignmentProcessor = new DartAlignmentProcessor(node, mySettings.getCommonSettings(DartLanguage.INSTANCE));
    myIndent = myIndentProcessor.getChildIndent(myNode);
  }

  @Override
  public Indent getIndent() {
    return myIndent;
  }

  @Override
  public Spacing getSpacing(Block child1, @NotNull Block child2) {
    return mySpacingProcessor.getSpacing(child1, child2);
  }

  @Override
  protected List<Block> buildChildren() {
    if (isLeaf()) {
      return EMPTY;
    }
    final ArrayList<Block> tlChildren = new ArrayList<Block>();
    for (ASTNode childNode = getNode().getFirstChildNode(); childNode != null; childNode = childNode.getTreeNext()) {
      if (FormatterUtil.containsWhiteSpacesOnly(childNode)) continue;
      final DartBlock childBlock = new DartBlock(childNode, createChildWrap(childNode), createChildAlignment(childNode), mySettings);
      childBlock.setParent(this);
      tlChildren.add(childBlock);
    }
    return tlChildren;
  }

  public Wrap createChildWrap(ASTNode child) {
    final IElementType childType = child.getElementType();
    final Wrap wrap = myWrappingProcessor.createChildWrap(child, Wrap.createWrap(WrapType.NONE, false), myChildWrap);

    if (childType == ASSIGNMENT_OPERATOR) {
      myChildWrap = wrap;
    }
    return wrap;
  }

  @Nullable
  protected Alignment createChildAlignment(ASTNode child) {
    if (child.getElementType() != LPAREN && child.getElementType() != BLOCK) {
      return myAlignmentProcessor.createChildAlignment();
    }
    return null;
  }

  @NotNull
  @Override
  public ChildAttributes getChildAttributes(final int newIndex) {
    final IElementType elementType = myNode.getElementType();
    final DartBlock previousBlock = newIndex == 0 ? null : (DartBlock)getSubBlocks().get(newIndex - 1);
    final IElementType previousType = previousBlock == null ? null : previousBlock.getNode().getElementType();

    if (previousType == LBRACE) {
      return new ChildAttributes(Indent.getNormalIndent(), null);
    }

    if (previousType == RPAREN && STATEMENTS_WITH_OPTIONAL_BRACES.contains(elementType)) {
      return new ChildAttributes(Indent.getNormalIndent(), null);
    }

    if (previousType == COLON && (elementType == SWITCH_CASE || elementType == DEFAULT_CASE)) {
      return new ChildAttributes(Indent.getNormalIndent(), null);
    }

    if (previousType == SWITCH_CASE || previousType == DEFAULT_CASE) {
      if (previousBlock != null) {
        final List<Block> subBlocks = previousBlock.getSubBlocks();
        if (!subBlocks.isEmpty()) {
          final DartBlock lastChildInPrevBlock = (DartBlock)subBlocks.get(subBlocks.size() - 1);
          final List<Block> subSubBlocks = lastChildInPrevBlock.getSubBlocks();
          if (!subSubBlocks.isEmpty()) {
            final DartBlock lastChildInLastChildInPrevBlock = (DartBlock)subSubBlocks.get(subSubBlocks.size() - 1);
            final IElementType typeOfLastChildInPrevBlock = lastChildInLastChildInPrevBlock.getNode().getElementType();

            if (LAST_TOKENS_IN_SWITCH_CASE.contains(typeOfLastChildInPrevBlock) ||
                typeOfLastChildInPrevBlock == SEMICOLON && lastButOneIsThrowStatement(subSubBlocks)) {
              return new ChildAttributes(Indent.getNormalIndent(), null);  // e.g. Enter after BREAK_STATEMENT
            }
          }
        }
      }

      final int indentSize = mySettings.getIndentSize(DartFileType.INSTANCE) * 2;
      return new ChildAttributes(Indent.getIndent(Indent.Type.SPACES, indentSize, false, false), null);
    }

    if (previousBlock == null) {
      return new ChildAttributes(Indent.getNoneIndent(), null);
    }

    return new ChildAttributes(previousBlock.getIndent(), previousBlock.getAlignment());
  }

  private static boolean lastButOneIsThrowStatement(@NotNull final List<Block> blocks) {
    return blocks.size() > 1 && ((DartBlock)blocks.get(blocks.size() - 2)).getNode().getElementType() == THROW_STATEMENT;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public BlockWithParent getParent() {
    return myParent;
  }

  @Override
  public void setParent(BlockWithParent newParent) {
    myParent = newParent;
  }
}