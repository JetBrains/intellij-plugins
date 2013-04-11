package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.HbIcons;
import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbSimpleInverse;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbSimpleInverseImpl extends HbMustacheImpl implements HbSimpleInverse {
  public HbSimpleInverseImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  @Override
  public String getName() {
    ASTNode elseNode = getElseNode();
    if (elseNode != null) {
      return elseNode.getText();
    }
    return ""; // no name for "{{^}}" expressions
  }

  @Nullable
  @Override
  public Icon getIcon(@IconFlags int flags) {
    if (getElseNode() != null) {
      return HbIcons.OPEN_MUSTACHE;
    }
    return HbIcons.OPEN_INVERSE;
  }

  /**
   * If this element was created from an "{{else}}" expression, it will have an {@link HbTokenTypes#ELSE} child.
   * Otherwise, it was created from "{{^}}"
   *
   * @return the {@link HbTokenTypes#ELSE} element if it exists, null otherwise
   */
  private ASTNode getElseNode() {
    ASTNode[] elseChildren = getNode().getChildren(TokenSet.create(HbTokenTypes.ELSE));
    if (elseChildren != null && elseChildren.length > 0) {
      return elseChildren[0];
    }
    return null;
  }
}
