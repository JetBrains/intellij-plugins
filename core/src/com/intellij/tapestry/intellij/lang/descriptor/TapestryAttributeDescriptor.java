package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jun 10, 2009
 *         Time: 2:45:07 PM
 */
public class TapestryAttributeDescriptor extends BasicXmlAttributeDescriptor {
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

  public void init(PsiElement element) {
  }

  public Object[] getDependences() {
    return new Object[0];
  }

  public boolean isRequired() {
    return myParam.isRequired();
  }

  public boolean isFixed() {
    return false;
  }

  public boolean hasIdType() {
    return false;
  }

  public boolean hasIdRefType() {
    return false;
  }

  public boolean isEnumerated() {
    return false;
  }

  @Nullable
  public String[] getEnumeratedValues() {
    return null;
  }

  public String getDefaultValue() {
    return myParam.getDefaultValue();
  }
}
