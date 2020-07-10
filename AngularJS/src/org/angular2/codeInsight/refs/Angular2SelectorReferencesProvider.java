// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.xml.Html5SchemaProvider;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptorEx;
import com.intellij.xml.util.XmlUtil;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2SelectorReferencesProvider extends PsiReferenceProvider {

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    Angular2DirectiveSelector directiveSelector;
    if (element instanceof Angular2HtmlNgContentSelector) {
      directiveSelector = ((Angular2HtmlNgContentSelector)element).getSelector();
    }
    else {
      directiveSelector = doIfNotNull(Angular2EntitiesProvider.getDirective(PsiTreeUtil.getParentOfType(element, ES6Decorator.class)),
                                      dir -> dir.getSelector());
    }
    if (directiveSelector == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    List<PsiReference> result = new SmartList<>();
    for (SimpleSelectorWithPsi selector : directiveSelector.getSimpleSelectorsWithPsi()) {
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

  private static @Nullable XmlNSDescriptorEx getNamespaceDescriptor(@NotNull PsiFile baseFile) {
    return CachedValuesManager.getCachedValue(baseFile, () -> {
      String htmlNS = ExternalResourceManagerEx.getInstanceEx().getDefaultHtmlDoctype(baseFile.getProject());
      if (htmlNS.isEmpty()) {
        htmlNS = Html5SchemaProvider.getHtml5SchemaLocation();
      }
      XmlFile xmlFile = XmlUtil.findNamespace(baseFile, htmlNS);
      if (xmlFile != null) {
        final XmlDocument document = AstLoadingFilter.forceAllowTreeLoading(xmlFile, xmlFile::getDocument);
        if (document != null && document.getMetaData() instanceof XmlNSDescriptorEx) {
          return CachedValueProvider.Result.create((XmlNSDescriptorEx)document.getMetaData(), xmlFile);
        }
      }
      return CachedValueProvider.Result.create(null, baseFile);
    });
  }

  public static @Nullable XmlElementDescriptor getElementDescriptor(@NotNull String name, @NotNull PsiFile baseFile) {
    XmlNSDescriptorEx descriptorEx = getNamespaceDescriptor(baseFile);
    return descriptorEx != null ? descriptorEx.getElementDescriptor(name, XmlUtil.XHTML_URI) : null;
  }

  private static final class HtmlElementReference extends PsiReferenceBase<PsiElement> {

    private final Angular2DirectiveSelectorPsiElement mySelectorPsiElement;

    private HtmlElementReference(@NotNull Angular2DirectiveSelectorPsiElement element) {
      super(element.getParent(), element.getTextRangeInParent(), true);
      mySelectorPsiElement = element;
    }

    @Override
    public @Nullable PsiElement resolve() {
      XmlElementDescriptor descriptor = getElementDescriptor(mySelectorPsiElement.getName(), getElement().getContainingFile());
      return descriptor != null ? descriptor.getDeclaration() : null;
    }
  }

  private static final class HtmlAttributeReference extends PsiPolyVariantReferenceBase<PsiElement> {

    private final String myElementName;

    private HtmlAttributeReference(@NotNull Angular2DirectiveSelectorPsiElement element, @Nullable String elementName) {
      super(element.getParent(), element.getTextRangeInParent(), true);
      myElementName = elementName;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
      XmlElementDescriptor descriptor =
        myElementName != null ? getElementDescriptor(myElementName, getElement().getContainingFile()) : null;
      if (descriptor == null) {
        descriptor = getElementDescriptor("div", getElement().getContainingFile());
      }
      XmlAttributeDescriptor attributeDescriptor = descriptor != null ? descriptor.getAttributeDescriptor(getValue(), null) : null;
      return attributeDescriptor != null
             ? PsiElementResolveResult.createResults(attributeDescriptor.getDeclarations())
             : ResolveResult.EMPTY_ARRAY;
    }
  }
}
