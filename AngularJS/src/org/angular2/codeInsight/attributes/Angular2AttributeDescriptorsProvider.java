// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.xml.XmlElementDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.Angular2HtmlVariable;
import org.angular2.lang.selector.Angular2DirectiveSelector;
import org.angular2.lang.selector.Angular2SelectorMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.angular2.codeInsight.Angular2Processor.NG_TEMPLATE;
import static org.angular2.entities.Angular2EntitiesProvider.findAttributeDirectivesCandidates;
import static org.angular2.entities.Angular2EntitiesProvider.findElementDirectivesCandidates;

public class Angular2AttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {

  public static XmlAttributeDescriptor getAttributeDescriptor(@Nullable final String attrName, @Nullable XmlTag xmlTag,
                                                              @NotNull Function<XmlTag, XmlAttributeDescriptor[]> attrDescrProvider) {
    if (attrName == null) {
      return null;
    }
    if (xmlTag != null) {
      for (XmlAttributeDescriptor d : attrDescrProvider.apply(xmlTag)) {
        if (attrName.equals(d.getName())) {
          return d;
        }
      }
    }
    return Angular2AttributeDescriptor.create(attrName);
  }

  public static List<String> getCustomNgAttrs() {
    return CUSTOM_NG_ATTRS;
  }

  private static final List<String> CUSTOM_NG_ATTRS = singletonList("i18n");
  private static final String TEMPLATE_ATTR_PREFIX = "*";

  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(@Nullable XmlTag xmlTag) {
    if (xmlTag == null) {
      return XmlAttributeDescriptor.EMPTY;
    }
    if (!Angular2LangUtil.isAngular2Context(xmlTag)) {
      return XmlAttributeDescriptor.EMPTY;
    }

    final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<>();
    Consumer<XmlAttributeDescriptor> addDescriptor =
      attr -> result.putIfAbsent(attr.getName(), attr);

    getApplicableDirectiveDescriptors(xmlTag).forEach(addDescriptor);
    getStandardPropertyAndEventDescriptors(xmlTag).forEach(addDescriptor);
    getExistingVarsAndRefsDescriptors(xmlTag).forEach(addDescriptor);

    for (String CUSTOM_NG_ATTR : CUSTOM_NG_ATTRS) {
      addDescriptor.accept(Angular2AttributeDescriptor.create(CUSTOM_NG_ATTR));
    }

    return result.values().toArray(XmlAttributeDescriptor.EMPTY);
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(@Nullable final String attrName, @Nullable XmlTag xmlTag) {
    return getAttributeDescriptor(attrName, xmlTag, this::getAttributeDescriptors);
  }

  @NotNull
  public static Collection<XmlAttributeDescriptor> getApplicableDirectiveDescriptors(@NotNull XmlTag xmlTag) {
    Set<Angular2Directive> directives = new HashSet<>();
    directives.addAll(findElementDirectivesCandidates(xmlTag.getProject(), xmlTag.getName()));
    directives.addAll(findElementDirectivesCandidates(xmlTag.getProject(), ""));
    boolean isTemplateTag = xmlTag.getName().equals(NG_TEMPLATE);
    for (XmlAttribute attribute : xmlTag.getAttributes()) {
      if (!isTemplateTag && attribute.getName().startsWith(TEMPLATE_ATTR_PREFIX)) {
        for (Angular2Directive d : findAttributeDirectivesCandidates(xmlTag.getProject(), attribute.getName().substring(1))) {
          if (d.isTemplate()) {
            directives.add(d);
          }
        }
      }
      else {
        for (Angular2Directive d : findAttributeDirectivesCandidates(xmlTag.getProject(), attribute.getName())) {
          if (isTemplateTag || !d.isTemplate()) {
            directives.add(d);
          }
        }
      }
    }
    Angular2SelectorMatcher<Angular2Directive> matcher = new Angular2SelectorMatcher<>();
    directives.forEach(d -> matcher.addSelectables(d.getDirectiveSelectors(), d));

    directives.clear();
    Angular2DirectiveSelector tagInfo = Angular2DirectiveSelector.createElementCssSelector(xmlTag);
    matcher.match(tagInfo, (selector, directive) -> directives.add(directive));

    List<XmlAttributeDescriptor> result = new ArrayList<>();
    for (Angular2Directive matchedDirective : directives) {
      result.addAll(Angular2AttributeDescriptor.getDirectiveDescriptors(matchedDirective));
    }
    return result;
  }

  @NotNull
  public static Collection<XmlAttributeDescriptor> getStandardPropertyAndEventDescriptors(@NotNull XmlTag xmlTag) {
    final XmlElementDescriptor descriptor = xmlTag.getDescriptor();
    if (!(descriptor instanceof HtmlElementDescriptorImpl)) {
      return Collections.emptyList();
    }
    List<XmlAttributeDescriptor> result = new ArrayList<>();
    final XmlAttributeDescriptor[] descriptors = ((HtmlElementDescriptorImpl)descriptor).getDefaultAttributeDescriptors(xmlTag);
    for (XmlAttributeDescriptor attributeDescriptor : descriptors) {
      final String name = attributeDescriptor.getName();
      //TODO - not all of the standard attributes are standard properties, mapping required for some
      if (name.startsWith("on")) {
        result.add(Angular2AttributeDescriptor.create("(" + name.substring(2) + ")", attributeDescriptor.getDeclaration()));
      }
      else {
        result.add(Angular2AttributeDescriptor.create("[" + name + "]", attributeDescriptor.getDeclaration()));
      }
    }
    return result;
  }

  @NotNull
  public static List<XmlAttributeDescriptor> getExistingVarsAndRefsDescriptors(@NotNull XmlTag xmlTag) {
    List<XmlAttributeDescriptor> result = new ArrayList<>();
    xmlTag.acceptChildren(new Angular2HtmlElementVisitor() {
      @Override
      public void visitVariable(Angular2HtmlVariable variable) {
        result
          .add(new Angular2AttributeDescriptor(variable.getName(), variable.getVariableName(), singletonList(variable.getNameElement())));
      }

      @Override
      public void visitReference(Angular2HtmlReference reference) {
        result.add(
          new Angular2AttributeDescriptor(reference.getName(), reference.getReferenceName(), singletonList(reference.getNameElement())));
      }
    });
    return result;
  }
}
