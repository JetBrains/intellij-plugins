// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.makefile.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.lang.makefile.psi.MakefileTypes.*;
import com.jetbrains.lang.makefile.psi.*;

public class MakefileFunctionImpl extends MakefileFunctionElementImpl implements MakefileFunction {

  public MakefileFunctionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitFunction(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MakefileFunctionName getFunctionName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, MakefileFunctionName.class));
  }

  @Override
  @NotNull
  public List<MakefileFunctionParam> getFunctionParamList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileFunctionParam.class);
  }

}
