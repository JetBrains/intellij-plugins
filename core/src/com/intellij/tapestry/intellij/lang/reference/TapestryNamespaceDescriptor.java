package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.java.PsiEmptyExpressionImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.schema.XmlElementDescriptorImpl;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import org.jetbrains.annotations.NotNull;

public class TapestryNamespaceDescriptor extends XmlNSDescriptorImpl {

  private static final PsiElement EMPTY_PSI_ELEMENT = new PsiEmptyExpressionImpl() {
    public PsiFile getContainingFile() {
      return null;
    }
  };

  public XmlElementDescriptor getElementDescriptor(@NotNull XmlTag tag) {
    return new TapestryElementDescriptor(tag);
  }

  public boolean isHierarhyEnabled() {
    return true;
  }

  private class TapestryElementDescriptor extends XmlElementDescriptorImpl {

    private final XmlTag _tag;

    public TapestryElementDescriptor(@NotNull XmlTag tag) {
      super(tag);
      _tag = tag;
    }

    @Override
    public PsiElement getDeclaration() {
      Component component = TapestryUtils.getComponentFromTag(_tag);
      if (component == null) return EMPTY_PSI_ELEMENT;

      final IJavaClassType classType = component.getElementClass();
      return classType == null ? EMPTY_PSI_ELEMENT : ((IntellijJavaClassType)classType).getPsiClass();
    }

    @Override
    public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
      final XmlNSDescriptor descriptor = childTag.getNSDescriptor(childTag.getNamespace(), true);
      return descriptor == null ? null : descriptor.getElementDescriptor(childTag);
    }

    @Override
    public String getName(PsiElement psiElement) {
      return _tag.getName();
    }

    @Override
    public String getName() {
      return _tag.getName();
    }
  }
}
