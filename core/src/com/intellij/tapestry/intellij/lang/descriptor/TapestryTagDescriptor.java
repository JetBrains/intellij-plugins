package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jun 10, 2009
 *         Time: 3:56:33 PM
 */
public class TapestryTagDescriptor extends BasicTapestryTagDescriptor {
  private final PresentationLibraryElement myComponent;

  public TapestryTagDescriptor(@NotNull PresentationLibraryElement component, @Nullable String namespacePrefix) {
    super(namespacePrefix);
    myComponent = component;
  }

  public String getDefaultName() {
    return getPrefixWithColon() + myComponent.getName().toLowerCase().replace('/', '.');
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return context != null
           ? DescriptorUtil.getAttributeDescriptors(context)
           : DescriptorUtil.getAttributeDescriptors((Component)myComponent, null);
  }

  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return context != null
           ? DescriptorUtil.getAttributeDescriptor(attributeName, context)
           : DescriptorUtil.getAttributeDescriptor(attributeName, (Component)myComponent);
  }

  @Override
  public Integer getMinOccurs() {
    return null;
  }

  @Override
  public Integer getMaxOccurs() {
    return null;
  }

  @Override
  public PsiElement getDeclaration() {
    return ((IntellijJavaClassType)myComponent.getElementClass()).getPsiClass();
  }
}
