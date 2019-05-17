// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
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
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.Angular2HtmlVariable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag;
import static org.angular2.codeInsight.template.Angular2TemplateScopesResolver.getHtmlElementClassType;
import static org.angular2.lang.html.parser.Angular2AttributeType.*;

public class Angular2AttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {

  @NonNls public static final String EVENT_ATTR_PREFIX = "on";

  public static XmlAttributeDescriptor getAttributeDescriptor(@Nullable final String attrName, @Nullable XmlTag xmlTag,
                                                              @NotNull Function<? super XmlTag, XmlAttributeDescriptor[]> attrDescrProvider) {
    if (attrName == null || xmlTag == null || DumbService.isDumb(xmlTag.getProject())) {
      return null;
    }
    Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(attrName, xmlTag);
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

  @NonNls private static final List<String> CUSTOM_NG_ATTRS = singletonList("i18n");
  @NonNls private static final Key<CachedValue<List<XmlAttributeDescriptor>>> STANDARD_PROPERTIES_KEY =
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
      attr -> result.merge(attr.getName(), attr, (a, b) -> {
        if (a instanceof Angular2AttributeDescriptor) {
          return ((Angular2AttributeDescriptor)a).merge(b);
        }
        else if (b instanceof Angular2AttributeDescriptor) {
          return ((Angular2AttributeDescriptor)b).merge(a);
        }
        return a;
      });

    getDirectiveDescriptors(xmlTag).forEach(addDescriptor);
    getStandardPropertyAndEventDescriptors(xmlTag).forEach(addDescriptor);
    getExistingVarsAndRefsDescriptors(xmlTag).forEach(addDescriptor);

    for (String CUSTOM_NG_ATTR : CUSTOM_NG_ATTRS) {
      addDescriptor.accept(Angular2AttributeDescriptor.create(xmlTag, CUSTOM_NG_ATTR));
    }

    return result.values().toArray(XmlAttributeDescriptor.EMPTY);
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(@Nullable final String attrName, @Nullable XmlTag xmlTag) {
    return xmlTag != null && Angular2LangUtil.isAngular2Context(xmlTag)
           ? getAttributeDescriptor(attrName, xmlTag, this::getAttributeDescriptors)
           : null;
  }

  @NotNull
  public static Collection<XmlAttributeDescriptor> getDirectiveDescriptors(@NotNull XmlTag xmlTag) {
    Angular2ApplicableDirectivesProvider applicableDirectives = new Angular2ApplicableDirectivesProvider(xmlTag);

    List<XmlAttributeDescriptor> result = new ArrayList<>();
    for (Angular2Directive matchedDirective : applicableDirectives.getMatched()) {
      result.addAll(Angular2AttributeDescriptor.getDirectiveDescriptors(matchedDirective, xmlTag));
    }

    Set<String> knownAttributes = new HashSet<>();
    for (XmlAttributeDescriptor attr : result) {
      knownAttributes.add(attr.getName());
    }
    getStandardPropertyAndEventDescriptors(xmlTag).forEach(
      attr -> knownAttributes.add(attr.getName()));

    boolean isTemplateTag = isTemplateTag(xmlTag);
    MultiMap<String, Angular2Directive> attrsFromSelectors = MultiMap.createSet();
    Map<String, Angular2DirectiveProperty> inputs = new HashMap<>();
    Map<String, Angular2DirectiveProperty> outputs = new HashMap<>();
    Map<String, Angular2DirectiveProperty> inOuts = new HashMap<>();
    for (Angular2Directive candidate : applicableDirectives.getCandidates()) {
      Consumer<String> addAttribute = (attrName) -> {
        if (!knownAttributes.contains(attrName)) {
          attrsFromSelectors.putValue(attrName, candidate);
        }
      };
      fillNamesAndProperties(inputs, candidate.getInputs(), p -> p);
      if (!isTemplateTag && candidate.isStructuralDirective()) {
        List<SimpleSelectorWithPsi> selectors = candidate.getSelector().getSimpleSelectorsWithPsi();
        for (SimpleSelectorWithPsi selector : selectors) {
          List<Angular2DirectiveSelectorPsiElement> attributeCandidates = selector.getAttributes();
          if (attributeCandidates.size() == 1) {
            addAttribute.accept("*" + attributeCandidates.get(0).getName());
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
              addAttribute.accept("*" + attrName);
            }
          }
        }
      }
      if (isTemplateTag || candidate.isRegularDirective()) {
        fillNamesAndProperties(outputs, candidate.getOutputs(), p -> p);
        fillNamesAndProperties(inOuts, candidate.getInOuts(), p -> p.first);
        for (SimpleSelectorWithPsi selector : candidate.getSelector().getSimpleSelectorsWithPsi()) {
          for (Angular2DirectiveSelectorPsiElement attr : selector.getAttributes()) {
            String attrName = attr.getName();
            boolean added = false;
            Angular2DirectiveProperty property;
            if (inOuts.get(attrName) != null) {
              addAttribute.accept(BANANA_BOX_BINDING.buildName(attrName));
              added = true;
            }
            if ((property = inputs.get(attrName)) != null) {
              addAttribute.accept(PROPERTY_BINDING.buildName(attrName));
              added = true;
              if (Angular2AttributeDescriptor.isOneTimeBindingProperty(property)) {
                addAttribute.accept(attrName);
              }
            }
            if (outputs.get(attrName) != null) {
              addAttribute.accept(EVENT.buildName(attrName));
              added = true;
            }
            if (!added) {
              addAttribute.accept(attrName);
            }
          }
          for (SimpleSelectorWithPsi notSelector : selector.getNotSelectors()) {
            for (Angular2DirectiveSelectorPsiElement attr : notSelector.getAttributes()) {
              addAttribute.accept(attr.getName());
            }
          }
        }
      }
    }
    attrsFromSelectors.entrySet().forEach(
      e -> result.add(new Angular2AttributeDescriptor(xmlTag, e.getKey(), e.getValue(), false)));
    return result;
  }

  private static <T> void fillNamesAndProperties(@NotNull Map<String, Angular2DirectiveProperty> map,
                                                 @NotNull Collection<? extends T> propertiesCollection,
                                                 @NotNull Function<? super T, ? extends Angular2DirectiveProperty> propertyExtractor) {
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
      if (name.startsWith(EVENT_ATTR_PREFIX)) {
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
      JSType tagClass = getHtmlElementClassType(xmlTag, xmlTag.getName());
      List<XmlAttributeDescriptor> result = new ArrayList<>();
      Set<Object> dependencies = new HashSet<>();
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
            if (property.getMemberName().startsWith(EVENT_ATTR_PREFIX)) {
              name = EVENT.buildName(property.getMemberName().substring(2));
            }
            else {
              name = PROPERTY_BINDING.buildName(property.getMemberName());
            }
            if (allowedElementProperties.remove(name)) {
              result.add(Angular2AttributeDescriptor.create(xmlTag, name, propertyDeclaration));
            }
          }
        }
      }
      for (String name : allowedElementProperties) {
        if (name.startsWith("(")) {
          result.add(new Angular2EventHandlerDescriptor(xmlTag, name, emptyList(), true));
        }
        else {
          result.add(new Angular2AttributeDescriptor(xmlTag, name, emptyList(), true));
        }
      }
      if (dependencies.isEmpty()) {
        dependencies = Collections.singleton(PsiModificationTracker.MODIFICATION_COUNT);
      }
      else {
        String tagName = xmlTag.getName();
        dependencies.add((ModificationTracker)() -> tagName.equals(xmlTag.getName()) ? 0 : 1);
      }
      return CachedValueProvider.Result.create(Collections.unmodifiableList(result), dependencies);
    });
  }

  @NotNull
  public static List<XmlAttributeDescriptor> getExistingVarsAndRefsDescriptors(@NotNull XmlTag xmlTag) {
    List<XmlAttributeDescriptor> result = new ArrayList<>();
    xmlTag.acceptChildren(new Angular2HtmlElementVisitor() {
      @Override
      public void visitVariable(Angular2HtmlVariable variable) {
        result.add(new Angular2AttributeDescriptor(xmlTag, variable.getName(),
                                                   singletonList(variable.getNameElement()),
                                                   true));
      }

      @Override
      public void visitReference(Angular2HtmlReference reference) {
        result.add(new Angular2AttributeDescriptor(xmlTag, reference.getName(),
                                                   singletonList(reference.getNameElement()),
                                                   true));
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
