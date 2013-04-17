package com.dmarcotte.handlebars.completion;

import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbPath;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class HbKeywordCompletionContributor extends CompletionContributor {
  public HbKeywordCompletionContributor() {
    extend(CompletionType.BASIC, psiElement(HbTokenTypes.ID).withSuperParent(2, psiElement(HbTokenTypes.PATH)),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               PsiElement position = PsiTreeUtil.getParentOfType(parameters.getPosition(), HbPath.class);
               PsiElement prevSibling = position != null ? position.getPrevSibling() : null;
               ASTNode prevSiblingNode = prevSibling != null ? prevSibling.getNode() : null;
               if (prevSiblingNode != null && prevSiblingNode.getElementType() == HbTokenTypes.OPEN_BLOCK) {
                 result.addElement(LookupElementBuilder.create("if"));
                 result.addElement(LookupElementBuilder.create("each"));
               }
             }
           });
  }
}
