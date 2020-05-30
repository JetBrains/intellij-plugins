// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.AtomicNullableLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CfmlAssignmentExpression extends CfmlCompositeElement implements CfmlExpression {
  private final AtomicNullableLazyValue<AssignedVariable> myAssignedVariable =
    AtomicNullableLazyValue.createValue(() -> createAssignedVariable());

  public CfmlAssignmentExpression(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  private AssignedVariable createAssignedVariable() {
    CfmlReferenceExpression varElement = getAssignedVariableElement();
    if (varElement == null) {
      return null;
    }
    CfmlImplicitVariable var = getContainingFile().findImplicitVariable(varElement.getText());
    if (var != null && var.getTextRange().getStartOffset() < this.getTextRange().getStartOffset()) {
      return null;
    }
    return new AssignedVariable(findChildByType(CfscriptTokenTypes.VAR_KEYWORD) != null);
  }

  @Nullable
  public CfmlReferenceExpression getAssignedVariableElement() {
    if (getFirstChild() instanceof CfmlArgumentNameReference) return (CfmlReferenceExpression)getFirstChild();
    PsiElement element = findChildByType(CfmlElementTypes.REFERENCE_EXPRESSION);
    if (!(element instanceof CfmlReferenceExpression)) {
      return null;
    }
    return (CfmlReferenceExpression)element;
  }

  @Nullable
  private PsiType getAssignedVariableElementType() {
    final CfmlExpression e = getRightHandExpr();
    return e != null ? e.getPsiType() : null;
  }

  @Nullable
  public CfmlExpression getRightHandExpr() {
    CfmlExpression[] expressions = findChildrenByClass(CfmlExpression.class);
    if (expressions.length != 2) {
      return null;
    }
    return expressions[1];
  }

  @Nullable
  public CfmlVariable getAssignedVariable() {
    return myAssignedVariable.getValue();
  }

  @Override
  public PsiType getPsiType() {
    return getAssignedVariableElementType();
  }

  public class AssignedVariable extends RenameableFakePsiElement implements CfmlVariable, CfmlScopeProvider {
    private final boolean myIsDefinition;

    public AssignedVariable(boolean isDefinition) {
      super(CfmlAssignmentExpression.this.getContainingFile());
      myIsDefinition = isDefinition;
    }

    @Nullable
    public CfmlExpression getRightHandExpr() {
      return CfmlAssignmentExpression.this.getRightHandExpr();
    }

    @Override
    @NotNull
    public String getName() {
      final CfmlReferenceExpression expression = getAssignedVariableElement();
      if (expression == null) {
        return "";
      }
      final String variableName = expression.getText();
      final String scopeName = getScopeName();
      if (scopeName != null && variableName != null && variableName.startsWith(scopeName + ".")) {
        return variableName.substring((scopeName + ".").length());
      }
      return variableName != null ? expression.getText() : "";
    }

    @Override
    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
      // no need of renaming definition as it is a reference also and
      // will be renamed on handleRename operation of CfmlReferenceExpression
      return this;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
      final CfmlReferenceExpression expression = getAssignedVariableElement();
      assert expression != null;
      final PsiElement namedElement = expression.getReferenceNameElement();
      return namedElement != null ? namedElement : this;
    }

    @Override
    public PsiElement getParent() {
      return getAssignedVariableElement();
    }

    @Override
    public String getTypeName() {
      return CfmlBundle.message("element.type.name.unknown.type");
    }

    @Override
    public Icon getIcon() {
      return PlatformIcons.VARIABLE_ICON;
    }

    @Override
    public PsiType getPsiType() {
      return getAssignedVariableElementType();
    }

    @Override
    public String toString() {
      return "AssignedVariable " + getName();
    }

    @Nullable
    private String getScopeName() {
      final CfmlReferenceExpression expression = getAssignedVariableElement();
      if (expression != null) {
        final PsiElement scope = expression.getScope();
        if (scope != null) {
          return scope.getText();
        }
      }
      return null;
    }

    @Override
    public int getProvidedScope() {
      return CfmlScopesInfo.getScopeByString(getScopeName());
    }

    @Override
    public boolean isTrulyDeclaration() {
      return myIsDefinition;
    }

    @Override
    public PsiElement getNameIdentifier() {
      return getNavigationElement();
    }

    @Override
    @NotNull
    public String getlookUpString() {
      final CfmlReferenceExpression expression = getAssignedVariableElement();
      if (expression == null) {
        return "";
      }
      return expression.getText();
    }
  }
}
