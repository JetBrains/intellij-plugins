package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public class TapestryParameterDescriptor extends BasicTapestryTagDescriptor {
  private final Component myComponent;
  private final TapestryParameter myParameter;

  public TapestryParameterDescriptor(Component component, @NotNull TapestryParameter parameter, @Nullable String namespacePrefix) {
    super(namespacePrefix);
    myComponent = component;
    myParameter = parameter;
  }

  public String getDefaultName() {
    return getPrefixWithColon() + myParameter.getName();
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return new XmlAttributeDescriptor[0];
  }

  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return null;
  }

  @Override
  public PsiElement getDeclaration() {
    final PsiClass psiClass = ((IntellijJavaClassType)myComponent.getElementClass()).getPsiClass();
    return psiClass == null ? null : psiClass.findFieldByName(myParameter.getName(), true);
  }
}
