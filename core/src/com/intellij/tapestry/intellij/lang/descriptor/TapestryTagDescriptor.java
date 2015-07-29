package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.model.presentation.Component;
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
 *         Date: Jun 10, 2009
 *         Time: 3:56:33 PM
 */
public class TapestryTagDescriptor extends BasicTapestryTagDescriptor {
  private final PresentationLibraryElement myComponent;
  private List<Mixin> myMixins;

  public TapestryTagDescriptor(@NotNull PresentationLibraryElement component,
                               @Nullable String prefix,
                               TapestryNamespaceDescriptor descriptor) {
    this(component, Collections.<Mixin>emptyList(), prefix, descriptor);
  }

  public TapestryTagDescriptor(@NotNull PresentationLibraryElement component,
                               List<Mixin> mixins,
                               @Nullable String namespacePrefix,
                               TapestryNamespaceDescriptor descriptor) {
    super(namespacePrefix, descriptor);
    myComponent = component;
    myMixins = mixins;
  }

  public String getDefaultName() {
    return getPrefixWithColon() + myComponent.getName().toLowerCase().replace('/', '.');
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return context != null
           ? DescriptorUtil.getAttributeDescriptors(context)
           : getAttributeDescriptors();
  }

  private XmlAttributeDescriptor[] getAttributeDescriptors() {
    final List<XmlAttributeDescriptor> result = new ArrayList<XmlAttributeDescriptor>();
    ContainerUtil.addAll(result, DescriptorUtil.getAttributeDescriptors((Component)myComponent, null));
    for (Mixin mixin : myMixins) {
      ContainerUtil.addAll(result, DescriptorUtil.getAttributeDescriptors(mixin, null));
    }
    return result.toArray(new XmlAttributeDescriptor[result.size()]);
  }

  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return context != null
           ? DescriptorUtil.getAttributeDescriptor(attributeName, context)
           : DescriptorUtil.getAttributeDescriptor(attributeName, (Component)myComponent, myMixins);
  }

  @Override
  public PsiElement getDeclaration() {
    return ((IntellijJavaClassType)myComponent.getElementClass()).getPsiClass();
  }
}
