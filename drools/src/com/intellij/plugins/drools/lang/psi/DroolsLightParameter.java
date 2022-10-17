package com.intellij.plugins.drools.lang.psi;

import com.intellij.ide.presentation.Presentation;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.light.LightParameter;
import org.jetbrains.annotations.NotNull;

@Presentation(typeName = DroolsLightParameter.PARAMETER)
public class DroolsLightParameter extends LightParameter {
  public static final String PARAMETER = "Parameter";
  private final DroolsParameter myDroolsParameter;

  public DroolsLightParameter(DroolsParameter droolsParameter, @NotNull String name, @NotNull PsiType type, PsiElement declarationScope) {
    super(name, type, declarationScope, JavaLanguage.INSTANCE);
    myDroolsParameter = droolsParameter;

    setNavigationElement(myDroolsParameter);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DroolsLightParameter parameter = (DroolsLightParameter)o;

    if (myDroolsParameter != null ? !myDroolsParameter.equals(parameter.myDroolsParameter) : parameter.myDroolsParameter != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return myDroolsParameter != null ? myDroolsParameter.hashCode() : 0;
  }
}
