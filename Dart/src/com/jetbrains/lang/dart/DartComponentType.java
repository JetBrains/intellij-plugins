// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Supplier;

public enum DartComponentType {
  CLASS(AllIcons.Nodes.Class, DartBundle.messagePointer("find.usages.type.class")) {
    @Override
    public Icon getIcon(@NotNull DartComponent component) {
      return component instanceof DartEnumDefinition ? AllIcons.Nodes.Enum
                                                     : component.isAbstract() ? AllIcons.Nodes.AbstractClass
                                                                              : getIcon();
    }
  },
  FUNCTION(AllIcons.Nodes.Lambda, DartBundle.messagePointer("find.usages.type.function")) {
    @Override
    public Icon getIcon(@NotNull DartComponent component) {
      if (component.isGetter()) {
        return AllIcons.Nodes.PropertyReadStatic;
      }
      if (component.isSetter()) {
        return AllIcons.Nodes.PropertyWriteStatic;
      }
      return getIcon();
    }
  },
  METHOD(AllIcons.Nodes.Method, DartBundle.messagePointer("find.usages.type.method")) {
    @Override
    public Icon getIcon(@NotNull DartComponent component) {
      if (component.isGetter()) {
        return component.isStatic() ? AllIcons.Nodes.PropertyReadStatic : AllIcons.Nodes.PropertyRead;
      }
      if (component.isSetter()) {
        return component.isStatic() ? AllIcons.Nodes.PropertyWriteStatic : AllIcons.Nodes.PropertyWrite;
      }
      return component.isAbstract() ? AllIcons.Nodes.AbstractMethod : getIcon();
    }
  },
  LOCAL_VARIABLE(AllIcons.Nodes.Variable, DartBundle.messagePointer("find.usages.type.local.variable")),
  GLOBAL_VARIABLE(AllIcons.Nodes.Variable, DartBundle.messagePointer("find.usages.type.top.level.variable")),
  FIELD(AllIcons.Nodes.Field, DartBundle.messagePointer("find.usages.type.field")),
  PARAMETER(AllIcons.Nodes.Parameter, DartBundle.messagePointer("find.usages.type.parameter")),
  TYPEDEF(AllIcons.Nodes.Annotationtype, DartBundle.messagePointer("find.usages.type.typedef")),
  CONSTRUCTOR(AllIcons.Nodes.Method, DartBundle.messagePointer("find.usages.type.constructor")),
  OPERATOR(AllIcons.Nodes.ClassInitializer, DartBundle.messagePointer("find.usages.type.operator")),
  LABEL(AllIcons.Nodes.Variable, DartBundle.messagePointer("find.usages.type.label"));

  private final Icon myIcon;
  private final Supplier<@Nls String> myUsageTypeSupplier;

  DartComponentType(final Icon icon, Supplier<@Nls String> supplier) {
    myIcon = icon;
    myUsageTypeSupplier = supplier;
  }

  public int getKey() {
    return ordinal();
  }

  public Icon getIcon() {
    return myIcon;
  }

  public Icon getIcon(@NotNull DartComponent component) {
    // Overridden in appropriate subclasses
    return getIcon();
  }

  public @NotNull @Nls String getUsageType() {
    return myUsageTypeSupplier.get();
  }

  public static @Nullable DartComponentType valueOf(int key) {
    return key >= 0 && key < values().length ? values()[key] : null;
  }

  public static @Nullable DartComponentType typeOf(@Nullable PsiElement element) {
    if (element instanceof DartComponentName) {
      return typeOf(element.getParent());
    }
    if ((element instanceof DartComponent && PsiTreeUtil.getParentOfType(element, DartNormalFormalParameter.class, false) != null) ||
        element instanceof DartNormalFormalParameter) {
      return PARAMETER;
    }
    if (element instanceof DartClass) {
      return CLASS;
    }
    if (element instanceof DartEnumConstantDeclaration) {
      return FIELD;
    }
    if (element instanceof DartFunctionTypeAlias) {
      return TYPEDEF;
    }
    if (element instanceof DartNamedConstructorDeclaration
        || element instanceof DartFactoryConstructorDeclaration) {
      return CONSTRUCTOR;
    }
    if (element instanceof DartFunctionFormalParameter
        || element instanceof DartFunctionDeclarationWithBody
        || element instanceof DartFunctionDeclarationWithBodyOrNative
        || element instanceof DartFunctionExpression) {
      return FUNCTION;
    }
    if (element instanceof DartGetterDeclaration || element instanceof DartSetterDeclaration) {
      final PsiElement dartClassCandidate = PsiTreeUtil.getParentOfType(element, DartComponent.class);
      return dartClassCandidate instanceof DartClass ? METHOD : FUNCTION;
    }
    if (element instanceof DartMethodDeclaration) {
      if (((DartMethodDeclaration)element).isOperator()) {
        return OPERATOR;
      }
      final DartClass dartClass = PsiTreeUtil.getParentOfType(element, DartClass.class);
      final String dartClassName = dartClass != null ? dartClass.getName() : null;
      return dartClassName != null && dartClassName.equals(((DartComponent)element).getName()) ? CONSTRUCTOR : METHOD;
    }
    if (element instanceof DartVarAccessDeclaration || element instanceof DartVarDeclarationListPart) {
      DartComponent parentComponent = PsiTreeUtil.getParentOfType(element, DartComponent.class);
      DartComponentType parentType = typeOf(parentComponent);
      if (parentType == CLASS) return FIELD;
      if (parentType == CONSTRUCTOR || parentType == METHOD || parentType == FUNCTION || parentType == OPERATOR) return LOCAL_VARIABLE;
      return GLOBAL_VARIABLE;
    }

    if (element instanceof DartForInPart) {
      return LOCAL_VARIABLE;
    }

    if (element instanceof DartLabel) {
      return LABEL;
    }

    return null;
  }
}
