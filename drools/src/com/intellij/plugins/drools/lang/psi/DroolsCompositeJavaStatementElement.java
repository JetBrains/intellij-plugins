package com.intellij.plugins.drools.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

public class DroolsCompositeJavaStatementElement extends ASTWrapperPsiElement implements PsiCodeBlock {
  public DroolsCompositeJavaStatementElement(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {

    if (!DroolsResolveUtil.processDeclarations(processor, state, lastParent, place)) return false;
    return super.processDeclarations(processor, state, lastParent, place);
  }

  @Override
  public PsiStatement @NotNull [] getStatements() {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    int count = 0;
    for (ASTNode child1 = getFirstChild().getNode(); child1 != null; child1 = child1.getTreeNext()) {
      if (child1.getPsi() instanceof PsiStatement) {
        count++;
      }
    }
    PsiStatement[] result = PsiStatement.ARRAY_FACTORY.create(count);
    if (count == 0) return result;
    int idx = 0;
    for (ASTNode child = getFirstChild().getNode(); child != null && idx < count; child = child.getTreeNext()) {
      if (child.getPsi() instanceof PsiStatement) {
        PsiStatement element = (PsiStatement)child.getPsi();
        //LOG.assertTrue(element != null, child);
        result[idx++] = element;
      }
    }
    return result;
  }

  @Override
  public PsiElement getFirstBodyElement() {
    return getFirstChild();
  }

  @Override
  public PsiElement getLastBodyElement() {
    return getLastChild();
  }

  @Override
  public PsiJavaToken getLBrace() {
    return null;
  }

  @Override
  public PsiJavaToken getRBrace() {
    return null;
  }

  @Override
  public boolean shouldChangeModificationCount(PsiElement place) {
    return false;
  }
}
