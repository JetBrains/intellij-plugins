// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi;

import com.intellij.ide.presentation.Presentation;
import com.intellij.plugins.drools.DroolsLanguage;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.impl.light.LightParameter;
import org.jetbrains.annotations.NotNull;

@Presentation(typeName = DroolsFunction.FUNCTION, icon = "AllIcons.Nodes.Method")
public class DroolsFunctionLightMethodBuilder extends LightMethodBuilder {
  private final DroolsFunctionStatement myFunction;

  public DroolsFunctionLightMethodBuilder(@NotNull DroolsFunctionStatement function) {
    super(function.getManager(), DroolsLanguage.INSTANCE, function.getFunctionName());
    myFunction = function;

    init(myFunction);
  }

  private void init(DroolsFunctionStatement function) {
    final DroolsType type = function.getType();
    if (type != null) {
      setMethodReturnType(type.getText());
    }

    final DroolsParameters parameters = function.getFunctionParameters();
    if (parameters != null) {
      for (DroolsParameter droolsParameter : parameters.getParameterList()) {
        addParameter(createParameter(droolsParameter));
      }
    }
  }

  @NotNull
  private PsiParameter createParameter(@NotNull DroolsParameter droolsParameter) {
    LightParameter parameter;
    final String paramName = droolsParameter.getNameId().getText();
    final PsiType psiType = DroolsResolveUtil.resolveType(droolsParameter.getType());

    if (psiType == null) {
      parameter = new DroolsLightParameter(droolsParameter, paramName, PsiTypes.nullType(), this);
    } else {
      parameter = new DroolsLightParameter(droolsParameter,paramName, psiType, this);
    }

    parameter.setNavigationElement(droolsParameter);

    return parameter;
  }

  @NotNull
  @Override
  public PsiElement getNavigationElement() {
    return myFunction;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DroolsFunctionLightMethodBuilder builder = (DroolsFunctionLightMethodBuilder)o;

    if (!myFunction.equals(builder.myFunction)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + myFunction.hashCode();
    return result;
  }
}
