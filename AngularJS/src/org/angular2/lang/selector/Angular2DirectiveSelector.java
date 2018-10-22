// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.selector;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import com.intellij.xml.util.XmlUtil;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Angular2DirectiveSelector {

  private final static Pattern SELECTOR_REGEXP = Pattern.compile(
    "(:not\\()|" +       //":not("
    "([-\\w]+)|" +         // "tag"
    "(?:\\.([-\\w]+))|" +  // ".class"
    // "-" should appear first in the regexp below as FF31 parses "[.-\w]" as a range
    "(?:\\[([-.\\w*]+)(?:=([\"']?)([^]\"']*)\\5)?])|" +  // "[name]", "[name=value]",
    //                                                          "[name="value"]",
    //                                                          "[name='value']"
    "(\\))|" +             // ")"
    "(\\s*,\\s*)"          // ","
  );

  @NotNull
  public static List<Angular2DirectiveSelector> parse(@NotNull String selector) throws ParseException {
    List<Angular2DirectiveSelector> results = new SmartList<>();
    Consumer<Angular2DirectiveSelector> addResult = cssSel -> {
      if (!cssSel.notSelectors.isEmpty() && cssSel.element == null && cssSel.classNames.isEmpty() &&
          cssSel.attrs.isEmpty()) {
        cssSel.element = "*";
      }
      results.add(cssSel);
    };
    Angular2DirectiveSelector cssSelector = new Angular2DirectiveSelector();
    Angular2DirectiveSelector current = cssSelector;
    boolean inNot = false;

    Matcher matcher = SELECTOR_REGEXP.matcher(selector);
    while (matcher.find()) {
      if (matcher.start(1) >= 0) {
        if (inNot) {
          throw new ParseException("Nesting :not is not allowed in a selector", matcher.start(1));
        }
        inNot = true;
        current = new Angular2DirectiveSelector();
        cssSelector.notSelectors.add(current);
      }
      else if (matcher.start(2) >= 0) {
        current.setElement(matcher.group(2));
      }
      if (matcher.start(3) >= 0) {
        current.addClassName(matcher.group(3));
      }
      if (matcher.start(4) >= 0) {
        current.addAttribute(matcher.group(4), matcher.group(6));
      }
      if (matcher.start(7) >= 0) {
        inNot = false;
        current = cssSelector;
      }
      if (matcher.start(8) >= 0) {
        if (inNot) {
          throw new ParseException("Multiple selectors in :not are not supported", matcher.start(8));
        }
        addResult.accept(cssSelector);
        cssSelector = current = new Angular2DirectiveSelector();
      }
    }
    addResult.accept(cssSelector);
    return results;
  }

  public static Angular2DirectiveSelector createElementCssSelector(@NotNull XmlTag element) {
    Angular2DirectiveSelector cssSelector = new Angular2DirectiveSelector();
    String elNameNoNs = XmlUtil.findLocalNameByQualifiedName(element.getName());

    cssSelector.setElement(elNameNoNs);

    boolean isTemplateTag = Angular2Processor.isTemplateTag(element.getName());
    for (XmlAttribute attr : element.getAttributes()) {
      String attrNameNoNs = XmlUtil.findLocalNameByQualifiedName(attr.getName());
      attrNameNoNs = Angular2AttributeNameParser.parse(attrNameNoNs, isTemplateTag).name;
      cssSelector.addAttribute(attrNameNoNs, attr.getValue());
      if (attr.getName().toLowerCase().equals("class") && attr.getValue() != null) {
        StringUtil.split(attr.getValue(), " ")
          .forEach(clsName -> cssSelector.addClassName(clsName));
      }
    }
    return cssSelector;
  }


  String element;
  final List<String> classNames = new SmartList<>();
  final List<String> attrs = new SmartList<>();
  final List<Angular2DirectiveSelector> notSelectors = new SmartList<>();

  Angular2DirectiveSelector() {
  }

  public boolean isElementSelector() {
    return this.hasElementSelector()
           && this.classNames.isEmpty()
           && this.attrs.isEmpty()
           && this.notSelectors.isEmpty();
  }

  public boolean hasElementSelector() {
    return element != null;
  }

  @Nullable
  public String getElementName() {
    return element;
  }

  public void setElement(@Nullable String element) { this.element = element; }

  /**
   * The selectors are encoded in pairs where:
   * - even locations are attribute names
   * - odd locations are attribute values.
   * <p>
   * Example:
   * Selector: `[key1=value1][key2]` would parse to:
   * ```
   * ['key1', 'value1', 'key2', '']
   * ```
   */
  @NotNull
  public List<String> getAttrs() {
    List<String> result = new ArrayList<>();
    if (!classNames.isEmpty()) {
      result.add("class");
      result.add(StringUtil.join(this.classNames, " "));
    }
    result.addAll(this.attrs);
    return result;
  }

  @NotNull
  public List<Angular2DirectiveSelector> getNotSelectors() {
    return notSelectors;
  }

  public void addAttribute(@NotNull String name, @Nullable String value) {
    attrs.add(name);
    attrs.add(value != null ? value.toLowerCase(Locale.ENGLISH) : "");
  }

  public void addClassName(@NotNull String name) {
    classNames.add(name.toLowerCase(Locale.ENGLISH));
  }

  @NotNull
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (element != null) {
      result.append(element);
    }
    classNames.forEach(cls -> {
      result.append('.');
      result.append(cls);
    });
    for (int i = 0; i < attrs.size(); i += 2) {
      result.append('[');
      result.append(attrs.get(i));
      String value = attrs.get(i + 1);
      if (!value.isEmpty()) {
        result.append("=");
        result.append(value);
      }
      result.append(']');
    }
    notSelectors.forEach(selector -> {
      result.append(":not(");
      result.append(selector.toString());
      result.append(')');
    });
    return result.toString();
  }
}
