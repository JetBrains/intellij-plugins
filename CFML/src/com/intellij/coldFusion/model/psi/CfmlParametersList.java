package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.psi.impl.CfmlFunctionParameterImpl;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

public class CfmlParametersList extends CfmlCompositeElement  {
  public CfmlParametersList(ASTNode node) {
      super(node);
  }

  public CfmlFunctionParameterImpl[] getParameters() {
      return findChildrenByClass(CfmlFunctionParameterImpl.class);
  }

  @NotNull
  public String getPresentableText() {
    final String s = getText();
    return s != null ? s.replaceAll("\\s+", " ") : "";
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    final CfmlFunctionParameterImpl[] functionParameters = getParameters();
    for (CfmlFunctionParameterImpl parameter : functionParameters) {
      if (!processor.execute(parameter, state)) {
        return false;
      }
    }
    return true;
  }
}
