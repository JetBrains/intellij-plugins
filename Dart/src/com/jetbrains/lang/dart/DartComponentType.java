package com.jetbrains.lang.dart;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import icons.DartIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author: Fedor.Korotkov
 */
public enum DartComponentType {
  CLASS(0) {
    @Override
    public Icon getIcon() {
      return DartIcons.Class_dart;
    }
  }, INTERFACE(1) {
    @Override
    public Icon getIcon() {
      return DartIcons.Interface_dart;
    }
  }, FUNCTION(2) {
    @Override
    public Icon getIcon() {
      return DartIcons.Function_dart;
    }
  }, METHOD(3) {
    @Override
    public Icon getIcon() {
      return DartIcons.Method_dart;
    }
  }, VARIABLE(4) {
    @Override
    public Icon getIcon() {
      return DartIcons.Variable_dart;
    }
  }, FIELD(5) {
    @Override
    public Icon getIcon() {
      return DartIcons.Field_dart;
    }
  }, PARAMETER(6) {
    @Override
    public Icon getIcon() {
      return DartIcons.Parameter_dart;
    }
  }, TYPEDEF(7) {
    @Override
    public Icon getIcon() {
      return DartIcons.Annotationtype_dart;
    }
  }, CONSTRUCTOR(8) {
    @Override
    public Icon getIcon() {
      return DartIcons.Class_dart;
    }
  }, OPERATOR(9) {
    @Override
    public Icon getIcon() {
      return DartIcons.Method_dart;
    }
  }, LABEL(10) {
    @Override
    public Icon getIcon() {
      return DartIcons.Label_dart;
    }
  };
  private final int myKey;

  DartComponentType(int key) {
    myKey = key;
  }

  public int getKey() {
    return myKey;
  }

  public abstract Icon getIcon();

  @Nullable
  public static DartComponentType valueOf(int key) {
    switch (key) {
      case 0:
        return CLASS;
      case 1:
        return INTERFACE;
      case 2:
        return FUNCTION;
      case 3:
        return METHOD;
      case 4:
        return VARIABLE;
      case 5:
        return FIELD;
      case 6:
        return PARAMETER;
      case 7:
        return TYPEDEF;
      case 8:
        return CONSTRUCTOR;
      case 9:
        return OPERATOR;
      case 10:
        return LABEL;
    }
    return null;
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
    if (element instanceof DartInterfaceDefinition) {
      return INTERFACE;
    }
    if (element instanceof DartFunctionTypeAlias || element instanceof DartClassTypeAlias) {
      return TYPEDEF;
    }
    if (element instanceof DartNamedConstructorDeclaration
        || element instanceof DartFactoryConstructorDeclaration) {
      return CONSTRUCTOR;
    }
    if (element instanceof DartFunctionDeclaration
        || element instanceof DartFunctionDeclarationWithBody
        || element instanceof DartFunctionDeclarationWithBodyOrNative
        || element instanceof DartFunctionExpression) {
      return FUNCTION;
    }
    if (element instanceof DartOperatorDeclaration
        || element instanceof DartAbstractOperatorDeclaration) {
      return OPERATOR;
    }
    if (element instanceof DartGetterDeclaration || element instanceof DartSetterDeclaration) {
      final PsiElement dartClassCandidate = PsiTreeUtil.getParentOfType(element, DartComponent.class, DartOperator.class);
      return dartClassCandidate instanceof DartClass ? METHOD : FUNCTION;
    }
    if (element instanceof DartMethodDeclaration
        || element instanceof DartMethodPrototypeDeclaration) {
      final DartClass dartClass = PsiTreeUtil.getParentOfType(element, DartClass.class);
      final String dartClassName = dartClass != null ? dartClass.getName() : null;
      return dartClassName != null && dartClassName.equals(((DartComponent)element).getName()) ? CONSTRUCTOR : METHOD;
    }
    if (element instanceof DartVarDeclaration
        || element instanceof DartVarAccessDeclaration
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
