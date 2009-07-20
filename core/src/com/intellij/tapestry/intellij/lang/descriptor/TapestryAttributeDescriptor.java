package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jun 10, 2009
 *         Time: 2:45:07 PM
 */
public class TapestryAttributeDescriptor extends BasicTapestryAttributeDescriptor {
  private final TapestryParameter myParam;

  public TapestryAttributeDescriptor(@NotNull TapestryParameter param) {
    myParam = param;
  }

  public PsiElement getDeclaration() {
    return ((IntellijJavaField)myParam.getParameterField()).getPsiField();
  }

  public String getName() {
    return myParam.getName();
  }

  @Override
  public boolean isRequired() {
    return myParam.isRequired();
  }

  @Override
  public String getDefaultValue() {
    return myParam.getDefaultValue();
  }
}
