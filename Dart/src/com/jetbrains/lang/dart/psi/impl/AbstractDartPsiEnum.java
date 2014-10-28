package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartEnum;
import com.jetbrains.lang.dart.psi.DartEnumConstantDeclaration;
import com.jetbrains.lang.dart.psi.DartEnumConstantDeclarationList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class AbstractDartPsiEnum extends AbstractDartComponentImpl implements DartEnum {

  public AbstractDartPsiEnum(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public List<DartEnumConstantDeclaration> getConstants() {
    final DartEnumConstantDeclarationList declarations = PsiTreeUtil.getChildOfType(this, DartEnumConstantDeclarationList.class);
    if (declarations != null) {
      return declarations.getEnumConstantDeclarationList();
    }
    return Collections.emptyList();
  }
}
