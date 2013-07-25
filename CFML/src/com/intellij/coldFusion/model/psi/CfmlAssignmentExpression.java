/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: vnikolaenko
 * Date: 28.04.2009
 */
public class CfmlAssignmentExpression extends CfmlCompositeElement implements CfmlExpression {
  private AssignedVariable myAssignedVariable = null;
  private boolean myVariableWasInitialized = false;

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
    if (element == null || !(element instanceof CfmlReferenceExpression)) {
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
    if (!myVariableWasInitialized) {
      myVariableWasInitialized = true;
      myAssignedVariable = createAssignedVariable();
    }

    return myAssignedVariable;
  }

  public PsiType getPsiType() {
    return getAssignedVariableElementType();
  }

  public class AssignedVariable extends RenameableFakePsiElement implements CfmlVariable, CfmlScopeProvider {
    private boolean myIsDefinition;

    public AssignedVariable(boolean isDefinition) {
      super(CfmlAssignmentExpression.this.getContainingFile());
      myIsDefinition = isDefinition;
    }

    @Nullable
    public CfmlExpression getRightHandExpr() {
      return CfmlAssignmentExpression.this.getRightHandExpr();
    }

    @NotNull
    public String getName() {
      final CfmlReferenceExpression expression = getAssignedVariableElement();
      if (expression == null) {
        return "";
      }
      final String variableName = expression.getText();
      final String scopeName = getScopeName();
      if (scopeName != null && variableName != null && variableName.startsWith(scopeName + ".")) {
        return variableName.substring((scopeName + ".").length(), variableName.length());
      }
      return variableName != null ? expression.getText() : "";
    }

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

    public PsiElement getParent() {
      //noinspection ConstantConditions
      return getAssignedVariableElement();
    }

    public String getTypeName() {
      return "Unknown type";
    }

    public Icon getIcon() {
      return PlatformIcons.VARIABLE_ICON;
    }

    public PsiType getPsiType() {
      return getAssignedVariableElementType();
    }

    public CfmlExpression getRightChildExpression() {
      CfmlExpression[] expressions = findChildrenByClass(CfmlExpression.class);
      if (expressions.length != 2) {
        return null;
      }
      return expressions[1];
    }

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

    public int getProvidedScope() {
      return CfmlScopesInfo.getScopeByString(getScopeName());
    }

    public boolean isTrulyDeclaration() {
      return myIsDefinition;
    }

    public PsiElement getNameIdentifier() {
      return getNavigationElement();
    }

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
