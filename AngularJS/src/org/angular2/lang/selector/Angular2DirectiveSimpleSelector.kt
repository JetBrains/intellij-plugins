// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.selector;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.xml.util.XmlUtil;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.web.Angular2WebSymbolsQueryConfigurator.ELEMENT_NG_TEMPLATE;

public class Angular2DirectiveSimpleSelector {

  private static final Pattern SELECTOR_REGEXP = Pattern.compile(
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

  public static @NotNull List<Angular2DirectiveSimpleSelector> parse(@NotNull String selector) throws ParseException {
    List<Angular2DirectiveSimpleSelector> results = new SmartList<>();
    Consumer<Angular2DirectiveSimpleSelector> addResult = cssSel -> {
      if (!cssSel.notSelectors.isEmpty() && cssSel.element == null && cssSel.classNames.isEmpty() &&
          cssSel.attrs.isEmpty()) {
        cssSel.element = "*";
      }
      results.add(cssSel);
    };
    Angular2DirectiveSimpleSelector cssSelector = new Angular2DirectiveSimpleSelector();
    Angular2DirectiveSimpleSelector current = cssSelector;
    boolean inNot = false;

    Matcher matcher = SELECTOR_REGEXP.matcher(selector);
    while (matcher.find()) {
      if (matcher.start(1) >= 0) {
        if (inNot) {
          throw new ParseException(Angular2Bundle.message("angular.parse.selector.nested-not"),
                                   new TextRange(matcher.start(1), matcher.end(1)));
        }
        inNot = true;
        current = new Angular2DirectiveSimpleSelector();
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
          throw new ParseException(Angular2Bundle.message("angular.parse.selector.multiple-not"),
                                   new TextRange(matcher.start(8), matcher.end(8)));
        }
        addResult.accept(cssSelector);
        cssSelector = current = new Angular2DirectiveSimpleSelector();
      }
    }
    addResult.accept(cssSelector);
    return results;
  }

  public static @NotNull List<Angular2DirectiveSimpleSelectorWithRanges> parseRanges(@NotNull String selector) throws ParseException {
    List<Angular2DirectiveSimpleSelectorWithRanges> results = new SmartList<>();

    Angular2DirectiveSimpleSelectorWithRanges cssSelector = new Angular2DirectiveSimpleSelectorWithRanges();
    Angular2DirectiveSimpleSelectorWithRanges current = cssSelector;
    boolean inNot = false;

    Matcher matcher = SELECTOR_REGEXP.matcher(selector);
    while (matcher.find()) {
      if (matcher.start(1) >= 0) {
        if (inNot) {
          throw new ParseException(Angular2Bundle.message("angular.parse.selector.nested-not"),
                                   new TextRange(matcher.start(1), matcher.end(1)));
        }
        inNot = true;
        current = new Angular2DirectiveSimpleSelectorWithRanges();
        cssSelector.notSelectors.add(current);
      }
      else if (matcher.start(2) >= 0) {
        current.setElement(matcher.group(2), matcher.start(2));
      }
      if (matcher.start(3) >= 0) {
        current.addClassName(matcher.group(3), matcher.start(3));
      }
      if (matcher.start(4) >= 0) {
        current.addAttribute(matcher.group(4), matcher.start(4));
      }
      if (matcher.start(7) >= 0) {
        inNot = false;
        current = cssSelector;
      }
      if (matcher.start(8) >= 0) {
        if (inNot) {
          throw new ParseException(Angular2Bundle.message("angular.parse.selector.multiple-not"),
                                   new TextRange(matcher.start(8), matcher.end(8)));
        }
        results.add(cssSelector);
        cssSelector = current = new Angular2DirectiveSimpleSelectorWithRanges();
      }
    }
    results.add(cssSelector);
    return results;
  }

  public static Angular2DirectiveSimpleSelector createTemplateBindingsCssSelector(@NotNull Angular2TemplateBindings bindings) {
    Angular2DirectiveSimpleSelector cssSelector = new Angular2DirectiveSimpleSelector();
    cssSelector.setElement(ELEMENT_NG_TEMPLATE);
    cssSelector.addAttribute(bindings.getTemplateName(), null);
    for (Angular2TemplateBinding binding : bindings.getBindings()) {
      if (!binding.keyIsVar()) {
        cssSelector.addAttribute(binding.getKey(), ObjectUtils.doIfNotNull(binding.getExpression(), JSExpression::getText));
      }
    }
    return cssSelector;
  }

  public static Angular2DirectiveSimpleSelector createElementCssSelector(@NotNull XmlTag element) {
    Angular2DirectiveSimpleSelector cssSelector = new Angular2DirectiveSimpleSelector();
    String elNameNoNs = XmlUtil.findLocalNameByQualifiedName(element.getName());

    cssSelector.setElement(elNameNoNs);

    for (XmlAttribute attr : element.getAttributes()) {
      String attrNameNoNs = XmlUtil.findLocalNameByQualifiedName(attr.getName());
      Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(attrNameNoNs, element);
      if (info.type == Angular2AttributeType.TEMPLATE_BINDINGS
          || info.type == Angular2AttributeType.LET
          || info.type == Angular2AttributeType.REFERENCE) {
        continue;
      }
      cssSelector.addAttribute(info.name, attr.getValue());
      if (StringUtil.toLowerCase(attr.getName()).equals("class") && attr.getValue() != null) {
        StringUtil.split(attr.getValue(), " ")
          .forEach(clsName -> cssSelector.addClassName(clsName));
      }
    }
    return cssSelector;
  }

  String element;
  final List<String> classNames = new SmartList<>();
  final List<String> attrs = new SmartList<>();
  final List<Angular2DirectiveSimpleSelector> notSelectors = new SmartList<>();

  Angular2DirectiveSimpleSelector() {
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

  public @Nullable String getElementName() {
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
  public @NotNull List<String> getAttrNames() {
    List<String> result = new ArrayList<>();
    if (!classNames.isEmpty()) {
      result.add("class");
    }
    for (int i = 0; i < attrs.size(); i += 2) {
      result.add(attrs.get(i));
    }
    return result;
  }

  public @NotNull List<Angular2DirectiveSimpleSelector> getNotSelectors() {
    return notSelectors;
  }

  public void addAttribute(@NotNull String name, @Nullable String value) {
    attrs.add(name);
    attrs.add(value != null ? StringUtil.toLowerCase(value) : "");
  }

  public void addClassName(@NotNull String name) {
    classNames.add(StringUtil.toLowerCase(name));
  }

  public @NotNull String toString() {
    @NonNls StringBuilder result = new StringBuilder();
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


  public static final class Angular2DirectiveSimpleSelectorWithRanges {

    private Pair<String, Integer> element;
    private final List<Pair<String, Integer>> classNames = new SmartList<>();
    private final List<Pair<String, Integer>> attrs = new SmartList<>();
    private final List<Angular2DirectiveSimpleSelectorWithRanges> notSelectors = new SmartList<>();

    private Angular2DirectiveSimpleSelectorWithRanges() {
    }

    private void addAttribute(@NotNull String name, int offset) {
      attrs.add(pair(name, offset));
    }

    private void addClassName(@NotNull String name, int offset) {
      classNames.add(pair(name, offset));
    }

    private void setElement(@NotNull String name, int offset) {
      element = pair(name, offset);
    }

    public @Nullable Pair<String, Integer> getElementRange() {
      return element;
    }

    public @NotNull List<Pair<String, Integer>> getClassNameRanges() {
      return classNames;
    }

    public @NotNull List<Pair<String, Integer>> getAttributeRanges() {
      return attrs;
    }

    public @NotNull List<Angular2DirectiveSimpleSelectorWithRanges> getNotSelectors() {
      return notSelectors;
    }
  }

  public static class ParseException extends Exception {

    private final TextRange myErrorRange;

    public ParseException(String s, TextRange errorRange) {
      super(s);
      myErrorRange = errorRange;
    }

    public TextRange getErrorRange() {
      return myErrorRange;
    }
  }
}
