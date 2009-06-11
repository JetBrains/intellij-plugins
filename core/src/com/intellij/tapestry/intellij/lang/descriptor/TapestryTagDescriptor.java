package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiWritableMetaData;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementDescriptorAwareAboutChildren;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jun 10, 2009
 *         Time: 3:56:33 PM
 */
public class TapestryTagDescriptor implements XmlElementDescriptor, PsiWritableMetaData, XmlElementDescriptorAwareAboutChildren {
  private final Component myComponent;
  public TapestryTagDescriptor(Component component) {
    myComponent = component;
  }

  public String getQualifiedName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getDefaultName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return new XmlElementDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return new XmlAttributeDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public XmlNSDescriptor getNSDescriptor() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  public PsiElement getDeclaration() {
    return ((IntellijJavaClassType)myComponent.getElementClass()).getPsiClass();
  }

  public String getName(PsiElement context) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void init(PsiElement element) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public Object[] getDependences() {
    return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void setName(String name) throws IncorrectOperationException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean allowElementsFromNamespace(String namespace, XmlTag context) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
