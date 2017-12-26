package org.angularjs.codeInsight.tags;

import com.intellij.html.impl.DelegatingRelaxedHtmlElementDescriptor;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.*;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
import org.angularjs.codeInsight.attributes.AngularJSAttributeDescriptorsProvider;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTagDescriptor implements XmlElementDescriptor {
  protected final String myName;
  private final JSImplicitElement myDeclaration;

  public AngularJSTagDescriptor(String name, JSImplicitElement declaration) {
    myName = name;
    myDeclaration = declaration;
  }

  @Override
  public String getQualifiedName() {
    return myName;
  }

  @Override
  public String getDefaultName() {
    return myName;
  }

  @Override
  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    XmlDocumentImpl xmlDocument = PsiTreeUtil.getParentOfType(context, XmlDocumentImpl.class);
    if (xmlDocument == null) return EMPTY_ARRAY;
    return ContainerUtil.map2Array(xmlDocument.getRootTagNSDescriptor().getRootElementsDescriptors(xmlDocument),
      XmlElementDescriptor.class, descriptor -> wrapInDelegating(descriptor));
  }

  @Override
  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    final XmlDocument document = PsiTreeUtil.getParentOfType(contextTag, XmlDocument.class);
    if (document == null) {
      return null;
    }
    final XmlNSDescriptor nsDescriptor = document.getDefaultNSDescriptor(childTag.getNamespace(), true);
    if (nsDescriptor instanceof XmlNSDescriptorEx) {
      XmlElementDescriptor descriptor = ((XmlNSDescriptorEx)nsDescriptor).getElementDescriptor(childTag.getLocalName(), childTag.getNamespace());
      return descriptor != null ? wrapInDelegating(descriptor) : null;
    }
    return null;
  }

  @NotNull
  public DelegatingRelaxedHtmlElementDescriptor wrapInDelegating(XmlElementDescriptor descriptor) {
    return descriptor instanceof DelegatingRelaxedHtmlElementDescriptor ? (DelegatingRelaxedHtmlElementDescriptor)descriptor :
           new DelegatingRelaxedHtmlElementDescriptor(descriptor);
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    final JSImplicitElement declaration = getDeclaration();
    final String string = declaration.getTypeString();
    final String attributes = string.split(";", -1)[3];
    final String[] split = attributes.split(",");
    final XmlAttributeDescriptor[] result;
    if (context != null && AngularIndexUtil.hasAngularJS2(context.getProject())) {
      result = AngularAttributeDescriptor.getFieldBasedDescriptors(declaration);
    } else if (split.length == 1 && split[0].isEmpty()) {
      result = XmlAttributeDescriptor.EMPTY;
    } else {
      result = new XmlAttributeDescriptor[split.length];
      for (int i = 0; i < split.length; i++) {
        result[i] = new AnyXmlAttributeDescriptor(DirectiveUtil.getAttributeName(split[i]));
      }
    }
    final XmlAttributeDescriptor[] commonAttributes = HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context);
    return ArrayUtil.mergeArrays(result, commonAttributes);
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), attribute.getParent());
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls final String attributeName, @Nullable XmlTag context) {
    final XmlAttributeDescriptor descriptor = ContainerUtil.find(getAttributesDescriptors(context),
                                                                 descriptor1 -> attributeName.equals(descriptor1.getName()));
    if (descriptor != null) return descriptor;
    return context != null ? AngularJSAttributeDescriptorsProvider.getAngular2Descriptor(attributeName, context.getProject()) : null;
  }

  @Override
  public XmlNSDescriptor getNSDescriptor() {
    return null;
  }

  @Nullable
  @Override
  public XmlElementsGroup getTopGroup() {
    return null;
  }

  @Override
  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  @Nullable
  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public JSImplicitElement getDeclaration() {
    return myDeclaration;
  }

  @Override
  public String getName(PsiElement context) {
    return getName();
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public void init(PsiElement element) {
  }

  @NotNull
  @Override
  public Object[] getDependences() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }
}
