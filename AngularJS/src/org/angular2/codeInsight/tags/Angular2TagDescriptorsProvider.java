// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.tags;

import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import com.intellij.xml.util.XmlUtil;
import icons.AngularJSIcons;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.angular2.lang.selector.Angular2SelectorMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Angular2TagDescriptorsProvider implements XmlElementDescriptorProvider, XmlTagNameProvider {
  private static final String NG_CONTAINER = "ng-container";
  private static final String NG_CONTENT = "ng-content";
  private static final String NG_TEMPLATE = "ng-template";

  @Override
  public void addTagNameVariants(@NotNull final List<LookupElement> elements, @NotNull XmlTag xmlTag, String prefix) {
    if (!(xmlTag instanceof HtmlTag && Angular2LangUtil.isAngular2Context(xmlTag))) {
      return;
    }
    final Project project = xmlTag.getProject();
    Language language = xmlTag.getContainingFile().getLanguage();
    Set<String> names = new HashSet<>();
    for (LookupElement el : elements) {
      names.add(el.getLookupString());
    }
    Angular2EntitiesProvider.getAllElementDirectives(project).forEach((name, list) -> {
      if (!names.contains(name) && !list.isEmpty()) {
        Angular2DirectiveSelectorPsiElement el = list.get(0).getSelector().getPsiElementForElement(name);
        addLookupItem(language, elements, el, name);
      }
    });
    addLookupItem(language, elements, NG_CONTAINER);
    addLookupItem(language, elements, NG_CONTENT);
    addLookupItem(language, elements, NG_TEMPLATE);
  }

  private static void addLookupItem(@NotNull Language language, @NotNull List<LookupElement> elements, @NotNull String name) {
    addLookupItem(language, elements, name, name);
  }

  private static void addLookupItem(@NotNull Language language,
                                    @NotNull List<LookupElement> elements,
                                    @NotNull Object component,
                                    @NotNull String name) {
    LookupElementBuilder element = LookupElementBuilder.create(component, name)
      .withIcon(AngularJSIcons.Angular2);
    if (language.isKindOf(XMLLanguage.INSTANCE)) {
      element = element.withInsertHandler(XmlTagInsertHandler.INSTANCE);
    }
    elements.add(element);
  }

  @Nullable
  @Override
  public XmlElementDescriptor getDescriptor(@NotNull XmlTag xmlTag) {
    final Project project = xmlTag.getProject();
    if (!(xmlTag instanceof HtmlTag && Angular2LangUtil.isAngular2Context(xmlTag))) {
      return null;
    }
    String tagName = xmlTag.getName();
    if (XmlUtil.isTagDefinedByNamespace(xmlTag)) return null;
    tagName = XmlUtil.findLocalNameByQualifiedName(tagName);
    if (NG_CONTAINER.equalsIgnoreCase(tagName) || NG_CONTENT.equalsIgnoreCase(tagName) || NG_TEMPLATE.equalsIgnoreCase(tagName)) {
      return new Angular2TagDescriptor(tagName, createDirective(xmlTag, tagName));
    }

    List<Angular2Directive> directiveCandidates = Angular2EntitiesProvider.findElementDirectivesCandidates(project, tagName);
    if (directiveCandidates.isEmpty()) {
      return null;
    }
    List<Angular2Directive> matchedDirectives = matchDirectives(xmlTag, directiveCandidates);
    return new Angular2TagDescriptor(tagName, (matchedDirectives.isEmpty() ? directiveCandidates : matchedDirectives).get(0).getSelector()
      .getPsiElementForElement(tagName));
  }

  @NotNull
  public static List<Angular2Directive> matchDirectives(@NotNull XmlTag xmlTag,
                                                        @NotNull Collection<Angular2Directive> directiveCandidates) {
    Angular2SelectorMatcher<Angular2Directive> matcher = new Angular2SelectorMatcher<>();
    directiveCandidates.forEach(d -> matcher.addSelectables(d.getSelector().getSimpleSelectors(), d));

    boolean isTemplateTag = Angular2Processor.isTemplateTag(xmlTag.getName());
    List<Angular2Directive> matchedDirectives = new ArrayList<>();
    Angular2DirectiveSimpleSelector tagInfo = Angular2DirectiveSimpleSelector.createElementCssSelector(xmlTag);
    matcher.match(tagInfo, (selector, directive) -> {
      if (!directive.isTemplate() || isTemplateTag) {
        matchedDirectives.add(directive);
      }
    });
    return matchedDirectives;
  }

  @NotNull
  private static JSImplicitElementImpl createDirective(@NotNull XmlTag xmlTag, @NotNull String name) {
    return new JSImplicitElementImpl.Builder(name, xmlTag).setTypeString("E;;;").toImplicitElement();
  }
}
