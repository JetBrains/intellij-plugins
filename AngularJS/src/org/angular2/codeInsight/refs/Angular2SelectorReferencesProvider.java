// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.xml.Html5SchemaProvider;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptorEx;
import com.intellij.xml.util.XmlUtil;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2SelectorReferencesProvider extends PsiReferenceProvider {

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    Angular2Directive directive = Angular2EntitiesProvider.getDirective(PsiTreeUtil.getParentOfType(element, ES6Decorator.class));
    if (directive == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    List<PsiReference> result = new SmartList<>();
    for (SimpleSelectorWithPsi selector : directive.getSelector().getSimpleSelectorsWithPsi()) {
      String elementName = null;
      if (selector.getElement() != null && selector.getElement().getParent() == element) {
        result.add(new HtmlElementReference(selector.getElement()));
        elementName = selector.getElement().getName();
      }
      for (Angular2DirectiveSelectorPsiElement attr : selector.getAttributes()) {
        if (attr.getParent() == element) {
          result.add(new HtmlAttributeReference(attr, elementName));
        }
      }
      for (SimpleSelectorWithPsi notSelector : selector.getNotSelectors()) {
        for (Angular2DirectiveSelectorPsiElement attr : notSelector.getAttributes()) {
          if (attr.getParent() == element) {
            result.add(new HtmlAttributeReference(attr, elementName));
          }
        }
      }
    }
    return result.toArray(PsiReference.EMPTY_ARRAY);
  }

  @Nullable
  private static XmlNSDescriptorEx getNamespaceDescriptor(@NotNull PsiFile baseFile) {
    return CachedValuesManager.getCachedValue(baseFile, () -> {
      String htmlNS = ExternalResourceManagerEx.getInstanceEx().getDefaultHtmlDoctype(baseFile.getProject());
      if (htmlNS.isEmpty()) {
        htmlNS = Html5SchemaProvider.getHtml5SchemaLocation();
      }
      XmlFile xmlFile = XmlUtil.findNamespace(baseFile, htmlNS);
      if (xmlFile != null) {
        final XmlDocument document = xmlFile.getDocument();
        if (document != null && document.getMetaData() instanceof XmlNSDescriptorEx) {
          return CachedValueProvider.Result.create((XmlNSDescriptorEx)document.getMetaData(), xmlFile);
        }
      }
      return CachedValueProvider.Result.create(null, baseFile);
    });
  }

  @Nullable
  public static XmlElementDescriptor getElementDescriptor(@NotNull String name, @NotNull PsiFile baseFile) {
    XmlNSDescriptorEx descriptorEx = getNamespaceDescriptor(baseFile);
    return descriptorEx != null ? descriptorEx.getElementDescriptor(name, "http://www.w3.org/1999/xhtml") : null;
  }

  private static class HtmlElementReference extends PsiReferenceBase<PsiElement> {

    private final Angular2DirectiveSelectorPsiElement mySelectorPsiElement;

    private HtmlElementReference(@NotNull Angular2DirectiveSelectorPsiElement element) {
      super(element.getParent(), element.getTextRangeInParent(), true);
      mySelectorPsiElement = element;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      XmlElementDescriptor descriptor = getElementDescriptor(mySelectorPsiElement.getName(), getElement().getContainingFile());
      return descriptor != null ? descriptor.getDeclaration() : mySelectorPsiElement;
    }
  }

  private static class HtmlAttributeReference extends PsiReferenceBase<PsiElement> {

    private final Angular2DirectiveSelectorPsiElement mySelectorPsiElement;
    private final String myElementName;

    private HtmlAttributeReference(@NotNull Angular2DirectiveSelectorPsiElement element, @Nullable String elementName) {
      super(element.getParent(), element.getTextRangeInParent(), true);
      mySelectorPsiElement = element;
      myElementName = elementName;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      XmlElementDescriptor descriptor =
        myElementName != null ? getElementDescriptor(myElementName, getElement().getContainingFile()) : null;
      if (descriptor == null) {
        descriptor = getElementDescriptor("div", getElement().getContainingFile());
      }
      XmlAttributeDescriptor attributeDescriptor = descriptor != null ? descriptor.getAttributeDescriptor(getValue(), null) : null;
      return attributeDescriptor != null ? attributeDescriptor.getDeclaration() : mySelectorPsiElement;
    }
  }
}
