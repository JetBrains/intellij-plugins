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
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2TagDescriptorsProvider implements XmlElementDescriptorProvider, XmlTagNameProvider {
  private static final String NG_CONTAINER = "ng-container";
  private static final String NG_CONTENT = "ng-content";
  private static final String NG_TEMPLATE = "ng-template";

  @Override
  public void addTagNameVariants(final List<LookupElement> elements, @NotNull XmlTag xmlTag, String prefix) {
    if (!(xmlTag instanceof HtmlTag && Angular2LangUtil.isAngular2Context(xmlTag))) {
      return;
    }
    final Project project = xmlTag.getProject();
    Language language = xmlTag.getContainingFile().getLanguage();
    Angular2EntitiesProvider.processDirectives(project, directive -> {
      if (directive.isComponent()) {
        addLookupItem(language, elements, directive, directive.getSelector());
      }
      return true;
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
  public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
    final Project project = xmlTag.getProject();
    if (!(xmlTag instanceof HtmlTag && Angular2LangUtil.isAngular2Context(xmlTag))) {
      return null;
    }
    final String tagName = xmlTag.getName();
    if (XmlUtil.isTagDefinedByNamespace(xmlTag)) return null;
    if (NG_CONTAINER.equalsIgnoreCase(tagName) || NG_CONTENT.equalsIgnoreCase(tagName) || NG_TEMPLATE.equalsIgnoreCase(tagName)) {
      return new Angular2TagDescriptor(createDirective(xmlTag, tagName));
    }
    for (Angular2Directive directive : Angular2EntitiesProvider.findElementDirectivesCandidates(project, tagName)) {
      if (directive.isComponent()) {
        return new Angular2TagDescriptor((Angular2Component)directive);
      }
    }
    return null;
  }

  @NotNull
  private static JSImplicitElementImpl createDirective(XmlTag xmlTag, String name) {
    return new JSImplicitElementImpl.Builder(name, xmlTag).setTypeString("E;;;").toImplicitElement();
  }
}
