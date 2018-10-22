// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import org.angular2.lang.selector.Angular2DirectiveSelector;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.util.Pair.pair;

public class Angular2EntityUtils {

  private static final String INDEX_ELEMENT_NAME_PREFIX = ">";
  private static final String INDEX_ATTRIBUTE_NAME_PREFIX = "=";
  private static final String INDEX_TEMPLATE_ATTRIBUTE_PREFIX = "=*";

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
  public static String getAttributeDirectiveIndexName(@NotNull String attributeName) {
    return INDEX_ATTRIBUTE_NAME_PREFIX + attributeName;
  }

  public static boolean isAttributeDirectiveIndexName(@NotNull String attributeName) {
    return attributeName.startsWith(INDEX_ATTRIBUTE_NAME_PREFIX);
  }

  @NotNull
  public static Set<String> getDirectiveIndexNames(@NotNull Project project, @NotNull String selector, boolean isTemplate) {
    List<Angular2DirectiveSelector> selectors;
    try {
      selectors = Angular2DirectiveSelector.parse(selector);
    }
    catch (ParseException e) {
      return Collections.emptySet();
    }
    Set<String> result = new HashSet<>();
    for (Angular2DirectiveSelector sel : selectors) {
      String elementName = sel.getElementName();
      if (StringUtil.isNotEmpty(elementName) && !"*".equals(elementName)) {
        result.add(INDEX_ELEMENT_NAME_PREFIX + elementName);
      }
      else {
        List<String> attributes = sel.getAttrs();
        if (!attributes.isEmpty()) {
          result.add(INDEX_ATTRIBUTE_NAME_PREFIX + attributes.get(0));
          if (isTemplate) {
            result.add(INDEX_TEMPLATE_ATTRIBUTE_PREFIX + attributes.get(0));
          }
        }
        else {
          result.add(INDEX_ELEMENT_NAME_PREFIX); //match all elements
        }
      }
    }
    return result;
  }

}
