// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.jhipster.psi.JdlTokenTypes.*;
import com.intellij.jhipster.psi.JdlEnumMixin;
import com.intellij.jhipster.psi.*;

public class JdlEnumImpl extends JdlEnumMixin implements JdlEnum {

  public JdlEnumImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JdlVisitor visitor) {
    visitor.visitEnum(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JdlVisitor) accept((JdlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public JdlEnumId getEnumId() {
    return findChildByClass(JdlEnumId.class);
  }

  @Override
  @NotNull
  public List<JdlEnumValue> getEnumValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, JdlEnumValue.class);
  }

}
