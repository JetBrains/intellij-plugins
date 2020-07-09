// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.json.psi.JsonElement;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.util.HtmlUtil;
import org.angular2.index.Angular2IndexingHandler;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class DirectiveUtil {

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
          String attrName = typeStr.substring(start);
          if (!attrName.isEmpty()) {
            return attrName;
          }
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

  public static boolean isAngular2Directive(final @Nullable PsiElement directive) {
    return directive instanceof JSImplicitElement
           && !Angular2IndexingHandler.isPipe((JSImplicitElement)directive)
           && (directive.getParent() instanceof JSCallExpression ||
               directive.getParent() instanceof ES6Decorator ||
               directive.getParent() instanceof JsonElement);
  }

  public static boolean processTagDirectives(final @NotNull Project project,
                                             @NotNull Processor<? super JSImplicitElement> processor) {
    final Collection<String> docDirectives = AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.KEY, project);
    for (String directiveName : docDirectives) {
      final JSImplicitElement directive = getTagDirective(project, directiveName, AngularDirectivesDocIndex.KEY);
      if (directive != null) {
        if (!processor.process(directive)) {
          return false;
        }
      }
    }
    final Collection<String> directives = AngularIndexUtil.getAllKeys(AngularDirectivesIndex.KEY, project);
    for (String directiveName : directives) {
      if (!docDirectives.contains(directiveName)) {
        final JSImplicitElement directive = getTagDirective(project, directiveName, AngularDirectivesIndex.KEY);
        if (directive != null) {
          if (!processor.process(directive)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public static @Nullable JSImplicitElement getTagDirective(@NotNull String directiveName, @NotNull Project project) {
    return getDirective(directiveName, project);
  }

  private static @Nullable JSImplicitElement getTagDirective(@NotNull Project project,
                                                             @NotNull String directiveName,
                                                             final @NotNull StubIndexKey<String, JSImplicitElementProvider> index) {
    return getDirective(directiveName, project, index);
  }

  private static @Nullable JSImplicitElement getDirective(@NotNull String name, @NotNull Project project) {
    final JSImplicitElement directive = getDirective(name, project, AngularDirectivesDocIndex.KEY);
    return directive == null ? getDirective(name, project, AngularDirectivesIndex.KEY) : directive;
  }

  private static @Nullable JSImplicitElement getDirective(@NotNull String name,
                                                          @NotNull Project project,
                                                          final @NotNull StubIndexKey<String, JSImplicitElementProvider> index) {
    JSImplicitElement directive = AngularIndexUtil.resolve(project, index, name);
    final String restrictions = directive != null ? directive.getTypeString() : null;
    if (restrictions != null) {
      final CharSequence restrict = AngularIndexUtil.convertRestrictions(project, restrictions.subSequence(0, restrictions.indexOf(';')));
      if (!StringUtil.isEmpty(restrict) && Strings.indexOfIgnoreCase(restrict, "E", 0) >= 0) {
        return directive;
      }
    }
    return null;
  }

  public static @Nullable JSImplicitElement getDirective(@Nullable PsiElement element) {
    if (element instanceof JSImplicitElement) {
      return isAngular2Directive(element) ? null : getDirective(element, ((JSImplicitElement)element).getName());
    }
    if (element instanceof JSLiteralExpression && ((JSLiteralExpression)element).isQuotedLiteral()) {
      return getDirective(element, StringUtil.unquoteString(element.getText()));
    }
    return null;
  }

  private static @Nullable JSImplicitElement getDirective(@NotNull PsiElement element, final @NotNull String directiveName) {
    final JSImplicitElement directive = AngularIndexUtil.resolve(element.getProject(), AngularDirectivesIndex.KEY, directiveName);
    if (directive != null && directive.isEquivalentTo(element)) {
      return directive;
    }
    return null;
  }
}
