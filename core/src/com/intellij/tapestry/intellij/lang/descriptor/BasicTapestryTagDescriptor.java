package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiWritableMetaData;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import static com.intellij.tapestry.intellij.lang.descriptor.DescriptorUtil.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jul 16, 2009
 *         Time: 3:25:47 PM
 */
public abstract class BasicTapestryTagDescriptor implements XmlElementDescriptor, PsiWritableMetaData {
  private final String myNamespacePrefix;

  public BasicTapestryTagDescriptor(@Nullable String namespacePrefix) {
    myNamespacePrefix = namespacePrefix;
  }

  protected final String getPrefixWithColon() {
    return myNamespacePrefix != null && myNamespacePrefix.length() > 0 ? myNamespacePrefix + ":" : "";
  }

  public String getQualifiedName() {
    return getDefaultName();
  }

  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return getTmlSubelementDescriptors(context);
  }

  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return getTmlOrHtmlTagDescriptor(childTag);
  }

  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), attribute.getParent());
  }

  public XmlNSDescriptor getNSDescriptor() {
    return TapestryNamespaceDescriptor.INSTANCE;
  }

  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  public PsiElement getDeclaration() {
    return null;
  }

  public String getName(PsiElement context) {
    return getDefaultName();
  }

  public String getName() {
    return getDefaultName();
  }

  public void init(PsiElement element) {
  }

  public Object[] getDependences() {
    return new Object[0];
  }

  public void setName(String name) throws IncorrectOperationException {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
