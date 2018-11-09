// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.inject.JSFormattableInjectionUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static org.angular2.lang.expr.parser.Angular2PsiParser.*;

public class Angular2Injector implements MultiHostInjector {

  @NotNull
  @Override
  public List<Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(JSLiteralExpression.class);
  }

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    if (!Angular2LangUtil.isAngular2Context(context)) return;
    final PsiElement parent = context.getParent();
    if (parent != null
        && context instanceof JSLiteralExpressionImpl
        && ((JSLiteralExpressionImpl)context).isQuotedLiteral()) {
      if (injectIntoDirectiveProperty(registrar, context, parent, "template", Angular2HtmlLanguage.INSTANCE, null)
          || (parent instanceof JSArrayLiteralExpression
              && injectIntoDirectiveProperty(registrar, context, ObjectUtils.tryCast(parent.getParent(), JSProperty.class),
                                             "styles", CSSLanguage.INSTANCE, null))
          || injectIntoEmbeddedLiteral(registrar, context, parent)) {
        return;
      }
      if (parent instanceof JSProperty && parent.getParent() instanceof JSObjectLiteralExpression) {
        final String name = ((JSProperty)parent).getName();
        final String fileExtension;
        if (name != null && (fileExtension = getExpressionFileExtension(context.getTextLength(), name, true)) != null) {
          injectIntoDirectiveProperty(registrar, context, parent.getParent().getParent(), "host", Angular2Language.INSTANCE,
                                      fileExtension);
        }
      }
    }
  }

  private static boolean injectIntoEmbeddedLiteral(@NotNull MultiHostRegistrar registrar, PsiElement context, PsiElement parent) {
    if (parent instanceof JSEmbeddedContent && !parent.getLanguage().is(Angular2Language.INSTANCE)) {
      final XmlAttribute attribute = PsiTreeUtil.getParentOfType(parent, XmlAttribute.class);
      final String expressionType;
      if (attribute != null && (expressionType = getExpressionFileExtension(context.getTextLength(), attribute.getName(), false)) != null) {
        inject(registrar, context, Angular2Language.INSTANCE, expressionType);
        return true;
      }
    }
    return false;
  }

  @Nullable
  private static String getExpressionFileExtension(int valueLength, @NotNull String attributeName, boolean hostBinding) {
    if (valueLength == 0) {
      return null;
    }
    if (!hostBinding) {
      attributeName = StringUtil.trimStart(attributeName.toLowerCase(Locale.ENGLISH), "data-");
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
                                                     @NotNull PsiElement context,
                                                     @Nullable PsiElement parent,
                                                     @NotNull String propertyName,
                                                     @NotNull Language language,
                                                     @Nullable String fileExtension) {
    return parent instanceof JSProperty
           && propertyName.equals(((JSProperty)parent).getName())
           && injectIntoDecoratorExpr(registrar, context, parent,
                                      decoratorName -> Angular2LangUtil.isDirective(decoratorName) || "View".equals(decoratorName),
                                      language, fileExtension);
  }


  private static boolean injectIntoDecoratorExpr(@NotNull MultiHostRegistrar registrar,
                                                 @NotNull PsiElement context,
                                                 @Nullable PsiElement parent,
                                                 @NotNull Predicate<String> decoratorNameAcceptor,
                                                 @NotNull Language language,
                                                 @Nullable String fileExtension) {
    final JSCallExpression callExpression = PsiTreeUtil.getParentOfType(parent, JSCallExpression.class);
    final JSExpression expression = callExpression != null ? callExpression.getMethodExpression() : null;
    if (expression instanceof JSReferenceExpression) {
      final String decoratorName = ((JSReferenceExpression)expression).getReferenceName();
      if (decoratorNameAcceptor.test(decoratorName)) {
        inject(registrar, context, language, fileExtension);
        JSFormattableInjectionUtil.setReformattableInjection(context, language);
      }
      return true;
    }
    return false;
  }


  private static void inject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context, @NotNull Language language,
                             @Nullable String extension) {
    final TextRange range = ElementManipulators.getValueTextRange(context);
    registrar.startInjecting(language, extension).addPlace(null, null, (PsiLanguageInjectionHost)context, range).doneInjecting();
  }
}
