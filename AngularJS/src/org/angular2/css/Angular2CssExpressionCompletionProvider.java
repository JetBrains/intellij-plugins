// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.refactoring.BasicJavascriptNamesValidator;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.resolve.HtmlCssClassOrIdReference;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class Angular2CssExpressionCompletionProvider extends CompletionProvider<CompletionParameters> {


  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters,
                                @NotNull ProcessingContext processingContext,
                                @NotNull CompletionResultSet result) {
    PsiReference ref = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
    if (ref instanceof PsiMultiReference) {
      ref = ContainerUtil.find(((PsiMultiReference)ref).getReferences(), HtmlCssClassOrIdReference.class::isInstance);
    }
    if (ref instanceof HtmlCssClassOrIdReference) {
      JSObjectLiteralExpression expression = ObjectUtils.tryCast(
        ObjectUtils.doIfNotNull(parameters.getPosition().getParent(), PsiElement::getParent),
        JSObjectLiteralExpression.class);
      Set<String> existingProps = new HashSet<>();
      if (expression != null) {
        existingProps.addAll(ContainerUtil.mapNotNull(expression.getProperties(), PsiNamedElement::getName));
      }
      ((HtmlCssClassOrIdReference)ref).addCompletions(parameters, result.getPrefixMatcher(), element -> {
        if (existingProps.add(element.getLookupString())) {
          if (!BasicJavascriptNamesValidator.isIdentifierName(element.getLookupString())
              && parameters.getPosition().getNode().getElementType() == JSTokenTypes.IDENTIFIER) {
            if (element instanceof PrioritizedLookupElement) {
              LookupElementBuilder builder = ObjectUtils.tryCast(((PrioritizedLookupElement)element).getDelegate(),
                                                                 LookupElementBuilder.class);
              if (builder != null) {
                String quotesToInsert = determineQuotesToInsert(parameters.getPosition());
                element = PrioritizedLookupElement.withPriority(
                  builder.withInsertHandler((@NotNull InsertionContext context, @NotNull LookupElement item) -> {
                    final Document doc = context.getDocument();
                    final int caretOffset = context.getEditor().getCaretModel().getOffset();
                    doc.insertString(context.getStartOffset(), quotesToInsert);
                    doc.insertString(context.getTailOffset(), quotesToInsert);
                    context.getEditor().getCaretModel().moveToOffset(caretOffset + 2);
                  }),
                  ((PrioritizedLookupElement)element).getPriority());
                result.addElement(element);
              }
            }
          }
          else {
            result.addElement(element);
          }
        }
      });
      result.stopHere();
    }
  }

  private static String determineQuotesToInsert(@NotNull PsiElement position) {
    PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(position.getProject()).getInjectionHost(position);
    XmlAttributeValue value;
    if (host instanceof XmlAttributeValue) {
      value = (XmlAttributeValue)host;
    }
    else {
      value = PsiTreeUtil.getParentOfType(position, XmlAttributeValue.class);
    }
    return value != null
           ? (value.getText().startsWith("'") ? "\"" : "'")
           : "'";
  }
}
