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
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;

public class HCLHeredocContentImpl extends HCLHeredocContentMixin implements HCLHeredocContent {

  public HCLHeredocContentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitHeredocContent(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HCLElementVisitor) accept((HCLElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull List<String> getLines() {
    return HCLPsiImplUtilJ.getLines(this);
  }

  @Override
  public @NotNull List<CharSequence> getLinesRaw() {
    return HCLPsiImplUtilJ.getLinesRaw(this);
  }

  @Override
  public int getLinesCount() {
    return HCLPsiImplUtilJ.getLinesCount(this);
  }

  @Override
  public @NotNull String getValue() {
    return HCLPsiImplUtilJ.getValue(this);
  }

  @Override
  public @Nullable Integer getMinimalIndentation() {
    return HCLPsiImplUtilJ.getMinimalIndentation(this);
  }

  @Override
  public @NotNull List<Pair<TextRange, String>> getTextFragments() {
    return HCLPsiImplUtilJ.getTextFragments(this);
  }

}
