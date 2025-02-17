// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import com.jetbrains.plugins.jade.psi.impl.JadeTagImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class JadeTagDescriptorProvider implements XmlElementDescriptorProvider, XmlTagNameProvider {
  public static final String BLOCK = "block";

  @Override
  public @Nullable XmlElementDescriptor getDescriptor(XmlTag tag) {
    if (tag instanceof JadeTagImpl && BLOCK.equals(tag.getName())) {
      XmlNSDescriptor descriptor = tag.getNSDescriptor(tag.getNamespace(), false);
      if (descriptor != null) {
        for (XmlElementDescriptor desc : descriptor.getRootElementsDescriptors(PsiTreeUtil.getParentOfType(tag, XmlDocument.class))) {
          if ("html".equals(desc.getName())) {
            return desc;
          }
        }
      }
    }
    return null;
  }

  @Override
  public void addTagNameVariants(List<LookupElement> elements, @NotNull XmlTag tag, String prefix) {
    if (tag instanceof JadeTagImpl && tag.getParent() instanceof XmlDocument) {
      elements.add(LookupElementBuilder.create("block"));
    }
  }
}
