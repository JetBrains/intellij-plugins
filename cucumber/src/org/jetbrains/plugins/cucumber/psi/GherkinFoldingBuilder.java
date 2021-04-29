// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinExamplesBlockImpl;

import java.util.ArrayList;
import java.util.List;


public class GherkinFoldingBuilder implements FoldingBuilder, DumbAware {
  private static final TokenSet BLOCKS_TO_FOLD = TokenSet.create(GherkinElementTypes.SCENARIO,
                                                                 GherkinElementTypes.SCENARIO_OUTLINE,
                                                                 GherkinElementTypes.EXAMPLES_BLOCK,
                                                                 GherkinTokenTypes.PYSTRING);


  @Override
  public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    List<FoldingDescriptor> descriptors = new ArrayList<>();
    appendDescriptors(node, descriptors);
    return descriptors.toArray(FoldingDescriptor.EMPTY);
  }

  private void appendDescriptors(ASTNode node, List<FoldingDescriptor> descriptors) {
    if (BLOCKS_TO_FOLD.contains(node.getElementType()) && node.getTextRange().getLength() >= 2) {
      descriptors.add(new FoldingDescriptor(node, node.getTextRange()));
    }
    ASTNode child = node.getFirstChildNode();
    while (child != null) {
      appendDescriptors(child, descriptors);
      child = child.getTreeNext();
    }
  }

  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    if (node.getPsi() instanceof GherkinStepsHolder ||
        node.getPsi() instanceof GherkinExamplesBlockImpl) {
      return ((NavigationItem) node.getPsi()).getPresentation().getPresentableText();
    }
    return "...";
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }
}
