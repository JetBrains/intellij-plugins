// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSTypeHelper;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.lang.javascript.psi.types.JSSpecialNamedTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeImpl;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptTypeHelper extends JSTypeHelper {
  private static final Key<PsiElement> ourResolvedTypeKey = Key.create("resolved.type");
  private static final JSTypeHelper ourTypeHelper = new ActionScriptTypeHelper();

  private ActionScriptTypeHelper() {
  }

  public static JSTypeHelper getInstance() {
    return ourTypeHelper;
  }

  @Override
  public boolean isAssignableToNamedType(@NotNull JSTypeImpl lOpType, @NotNull JSType rOpType, @NotNull ProcessingContext processingContext) {
    @SuppressWarnings("unchecked")
    Map<Key<PsiElement>, PsiElement> cachesMap = (Map<Key<PsiElement>, PsiElement>)processingContext.get(this);

    PsiElement type = cachesMap == null ? null : cachesMap.get(ourResolvedTypeKey);
    JSClass clazz = null;

    if (type == null) {
      type = lOpType.resolveClass();
      if (cachesMap != null) {
        cachesMap.put(ourResolvedTypeKey, type != null ? type : PsiUtilCore.NULL_PSI_ELEMENT);
      }
    }
    else if (type == PsiUtilCore.NULL_PSI_ELEMENT) {
      type = null;
    }

    if (type instanceof JSClass) clazz = (JSClass)type;
    JSClass jsClass = rOpType.resolveClass();

    if (jsClass == null && rOpType instanceof JSTypeImpl || rOpType instanceof JSSpecialNamedTypeImpl) {
      return areNamedTypesAssignable(lOpType, (JSNamedType)rOpType, processingContext);
    }
    if (jsClass != null && clazz != null) {
      return JSInheritanceUtil.isParentClass(jsClass, clazz, false, jsClass);
    }
    if (clazz == null) {
      return false;
    }
    return true;
  }

  @Nullable
  @Override
  public JSType getTypeForIndexing(@Nullable JSExpression expression, @NotNull PsiElement context) {
    return null;
  }
}
