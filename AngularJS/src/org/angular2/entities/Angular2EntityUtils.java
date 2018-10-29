// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.angular2.entities.source.Angular2SourceEntity;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.intellij.openapi.util.Pair.pair;

public class Angular2EntityUtils {

  private static final String INDEX_ELEMENT_NAME_PREFIX = ">";
  private static final String INDEX_ATTRIBUTE_NAME_PREFIX = "=";

  @NotNull
  public static Collection<? extends TypeScriptFunction> getPipeTransformMethods(@NotNull TypeScriptClass cls) {
    //noinspection RedundantCast,unchecked
    return (Collection<? extends TypeScriptFunction>)(Collection)TypeScriptTypeParser
      .buildTypeFromClass(cls, false)
      .getProperties()
      .stream()
      .filter(prop -> Angular2EntitiesProvider.TRANSFORM_METHOD.equals(prop.getMemberName())
                      && prop.getMemberSource()
                        .getSingleElement() instanceof TypeScriptFunction)
      .findFirst()
      .map(sig -> sig.getMemberSource()
        .getAllSourceElements()
        .stream()
        .filter(fun -> fun instanceof TypeScriptFunction && !(fun instanceof TypeScriptFunctionSignature))
        .collect(Collectors.toList()))
      .map(Collections::unmodifiableCollection)
      .orElseGet(Collections::emptyList);
  }

  @NotNull
  public static Pair<String, String> parsePropertyMapping(@NotNull String property) {
    int ind = property.indexOf(':');
    if (ind > 0) {
      return pair(property.substring(0, ind).trim(), property.substring(ind + 1).trim());
    }
    return pair(property.trim(), property.trim());
  }

  @NotNull
  public static String getElementDirectiveIndexName(@NotNull String elementName) {
    return INDEX_ELEMENT_NAME_PREFIX + elementName;
  }

  public static boolean isElementDirectiveIndexName(@NotNull String elementName) {
    return elementName.startsWith(INDEX_ELEMENT_NAME_PREFIX);
  }

  @NotNull
  public static String getElementName(@NotNull String elementDirectiveIndexName) {
    if (!isElementDirectiveIndexName(elementDirectiveIndexName)) {
      throw new IllegalArgumentException();
    }
    return elementDirectiveIndexName.substring(1);
  }

  @NotNull
  public static String getAttributeDirectiveIndexName(@NotNull String attributeName) {
    return INDEX_ATTRIBUTE_NAME_PREFIX + attributeName;
  }

  public static boolean isAttributeDirectiveIndexName(@NotNull String attributeName) {
    return attributeName.startsWith(INDEX_ATTRIBUTE_NAME_PREFIX);
  }

  @NotNull
  public static Set<String> getDirectiveIndexNames(@NotNull String selector, boolean isTemplate) {
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
      else if (directive.isTemplate()) {
        result.append("template");
      }
      else {
        result.append("directive");
      }
      result.append(">")
        .append(": selector=")
        .append(directive.getSelector().getText());
      if (directive.getExportAs() != null) {
        result.append("; exportAs=")
          .append(directive.getExportAs());
      }
      result.append("; inputs=")
        .append(directive.getInputs().toString())
        .append("; outputs=")
        .append(directive.getOutputs().toString())
        .append("; inOuts=")
        .append(directive.getInOuts().toString());
      return result.toString();
    }
    else if (element instanceof Angular2Pipe) {
      return ((Angular2Pipe)element).getName() + "<" + sourceKind + " pipe>";
    }
    else if (element instanceof Angular2DirectiveProperty) {
      return ((Angular2DirectiveProperty)element).getName();
    }
    else {
      return element.toString();
    }
  }
}
