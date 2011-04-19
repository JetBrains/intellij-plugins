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
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.css.CssSupportLoader;
import com.intellij.struts2.StrutsConstants;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Adds CSS inline support for UI/jQuery-plugin tags in Freemarker.
 *
 * @author Yann C&eacute;bron
 */
public class FreeMarkerCssInlineStyleInjector implements MultiHostInjector {

  private static final PsiElementPattern.Capture<FtlStringLiteral> CSS_ELEMENT_PATTERN =
    psiElement(FtlStringLiteral.class)
      .withParent(psiElement(FtlNameValuePair.class).with(new PatternCondition<FtlNameValuePair>("S2 taglib CSS Attributes") {
        @Override
        public boolean accepts(@NotNull final FtlNameValuePair ftlNameValuePair, final ProcessingContext processingContext) {
          return Arrays.binarySearch(StrutsConstants.TAGLIB_STRUTS_UI_CSS_ATTRIBUTES, ftlNameValuePair.getName()) > -1;
        }
      }))
      .withSuperParent(3, psiElement(FtlMacro.class).with(new PatternCondition<FtlMacro>("S2 taglib prefix") {
        @Override
        public boolean accepts(@NotNull final FtlMacro ftlMacro, final ProcessingContext processingContext) {
          final String name = ftlMacro.getName();
          return StringUtil.startsWith(name, '@' + StrutsConstants.TAGLIB_STRUTS_UI_PREFIX + '.') ||
                 StringUtil.startsWith(name, '@' + StrutsConstants.TAGLIB_JQUERY_PLUGIN_PREFIX + '.') ||
                 StringUtil.startsWith(name, '@' + StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_PREFIX + '.');
        }
      }));

  @Override
  public void getLanguagesToInject(@NotNull final MultiHostRegistrar multiHostRegistrar, @NotNull final PsiElement psiElement) {
    if (CSS_ELEMENT_PATTERN.accepts(psiElement)) {
      final TextRange range = new TextRange(1, psiElement.getTextLength() - 1);
      multiHostRegistrar.startInjecting(CssSupportLoader.CSS_FILE_TYPE.getLanguage())
        .addPlace("inline.style {", "}", (PsiLanguageInjectionHost) psiElement, range)
        .doneInjecting();
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(FtlStringLiteral.class);
  }

}