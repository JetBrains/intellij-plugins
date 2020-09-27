// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.util.HtmlUtil;
import org.angularjs.index.AngularJSDirectivesSupport;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static org.angularjs.index.AngularJSDirectivesSupport.isDirective;

public final class DirectiveUtil {

  public static @NotNull String getPropertyAlias(@NotNull String propertyName, final @Nullable JSExpression attributeTypeExpr) {
    if (attributeTypeExpr instanceof JSLiteralExpression) {
      String typeStr = ((JSLiteralExpression)attributeTypeExpr).getStringValue();
      if (typeStr != null) {
        int start;
        //noinspection StatementWithEmptyBody
        for (start = 0;
             start < typeStr.length() && "@=*<&?".indexOf(typeStr.charAt(start)) >= 0;
             start++) {
        }
        if (start < typeStr.length()) {
          return typeStr.substring(start);
        }
      }
    }
    return propertyName;
  }

  public static @NotNull String getAttributeName(final @NotNull String text) {
    return getAttributeName(text, false);
  }

  public static @NotNull Collection<String> getAttributeNameVariations(final @NotNull String text) {
    return ContainerUtil.newHashSet(
      getAttributeName(text, false),
      getAttributeName(text, true)
    );
  }

  private static @NotNull String getAttributeName(final @NotNull String text, boolean splitDigits) {
    StringBuilder result = new StringBuilder();
    boolean wasDigit = false;
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (Character.isDigit(ch)) {
        if (splitDigits && !wasDigit) {
          result.append('-');
        }
        result.append(ch);
        wasDigit = true;
      }
      else {
        if (Character.isUpperCase(ch)) {
          result.append('-');
        }
        result.append(Character.toLowerCase(ch));
        wasDigit = false;
      }
    }
    return result.toString();
  }

  @Contract("null -> null")
  public static String normalizeAttributeName(String name) {
    return name == null ? null : normalizeAttributeName(name, true).toString();
  }

  @Contract("null,_ -> null")
  public static CharSequence normalizeAttributeName(String name, boolean stripStartEnd) {
    if (name == null) return null;
    int index = 0;
    if (name.startsWith(HtmlUtil.HTML5_DATA_ATTR_PREFIX)) {
      index = 5;
    }
    else if (name.startsWith("x-")) {
      index = 2;
    }
    StringBuilder result = new StringBuilder(name.length());
    boolean upperCase = false;
    for (; index < name.length(); index++) {
      char ch = name.charAt(index);
      if (ch == ':' || ch == '_' || ch == '-') {
        upperCase = true;
      }
      else if (upperCase) {
        result.append(Character.toUpperCase(ch));
        upperCase = false;
      }
      else {
        result.append(Character.toLowerCase(ch));
      }
    }
    if (stripStartEnd) {
      if (result.indexOf("Start", result.length() - 5) >= 0) {
        result.setLength(result.length() - 5);
      }
      else if (result.indexOf("End", result.length() - 3) >= 0) {
        result.setLength(result.length() - 3);
      }
    }
    return result;
  }

  public static @Nullable JSImplicitElement getTagDirective(@NotNull String directiveName, @NotNull Project project) {
    return ContainerUtil.getFirstItem(AngularJSDirectivesSupport.findTagDirectives(project, directiveName));
  }

  public static @Nullable JSImplicitElement getDirective(@Nullable PsiElement element) {
    if (element instanceof JSImplicitElement) {
      String name = ((JSImplicitElement)element).getName();
      return name != null && isDirective((JSImplicitElement)element) ? getDirective(element, name) : null;
    }
    if (element instanceof JSLiteralExpression && ((JSLiteralExpression)element).isQuotedLiteral()) {
      return getDirective(element, StringUtil.unquoteString(element.getText()));
    }
    return null;
  }

  private static @Nullable JSImplicitElement getDirective(@NotNull PsiElement element, final @NotNull String directiveName) {
    final JSImplicitElement directive = AngularJSDirectivesSupport.findDirective(element.getProject(), directiveName);
    if (directive != null && directive.isEquivalentTo(element)) {
      return directive;
    }
    return null;
  }
}
