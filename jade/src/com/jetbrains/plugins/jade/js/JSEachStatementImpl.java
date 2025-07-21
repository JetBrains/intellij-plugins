// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSStatement;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.impl.JSForInStatementImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JSEachStatementImpl extends JSForInStatementImpl {
  public JSEachStatementImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public JSVarStatement getVarDeclaration() {
    JSVarStatement[] allDeclarationStatements = getAllDeclarationStatements();
    if (allDeclarationStatements.length >= 2) {
      return allDeclarationStatements[1];
    }
    else {
      return null;
    }
  }

  @Override
  public JSStatement getBody() {
    JSExpression collectionExpression = getCollectionExpression();
    if (collectionExpression == null) {
      return null;
    }

    ASTNode next = collectionExpression.getNode().getTreeNext();
    if (next != null && JSExtendedLanguagesTokenSetProvider.STATEMENTS.contains(next.getElementType())) {
      return ((JSStatement)next.getPsi());
    }
    return null;
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if (lastParent != null) {
      JSVarStatement[] varStatements = getAllDeclarationStatements();
      for (JSVarStatement varStatement : varStatements) {
        varStatement.processDeclarations(processor, state, lastParent, place);
      }
    }
    return true;
  }

  private JSVarStatement[] getAllDeclarationStatements() {
    List<JSVarStatement> varStatements = new ArrayList<>(2);
    for (ASTNode child = getNode().getFirstChildNode(); child != null; child = child.getTreeNext()) {
      if (child.getElementType() == JSElementTypes.VAR_STATEMENT) {
        varStatements.add(((JSVarStatement)child.getPsi()));
      }
      else if (child.getElementType() == JSTokenTypes.IN_KEYWORD) {
        break;
      }
    }

    return varStatements.toArray(JSVarStatement.EMPTY_ARRAY);
  }
}
