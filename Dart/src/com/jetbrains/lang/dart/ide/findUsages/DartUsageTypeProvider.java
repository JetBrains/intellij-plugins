// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.findUsages;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartUsageTypeProvider implements UsageTypeProvider {
  @Override
  public UsageType getUsageType(@NotNull PsiElement element) {
    if (!(element instanceof DartReference)) {
      return null;
    }
    final PsiElement parent = element.getParent();
    if (parent instanceof DartType) {
      return getUsageTypeByType((DartType)parent);
    }

    if (DartResolveUtil.isLValue(element)) {
      return UsageType.WRITE;
    }

    if (!(parent instanceof DartReference)) {
      return UsageType.READ;
    }
    final PsiElement target = ((DartReference)element).resolve();
    final DartComponentType targetType = DartComponentType.typeOf(target == null ? null : target.getParent());

    final DartReference[] references = PsiTreeUtil.getChildrenOfType(parent, DartReference.class);
    final boolean isFirstChild = references != null && references.length > 0 && references[0] == element;
    if (isFirstChild && references.length == 2 && targetType == DartComponentType.CLASS) {
      return UsageType.CLASS_STATIC_MEMBER_ACCESS;
    }

    if (isFirstChild) {
      return UsageType.READ;
    }
    if (!(parent.getParent() instanceof DartCallExpression)) {
      return UsageType.READ;
    }
    return null;
  }

  @Nullable
  private static UsageType getUsageTypeByType(DartType type) {
    final PsiElement typeParent = type.getParent();
    final DartComponentType typeParentType = DartComponentType.typeOf(typeParent);
    if (typeParent instanceof DartSuperclass) {
      return UsageType.CLASS_EXTENDS_IMPLEMENTS_LIST;
    }
    if (typeParent instanceof DartNewExpression) {
      return UsageType.CLASS_NEW_OPERATOR;
    }
    if (typeParent instanceof DartTypeList && typeParent.getParent() instanceof DartInterfaces) {
      return UsageType.CLASS_EXTENDS_IMPLEMENTS_LIST;
    }
    if (typeParent instanceof DartTypeList && typeParent.getParent() instanceof DartTypeArguments) {
      return UsageType.TYPE_PARAMETER;
    }
    if (typeParent instanceof DartReturnType) {
      return UsageType.CLASS_METHOD_RETURN_TYPE;
    }
    if (typeParent instanceof DartVarAccessDeclaration && typeParentType != null) {
      return switch (typeParentType) {
        case PARAMETER -> UsageType.CLASS_METHOD_PARAMETER_DECLARATION;
        case FIELD -> UsageType.CLASS_FIELD_DECLARATION;
        case METHOD -> UsageType.CLASS_METHOD_RETURN_TYPE;
        default -> null;
      };
    }
    return null;
  }
}
