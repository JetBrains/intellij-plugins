// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.protobuf.lang.psi.PbTypes.*;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import static com.intellij.protobuf.lang.psi.ProtoTokenTypes.*;

public class PbEnumReservedRangeImpl extends PbEnumReservedRangeMixin implements PbEnumReservedRange {

  public PbEnumReservedRangeImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PbVisitor visitor) {
    visitor.visitEnumReservedRange(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbVisitor) accept((PbVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PbNumberValue> getNumberValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbNumberValue.class);
  }

  @Override
  @NotNull
  public PbNumberValue getFromValue() {
    List<PbNumberValue> p1 = getNumberValueList();
    return p1.get(0);
  }

  @Override
  @Nullable
  public PbNumberValue getToValue() {
    List<PbNumberValue> p1 = getNumberValueList();
    return p1.size() < 2 ? null : p1.get(1);
  }

}
