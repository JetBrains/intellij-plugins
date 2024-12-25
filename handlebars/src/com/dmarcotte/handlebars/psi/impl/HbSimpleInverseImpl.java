// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.psi.impl;

import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbSimpleInverse;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import icons.HandlebarsIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HbSimpleInverseImpl extends HbPlainMustacheImpl implements HbSimpleInverse {
  public HbSimpleInverseImpl(@NotNull ASTNode astNode) {
    super(astNode);
  }

  /**
   * Uses "else" as the name for this element if the "else" syntax is used,
   * and uses no name (i.e. the "{^}" icon is the representative) in the simple
   * inverse case
   */
  @Override
  public String getName() {
    ASTNode elseNode = getElseNode();
    if (elseNode != null) {
      return elseNode.getText();
    }
    return ""; // no name for "{{^}}" expressions
  }

  @Override
  public @Nullable Icon getIcon(@IconFlags int flags) {
    if (getElseNode() != null) {
      return HandlebarsIcons.Elements.OpenMustache;
    }
    return HandlebarsIcons.Elements.OpenInverse;
  }

  /**
   * If this element was created from an "{{else}}" expression, it will have an {@link HbTokenTypes#ELSE} child.
   * Otherwise, it was created from "{{^}}"
   *
   * @return the {@link HbTokenTypes#ELSE} element if it exists, null otherwise
   */
  private ASTNode getElseNode() {
    ASTNode[] elseChildren = getNode().getChildren(TokenSet.create(HbTokenTypes.ELSE));
    if (elseChildren.length > 0) {
      return elseChildren[0];
    }
    return null;
  }
}
