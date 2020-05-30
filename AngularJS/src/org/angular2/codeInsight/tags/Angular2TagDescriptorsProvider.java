// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.tags;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.xml.util.XmlUtil;
import icons.AngularJSIcons;
import org.angular2.codeInsight.Angular2CodeInsightUtils;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Angular2TagDescriptorsProvider implements XmlElementDescriptorProvider, XmlTagNameProvider {

  @NonNls public static final String NG_CONTAINER = "ng-container";
  @NonNls public static final String NG_CONTENT = "ng-content";
  @NonNls public static final String NG_TEMPLATE = "ng-template";

  public static final Set<String> NG_SPECIAL_TAGS = ContainerUtil.newHashSet(NG_CONTAINER, NG_CONTENT, NG_TEMPLATE);

  @Override
  public void addTagNameVariants(final @NotNull List<LookupElement> elements, @NotNull XmlTag xmlTag, String prefix) {
    if (!(xmlTag instanceof HtmlTag)
        || DumbService.isDumb(xmlTag.getProject())
        || !Angular2LangUtil.isAngular2Context(xmlTag)
        || HtmlUtil.SVG_NAMESPACE.equals(xmlTag.getNamespace())) {
      return;
    }
    final Project project = xmlTag.getProject();
    Language language = xmlTag.getContainingFile().getLanguage();
    Set<String> names = new HashSet<>();
    for (LookupElement el : elements) {
      names.add(el.getLookupString());
    }
    for (String name : NG_SPECIAL_TAGS) {
      if (names.add(name)) {
        addLookupItem(language, elements, name);
      }
    }
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(xmlTag);
    Angular2EntitiesProvider.getAllElementDirectives(project).forEach((name, list) -> {
      if (!list.isEmpty() && !name.isEmpty() && names.add(name)) {
        Angular2DirectiveSelectorPsiElement el = list.get(0).getSelector().getPsiElementForElement(name);
        addLookupItem(language, elements, el, name, list, scope);
      }
    });

    Angular2CodeInsightUtils.getAvailableNgContentSelectorsStream(xmlTag, scope)
      .map(SimpleSelectorWithPsi::getElement)
      .nonNull()
      .forEach(element -> {
        if (names.add(element.getName())) {
          addNgContentSelectorBasedLookupItem(language, elements, element);
        }
      });
  }

  private static void addLookupItem(@NotNull Language language, @NotNull List<? super LookupElement> elements, @NotNull String name) {
    addLookupItem(language, elements, name, name, null, null);
  }

  private static void addNgContentSelectorBasedLookupItem(@NotNull Language language,
                                                          @NotNull List<LookupElement> elements,
                                                          @NotNull Angular2DirectiveSelectorPsiElement selectorElement) {
    LookupElementBuilder element = LookupElementBuilder.create(selectorElement, selectorElement.getName())
      .withIcon(AngularJSIcons.Angular2)
      .withBoldness(true);
    if (language.isKindOf(XMLLanguage.INSTANCE)) {
      element = element.withInsertHandler(XmlTagInsertHandler.INSTANCE);
    }
    elements.add(PrioritizedLookupElement.withPriority(element, 2));
  }

  private static void addLookupItem(@NotNull Language language,
                                    @NotNull List<? super LookupElement> elements,
                                    @NotNull Object component,
                                    @NotNull String name,
                                    @Nullable List<Angular2Directive> directives,
                                    @Nullable Angular2DeclarationsScope scope) {
    DeclarationProximity proximity = scope != null && directives != null
                                     ? scope.getDeclarationsProximity(directives)
                                     : DeclarationProximity.IN_SCOPE;
    if (proximity == DeclarationProximity.NOT_REACHABLE) {
      return;
    }
    LookupElementBuilder element = LookupElementBuilder.create(component, name)
      .withIcon(AngularJSIcons.Angular2);
    if (language.isKindOf(XMLLanguage.INSTANCE)) {
      element = element.withInsertHandler(XmlTagInsertHandler.INSTANCE);
    }
    if (proximity != DeclarationProximity.IN_SCOPE) {
      element = Angular2CodeInsightUtils.wrapWithImportDeclarationModuleHandler(
        Angular2CodeInsightUtils.decorateLookupElementWithModuleSource(element, directives, proximity, scope),
        XmlTag.class);
    }
    elements.add(PrioritizedLookupElement.withPriority(
      element, proximity == DeclarationProximity.IN_SCOPE
               || proximity == DeclarationProximity.EXPORTED_BY_PUBLIC_MODULE ? 1 : 0));
  }

  @Override
  public @Nullable XmlElementDescriptor getDescriptor(@NotNull XmlTag xmlTag) {
    if (!(xmlTag instanceof HtmlTag)
        || DumbService.isDumb(xmlTag.getProject())
        || !Angular2LangUtil.isAngular2Context(xmlTag)) {
      return null;
    }
    if (XmlUtil.isTagDefinedByNamespace(xmlTag)) {
      return getWrappedDescriptorFromNamespace(xmlTag);
    }
    String tagName = StringUtil.toLowerCase(xmlTag.getLocalName());
    if (NG_CONTENT.equals(tagName)) {
      return new Angular2NgContentDescriptor(xmlTag);
    }
    if (NG_SPECIAL_TAGS.contains(tagName)) {
      return new Angular2TagDescriptor(xmlTag);
    }

    Angular2ApplicableDirectivesProvider provider = new Angular2ApplicableDirectivesProvider(xmlTag, true);
    Collection<?> sources = provider.getMatched();
    boolean implied = false;
    if (sources.isEmpty()) {
      sources = Angular2CodeInsightUtils.getAvailableNgContentSelectorsStream(xmlTag, new Angular2DeclarationsScope(xmlTag))
        .map(SimpleSelectorWithPsi::getElement)
        .nonNull()
        .filter(el -> StringUtil.toLowerCase(el.getName()).equals(tagName))
        .toList();
      implied = !sources.isEmpty();
    }
    if (sources.isEmpty()) {
      sources = provider.getCandidates();
    }
    return !sources.isEmpty() ? new Angular2TagDescriptor(xmlTag, implied, sources)
                              : null;
  }

  private static XmlElementDescriptor getWrappedDescriptorFromNamespace(@NotNull XmlTag xmlTag) {
    XmlElementDescriptor elementDescriptor = null;
    final XmlNSDescriptor nsDescriptor = xmlTag.getNSDescriptor(xmlTag.getNamespace(), false);

    if (nsDescriptor != null) {
      if (!DumbService.getInstance(xmlTag.getProject()).isDumb() || DumbService.isDumbAware(nsDescriptor)) {
        elementDescriptor = nsDescriptor.getElementDescriptor(xmlTag);
      }
    }
    if (elementDescriptor instanceof HtmlElementDescriptorImpl) {
      return new Angular2StandardTagDescriptor((HtmlElementDescriptorImpl)elementDescriptor);
    }
    return null;
  }
}
