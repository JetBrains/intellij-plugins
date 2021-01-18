/*
 * Copyright 2011 The authors
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
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.util.TextRange;
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
 * Adds JavaScript support for Struts UI/jQuery-plugin attributes in Freemarker.
 *
 * @author Yann C&eacute;bron
 */
final class FreeMarkerJavaScriptInjector implements MultiHostInjector {
  private static class Holder {
    private static final PsiElementPattern.Capture<FtlStringLiteral> JS_ELEMENT_PATTERN =
      psiElement(FtlStringLiteral.class)
        .withParent(psiElement(FtlNameValuePair.class).with(new PatternCondition<>("S2 taglib JS Attributes") {
          @Override
          public boolean accepts(@NotNull final FtlNameValuePair ftlNameValuePair, final ProcessingContext processingContext) {
            final String name = ftlNameValuePair.getName();
            return name != null &&
                   (name.startsWith("on") || name.startsWith("doubleOn")) &&
                   !name.endsWith("Topics");
          }
        }))
        .withSuperParent(3, psiElement(FtlMacro.class).with(FreemarkerInjectionConstants.TAGLIB_PREFIX));
  }
  @Override
  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement host) {
    if (Holder.JS_ELEMENT_PATTERN.accepts(host)) {
      registrar.startInjecting(JavaScriptSupportLoader.JAVASCRIPT.getLanguage())
        .addPlace(null, null, (PsiLanguageInjectionHost) host,
                  TextRange.from(1, host.getTextLength() - 2))
        .doneInjecting();
    }
  }

  @Override
  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(FtlStringLiteral.class);
  }

}