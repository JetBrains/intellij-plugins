// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.NullableFunction;
import org.angular2.cli.config.AngularConfig;
import org.angular2.cli.config.AngularConfigProvider;
import org.angular2.cli.config.AngularProject;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.lang.expr.parser.Angular2PsiParser.*;

public class Angular2Injector implements MultiHostInjector {
  static final class Holder {
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

    if (context instanceof XmlText) {
      injectInterpolations(registrar, context);
      return;
    }

    if (!(context instanceof JSLiteralExpression && ((JSLiteralExpression)context).isQuotedLiteral())) {
      return;
    }

    JSLiteralExpression literalExpression = (JSLiteralExpression)context;

    if (parent instanceof JSProperty && Objects.equals(((JSProperty)parent).getName(), "template")) {
      injectIntoDecoratorExpr(registrar, literalExpression, parent, Angular2HtmlLanguage.INSTANCE, null);
    }

    PsiElement grandParent = parent.getParent();
    if (parent instanceof JSArrayLiteralExpression
        && grandParent instanceof JSProperty
        && Objects.equals(((JSProperty)grandParent).getName(), "styles")) {
      injectIntoDecoratorExpr(registrar, literalExpression, grandParent, getCssDialect(literalExpression), null);
    }

    if (injectIntoEmbeddedLiteral(registrar, literalExpression, parent)) {
      return;
    }

    if (parent instanceof JSProperty && parent.getParent() instanceof JSObjectLiteralExpression) {
      final String name = ((JSProperty)parent).getName();
      final String fileExtension;
      if (name != null && (fileExtension = getExpressionFileExtension(literalExpression.getTextLength(), name, true)) != null) {
        PsiElement ancestor = parent.getParent().getParent();
        if (ancestor instanceof JSProperty && "host".equals(((JSProperty)ancestor).getName())) {
          injectIntoDecoratorExpr(registrar, literalExpression, ancestor, Angular2Language.INSTANCE, fileExtension);
        }
      }
    }
  }

  @NotNull
  private static Language getCssDialect(@NotNull JSLiteralExpression literalExpression) {
    PsiFile file = literalExpression.getContainingFile().getOriginalFile();

    return CachedValuesManager.getCachedValue(file, () -> {
      AngularConfig angularConfig = AngularConfigProvider.getAngularConfig(file.getProject(), file.getVirtualFile());

      if (angularConfig == null) {
        return CachedValueProvider.Result.create(CSSLanguage.INSTANCE, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS);
      }

      Language cssDialect = CSSLanguage.INSTANCE;
      AngularProject angularProject = angularConfig.getProject(file.getVirtualFile());
      if (angularProject != null) {
        Language projectCssDialect = angularProject.getInlineStyleLanguage();
        if (projectCssDialect != null) {
          cssDialect = projectCssDialect;
        }
      }
      return CachedValueProvider.Result.create(cssDialect, angularConfig.getAngularJsonFile(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS);
    });
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

  private static void injectIntoDecoratorExpr(@NotNull MultiHostRegistrar registrar,
                                              @NotNull JSLiteralExpression context,
                                              @NotNull PsiElement ancestor,
                                              @NotNull Language language,
                                              @Nullable String fileExtension) {
    final ES6Decorator decorator = PsiTreeUtil.getContextOfType(ancestor, ES6Decorator.class);
    if (decorator != null) {
      if (isAngularEntityDecorator(decorator, COMPONENT_DEC, DIRECTIVE_DEC, VIEW_DEC)) {
        inject(registrar, context, language, fileExtension);
        JSFormattableInjectionUtil.setReformattableInjection(context, language);
      }
    }
  }


  private static void inject(@NotNull MultiHostRegistrar registrar, @NotNull JSLiteralExpression context, @NotNull Language language,
                             @Nullable String extension) {
    JSInjectionUtil.injectInQuotedLiteral(registrar, language, extension, context, null, null);
  }
}
