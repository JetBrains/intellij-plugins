// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlAttribute;
import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.CfmlComponentReference;
import com.intellij.coldFusion.model.psi.CfmlProperty;
import com.intellij.lang.ASTNode;
import com.intellij.lang.StdLanguages;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.intellij.codeInsight.completion.CompletionType.BASIC;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlCompletionContributor extends CompletionContributor {

  @Override
  public void beforeCompletion(@NotNull CompletionInitializationContext context) {
    int offset = context.getStartOffset();
    if (offset == 0 || !context.getFile().getViewProvider().getLanguages().contains(CfmlLanguage.INSTANCE)) {
      return;
    }
    final PsiElement element = context.getFile().findElementAt(offset);
    if (element != null && element.getTextRange().getStartOffset() != offset
        && context.getFile().findReferenceAt(offset) != null) {
      context.setDummyIdentifier("");
    }
    /*else {
            final CharSequence chars = context.getEditor().getDocument().getCharsSequence();
            if (offset < 1) {
                return;
            }
            char currChar = chars.charAt(offset - 1);
            if (currChar == '<' || (offset >= 2 && currChar == '/' && chars.charAt(offset - 2) == '<')) {
                context.setFileCopyPatcher(new DummyIdentifierPatcher("cf"));
            } else if ((currChar == 'c' || currChar == 'C') &&
                    ((offset >= 2 && chars.charAt(offset - 2) == '<') || ((offset >= 3 && chars.charAt(offset - 2) == '/' && chars.charAt(offset - 3) == '<')))) {
                context.setFileCopyPatcher(new DummyIdentifierPatcher("f"));
            }
    }*/
  }

  public CfmlCompletionContributor() {
    // TODO: check fileType somewhere

    // tag names completion in template data, in open and close constructions in cfml data
    CfmlTagNamesCompletionProvider tagNamesCompletionProvider = new CfmlTagNamesCompletionProvider();
    extend(BASIC, psiElement().afterLeaf(psiElement().withText("<")).withLanguage(XMLLanguage.INSTANCE),
           tagNamesCompletionProvider);
    extend(BASIC, psiElement().afterLeaf(psiElement().withText("<")).withLanguage(CfmlLanguage.INSTANCE),
           tagNamesCompletionProvider);
    extend(BASIC, psiElement().afterLeaf(psiElement().withText("</")).withLanguage(CfmlLanguage.INSTANCE),
           tagNamesCompletionProvider);
    extend(BASIC, psiElement().inside(xmlTag()), tagNamesCompletionProvider);

    // attributes completion
    extend(BASIC, psiElement().withElementType(CfmlTokenTypes.ATTRIBUTE).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlAttributeNamesCompletionProvider());
    // attributes completion in script based components
    extend(BASIC, psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER).withParent(CfmlAttribute.class).withLanguage(
      CfmlLanguage.INSTANCE),
           new CfmlAttributeNamesCompletionProvider());
    // attribute completion for script property
    extend(BASIC,
           psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER).withParent(CfmlProperty.class).withLanguage(
             CfmlLanguage.INSTANCE),
           new CfmlAttributeNamesCompletionProvider());
    //return type completion in script function definition
    final PatternCondition<PsiElement> withinTypeCondition = new PatternCondition<>("") {
      @Override
      public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
        return (psiElement.getParent() != null && psiElement.getParent().getNode().getElementType() == CfmlElementTypes.TYPE);
      }
    };
    extend(BASIC,
           psiElement().withParent(psiElement(CfmlElementTypes.TYPE))
             .withLanguage(CfmlLanguage.INSTANCE),
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               PsiElement position = parameters.getPosition();
               String text = position.getParent().getText();
               String[] attributeValues = text.indexOf('.') == -1 ?
                                          CfmlUtil.getAttributeValues("cffunction", "returntype", position.getProject()) :
                                          ArrayUtilRt.EMPTY_STRING_ARRAY;
               Set<LookupElement> lookupResult = ContainerUtil
                 .map2Set(attributeValues, argumentValue -> LookupElementBuilder.create(argumentValue).withCaseSensitivity(false));

               Object[] objects =
                 CfmlComponentReference.buildVariants(text, position.getContainingFile(), position.getProject(), null, false);
               for (Object o : objects) {
                 result.addElement((LookupElement)o);
               }
               result.addAllElements(lookupResult);
             }
           });
    // property word completion for script
    extend(BASIC,
           psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER).withSuperParent(2, CfmlComponent.class)
             .withLanguage(
               CfmlLanguage.INSTANCE).with(new PropertyPatternCondition()),
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               result.addElement(LookupElementBuilder.create("property").withCaseSensitivity(false));
             }
           });
    //attribute completion for script property without any attributes and ';' character//
    extend(BASIC, psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER),
           new CfmlAttributeNamesCompletionProvider());
    //  cfml createObject attribute completion
    extend(BASIC, psiElement().withElementType(CfmlTokenTypes.STRING_TEXT).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlArgumentValuesCompletionProvider());
    // predefined attributes values completion
    extend(BASIC, psiElement().withElementType(CfmlTokenTypes.STRING_TEXT).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlAttributeValuesCompletionProvider());
    // java class names completion
    extend(BASIC, psiElement().withElementType(CfmlTokenTypes.STRING_TEXT).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlJavaClassNamesCompletion());
    // predefined and user defined function names completion
    extend(BASIC, psiElement().
      withElementType(CfscriptTokenTypes.IDENTIFIER).
      withLanguage(CfmlLanguage.INSTANCE).
      with(new PatternCondition<>("") {
        @Override
        public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
          if (withinTypeCondition.accepts(psiElement, context)) return false;
          return !(psiElement.getParent() instanceof CfmlAttribute) && (psiElement.getPrevSibling() == null ||
                                                                        psiElement.getPrevSibling().getNode().getElementType() !=
                                                                        CfscriptTokenTypes.POINT);
        }
      }), new CfmlFunctionNamesCompletionProvider());
    // predefined variables completion
    extend(BASIC, psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlPredefinedVariablesCompletion());
  }

  private static class PropertyPatternCondition extends PatternCondition<PsiElement> {
    PropertyPatternCondition() {
      super("");
    }

    @Override
    public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
      boolean result = false;
      PsiElement parent = psiElement.getParent();
      ASTNode prevNode = FormatterUtil.getPreviousNonWhitespaceSibling(parent.getNode());
      if (prevNode != null) {
        result = prevNode.getElementType() == CfscriptTokenTypes.L_CURLYBRACKET;
        if (!result && prevNode.getElementType() == CfscriptTokenTypes.SEMICOLON) {
          ASTNode superPrevNode = FormatterUtil.getPreviousNonWhitespaceSibling(prevNode);
          result = superPrevNode != null && superPrevNode.getElementType() == CfmlElementTypes.PROPERTY;
        }
      }

      return result;
    }
  }
}
