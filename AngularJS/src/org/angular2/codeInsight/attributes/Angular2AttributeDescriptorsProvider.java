// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl;
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.codeInsight.tags.Angular2TagDescriptorsProvider;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.Angular2HtmlVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.angular2.entities.Angular2EntitiesProvider.findElementDirectivesCandidates;
import static org.angular2.lang.html.parser.Angular2AttributeType.*;

public class Angular2AttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {

  public static XmlAttributeDescriptor getAttributeDescriptor(@Nullable final String attrName, @Nullable XmlTag xmlTag,
                                                              @NotNull Function<XmlTag, XmlAttributeDescriptor[]> attrDescrProvider) {
    if (attrName == null || xmlTag == null || DumbService.isDumb(xmlTag.getProject())) {
      return null;
    }
    Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(attrName, true);
    for (XmlAttributeDescriptor d : attrDescrProvider.apply(xmlTag)) {
      if (d instanceof Angular2AttributeDescriptor) {
        if (attrName.equals(d.getName())
            || info.isEquivalent(((Angular2AttributeDescriptor)d).getInfo())) {
          return d;
        }
      }
      else if (attrName.equalsIgnoreCase(d.getName())) {
        return d;
      }
    }
    for (Angular2AttributesProvider provider :
      Angular2AttributesProvider.ANGULAR_ATTRIBUTES_PROVIDER_EP.getExtensionList()) {
      Angular2AttributeDescriptor descriptor = provider.getDescriptor(xmlTag, attrName, info);
      if (descriptor != null) {
        return descriptor;
      }
    }
    return null;
  }

  public static List<String> getCustomNgAttrs() {
    return CUSTOM_NG_ATTRS;
  }

  private static final List<String> CUSTOM_NG_ATTRS = singletonList("i18n");
  private static final Key<CachedValue<List<XmlAttributeDescriptor>>> STANDARD_PROPERTIES_KEY =
    new Key<>("angular.standard.properties");

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

  public static List<Angular2Directive> getApplicableDirectives(@NotNull XmlTag xmlTag) {
    return getApplicableDirectives(xmlTag, new HashSet<>());
  }

  private static List<Angular2Directive> getApplicableDirectives(@NotNull XmlTag xmlTag,
                                                                 @NotNull Set<Angular2Directive> directiveCandidates) {
    directiveCandidates.addAll(findElementDirectivesCandidates(xmlTag.getProject(), xmlTag.getName()));
    directiveCandidates.addAll(findElementDirectivesCandidates(xmlTag.getProject(), ""));
    return Angular2TagDescriptorsProvider.matchDirectives(xmlTag, directiveCandidates);
  }

  @NotNull
  public static Collection<XmlAttributeDescriptor> getDirectiveDescriptors(@NotNull XmlTag xmlTag) {
    Set<Angular2Directive> directiveCandidates = new HashSet<>();
    List<Angular2Directive> matchedDirectives = getApplicableDirectives(xmlTag, directiveCandidates);

    boolean isTemplateTag = Angular2Processor.isTemplateTag(xmlTag.getName());
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

    MultiMap<String, PsiElement> attrsFromSelectors = new MultiMap<>();
    BiConsumer<String, PsiElement> addAttribute = (attrName, element) -> {
      if (!knownAttributes.contains(attrName)) {
        attrsFromSelectors.putValue(attrName, element);
      }
    };
    Map<String, Angular2DirectiveProperty> inputs = new HashMap<>();
    Map<String, Angular2DirectiveProperty> outputs = new HashMap<>();
    Map<String, Angular2DirectiveProperty> inOuts = new HashMap<>();
    for (Angular2Directive candidate : directiveCandidates) {
      fillNamesAndProperties(inputs, candidate.getInputs(), p -> p);
      if (!isTemplateTag && candidate.isTemplate()) {
        List<SimpleSelectorWithPsi> selectors = candidate.getSelector().getSimpleSelectorsWithPsi();
        if (selectors.size() == 1) {
          List<Angular2DirectiveSelectorPsiElement> attributeCandidates = selectors.get(0).getAttributes();
          if (attributeCandidates.size() == 1) {
            addAttribute.accept("*" + attributeCandidates.get(0).getName(), attributeCandidates.get(0).getNavigationElement());
          }
          else {
            CANDIDATES_LOOP:
            for (Angular2DirectiveSelectorPsiElement attr : attributeCandidates) {
              String attrName = attr.getName();
              for (String input : inputs.keySet()) {
                if (!input.startsWith(attrName)) {
                  break CANDIDATES_LOOP;
                }
              }
              addAttribute.accept("*" + attrName, attr.getNavigationElement());
            }
          }
        }
      }
      else {
        fillNamesAndProperties(outputs, candidate.getOutputs(), p -> p);
        fillNamesAndProperties(inOuts, candidate.getInOuts(), p -> p.first);
        for (SimpleSelectorWithPsi selector : candidate.getSelector().getSimpleSelectorsWithPsi()) {
          for (Angular2DirectiveSelectorPsiElement attr : selector.getAttributes()) {
            String attrName = attr.getName();
            boolean added = false;
            Angular2DirectiveProperty property;
            if ((property = inOuts.get(attrName)) != null) {
              addAttribute.accept(BANANA_BOX_BINDING.buildName(attrName), property.getNavigableElement());
              added = true;
            }
            if ((property = inputs.get(attrName)) != null) {
              addAttribute.accept(PROPERTY_BINDING.buildName(attrName), property.getNavigableElement());
              added = true;
              if (Angular2AttributeDescriptor.isOneTimeBindingProperty(property)) {
                addAttribute.accept(attrName, property.getNavigableElement());
              }
            }
            if ((property = outputs.get(attrName)) != null) {
              addAttribute.accept(EVENT.buildName(attrName), property.getNavigableElement());
              added = true;
            }
            if (!added) {
              addAttribute.accept(attrName, attr.getNavigationElement());
            }
          }
          for (SimpleSelectorWithPsi notSelector : selector.getNotSelectors()) {
            for (Angular2DirectiveSelectorPsiElement attr : notSelector.getAttributes()) {
              addAttribute.accept(attr.getName(), attr.getNavigationElement());
            }
          }
        }
      }
    }
    attrsFromSelectors.entrySet().forEach(e -> result.add(
      new Angular2AttributeDescriptor(e.getKey(), isTemplateTag, e.getValue())));
    return result;
  }

  private static <T> void fillNamesAndProperties(@NotNull Map<String, Angular2DirectiveProperty> map,
                                                 @NotNull Collection<T> propertiesCollection,
                                                 @NotNull Function<T, Angular2DirectiveProperty> propertyExtractor) {
    map.clear();
    for (T item : propertiesCollection) {
      Angular2DirectiveProperty property = propertyExtractor.apply(item);
      map.put(property.getName(), property);
    }
  }

  @NotNull
  public static List<String> getStandardTagEventAttributeNames(@NotNull XmlTag xmlTag) {
    return ContainerUtil.mapNotNull(getDefaultAttributeDescriptors(xmlTag), attrDescriptor -> {
      String name = attrDescriptor.getName();
      if (name.startsWith("on")) {
        return name;
      }
      return null;
    });
  }

  @NotNull
  public static Collection<XmlAttributeDescriptor> getStandardPropertyAndEventDescriptors(@NotNull XmlTag xmlTag) {
    return CachedValuesManager.getCachedValue(xmlTag, STANDARD_PROPERTIES_KEY, () -> {
      Set<String> allowedElementProperties = new HashSet<>(DomElementSchemaRegistry.getElementProperties(xmlTag.getName()));
      allowedElementProperties.addAll(ContainerUtil.map(
        getStandardTagEventAttributeNames(xmlTag), eventName -> EVENT.buildName(eventName.substring(2))));
      JSType tagClass = Angular2Processor.getHtmlElementClassType(xmlTag, xmlTag.getName());
      List<XmlAttributeDescriptor> result = new ArrayList<>();
      Set<Object> dependencies = new HashSet<>();
      boolean isInTemplateTag = Angular2Processor.isTemplateTag(xmlTag.getName());
      if (tagClass != null) {
        for (JSRecordType.PropertySignature property : tagClass.asRecordType().getProperties()) {
          if (property.getMemberSource().getSingleElement() instanceof JSAttributeListOwner) {
            JSAttributeListOwner propertyDeclaration =
              (JSAttributeListOwner)property.getMemberSource().getSingleElement();
            if (!(propertyDeclaration instanceof TypeScriptPropertySignature)
                || (propertyDeclaration.getAttributeList() != null
                    && propertyDeclaration.getAttributeList().hasModifier(JSAttributeList.ModifierType.READONLY))) {
              continue;
            }
            dependencies.add(propertyDeclaration.getContainingFile());
            String name;
            if (property.getMemberName().startsWith("on")) {
              name = EVENT.buildName(property.getMemberName().substring(2));
            }
            else {
              name = PROPERTY_BINDING.buildName(property.getMemberName());
            }
            if (allowedElementProperties.remove(name)) {
              result.add(Angular2AttributeDescriptor.create(name, propertyDeclaration));
            }
          }
        }
      }
      for (String name : allowedElementProperties) {
        if (name.startsWith("(")) {
          result.add(new Angular2EventHandlerDescriptor(name, isInTemplateTag, Collections.emptyList()));
        }
        else {
          result.add(new Angular2AttributeDescriptor(name, isInTemplateTag, Collections.emptyList()));
        }
      }
      return CachedValueProvider.Result.create(Collections.unmodifiableList(result),
                                               !dependencies.isEmpty() ? dependencies :
                                               Collections.singleton(PsiModificationTracker.MODIFICATION_COUNT));
    });
  }

  @NotNull
  public static List<XmlAttributeDescriptor> getExistingVarsAndRefsDescriptors(@NotNull XmlTag xmlTag) {
    List<XmlAttributeDescriptor> result = new ArrayList<>();
    boolean isInTemplateTag = Angular2Processor.isTemplateTag(xmlTag.getName());
    xmlTag.acceptChildren(new Angular2HtmlElementVisitor() {
      @Override
      public void visitVariable(Angular2HtmlVariable variable) {
        result.add(new Angular2AttributeDescriptor(variable.getName(), isInTemplateTag,
                                                   singletonList(variable.getNameElement())));
      }

      @Override
      public void visitReference(Angular2HtmlReference reference) {
        result.add(new Angular2AttributeDescriptor(reference.getName(), isInTemplateTag,
                                                   singletonList(reference.getNameElement())));
      }
    });
    return result;
  }

  @NotNull
  public static XmlAttributeDescriptor[] getDefaultAttributeDescriptors(@NotNull XmlTag tag) {
    HtmlElementDescriptorImpl descriptor = ObjectUtils.tryCast(tag.getDescriptor(), HtmlElementDescriptorImpl.class);
    if (descriptor == null) {
      descriptor = ObjectUtils.tryCast(HtmlNSDescriptorImpl.guessTagForCommonAttributes(tag), HtmlElementDescriptorImpl.class);
      if (descriptor == null) {
        return XmlAttributeDescriptor.EMPTY;
      }
    }
    return ObjectUtils.notNull(descriptor.getDefaultAttributeDescriptors(tag), XmlAttributeDescriptor.EMPTY);
  }
}
