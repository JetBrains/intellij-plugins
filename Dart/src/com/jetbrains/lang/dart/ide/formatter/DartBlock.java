package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.*;
import com.intellij.formatting.templateLanguages.BlockWithParent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.BLOCKS;

public class DartBlock extends AbstractBlock implements BlockWithParent {
  public static final List<DartBlock> DART_EMPTY = Collections.emptyList();

  private static final TokenSet STATEMENTS_WITH_OPTIONAL_BRACES = TokenSet.create(IF_STATEMENT, WHILE_STATEMENT, FOR_STATEMENT);

  private static final TokenSet LAST_TOKENS_IN_SWITCH_CASE = TokenSet.create(BREAK_STATEMENT, CONTINUE_STATEMENT, RETURN_STATEMENT);

  private final DartIndentProcessor myIndentProcessor;
  private final DartSpacingProcessor mySpacingProcessor;
  private final DartWrappingProcessor myWrappingProcessor;
  private final DartAlignmentProcessor myAlignmentProcessor;
  private final CodeStyleSettings mySettings;
  private final DartBlockContext myContext;
  private Wrap myChildWrap = null;
  private final Indent myIndent;
  private BlockWithParent myParent;
  private List<DartBlock> mySubDartBlocks;

  protected DartBlock(ASTNode node, Wrap wrap, Alignment alignment, CodeStyleSettings settings, DartBlockContext context) {
    super(node, wrap, alignment);
    mySettings = settings;
    myContext = context;
    myIndentProcessor = new DartIndentProcessor(context.getDartSettings());
    mySpacingProcessor = new DartSpacingProcessor(node, context.getDartSettings());
    myWrappingProcessor = new DartWrappingProcessor(node, context.getDartSettings());
    myAlignmentProcessor = new DartAlignmentProcessor(node, context.getDartSettings());
    myIndent = myIndentProcessor.getChildIndent(myNode, context.getMode());
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
    final ArrayList<Block> tlChildren = new ArrayList<>();
    for (ASTNode childNode = getNode().getFirstChildNode(); childNode != null; childNode = childNode.getTreeNext()) {
      if (FormatterUtil.containsWhiteSpacesOnly(childNode)) continue;
      final DartBlock childBlock =
        new DartBlock(childNode, createChildWrap(childNode), createChildAlignment(childNode), mySettings, myContext);
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
    final IElementType type = child.getElementType();
    if (type != LPAREN && !BLOCKS.contains(type)) {
      return myAlignmentProcessor.createChildAlignment();
    }
    return null;
  }

  @Override
  public boolean isIncomplete() {
    return super.isIncomplete() || myNode.getElementType() == ARGUMENT_LIST;
  }

  @NotNull
  @Override
  public ChildAttributes getChildAttributes(final int newIndex) {
    final IElementType elementType = myNode.getElementType();
    final DartBlock previousBlock = newIndex == 0 ? null : getSubDartBlocks().get(newIndex - 1);
    final IElementType previousType = previousBlock == null ? null : previousBlock.getNode().getElementType();

    if (previousType == LBRACE || previousType == LBRACKET) {
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
        final List<DartBlock> subBlocks = previousBlock.getSubDartBlocks();
        if (!subBlocks.isEmpty()) {
          final DartBlock lastChildInPrevBlock = subBlocks.get(subBlocks.size() - 1);
          final List<DartBlock> subSubBlocks = lastChildInPrevBlock.getSubDartBlocks();
          if (isLastTokenInSwitchCase(subSubBlocks)) {
            return new ChildAttributes(Indent.getNormalIndent(), null);  // e.g. Enter after BREAK_STATEMENT
          }
        }
      }

      final int indentSize = mySettings.getIndentSize(DartFileType.INSTANCE) * 2;
      return new ChildAttributes(Indent.getIndent(Indent.Type.SPACES, indentSize, false, false), null);
    }

    if (previousBlock == null) {
      return new ChildAttributes(Indent.getNoneIndent(), null);
    }

    if (!previousBlock.isIncomplete() && newIndex < getSubDartBlocks().size() && previousType != TokenType.ERROR_ELEMENT) {
      return new ChildAttributes(previousBlock.getIndent(), previousBlock.getAlignment());
    }
    if (myParent instanceof DartBlock && ((DartBlock)myParent).isIncomplete()) {
      ASTNode child = myNode.getFirstChildNode();
      if (child == null || !(child.getElementType() == OPEN_QUOTE && child.getTextLength() == 3)) {
        return new ChildAttributes(Indent.getContinuationIndent(), null);
      }
    }
    if (myParent == null && isIncomplete()) {
      return new ChildAttributes(Indent.getContinuationIndent(), null);
    }
    return new ChildAttributes(previousBlock.getIndent(), previousBlock.getAlignment());
  }

  private static boolean isLastTokenInSwitchCase(@NotNull final List<DartBlock> blocks) {
    int size = blocks.size();
    // No blocks.
    if (size == 0) {
      return false;
    }
    // [return x;]
    DartBlock lastBlock = blocks.get(size - 1);
    final IElementType type = lastBlock.getNode().getElementType();
    if (LAST_TOKENS_IN_SWITCH_CASE.contains(type)) {
      return true;
    }
    // [throw expr][;]
    if (type == SEMICOLON && size > 1) {
      DartBlock lastBlock2 = blocks.get(size - 2);
      return lastBlock2.getNode().getElementType() == THROW_EXPRESSION;
    }
    return false;
  }

  public List<DartBlock> getSubDartBlocks() {
    if (mySubDartBlocks == null) {
      mySubDartBlocks = new ArrayList<>();
      for (Block block : getSubBlocks()) {
        mySubDartBlocks.add((DartBlock)block);
      }
      mySubDartBlocks = !mySubDartBlocks.isEmpty() ? mySubDartBlocks : DART_EMPTY;
    }
    return mySubDartBlocks;
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
