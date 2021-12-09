// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.StreamEx;
import org.angular2.entities.ivy.Angular2IvyEntity;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.angular2.entities.source.Angular2SourceEntity;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector.ParseException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.intellij.openapi.util.Pair.pair;

public final class Angular2EntityUtils {

  @NonNls public static final String ELEMENT_REF = "ElementRef";
  @NonNls public static final String TEMPLATE_REF = "TemplateRef";
  @NonNls public static final String VIEW_CONTAINER_REF = "ViewContainerRef";

  private static final String INDEX_ELEMENT_NAME_PREFIX = ">";
  private static final String INDEX_ANY_ELEMENT_NAME = "E";
  private static final String INDEX_ATTRIBUTE_NAME_PREFIX = "=";

  public static @NotNull Collection<? extends TypeScriptFunction> getPipeTransformMethods(@NotNull TypeScriptClass cls) {
    //noinspection RedundantCast,unchecked
    return (Collection<? extends TypeScriptFunction>)(Collection)TypeScriptTypeParser
      .buildTypeFromClass(cls, false)
      .getProperties()
      .stream()
      .filter(prop -> Angular2EntitiesProvider.TRANSFORM_METHOD.equals(prop.getMemberName())
                      && prop.getMemberSource()
                        .getSingleElement() instanceof TypeScriptFunction)
      .findFirst()
      .map(sig -> ContainerUtil.filter(sig.getMemberSource()
                                         .getAllSourceElements(), fun -> fun instanceof TypeScriptFunction &&
                                                                         !(fun instanceof TypeScriptFunctionSignature)))
      .map(Collections::unmodifiableCollection)
      .orElseGet(Collections::emptyList);
  }

  public static @NotNull Pair<String, String> parsePropertyMapping(@NotNull String property) {
    int ind = property.indexOf(':');
    if (ind > 0) {
      return pair(property.substring(0, ind).trim(), property.substring(ind + 1).trim());
    }
    return pair(property.trim(), property.trim());
  }

  public static @NotNull String getElementDirectiveIndexName(@NotNull String elementName) {
    return INDEX_ELEMENT_NAME_PREFIX + elementName;
  }

  public static @NotNull String getAnyElementDirectiveIndexName() {
    return INDEX_ANY_ELEMENT_NAME;
  }

  public static boolean isElementDirectiveIndexName(@NotNull String elementName) {
    return elementName.startsWith(INDEX_ELEMENT_NAME_PREFIX);
  }

  public static @NotNull String getElementName(@NotNull String elementDirectiveIndexName) {
    if (!isElementDirectiveIndexName(elementDirectiveIndexName)) {
      throw new IllegalArgumentException();
    }
    return elementDirectiveIndexName.substring(1);
  }

  public static @NotNull String getAttributeDirectiveIndexName(@NotNull String attributeName) {
    return INDEX_ATTRIBUTE_NAME_PREFIX + attributeName;
  }

  public static boolean isAttributeDirectiveIndexName(@NotNull String attributeName) {
    return attributeName.startsWith(INDEX_ATTRIBUTE_NAME_PREFIX);
  }

  public static @NotNull String getAttributeName(@NotNull String attributeIndexName) {
    if (!isAttributeDirectiveIndexName(attributeIndexName)) {
      throw new IllegalArgumentException();
    }
    return attributeIndexName.substring(1);
  }

  public static <T extends Angular2Module> @Nullable T defaultChooseModule(@NotNull Stream<T> modulesStream) {
    return modulesStream.min(Comparator.comparing(Angular2Module::getName)).orElse(null);
  }

  public static @NotNull Set<String> getDirectiveIndexNames(@NotNull String selector) {
    List<Angular2DirectiveSimpleSelector> selectors;
    try {
      selectors = Angular2DirectiveSimpleSelector.parse(selector);
    }
    catch (ParseException e) {
      return Collections.emptySet();
    }
    Set<String> result = new HashSet<>();
    Consumer<Angular2DirectiveSimpleSelector> indexSelector = sel -> {
      String elementName = sel.getElementName();
      if (StringUtil.isEmpty(elementName) || "*".equals(elementName)) {
        result.add(INDEX_ELEMENT_NAME_PREFIX);
      }
      else {
        result.add(INDEX_ELEMENT_NAME_PREFIX + elementName);
        result.add(INDEX_ANY_ELEMENT_NAME);
      }
      for (String attrName : sel.getAttrNames()) {
        result.add(INDEX_ATTRIBUTE_NAME_PREFIX + attrName);
      }
    };
    for (Angular2DirectiveSimpleSelector sel : selectors) {
      indexSelector.accept(sel);
      sel.getNotSelectors().forEach(indexSelector);
    }
    return result;
  }

  public static String toString(Angular2Element element) {
    String sourceKind;
    if (element instanceof Angular2SourceEntity) {
      sourceKind = "source";
    }
    else if (element instanceof Angular2MetadataEntity) {
      sourceKind = "metadata";
    }
    else if (element instanceof Angular2IvyEntity) {
      sourceKind = "ivy";
    }
    else {
      sourceKind = "unknown";
    }
    if (element instanceof Angular2Directive) {
      StringBuilder result = new StringBuilder();
      Angular2Directive directive = (Angular2Directive)element;
      result.append(directive.getName())
        .append(" <")
        .append(sourceKind)
        .append(' ');
      if (directive.isComponent()) {
        result.append("component");
      }
      else {
        Angular2DirectiveKind kind = directive.getDirectiveKind();
        if (kind.isStructural()) {
          if (kind.isRegular()) {
            result.append("directive/");
          }
          result.append("template");
        }
        else {
          result.append("directive");
        }
      }
      result.append(">")
        .append(": selector=")
        .append(directive.getSelector().getText());
      if (directive instanceof Angular2Component
          && !((Angular2Component)directive).getNgContentSelectors().isEmpty()) {
        result.append("; ngContentSelectors=");
        result.append(((Angular2Component)directive).getNgContentSelectors());
      }
      if (!directive.getExportAsList().isEmpty()) {
        result.append("; exportAs=")
          .append(StringUtil.join(directive.getExportAsList(), ","));
      }
      result.append("; inputs=")
        .append(directive.getInputs().toString())
        .append("; outputs=")
        .append(directive.getOutputs().toString())
        .append("; inOuts=")
        .append(directive.getInOuts().toString());

      if (!directive.getAttributes().isEmpty()) {
        result.append("; attributes=")
          .append(directive.getAttributes());
      }

      return result.toString();
    }
    else if (element instanceof Angular2Pipe) {
      return ((Angular2Pipe)element).getName() + " <" + sourceKind + " pipe>";
    }
    else if (element instanceof Angular2DirectiveProperty) {
      return ((Angular2DirectiveProperty)element).getName();
    }
    else if (element instanceof Angular2DirectiveAttribute) {
      return ((Angular2DirectiveAttribute)element).getName();
    }
    else if (element instanceof Angular2Module) {
      Angular2Module module = (Angular2Module)element;
      return module.getName() +
             " <" +
             sourceKind +
             " module>: imports=[" +
             StreamEx.of(module.getImports()).map(Angular2Module::getName).sorted().joining(", ") +
             "]; declarations=[" +
             StreamEx.of(module.getDeclarations()).map(Angular2Entity::getName).sorted().joining(", ") +
             "]; exports=[" +
             StreamEx.of(module.getExports()).map(Angular2Entity::getName).sorted().joining(", ") +
             "]; scopeFullyResolved=" +
             module.isScopeFullyResolved() +
             "; exportsFullyResolved=" +
             module.areExportsFullyResolved();
    }
    else {
      return element.getClass().getName() + "@" + Integer.toHexString(element.hashCode());
    }
  }

  public static <T extends Angular2Entity> String renderEntityList(Collection<T> entities) {
    StringBuilder result = new StringBuilder();
    int i = -1;
    for (Angular2Entity entity : entities) {
      if (++i > 0) {
        if (i == entities.size() - 1) {
          result.append(' ');
          result.append(Angular2Bundle.message("angular.description.and-separator"));
          result.append(' ');
        }
        else {
          result.append(", ");
        }
      }
      result.append(getEntityClassName(entity));
      if (entity instanceof Angular2Pipe) {
        result.append(" (");
        result.append(entity.getName());
        result.append(")");
      }
      else if (entity instanceof Angular2Directive) {
        result.append(" (");
        result.append(((Angular2Directive)entity).getSelector().getText());
        result.append(')');
      }
    }
    return result.toString();
  }


  public static String getEntityClassName(@NotNull Angular2Entity entity) {
    if (entity instanceof Angular2Pipe) {
      return ObjectUtils.notNull(ObjectUtils.doIfNotNull(entity.getTypeScriptClass(), TypeScriptClass::getName),
                                 Angular2Bundle.message("angular.description.unknown-class"));
    }
    return entity.getName();
  }

  public static String getEntityClassName(@NotNull ES6Decorator decorator) {
    Angular2Entity entity = Angular2EntitiesProvider.getEntity(decorator);
    if (entity == null) {
      return Angular2Bundle.message("angular.description.unknown-class");
    }
    return getEntityClassName(entity);
  }

  public static @NotNull String unquote(@NotNull String s) {
    return s.length() > 1 && ("'\"`".indexOf(s.charAt(0)) >= 0) && s.charAt(0) == s.charAt(s.length() - 1) ?
           s.substring(1, s.length() - 1) : s;
  }
}
