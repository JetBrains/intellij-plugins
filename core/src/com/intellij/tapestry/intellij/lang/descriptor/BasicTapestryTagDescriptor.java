package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiWritableMetaData;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.tapestry.intellij.lang.descriptor.DescriptorUtil.getTmlOrHtmlTagDescriptor;
import static com.intellij.tapestry.intellij.lang.descriptor.DescriptorUtil.getTmlSubelementDescriptors;

/**
 * @author Alexey Chmutov
 */
public abstract class BasicTapestryTagDescriptor implements XmlElementDescriptor, PsiWritableMetaData {
  private final TapestryNamespaceDescriptor myNamespaceDescriptor;
  private final String myNamespacePrefix;

  public BasicTapestryTagDescriptor(@Nullable String namespacePrefix, TapestryNamespaceDescriptor descriptor) {
    myNamespacePrefix = namespacePrefix;
    myNamespaceDescriptor = descriptor;
  }

  protected final String getPrefixWithColon() {
    return myNamespacePrefix != null && myNamespacePrefix.length() > 0 ? myNamespacePrefix + ":" : "";
  }

  public String getQualifiedName() {
    return getDefaultName();
  }

  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return getTmlSubelementDescriptors(context, myNamespaceDescriptor);
  }

  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return getTmlOrHtmlTagDescriptor(childTag);
  }

  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    String prefix = attribute.getNamespacePrefix();
    return prefix.length() == 0 || prefix.equals(myNamespacePrefix)
           ? getAttributeDescriptor(attribute.getName(), attribute.getParent())
           : null;
  }

  public XmlNSDescriptor getNSDescriptor() {
    return myNamespaceDescriptor;
  }

  /**
   * @return minimal occurrence constraint value (e.g. 0 or 1), on null if not applied
   */
  @Override
  public XmlElementsGroup getTopGroup() {
    return null;
  }

  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Nullable
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

  @NotNull
  public Object[] getDependencies() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public void setName(String name) throws IncorrectOperationException {

  }
}
