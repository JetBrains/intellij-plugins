// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.terraform.hil.HILElementTypes.*;
import org.intellij.terraform.hil.psi.*;

public class ILTemplateBlockBodyImpl extends ILExpressionBase implements ILTemplateBlockBody {

  public ILTemplateBlockBodyImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ILGeneratedVisitor visitor) {
    visitor.visitILTemplateBlockBody(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ILGeneratedVisitor) accept((ILGeneratedVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<BadTag> getBadTagList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BadTag.class);
  }

  @Override
  @NotNull
  public List<ILExpression> getILExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ILExpression.class);
  }

  @Override
  @NotNull
  public List<ILTemplateHolder> getILTemplateHolderList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ILTemplateHolder.class);
  }

}
