// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSFunctionType;
import com.intellij.lang.javascript.psi.JSParameterTypeDecorator;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.lang.javascript.psi.types.primitives.TypeScriptNeverJSTypeImpl;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlTag;
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.javascript.psi.JSTypeUtils.processExpandedType;
import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.psi.util.CachedValuesManager.getCachedValue;

public class Angular2TypeUtils {

  public static @Nullable JSType getEventVariableType(@Nullable JSType type) {
    if (type == null) {
      return null;
    }
    List<JSType> result = new ArrayList<>();
    processExpandedType(subType -> {
      if (subType instanceof JSGenericTypeImpl) {
        List<JSType> arguments = ((JSGenericTypeImpl)subType).getArguments();
        if (arguments.size() == 1) {
          result.add(arguments.get(0));
        }
        return false;
      }
      else if (subType instanceof JSFunctionType) {
        List<JSParameterTypeDecorator> params = ((JSFunctionType)subType).getParameters();
        if (params.size() == 1 && !params.get(0).isRest()) {
          result.add(params.get(0).getSimpleType());
        }
        return false;
      }
      return true;
    }, type);
    if (result.isEmpty()) {
      return null;
    }
    return JSCompositeTypeImpl.getCommonType(result, type.getSource(), false);
  }

  public static @Nullable JSType getTemplateBindingsContextType(@NotNull Angular2TemplateBindings bindings) {
    return BindingsTypeResolver.get(bindings).resolveTemplateContextType();
  }

  public static @Nullable JSType getNgTemplateTagContextType(@NotNull XmlTag tag) {
    return Angular2TemplateElementsScopeProvider.isTemplateTag(tag) ? getCachedValue(tag, () -> {
      JSType result = BindingsTypeResolver.get(tag).resolveTemplateContextType();
      if (result instanceof TypeScriptNeverJSTypeImpl) {
        result = null;
      }
      return create(result, PsiModificationTracker.MODIFICATION_COUNT);
    }) : null;
  }
}
