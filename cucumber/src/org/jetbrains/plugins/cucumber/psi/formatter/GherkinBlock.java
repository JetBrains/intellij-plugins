package org.jetbrains.plugins.cucumber.psi.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.xml.ReadOnlyBlock;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinElementTypes;
import org.jetbrains.plugins.cucumber.psi.GherkinTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yole
 */
public class GherkinBlock implements ASTBlock {
  private final ASTNode myNode;
  private final Indent myIndent;
  private final TextRange myTextRange;
  private final boolean myLeaf;
  private List<Block> myChildren = null;

  private static final TokenSet BLOCKS_TO_INDENT = TokenSet.create(GherkinElementTypes.FEATURE_HEADER,
                                                                   GherkinElementTypes.SCENARIO,
                                                                   GherkinElementTypes.SCENARIO_OUTLINE,
                                                                   GherkinElementTypes.STEP,
                                                                   GherkinElementTypes.TABLE,
                                                                   GherkinElementTypes.EXAMPLES_BLOCK);

  private static final TokenSet BLOCKS_TO_INDENT_CHILDREN = TokenSet.create(GherkinElementTypes.GHERKIN_FILE,
                                                                            GherkinElementTypes.FEATURE,
                                                                            GherkinElementTypes.SCENARIO,
                                                                            GherkinElementTypes.SCENARIO_OUTLINE);

  public GherkinBlock(ASTNode node) {
    this(node, Indent.getAbsoluteNoneIndent());
  }

  public GherkinBlock(ASTNode node, Indent indent) {
    this(node, indent, node.getTextRange());
  }

  public GherkinBlock(ASTNode node, Indent indent, final TextRange textRange) {
    this(node, indent, textRange, false);
  }

  public GherkinBlock(ASTNode node, Indent indent, final TextRange textRange, final boolean leaf) {
    myNode = node;
    myIndent = indent;
    myTextRange = textRange;
    myLeaf = leaf;
  }

  public ASTNode getNode() {
    return myNode;
  }

  @NotNull
  public TextRange getTextRange() {
    return myTextRange;
  }

  @NotNull
  public List<Block> getSubBlocks() {
    if (myLeaf) return Collections.emptyList();
    if (myChildren == null) {
      myChildren = buildChildren();
    }
    return myChildren;
  }

  private List<Block> buildChildren() {
    final ASTNode[] children = myNode.getChildren(null);
    if (children.length == 0) {
      return Collections.emptyList();
    }

    List<Block> result = new ArrayList<>();
    for (ASTNode child : children) {
      if (child.getElementType() == TokenType.WHITE_SPACE) {
        continue;
      }
      Indent indent = BLOCKS_TO_INDENT.contains(child.getElementType()) ? Indent.getNormalIndent() : Indent.getNoneIndent();
      // skip epmty cells
      if (child.getElementType() == GherkinElementTypes.TABLE_CELL) {
        if (child.getChildren(null).length == 0) {
          continue;
        }
      }
      if (child.getElementType() == GherkinTokenTypes.COMMENT) {
        final ASTNode commentIndentElement = child.getTreePrev();
        final ASTNode parentIndentElement = myNode.getTreePrev();

        if (commentIndentElement != null && (commentIndentElement.getText().contains("\n") || commentIndentElement.getTreePrev() == null)) {
          final String whiteSpaceText = commentIndentElement.getText();
          final int lineBreakIndex = whiteSpaceText.lastIndexOf("\n");

          int parentIndent = 0;
          if (parentIndentElement != null && parentIndentElement.getText().contains("\n")) {
            String parentIndentText = parentIndentElement.getText();
            parentIndent = parentIndentText.length() - parentIndentText.lastIndexOf("\n") - 1;
          }
          indent = Indent.getSpaceIndent(whiteSpaceText.length() - lineBreakIndex - 1 - parentIndent);
        }
      }
      result.add(new GherkinBlock(child, indent));
    }
    return result;
  }

  @Nullable
  public Wrap getWrap() {
    return null;
  }

  public Indent getIndent() {
    return myIndent;
  }

  public Alignment getAlignment() {
    return null;
  }

  @Override
  public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    if (child1 == null) {
      return null;
    }

    ASTBlock block1 = (ASTBlock) child1;
    ASTBlock block2 = (ASTBlock) child2;
    final IElementType elementType1 = block1.getNode().getElementType();
    final IElementType elementType2 = block2.getNode().getElementType();

    if(elementType2 == GherkinElementTypes.PYSTRING) return Spacing.getReadOnlySpacing();
    if (GherkinElementTypes.SCENARIOS.contains(elementType2) && elementType1 != GherkinTokenTypes.COMMENT) {
      return Spacing.createSpacing(0, 0, 2, true, 2);
    }
    if (elementType1 == GherkinTokenTypes.PIPE &&
        elementType2 == GherkinElementTypes.TABLE_CELL) {
      return Spacing.createSpacing(1, 1, 0, false, 0);
    }
    if ((elementType1 == GherkinElementTypes.TABLE_CELL || elementType1 == GherkinTokenTypes.PIPE) &&
        elementType2 == GherkinTokenTypes.PIPE) {
      final ASTNode tableNode = TreeUtil.findParent(block1.getNode(), GherkinElementTypes.TABLE);
      if (tableNode != null) {
        int columnIndex = getTableCellColumnIndex(block1.getNode());
        int maxWidth = ((GherkinTable) tableNode.getPsi()).getColumnWidth(columnIndex);
        int spacingWidth = (maxWidth - block1.getNode().getText().trim().length()) + 1;
        if (elementType1 == GherkinTokenTypes.PIPE) {
          spacingWidth += 2;
        }
        return Spacing.createSpacing(spacingWidth, spacingWidth, 0, false, 0);
      }
    }
    return null;
  }

  private static int getTableCellColumnIndex(ASTNode node) {
    int pipeCount = 0;
    while(node != null) {
      if (node.getElementType() == GherkinTokenTypes.PIPE) {
        pipeCount++;
      }
      node = node.getTreePrev();
    }
    return pipeCount-1;
  }

  @NotNull
  public ChildAttributes getChildAttributes(int newChildIndex) {
    Indent childIndent = BLOCKS_TO_INDENT_CHILDREN.contains(getNode().getElementType()) ? Indent.getNormalIndent() : Indent.getNoneIndent();
    return new ChildAttributes(childIndent, null);
  }

  public boolean isIncomplete() {
    if (GherkinElementTypes.SCENARIOS.contains(getNode().getElementType())) {
      return true;
    }
    if (getNode().getElementType() == GherkinElementTypes.FEATURE) {
      return getNode().getChildren(TokenSet.create(GherkinElementTypes.FEATURE_HEADER,
                                                   GherkinElementTypes.SCENARIO,
                                                   GherkinElementTypes.SCENARIO_OUTLINE)).length == 0;
    }
    return false;
  }

  public boolean isLeaf() {
    return myLeaf || getSubBlocks().size() == 0;
  }

}
