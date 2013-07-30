package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
abstract public class TypedefDartPsiClass extends AbstractDartPsiClass {
  public TypedefDartPsiClass(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public DartType getSuperClass() {
    return PsiTreeUtil.getChildOfType(this, DartType.class);
  }
}
