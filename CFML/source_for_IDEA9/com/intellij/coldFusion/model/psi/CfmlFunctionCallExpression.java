/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 * Date: 24.02.2009
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

    if (!(create.toLowerCase().equals("create") && secondChild instanceof CfmlReferenceExpression)) {
      return false;
    }
    PsiType type = ((CfmlReferenceExpression)secondChild).getPsiType();
    if (type == null) {
      return false;
    }
    return type.getCanonicalText().toLowerCase().equals("javaloader");
  }

  public class PsiClassStaticType extends PsiClassReferenceType {
    private String myClassName;

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
    if ("createobject".equals(functionName.toLowerCase())) {
      final CfmlArgumentList cfmlArgumentList = findArgumentList();
      if (cfmlArgumentList == null) {
        return null;
      }
      CfmlExpression[] argumentsList = cfmlArgumentList.getArguments();
      if (argumentsList.length == 0) {
        return null;
      }
      if (argumentsList[0] instanceof CfmlStringLiteralExpression) {
        final String secondParameterName = ((CfmlStringLiteralExpression)argumentsList[0]).getValue().toLowerCase();
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
          if (references.length != 0 && references[0] instanceof CfmlComponentReference) {
            final CfmlComponentReference componentRef = ((CfmlComponentReference)references[0]);
            if (componentRef != null) {
              return new CfmlComponentType(componentRef.getText(), getContainingFile(), getProject());
            }
          }
        }
      }
    }
    else if ("init".equals(getFunctionShortName().toLowerCase())) {
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

  @Nullable
  public PsiType getPsiType() {
    PsiType externalType = getExternalType();

    if (externalType == null) {
      CfmlReference referenceExpression = getReferenceExpression();
      return referenceExpression != null ? referenceExpression.getPsiType() : null;
    }

    return externalType;
  }

  @NotNull
  public CfmlExpression[] getArguments() {
    CfmlArgumentList argumentListEl = findChildByClass(CfmlArgumentList.class);
    if (argumentListEl == null) {
      return new CfmlExpression[0];
    }
    return argumentListEl.getArguments();
  }

  @NotNull
  public String[] getArgumentsAsStrings() {
    CfmlArgumentList argumentListEl = findChildByClass(CfmlArgumentList.class);
    if (argumentListEl == null) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
    final CfmlExpression[] args = argumentListEl.getArguments();
    return ContainerUtil.map(args, new Function<CfmlExpression, String>() {
      public String fun(CfmlExpression cfmlExpression) {
        if (cfmlExpression instanceof CfmlStringLiteralExpression) {
          return ((CfmlStringLiteralExpression)cfmlExpression).getValue().toLowerCase();
        }
        return "";
      }
    }, ArrayUtil.EMPTY_STRING_ARRAY);
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
    return getFunctionName().toLowerCase().equals("createobject");
  }

  public boolean isExpandPath() {
    return getFunctionName().toLowerCase().equals("expandpath");
  }

  @Nullable
  public CfmlReference getReferenceExpression() {
    return findChildByClass(CfmlReferenceExpression.class);
  }

  @Nullable
  public CfmlArgumentList findArgumentList() {
    return findChildByClass(CfmlArgumentList.class);
  }

  public PsiType[] getArgumentTypes() {
    CfmlArgumentList argumentsList = findArgumentList();
    if (argumentsList == null) {
      return PsiType.EMPTY_ARRAY;
    }
    CfmlExpression[] args = argumentsList.getArguments();
    return ContainerUtil.map2Array(args, PsiType.class, new NullableFunction<CfmlExpression, PsiType>() {
      public PsiType fun(final CfmlExpression expression) {
        return expression.getPsiType();
      }
    });
  }
}
