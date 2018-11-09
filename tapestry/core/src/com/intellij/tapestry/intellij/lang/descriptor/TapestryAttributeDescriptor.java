package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TapestryAttributeDescriptor extends BasicTapestryAttributeDescriptor {
  private final TapestryParameter myParam;

  public TapestryAttributeDescriptor(@NotNull TapestryParameter param) {
    myParam = param;
  }

  @Override
  public PsiElement getDeclaration() {
    final IJavaField field = myParam.getParameterField();
    if (field instanceof IntellijJavaField) return ((IntellijJavaField)field).getPsiField();
    return null; // built in attribute
  }

  @Override
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

  public String getDefaultPrefix() {
    return myParam.getDefaultPrefix();
  }
}
