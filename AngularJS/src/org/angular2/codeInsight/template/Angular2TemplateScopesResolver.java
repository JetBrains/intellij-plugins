// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;
import one.util.streamex.StreamEx;
import org.angular2.entities.Angular2ComponentLocator;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.intellij.util.ObjectUtils.notNull;

public final class Angular2TemplateScopesResolver {

  @NonNls private static final String HTML_ELEMENT_CLASS_NAME = "HTMLElement";
  @NonNls private static final String HTML_ELEMENT_TAG_NAME_MAP_CLASS_NAME = "HTMLElementTagNameMap";

  public static void resolve(final @NotNull PsiElement element, final @NotNull Processor<? super ResolveResult> processor) {
    PsiElement original = CompletionUtil.getOriginalOrSelf(element);
    if (!checkLanguage(original)) {
      return;
    }
    boolean expressionIsInjected = original.getContainingFile().getLanguage().is(Angular2Language.INSTANCE);
    final PsiElement hostElement;
    if (expressionIsInjected) {
      //we are working within injection
      hostElement = InjectedLanguageManager.getInstance(element.getProject()).getInjectionHost(element);
      if (hostElement == null) {
        return;
      }
    }
    else {
      hostElement = null;
    }

    StreamEx.of(Angular2TemplateScopesProvider.EP_NAME.getExtensionList())
      .flatCollection(provider -> provider.getScopes(element, hostElement))
      .findFirst(s -> s.resolveAllScopesInHierarchy(processor));
  }

  private static boolean checkLanguage(@NotNull PsiElement element) {
    return element.getLanguage().is(Angular2Language.INSTANCE)
           || element.getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE)
           || (element.getParent() != null
               && (element.getParent().getLanguage().is(Angular2Language.INSTANCE)
                   || element.getParent().getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE)
               ));
  }

  @NonNls
  public static @Nullable JSType getHtmlElementClassType(@NotNull PsiElement context, @NotNull @NonNls String tagName) {
    JSTypeSource typeSource = JSTypeSourceFactory.createTypeSource(
      notNull(Angular2ComponentLocator.findComponentClass(context), context), true);
    return Optional
      .ofNullable(JSTypeUtils.createType(HTML_ELEMENT_TAG_NAME_MAP_CLASS_NAME, typeSource))
      .map(tagNameMap -> tagNameMap.asRecordType().findPropertySignature(StringUtil.toLowerCase(tagName)))
      .map(JSRecordType.PropertySignature::getJSType)
      .orElseGet(() -> JSTypeUtils.createType(HTML_ELEMENT_CLASS_NAME, typeSource));
  }
}
