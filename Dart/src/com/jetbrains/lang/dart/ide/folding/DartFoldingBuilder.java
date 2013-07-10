package com.jetbrains.lang.dart.ide.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.jetbrains.lang.dart.DartTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fedorkorotkov.
 */
public class DartFoldingBuilder implements FoldingBuilder {
  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    List<FoldingDescriptor> list = new ArrayList<FoldingDescriptor>();
    buildFolding(node, list);
    FoldingDescriptor[] descriptors = new FoldingDescriptor[list.size()];
    return list.toArray(descriptors);
  }

  private static void buildFolding(ASTNode node, List<FoldingDescriptor> list) {
    boolean isBlock = node.getElementType() == DartTokenTypes.BLOCK || node.getElementType() == DartTokenTypes.CLASS_BODY;
    if (isBlock && !node.getTextRange().isEmpty()) {
      final TextRange range = node.getTextRange();
      if (range.getLength() > 1) {
        list.add(new FoldingDescriptor(node, range));
      }
    }
    for (ASTNode child : node.getChildren(null)) {
      buildFolding(child, list);
    }
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    if (node.getElementType() == DartTokenTypes.CLASS_BODY) {
      return "...";
    }
    return "{...}";
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }
}
