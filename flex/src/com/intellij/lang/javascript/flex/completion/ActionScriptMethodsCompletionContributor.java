package com.intellij.lang.javascript.flex.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.validation.ActionScriptImplementedMethodProcessor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class ActionScriptMethodsCompletionContributor extends CompletionContributor {

  public ActionScriptMethodsCompletionContributor() {
    extend(null, PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER)
             .withParent(JSQualifiedNamedElement.class),
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final PsiElement position = parameters.getPosition();
               final PsiElement prev = PsiTreeUtil.skipWhitespacesAndCommentsBackward(position);
               final PsiElement parent = position.getParent();
               assert parent instanceof JSQualifiedNamedElement : "must be filtered in JSPatternBasedCompletionContributor";

               if (parent instanceof JSPackageStatement) return;
               PsiElement possibleClazz;

               if (parent instanceof JSFunction && (possibleClazz = parent.getParent()) instanceof JSClass &&
                   prev != null && prev.getNode().getElementType() == JSTokenTypes.FUNCTION_KEYWORD) {
                 JSAttributeList attributeList = ((JSFunction)parent).getAttributeList();
                 if (attributeList != null && attributeList.hasExplicitModifier(JSAttributeList.ModifierType.OVERRIDE)) {
                   for (JSFunction fun : JSInheritanceUtil.collectFunctionsToOverride((JSClass)possibleClazz)) {
                     final String name = fun.getName();
                     if (name != null) {
                       result.addElement(LookupElementBuilder.create(name));
                     }
                   }
                 }
                 else {
                   for (JSFunction fun : ActionScriptImplementedMethodProcessor.collectFunctionsToImplement((JSClass)possibleClazz)) {
                     final String name = fun.getName();
                     if (name != null) {
                       result.addElement(LookupElementBuilder.create(name));
                     }
                   }
                 }

                 final String name = ((JSClass)possibleClazz).getName();
                 if (name != null) {
                   result.addElement(LookupElementBuilder.create(name));
                 }
               }
             }
           });
  }
}
