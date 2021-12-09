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

package com.intellij.lang.ognl.completion;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.lang.ognl.psi.*;
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

  private static final PsiElementPattern.Capture<PsiElement> FQN_TYPE_EXPRESSION =
    psiElement().inside(OgnlFqnTypeExpression.class);

  private static final PsiElementPattern.Capture<PsiElement> VARIABLE_EXPRESSION =
    psiElement().inside(OgnlVariableExpression.class);

  private static final PsiElementPattern.Capture<PsiElement> VARIABLE_ASSIGNMENT_EXPRESSION =
    psiElement().inside(OgnlVariableAssignmentExpression.class);

  private static final PsiElementPattern.Capture<PsiElement> AFTER_OPERATIONS =
    psiElement().afterLeaf(psiElement().withElementType(OgnlTokenGroups.OPERATIONS));

  private static final PsiElementPattern.Capture<PsiElement> AFTER_NEW =
    psiElement().afterLeaf(psiElement().withElementType(OgnlTypes.NEW_KEYWORD));

  private static final PsiElementPattern.Capture<PsiElement> AFTER_QUESTION =
    psiElement().afterLeaf(psiElement().withElementType(OgnlTypes.QUESTION));

  private static final PsiElementPattern.Capture<PsiElement> AFTER_COLON =
    psiElement().afterLeaf(psiElement().withElementType(OgnlTypes.COLON));

  private static final PsiElementPattern.Capture<PsiElement> AFTER_EXPRESSION =
    psiElement().afterLeaf(psiElement().inside(OgnlExpression.class))
      .andNot(AFTER_OPERATIONS)
      .andNot(AFTER_QUESTION)
      .andNot(AFTER_COLON)
      .andNot(AFTER_NEW)
      .andNot(VARIABLE_EXPRESSION)
      .andNot(VARIABLE_ASSIGNMENT_EXPRESSION)
      .andNot(FQN_TYPE_EXPRESSION);

  private static final PsiElementPattern.Capture<PsiElement> AFTER_IDENTIFIER =
    psiElement().afterLeaf(psiElement().inside(OgnlReferenceExpression.class));

  public OgnlKeywordCompletionContributor() {
    installBinaryOperations();
    installSequence();
    installIdentifier();
    installBooleanNull();
    installNew();
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
      TokenSet.create(OgnlTypes.EQUAL,
                      OgnlTypes.EQ_KEYWORD,
                      OgnlTypes.NOT_EQUAL,
                      OgnlTypes.NEQ_KEYWORD,
                      OgnlTypes.QUESTION,
                      OgnlTypes.COLON,
                      OgnlTypes.AND_KEYWORD,
                      OgnlTypes.AND_AND,
                      OgnlTypes.OR_KEYWORD,
                      OgnlTypes.OR_OR,
                      OgnlTypes.NEGATE,
                      OgnlTypes.NOT_KEYWORD,
                      OgnlTypes.EQ);
    extendKeywordCompletion(psiElement().afterLeaf(psiElement().inside(OgnlExpression.class)
                                                     .withElementType(precedingOperators)),
                            FALSE, TRUE, NULL);
  }

  // TODO simplify -> expression with no text
  private void installNew() {
    extendKeywordCompletion(psiElement().atStartOf(psiElement(OgnlExpression.class))
                              .andNot(AFTER_OPERATIONS)
                              .andNot(AFTER_NEW), NEW);
  }

  private void extendKeywordCompletion(final PsiElementPattern.Capture<PsiElement> pattern,
                                       final String... keywords) {
    extend(CompletionType.BASIC,
           pattern,
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull final CompletionParameters completionParameters,
                                           @NotNull final ProcessingContext processingContext,
                                           @NotNull final CompletionResultSet completionResultSet) {
               for (final String keyword : keywords) {
                 final LookupElementBuilder builder = LookupElementBuilder.create(keyword).bold();
                 completionResultSet.addElement(TailTypeDecorator.withTail(builder, TailType.SPACE));
               }
             }
           });
  }
}