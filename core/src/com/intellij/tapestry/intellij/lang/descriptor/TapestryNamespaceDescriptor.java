package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TapestryNamespaceDescriptor extends XmlNSDescriptorImpl {
  @Override
  public void init(PsiElement element) {
    throw new UnsupportedOperationException("Method init is not yet implemented in " + getClass().getName());
  }

  @Override
  public XmlElementDescriptor getElementDescriptor(@NotNull XmlTag tag) {
    return super.getElementDescriptor(tag);
  }

  @Override
  protected XmlElementDescriptor createElementDescriptor(XmlTag tag) {
    return super.createElementDescriptor(tag);
  }

  @Override
  public XmlElementDescriptor getElementDescriptor(String localName, String namespace) {
    return super.getElementDescriptor(localName, namespace);
  }

  @Override
  @NotNull
  public XmlElementDescriptor[] getRootElementsDescriptors(@Nullable final XmlDocument doc) {
    if (doc == null) return XmlElementDescriptor.EMPTY_ARRAY;
    Module module = ModuleUtil.findModuleForPsiElement(doc.getContainingFile());
    TapestryModuleSupportLoader tapestryModuleSupport = TapestryModuleSupportLoader.getInstance(module);
    if (tapestryModuleSupport == null) return XmlElementDescriptor.EMPTY_ARRAY;
    String namespacePrefix = doc.getRootTag().getPrefixByNamespace(TapestryConstants.TEMPLATE_NAMESPACE);
    return DescriptorUtil.getElementsDescriptors(tapestryModuleSupport.getComponents(), namespacePrefix);
  }
}
