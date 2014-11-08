package com.jetbrains.lang.dart;

import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public enum DartComponentType {
  CLASS(AllIcons.Nodes.Class){
    @Override
    public Icon getIcon(@NotNull DartComponent component) {
      return component.isAbstract() ? AllIcons.Nodes.AbstractClass : getIcon();
    }
  },
  FUNCTION(AllIcons.Nodes.Function),
  METHOD(AllIcons.Nodes.Method) {
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
  VARIABLE(AllIcons.Nodes.Variable),
  FIELD(AllIcons.Nodes.Field),
  PARAMETER(AllIcons.Nodes.Parameter),
  TYPEDEF(AllIcons.Nodes.Annotationtype),
  CONSTRUCTOR(AllIcons.Nodes.Method),
  OPERATOR(AllIcons.Nodes.ClassInitializer),
  LABEL(AllIcons.Nodes.Variable);


  private final Icon myIcon;

  DartComponentType(final Icon icon) {
    myIcon = icon;
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

  @Nullable
  public static DartComponentType valueOf(int key) {
    return key >= 0 && key < values().length ? values()[key] : null;
  }

  @Nullable
  public static DartComponentType typeOf(@Nullable PsiElement element) {
    if (element instanceof DartComponentName) {
      return typeOf(element.getParent());
    }
    if ((element instanceof DartComponent && PsiTreeUtil.getParentOfType(element, DartNormalFormalParameter.class, false) != null) ||
        element instanceof DartNormalFormalParameter) {
      return PARAMETER;
    }
    if (element instanceof DartClassDefinition) {
      return CLASS;
    }
    if (element instanceof DartFunctionTypeAlias) {
      return TYPEDEF;
    }
    if (element instanceof DartNamedConstructorDeclaration
        || element instanceof DartFactoryConstructorDeclaration) {
      return CONSTRUCTOR;
    }
    if (element instanceof DartFunctionSignature
        || element instanceof DartFunctionDeclarationWithBody
        || element instanceof DartFunctionDeclarationWithBodyOrNative
        || element instanceof DartFunctionExpression) {
      return FUNCTION;
    }
    if (element instanceof DartOperatorDeclaration) {
      return OPERATOR;
    }
    if (element instanceof DartGetterDeclaration || element instanceof DartSetterDeclaration) {
      final PsiElement dartClassCandidate = PsiTreeUtil.getParentOfType(element, DartComponent.class, DartOperator.class);
      return dartClassCandidate instanceof DartClass ? METHOD : FUNCTION;
    }
    if (element instanceof DartMethodDeclaration) {
      final DartClass dartClass = PsiTreeUtil.getParentOfType(element, DartClass.class);
      final String dartClassName = dartClass != null ? dartClass.getName() : null;
      return dartClassName != null && dartClassName.equals(((DartComponent)element).getName()) ? CONSTRUCTOR : METHOD;
    }
    if (element instanceof DartVarAccessDeclaration
        || element instanceof DartVarDeclarationListPart) {
      return PsiTreeUtil.getParentOfType(element, DartComponent.class, DartOperator.class) instanceof DartClass ? FIELD : VARIABLE;
    }

    if (element instanceof DartForInPart) {
      return VARIABLE;
    }

    if (element instanceof DartLabel) {
      return LABEL;
    }

    return null;
  }

}
