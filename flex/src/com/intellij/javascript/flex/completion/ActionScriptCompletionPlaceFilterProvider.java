// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.completion;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagValue;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionScriptCompletionPlaceFilterProvider implements JSCompletionPlaceFilterProvider {

  public static final JSCompletionPlaceFilter AS_TYPE_CONTEXT = new JSCompletionPlaceFilter() {
    @Override
    public boolean isAcceptable(@NotNull JSPsiElementBase element) {
      if (AS_STRICT_TYPE_CONTEXT.isAcceptable(element)) return true;

      return !(element.getContext() instanceof JSClass) && (element instanceof JSFunction || element instanceof JSVariable);
    }
  };

  public static final JSCompletionPlaceFilter AS_STRICT_TYPE_CONTEXT = new JSCompletionPlaceFilter() {
    @Override
    public boolean isAcceptable(@NotNull JSPsiElementBase element) {
      return (element instanceof JSClass || element instanceof JSPackage) &&
             !JSCommonTypeNames.ARGUMENTS_TYPE_NAME.equals(element.getName());
    }

    @Override
    public boolean isPartialResult(@NotNull JSPsiElementBase element) {
      return false;
    }
  };

  public static final JSCompletionPlaceFilter AS_NEW_EXPRESSION_FILTER = new JSCompletionPlaceFilter() {
    @Override
    public boolean isAcceptable(@NotNull JSPsiElementBase element) {
      if (element instanceof JSClass && ((JSClass)element).isInterface() ||
          element instanceof JSFunction &&
          element.getParent() instanceof JSClass &&
          !((JSFunction)element).isConstructor() &&
          (!((JSFunction)element).isGetProperty() || !typeCanBePresentInNew(((JSFunction)element).getReturnType()))) {
        return false;
      }
      else if (element instanceof JSVariable &&
               !typeCanBePresentInNew(((JSVariable)element).getJSType())) {
        return false;
      }
      return true;
    }

    private boolean typeCanBePresentInNew(@Nullable JSType type) {
      if (type == null || type instanceof JSAnyType) return false;

      return JSNamedType.isNamedTypeWithName(type, "Class");
    }
  };


  @Override
  public @Nullable JSCompletionPlaceFilter forPlace(@NotNull PsiElement place) {
    if (!DialectDetector.isActionScript(place)) return null;

    if (place instanceof JSDocTagValue) {
      return AS_TYPE_CONTEXT;
    }
    if (place instanceof JSReferenceExpression && ((JSReferenceExpression)place).getQualifier() == null) {
      if (ResolveProcessor.completeConstructorName(place)) {
        return AS_NEW_EXPRESSION_FILTER;
      }

      boolean strictTypeContext = JSResolveUtil.isExprInTypeContext((JSReferenceExpression)place);
      if (strictTypeContext) {
        return AS_STRICT_TYPE_CONTEXT;
      }
      else if (JSResolveUtil.isInPlaceWhereTypeCanBeDuringCompletion(place)) {
        return AS_TYPE_CONTEXT;
      }
      else {
        return JSDefaultPlaceFilters.ANY;
      }
    }

    return null;
  }
}
