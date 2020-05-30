// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSInjectionBracesUtil;
import com.intellij.lang.javascript.injections.JSFormattableInjectionUtil;
import com.intellij.lang.javascript.injections.JSInjectionUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.NullableFunction;
import com.intellij.util.ObjectUtils;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.lang.expr.parser.Angular2PsiParser.*;

public class Angular2Injector implements MultiHostInjector {
  static class Holder {
    static final NullableFunction<PsiElement, Pair<String, String>> BRACES_FACTORY = JSInjectionBracesUtil
      .delimitersFactory(Angular2HtmlLanguage.INSTANCE.getDisplayName(),
                         (project, key) -> /* no support for custom delimiters*/ null);
  }

  @Override
  public @NotNull List<Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(JSLiteralExpression.class, XmlText.class);
  }

  @SuppressWarnings("HardCodedStringLiteral")
  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    final PsiElement parent = context.getParent();
    if (parent == null
        || parent.getLanguage().is(Angular2Language.INSTANCE)
        || parent.getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE)
        || !Angular2LangUtil.isAngular2Context(context)) {
      return;
    }
    if (context instanceof JSLiteralExpression
        && ((JSLiteralExpression)context).isQuotedLiteral()) {
      JSLiteralExpression literalExpression = (JSLiteralExpression)context;
      if (injectIntoDirectiveProperty(registrar, literalExpression, parent, "template", Angular2HtmlLanguage.INSTANCE, null)
          || (parent instanceof JSArrayLiteralExpression
              && injectIntoDirectiveProperty(registrar, literalExpression, ObjectUtils.tryCast(parent.getParent(), JSProperty.class),
                                             "styles", CSSLanguage.INSTANCE, null))
          || injectIntoEmbeddedLiteral(registrar, literalExpression, parent)) {
        return;
      }
      if (parent instanceof JSProperty && parent.getParent() instanceof JSObjectLiteralExpression) {
        final String name = ((JSProperty)parent).getName();
        final String fileExtension;
        if (name != null && (fileExtension = getExpressionFileExtension(literalExpression.getTextLength(), name, true)) != null) {
          injectIntoDirectiveProperty(registrar, literalExpression, parent.getParent().getParent(), "host", Angular2Language.INSTANCE,
                                      fileExtension);
        }
      }
    }
    else if (context instanceof XmlText) {
      injectInterpolations(registrar, context);
    }
  }

  private static boolean injectIntoEmbeddedLiteral(@NotNull MultiHostRegistrar registrar,
                                                   @NotNull JSLiteralExpression context,
                                                   @NotNull PsiElement parent) {
    if (parent instanceof JSEmbeddedContent) {
      final XmlAttribute attribute = PsiTreeUtil.getParentOfType(parent, XmlAttribute.class);
      final String expressionType;
      if (attribute != null) {
        if ((expressionType = getExpressionFileExtension(context.getTextLength(), attribute.getName(), false)) != null) {
          inject(registrar, context, Angular2Language.INSTANCE, expressionType);
        }
        else {
          injectInterpolations(registrar, context);
        }
        return true;
      }
    }
    return false;
  }

  private static void injectInterpolations(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    final Pair<String, String> braces = Holder.BRACES_FACTORY.fun(context);
    if (braces == null) return;
    JSInjectionBracesUtil.injectInXmlTextByDelimiters(registrar, context, Angular2Language.INSTANCE,
                                                      braces.first, braces.second, INTERPOLATION);
  }

  @SuppressWarnings("HardCodedStringLiteral")
  private static @Nullable String getExpressionFileExtension(int valueLength, @NotNull String attributeName, boolean hostBinding) {
    if (valueLength == 0) {
      return null;
    }
    if (!hostBinding) {
      attributeName = Angular2AttributeNameParser.normalizeAttributeName(attributeName);
    }
    if ((attributeName.startsWith("(") && attributeName.endsWith(")"))
        || (!hostBinding && attributeName.startsWith("on-"))) {
      return ACTION;
    }
    if ((attributeName.startsWith("[") && attributeName.endsWith("]"))) {
      return hostBinding ? SIMPLE_BINDING : BINDING;
    }
    if (!hostBinding
        && (attributeName.startsWith("bind-") || attributeName.startsWith("bindon-"))) {
      return BINDING;
    }
    if (!hostBinding && attributeName.startsWith("*")) {
      return attributeName.substring(1) + "." + TEMPLATE_BINDINGS;
    }
    return null;
  }

  private static boolean injectIntoDirectiveProperty(@NotNull MultiHostRegistrar registrar,
                                                     @NotNull JSLiteralExpression context,
                                                     @Nullable PsiElement parent,
                                                     @NotNull String propertyName,
                                                     @NotNull Language language,
                                                     @Nullable String fileExtension) {
    return parent instanceof JSProperty
           && propertyName.equals(((JSProperty)parent).getName())
           && injectIntoDecoratorExpr(registrar, context, parent,
                                      decorator -> isAngularEntityDecorator(decorator, COMPONENT_DEC, DIRECTIVE_DEC, VIEW_DEC),
                                      language, fileExtension);
  }


  private static boolean injectIntoDecoratorExpr(@NotNull MultiHostRegistrar registrar,
                                                 @NotNull JSLiteralExpression context,
                                                 @Nullable PsiElement parent,
                                                 @NotNull Predicate<? super ES6Decorator> decoratorAcceptor,
                                                 @NotNull Language language,
                                                 @Nullable String fileExtension) {
    final ES6Decorator decorator = PsiTreeUtil.getContextOfType(parent, ES6Decorator.class);
    if (decorator != null) {
      if (decoratorAcceptor.test(decorator)) {
        inject(registrar, context, language, fileExtension);
        JSFormattableInjectionUtil.setReformattableInjection(context, language);
      }
      return true;
    }
    return false;
  }


  private static void inject(@NotNull MultiHostRegistrar registrar, @NotNull JSLiteralExpression context, @NotNull Language language,
                             @Nullable String extension) {
    JSInjectionUtil.injectInQuotedLiteral(registrar, language, extension, context, null, null);
  }
}
