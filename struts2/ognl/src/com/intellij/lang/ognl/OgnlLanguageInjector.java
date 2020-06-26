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

package com.intellij.lang.ognl;

import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiLanguageInjectionHost;

/**
 * Injects OGNL language.
 *
 * @author Yann C&eacute;bron
 */
public final class OgnlLanguageInjector {

  private final MultiHostRegistrar registrar;
  private final PsiLanguageInjectionHost element;

  private boolean addPrefixSuffix;

  private OgnlLanguageInjector(final MultiHostRegistrar registrar,
                               final PsiLanguageInjectionHost element) {
    this.registrar = registrar;
    this.element = element;
  }

  private OgnlLanguageInjector addPrefixSuffix() {
    this.addPrefixSuffix = true;
    return this;
  }

  public static void injectElementWithPrefixSuffix(final MultiHostRegistrar registrar,
                                                   final PsiLanguageInjectionHost element) {
    new OgnlLanguageInjector(registrar, element)
        .addPrefixSuffix()
        .injectWholeXmlAttributeValue();
  }

  public static void injectOccurrences(final MultiHostRegistrar registrar,
                                       final PsiLanguageInjectionHost element) {
    new OgnlLanguageInjector(registrar, element).injectOccurrences();
  }

  private void injectWholeXmlAttributeValue() {
    final int textLength = element.getTextLength();
    if (textLength < 2) {
      return;
    }

    final TextRange range = new TextRange(1, textLength - 1);
    registrar.startInjecting(OgnlLanguage.INSTANCE)
             .addPlace(addPrefixSuffix ? OgnlLanguage.EXPRESSION_PREFIX : null,
                       addPrefixSuffix ? OgnlLanguage.EXPRESSION_SUFFIX : null,
                       element,
                       range)
             .doneInjecting();
  }

  private void injectOccurrences() {
    registrar.startInjecting(OgnlLanguage.INSTANCE);

    final String text = element.getText();
    final int textLength = text.length() - 1;
    final int lastStartPosition = Math.max(textLength, text.lastIndexOf(OgnlLanguage.EXPRESSION_SUFFIX));

    int startOffset = 0;
    while (startOffset < lastStartPosition) {
      startOffset = text.indexOf(OgnlLanguage.EXPRESSION_PREFIX, startOffset);
      if (startOffset == -1) {
        break;
      }

      // search closing '}' from text end/next expr start backwards to support sequence expressions
      final int nextStartOffset = text.indexOf(OgnlLanguage.EXPRESSION_PREFIX,
                                               startOffset + OgnlLanguage.EXPRESSION_PREFIX.length());
      final int searchClosingBraceIdx = nextStartOffset != -1 ? nextStartOffset : textLength;
      final int closingBraceIdx = text.lastIndexOf(OgnlLanguage.EXPRESSION_SUFFIX, searchClosingBraceIdx);
      final int length = (closingBraceIdx != -1 && closingBraceIdx > nextStartOffset ? closingBraceIdx + 1 : textLength) - startOffset;
      final TextRange range = TextRange.from(startOffset, length);
      registrar.addPlace(null, null, element, range);
      startOffset += length;
    }

    registrar.doneInjecting();
  }

}