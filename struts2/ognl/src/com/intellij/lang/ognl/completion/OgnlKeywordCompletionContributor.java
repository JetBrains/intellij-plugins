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

package com.intellij.lang.ognl.completion;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.lang.ognl.psi.OgnlExpression;
import com.intellij.lang.ognl.psi.OgnlReferenceExpression;
import com.intellij.lang.ognl.psi.OgnlTokenTypes;
import com.intellij.lang.ognl.psi.OgnlVariableExpression;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.ognl.psi.OgnlKeyword.*;
import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * OGNL keyword completion.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlKeywordCompletionContributor extends CompletionContributor implements DumbAware {

  private static final PsiElementPattern.Capture<PsiElement> VARIABLE_EXPRESSION =
      psiElement().inside(OgnlVariableExpression.class);

  private static final PsiElementPattern.Capture<PsiElement> AFTER_OPERATIONS =
      psiElement().afterLeaf(psiElement().withElementType(OgnlTokenTypes.OPERATIONS));

  private static final PsiElementPattern.Capture<PsiElement> AFTER_EXPRESSION =
      psiElement().afterLeaf(psiElement().inside(OgnlExpression.class))
          .andNot(AFTER_OPERATIONS)
          .andNot(VARIABLE_EXPRESSION); // TODO

  private static final PsiElementPattern.Capture<PsiElement> AFTER_IDENTIFIER =
      psiElement().afterLeaf(psiElement().inside(OgnlReferenceExpression.class));

  public OgnlKeywordCompletionContributor() {
    installBinaryOperations();
    installSequence();
    installIdentifier();
    installBooleanNull();
    // TODO: new
  }

  private void installBinaryOperations() {
    extendKeywordCompletion(AFTER_EXPRESSION,
                            SHL, SHR, USHR,
                            AND, BAND, OR, BOR, XOR,
                            EQ, NEQ, LT, LTE, GT, GTE);
  }

  private void installSequence() {
    extendKeywordCompletion(AFTER_EXPRESSION,
                            NOT_IN, IN);
  }

  private void installIdentifier() {
    extendKeywordCompletion(AFTER_IDENTIFIER,
                            INSTANCEOF);
  }

  private void installBooleanNull() {
    final TokenSet precedingOperators =
        TokenSet.create(OgnlTokenTypes.EQUAL,
                        OgnlTokenTypes.EQ_KEYWORD,
                        OgnlTokenTypes.NOT_EQUAL,
                        OgnlTokenTypes.NEQ_KEYWORD,
                        OgnlTokenTypes.QUESTION,
                        OgnlTokenTypes.COLON,
                        OgnlTokenTypes.AND_KEYWORD,
                        OgnlTokenTypes.AND_AND,
                        OgnlTokenTypes.OR_KEYWORD,
                        OgnlTokenTypes.OR_OR,
                        OgnlTokenTypes.NEGATE,
                        OgnlTokenTypes.NOT_KEYWORD);
    extendKeywordCompletion(psiElement().afterLeaf(psiElement().inside(OgnlExpression.class)
                                                       .withElementType(precedingOperators)),
                            FALSE, TRUE, NULL);
  }

  private void extendKeywordCompletion(final PsiElementPattern.Capture<PsiElement> pattern,
                                       final String... keywords) {
    extend(CompletionType.BASIC,
           pattern,
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull final CompletionParameters completionParameters,
                                           final ProcessingContext processingContext,
                                           @NotNull final CompletionResultSet completionResultSet) {
               for (final String keyword : keywords) {
                 final LookupElementBuilder builder = LookupElementBuilder.create(keyword).setBold();
                 completionResultSet.addElement(TailTypeDecorator.withTail(builder, TailType.SPACE));
               }
             }
           });
  }

}