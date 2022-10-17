package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.DroolsLhsPatternBind;
import com.intellij.plugins.drools.lang.psi.DroolsNameId;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class DroolsLhsPatternBindVariableImpl extends DroolsAbstractVariableImpl implements DroolsLhsPatternBind {

  public DroolsLhsPatternBindVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public DroolsNameId getNamedIdElement() {
    return getNameId();
  }

  @NotNull
  @Override
  public PsiType getType() {
    final Set<PsiClass> psiClasses = DroolsResolveUtil.getPatternBindType(this.getLhsPatternList());
    return psiClasses.size() == 0 ? PsiType.NULL : JavaPsiFacade.getElementFactory(getProject()).createType(psiClasses.iterator().next());
  }
}
