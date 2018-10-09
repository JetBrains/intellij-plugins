// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeContext;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations;
import com.intellij.lang.javascript.psi.types.primitives.JSStringType;
import com.intellij.psi.PsiElement;
import com.intellij.util.NotNullFunction;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.entities.Angular2DirectiveProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2BindingDescriptor extends Angular2AttributeDescriptor {
  public static final JSType STRING_TYPE = new JSStringType(true, JSTypeSource.EXPLICITLY_DECLARED, JSTypeContext.INSTANCE);
  public static final String INPUT = "Input";
  public static final NotNullFunction<Angular2DirectiveProperty, XmlAttributeDescriptor> FACTORY = Angular2BindingDescriptor::createBinding;
  public static final NullableFunction<Angular2DirectiveProperty, XmlAttributeDescriptor> FACTORY2 =
    Angular2BindingDescriptor::createOneTimeBinding;

  public Angular2BindingDescriptor(@NotNull PsiElement element,
                                   @NotNull String attributeName) {
    super(element.getProject(), attributeName, null, element);
  }

  public static List<XmlAttributeDescriptor> getBindingDescriptors(JSImplicitElement declaration) {
    return ContainerUtil.concat(getDescriptors(declaration, INPUT, FACTORY),
                                getDescriptors(declaration, INPUT, FACTORY2));
  }

  @NotNull
  private static Angular2BindingDescriptor createBinding(Angular2DirectiveProperty info) {
    return new Angular2BindingDescriptor(info.getNavigableElement(), "[" + info.getName() + "]");
  }

  @Nullable
  private static Angular2BindingDescriptor createOneTimeBinding(Angular2DirectiveProperty info) {
    return info.getType() != null
           && expandStringLiteralTypes(info.getType()).isDirectlyAssignableType(STRING_TYPE, null) ?
           new Angular2BindingDescriptor(info.getNavigableElement(), info.getName()) : null;
  }

  @Contract("null->null")
  private static JSType expandStringLiteralTypes(@Nullable JSType type) {
    if (type == null) return null;

    type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
    return type.transformTypeHierarchy(toApply -> toApply instanceof JSStringLiteralTypeImpl ? STRING_TYPE : toApply);
  }

  @Nullable
  @Override
  public String handleTargetRename(@NotNull @NonNls String newTargetName) {
    return "[" + newTargetName + "]";
  }
}
