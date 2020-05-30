/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.reference.common.BeanPropertyPathReference;
import com.intellij.struts2.reference.common.BeanPropertyPathReferenceSet;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Resolves action class properties in {@code <result>}.
 *
 * @author Yann C&eacute;bron
 */
public class ResultActionPropertyReferenceProvider extends PsiReferenceProvider {

  private static final String EXPRESSION_START = "${";
  private static final String EXPRESSION_END = "}";

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement psiElement,
                                                         @NotNull final ProcessingContext processingContext) {
    final Result result = (Result)DomUtil.getDomElement(psiElement);
    assert result != null : psiElement.getText();
    final Action action = result.getParentOfType(Action.class, true);
    assert action != null : psiElement.getText();
    final PsiClass actionClass = action.searchActionClass();
    if (actionClass == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final int tagValueStartOffset = ElementManipulators.getOffsetInElement(result.getXmlTag());
    PsiReference[] references = new PsiReference[1];

    final String stringValue = result.getStringValue();
    if (!StringUtil.isNotEmpty(stringValue)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final String resultText = StringUtil.replace(stringValue, "&", "&amp;");
    final int lastExpressionEnd = Math.max(resultText.length(),         // missing '}'
                                           resultText.lastIndexOf(EXPRESSION_START));

    int startOffset = 0;
    while (startOffset < lastExpressionEnd) {
      startOffset = resultText.indexOf(EXPRESSION_START, startOffset);
      if (startOffset == -1) {
        break;
      }

      startOffset += EXPRESSION_START.length();
      final int closingBraceIdx = resultText.indexOf(EXPRESSION_END, startOffset);
      final int length = (closingBraceIdx != -1 ? closingBraceIdx : resultText.length()) - startOffset;

      final String expressionString = resultText.substring(startOffset, startOffset + length);

      // we only "fake" OGNL here, skip method call expressions for now
      if (StringUtil.containsChar(expressionString, '(')) {
        continue;
      }

      final BeanPropertyPathReferenceSet propertyPathReferenceSet =
        new BeanPropertyPathReferenceSet(expressionString,
                                         psiElement,
                                         startOffset,
                                         '.',
                                         actionClass,
                                         true) {

          // CTOR creates references eagerly, so we have to subclass here
          @Override
          public boolean isSoft() {
            return false;
          }

          @NotNull
          @Override
          protected BeanPropertyPathReference createReference(final TextRange range, final int index) {
            final TextRange shift = TextRange.from(range.getStartOffset() + tagValueStartOffset,
                                                   range.getLength()); // shift range to XmlTag value range
            return createBeanPropertyPathReference(shift, index);
          }
        };

      references = ArrayUtil.mergeArrays(references, propertyPathReferenceSet.getPsiReferences());
    }

    return references;
  }
}