package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.Mixin;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexey Chmutov
 */
public class TapestryTagDescriptor extends BasicTapestryTagDescriptor {
  private final PresentationLibraryElement myComponent;
  private final List<Mixin> myMixins;

  public TapestryTagDescriptor(@NotNull PresentationLibraryElement component,
                               @Nullable String prefix,
                               TapestryNamespaceDescriptor descriptor) {
    this(component, Collections.emptyList(), prefix, descriptor);
  }

  public TapestryTagDescriptor(@NotNull PresentationLibraryElement component,
                               List<Mixin> mixins,
                               @Nullable String namespacePrefix,
                               TapestryNamespaceDescriptor descriptor) {
    super(namespacePrefix, descriptor);
    myComponent = component;
    myMixins = mixins;
  }

  @Override
  public String getDefaultName() {
    TapestryLibrary library = myComponent.getLibrary();
    String name = StringUtil.toLowerCase(myComponent.getName()).replace('/', '.');
    if (library != null && library.getShortName() != null) {
      name = library.getShortName() + '.' + name;
    }
    return getPrefixWithColon() + name;
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return context != null
           ? DescriptorUtil.getAttributeDescriptors(context)
           : getAttributeDescriptors();
  }

  private XmlAttributeDescriptor[] getAttributeDescriptors() {
    final List<XmlAttributeDescriptor> result = new ArrayList<>();
    ContainerUtil.addAll(result, DescriptorUtil.getAttributeDescriptors((TapestryComponent)myComponent, null));
    for (Mixin mixin : myMixins) {
      ContainerUtil.addAll(result, DescriptorUtil.getAttributeDescriptors(mixin, null));
    }
    return result.toArray(XmlAttributeDescriptor.EMPTY);
  }

  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return context != null
           ? DescriptorUtil.getAttributeDescriptor(attributeName, context)
           : DescriptorUtil.getAttributeDescriptor(attributeName, (TapestryComponent)myComponent, myMixins);
  }

  @Override
  public PsiElement getDeclaration() {
    return ((IntellijJavaClassType)myComponent.getElementClass()).getPsiClass();
  }
}
