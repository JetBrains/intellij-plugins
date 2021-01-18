/*
 * Copyright 2015 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.struts2.freemarker;

import com.intellij.freemarker.psi.FtlNameValuePair;
import com.intellij.freemarker.psi.FtlStringLiteral;
import com.intellij.freemarker.psi.directives.FtlMacro;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.OgnlLanguageInjector;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Adds OGNL support for UI/jQuery-plugin tags in Freemarker.
 *
 * @author Yann C&eacute;bron
 */
final class FreeMarkerOgnlInjector implements MultiHostInjector {
  private final PsiElementPattern.Capture<FtlStringLiteral> myOgnlElementPattern =
    createPattern(s -> s.contains(OgnlLanguage.EXPRESSION_PREFIX));

  private final PsiElementPattern.Capture<FtlStringLiteral> myOgnlListElementPattern =
    createPattern(s -> s.startsWith("{"));

  private static PsiElementPattern.Capture<FtlStringLiteral> createPattern(final Condition<String> valueTextCondition) {
    return psiElement(FtlStringLiteral.class)
        .withParent(psiElement(FtlNameValuePair.class).with(new PatternCondition<>("S2 OGNL") {
          @Override
          public boolean accepts(@NotNull final FtlNameValuePair ftlNameValuePair,
                                 final ProcessingContext processingContext) {
            final PsiElement valueElement = ftlNameValuePair.getValueElement();
            return valueElement != null &&
                   valueTextCondition.value(StringUtil.unquoteString(valueElement.getText()));
          }
        }))
        .withSuperParent(3, psiElement(FtlMacro.class).with(FreemarkerInjectionConstants.TAGLIB_PREFIX));
  }

  @Override
  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement context) {
    if (myOgnlElementPattern.accepts(context)) {
      OgnlLanguageInjector.injectOccurrences(registrar, (PsiLanguageInjectionHost) context);
      return;
    }

    if (myOgnlListElementPattern.accepts(context)) {
      OgnlLanguageInjector.injectElementWithPrefixSuffix(registrar, (PsiLanguageInjectionHost) context);
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(FtlStringLiteral.class);
  }

}