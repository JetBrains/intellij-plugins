// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.xml.XmlElementDescriptor;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.Angular2HtmlVariable;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.angular2.lang.selector.Angular2SelectorMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.singletonList;
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

  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(@Nullable XmlTag xmlTag) {
    if (xmlTag == null
        || DumbService.isDumb(xmlTag.getProject())
        || !Angular2LangUtil.isAngular2Context(xmlTag)) {
      return XmlAttributeDescriptor.EMPTY;
    }

    final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<>();
    Consumer<XmlAttributeDescriptor> addDescriptor =
      attr -> result.putIfAbsent(attr.getName(), attr);

    getDirectiveDescriptors(xmlTag).forEach(addDescriptor);
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
  public static Collection<XmlAttributeDescriptor> getDirectiveDescriptors(@NotNull XmlTag xmlTag) {
    Set<Angular2Directive> directiveCandidates = new HashSet<>();
    directiveCandidates.addAll(findElementDirectivesCandidates(xmlTag.getProject(), xmlTag.getName()));
    directiveCandidates.addAll(findElementDirectivesCandidates(xmlTag.getProject(), ""));

    Angular2SelectorMatcher<Angular2Directive> matcher = new Angular2SelectorMatcher<>();
    directiveCandidates.forEach(d -> matcher.addSelectables(d.getSelector().getSimpleSelectors(), d));

    boolean isTemplateTag = Angular2Processor.isTemplateTag(xmlTag.getName());
    Set<Angular2Directive> matchedDirectives = new HashSet<>();
    Angular2DirectiveSimpleSelector tagInfo = Angular2DirectiveSimpleSelector.createElementCssSelector(xmlTag);
    matcher.match(tagInfo, (selector, directive) -> {
      if (!directive.isTemplate() || isTemplateTag) {
        matchedDirectives.add(directive);
      }
    });

    List<XmlAttributeDescriptor> result = new ArrayList<>();
    for (Angular2Directive matchedDirective : matchedDirectives) {
      result.addAll(Angular2AttributeDescriptor.getDirectiveDescriptors(matchedDirective, isTemplateTag));
    }

    Set<String> knownAttributes = new HashSet<>();
    for (XmlAttributeDescriptor attr : result) {
      knownAttributes.add(attr.getName());
    }
    getStandardPropertyAndEventDescriptors(xmlTag).forEach(
      attr -> knownAttributes.add(attr.getName()));

    Map<String, List<PsiElement>> attrsFromSelectors = new HashMap<>();
    Set<String> inputs = new HashSet<>();
    Set<String> outputs = new HashSet<>();
    Set<String> inOuts = new HashSet<>();
    for (Angular2Directive candidate : directiveCandidates) {
      inputs.clear();
      candidate.getInputs().forEach(in -> inputs.add(in.getName()));

      BiConsumer<String, PsiElement> addAttribute = (attrName, element) -> {
        if (!knownAttributes.contains(attrName)) {
          attrsFromSelectors.computeIfAbsent(attrName, k -> new SmartList<>()).add(element);
        }
      };
      if (!isTemplateTag && candidate.isTemplate()) {
        List<SimpleSelectorWithPsi> selectors = candidate.getSelector().getSimpleSelectorsWithPsi();
        if (selectors.size() == 1) {
          List<Angular2DirectiveSelectorPsiElement> attributeCandidates = selectors.get(0).getAttributes();
          if (attributeCandidates.size() == 1) {
            addAttribute.accept("*" + attributeCandidates.get(0).getName(), attributeCandidates.get(0));
          }
          else {
            CANDIDATES_LOOP:
            for (Angular2DirectiveSelectorPsiElement attr : attributeCandidates) {
              String attrName = attr.getName();
              for (String input : inputs) {
                if (!input.startsWith(attrName)) {
                  break CANDIDATES_LOOP;
                }
              }
              addAttribute.accept("*" + attrName, attr);
            }
          }
        }
      }
      else {
        outputs.clear();
        candidate.getOutputs().forEach(out -> outputs.add(out.getName()));
        inOuts.clear();
        candidate.getInOuts().forEach(inOut -> inOuts.add(inOut.first.getName()));
        for (SimpleSelectorWithPsi selector : candidate.getSelector().getSimpleSelectorsWithPsi()) {
          for (Angular2DirectiveSelectorPsiElement attr : selector.getAttributes()) {
            String attrName = attr.getName();
            addAttribute.accept(attrName, attr);
            if (inOuts.contains(attrName)) {
              addAttribute.accept("[(" + attrName + ")]", attr);
            }
            if (inputs.contains(attrName)) {
              addAttribute.accept("[" + attrName + "]", attr);
            }
            if (outputs.contains(attrName)) {
              addAttribute.accept("(" + attrName + ")", attr);
            }
          }
          for (SimpleSelectorWithPsi notSelector : selector.getNotSelectors()) {
            for (Angular2DirectiveSelectorPsiElement attr : notSelector.getAttributes()) {
              addAttribute.accept(attr.getName(), attr);
            }
          }
        }
      }
    }
    attrsFromSelectors.forEach((k, v) -> {
      Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(k, isTemplateTag);
      result.add(new Angular2AttributeDescriptor(
        k, info.elementType != XmlElementType.XML_ATTRIBUTE ? info.name : null, v));
    });
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
