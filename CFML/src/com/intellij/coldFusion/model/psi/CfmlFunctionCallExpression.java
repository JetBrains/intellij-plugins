// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlFunctionCallExpression extends CfmlCompositeElement implements CfmlExpression, CfmlFunctionCall, CfmlTypedElement {
  public CfmlFunctionCallExpression(final ASTNode node) {
    super(node);
  }

  public boolean isCreateFromJavaLoader() {
    PsiElement firstChild = findChildByType(CfmlElementTypes.REFERENCE_EXPRESSION);
    if (firstChild == null) {
      return false;
    }
    PsiElement lastChild = firstChild.getLastChild();
    if (lastChild == null) {
      return false;
    }
    String create = lastChild.getText();
    PsiElement secondChild = firstChild.getFirstChild();

    if (!(StringUtil.toLowerCase(create).equals("create") && secondChild instanceof CfmlReferenceExpression)) {
      return false;
    }
    PsiType type = ((CfmlReferenceExpression)secondChild).getPsiType();
    if (type == null) {
      return false;
    }
    return StringUtil.toLowerCase(type.getCanonicalText()).equals("javaloader");
  }

  public class PsiClassStaticType extends PsiClassReferenceType {
    private final String myClassName;

    public PsiClassStaticType(final PsiJavaCodeReferenceElement reference, String className) {
      super(reference, null);
      myClassName = className;
    }

    @Nullable
    public PsiType getRawType() {
      return CfmlPsiUtil.getTypeByName(myClassName, getProject());
    }
  }

  @Nullable
  public PsiType getExternalType() {
    String functionName = getFunctionName();

    final CfmlReference referenceExpression = getReferenceExpression();
    // createObject specific code
    if ("createobject".equals(StringUtil.toLowerCase(functionName))) {
      final CfmlArgumentList cfmlArgumentList = findArgumentList();
      if (cfmlArgumentList == null) {
        return null;
      }
      CfmlExpression[] argumentsList = cfmlArgumentList.getArguments();
      if (argumentsList.length == 0) {
        return null;
      }
      if (argumentsList[0] instanceof CfmlStringLiteralExpression) {
        final String secondParameterName = StringUtil.toLowerCase(((CfmlStringLiteralExpression)argumentsList[0]).getValue());
        if ("java".equals(secondParameterName) && argumentsList.length >= 2) {
          String className = argumentsList[1].getText();
          className = className.substring(1, className.length() - 1);
          final PsiJavaCodeReferenceElement reference =
            JavaPsiFacade.getInstance(getProject()).getElementFactory().createReferenceElementByFQClassName(className, getResolveScope());
          return new PsiClassStaticType(reference, className);
        }
        else if (("component".equals(secondParameterName) && argumentsList.length >= 2) ||
                 (argumentsList.length == 1)) {
          final PsiReference[] references = argumentsList[argumentsList.length == 1 ? 0 : 1].getReferences();
          if (references.length != 0 && references[0] instanceof CfmlComponentReference componentRef) {
            return new CfmlComponentType(componentRef.getText(), getContainingFile(), getProject());
          }
        }
      }
    }
    else if ("init".equals(StringUtil.toLowerCase(getFunctionShortName()))) {
      CfmlReference qualifier = CfmlPsiUtil.getQualifierInner(this);
      CfmlReference sourceObject = CfmlPsiUtil.getQualifierInner(qualifier);

      if (sourceObject != null) {
        final PsiType type = sourceObject.getPsiType();
        if (type instanceof PsiClassStaticType) {
          return ((PsiClassStaticType)type).getRawType();
        }
        if (type instanceof CfmlComponentType) {
          return type;
        }
      }
    }
    else if (isCreateFromJavaLoader()) {
      CfmlArgumentList argumentList = findArgumentList();
      if (argumentList == null) {
        return null;
      }
      CfmlExpression[] argumentsList = argumentList.getArguments();
      if (argumentsList.length == 0) {
        return null;
      }
      String className = argumentsList[0].getText();
      className = className.substring(1, className.length() - 1);
      return CfmlPsiUtil.getTypeByName(className, getProject());
    }
    else if (referenceExpression != null) {
      final PsiElement resolve = referenceExpression.resolve();
      return resolve instanceof CfmlFunction ? ((CfmlFunction)resolve).getReturnType() : null;
    }

    return null;
  }

  @Override
  @Nullable
  public PsiType getPsiType() {
    PsiType externalType = getExternalType();

    if (externalType == null) {
      CfmlReference referenceExpression = getReferenceExpression();
      return referenceExpression != null ? referenceExpression.getPsiType() : null;
    }

    return externalType;
  }

  public CfmlExpression @NotNull [] getArguments() {
    CfmlArgumentList argumentListEl = findChildByClass(CfmlArgumentList.class);
    if (argumentListEl == null) {
      return new CfmlExpression[0];
    }
    return argumentListEl.getArguments();
  }

  public String @NotNull [] getArgumentsAsStrings() {
    CfmlArgumentList argumentListEl = findChildByClass(CfmlArgumentList.class);
    if (argumentListEl == null) {
      return ArrayUtilRt.EMPTY_STRING_ARRAY;
    }
    final CfmlExpression[] args = argumentListEl.getArguments();
    return ContainerUtil.map(args, cfmlExpression -> {
      if (cfmlExpression instanceof CfmlStringLiteralExpression) {
        return StringUtil.toLowerCase(((CfmlStringLiteralExpression)cfmlExpression).getValue());
      }
      return "";
    }, ArrayUtilRt.EMPTY_STRING_ARRAY);
  }

  public String getFunctionShortName() {
    CfmlReference referenceExpression = getReferenceExpression();
    if (referenceExpression == null) {
      return "";
    }
    final PsiElement child = referenceExpression.getLastChild();
    return child != null ? child.getText() : "";
  }

  public String getFunctionName() {
    String functionName = "";
    CfmlReference referenceExpression = getReferenceExpression();
    if (referenceExpression != null && referenceExpression.getText() != null) {
      functionName = referenceExpression.getText();
    }
    return functionName;
  }

  public boolean isCreateObject() {
    return StringUtil.toLowerCase(getFunctionName()).equals("createobject");
  }

  public boolean isExpandPath() {
    return StringUtil.toLowerCase(getFunctionName()).equals("expandpath");
  }

  @Override
  @Nullable
  public CfmlReference getReferenceExpression() {
    return findChildByClass(CfmlReferenceExpression.class);
  }

  @Override
  @Nullable
  public CfmlArgumentList findArgumentList() {
    return findChildByClass(CfmlArgumentList.class);
  }

  @Override
  public PsiType[] getArgumentTypes() {
    CfmlArgumentList argumentsList = findArgumentList();
    if (argumentsList == null) {
      return PsiType.EMPTY_ARRAY;
    }
    CfmlExpression[] args = argumentsList.getArguments();
    return ContainerUtil.map2Array(args, PsiType.class, (NullableFunction<CfmlExpression, PsiType>)expression -> expression.getPsiType());
  }
}
