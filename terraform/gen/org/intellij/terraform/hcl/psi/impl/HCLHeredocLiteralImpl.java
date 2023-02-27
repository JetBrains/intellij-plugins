// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.terraform.hcl.HCLElementTypes.*;
import org.intellij.terraform.hcl.psi.*;

public class HCLHeredocLiteralImpl extends HCLLiteralImpl implements HCLHeredocLiteral {

  public HCLHeredocLiteralImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitHeredocLiteral(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HCLElementVisitor) accept((HCLElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull String getValue() {
    return HCLPsiImplUtilJ.getValue(this);
  }

  @Override
  @NotNull
  public HCLHeredocContent getContent() {
    return findNotNullChildByClass(HCLHeredocContent.class);
  }

  @Override
  @NotNull
  public HCLHeredocMarker getMarkerStart() {
    List<HCLHeredocMarker> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, HCLHeredocMarker.class);
    return p1.get(0);
  }

  @Override
  @Nullable
  public HCLHeredocMarker getMarkerEnd() {
    List<HCLHeredocMarker> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, HCLHeredocMarker.class);
    return p1.size() < 2 ? null : p1.get(1);
  }

  @Override
  public boolean isIndented() {
    return HCLPsiImplUtilJ.isIndented(this);
  }

  @Override
  public @Nullable Integer getIndentation() {
    return HCLPsiImplUtilJ.getIndentation(this);
  }

}
