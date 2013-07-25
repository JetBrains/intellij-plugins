/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
import com.intellij.coldFusion.model.psi.CfmlProperty;
import com.intellij.coldFusion.model.psi.impl.CfmlFunctionImpl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.intellij.codeInsight.completion.CompletionType.BASIC;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * Created by Lera Nikolaenko
 * Date: 09.10.2008
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
    else {/*
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
            */
    }
  }

  @Override
  public String handleEmptyLookup(@NotNull CompletionParameters parameters, Editor editor) {
    return super.handleEmptyLookup(parameters, editor);    //To change body of overridden methods use File | Settings | File Templates.
  }

  public CfmlCompletionContributor() {
    // TODO: check fileType somewhere

    // tag names completion in template data, in open and close constructions in cfml data
    CfmlTagNamesCompletionProvider tagNamesCompletionProvider = new CfmlTagNamesCompletionProvider();
    extend(BASIC, PlatformPatterns.psiElement().afterLeaf(psiElement().withText("<")).withLanguage(StdLanguages.XML),
           tagNamesCompletionProvider);
    extend(BASIC, PlatformPatterns.psiElement().afterLeaf(psiElement().withText("<")).withLanguage(CfmlLanguage.INSTANCE),
           tagNamesCompletionProvider);
    extend(BASIC, PlatformPatterns.psiElement().afterLeaf(psiElement().withText("</")).withLanguage(CfmlLanguage.INSTANCE),
           tagNamesCompletionProvider);
    extend(BASIC, psiElement().inside(xmlTag()), tagNamesCompletionProvider);

    // attributes completion
    extend(BASIC, PlatformPatterns.psiElement().withElementType(CfmlTokenTypes.ATTRIBUTE).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlAttributeNamesCompletionProvider());
    // attributes completion in script based components
    extend(BASIC, PlatformPatterns.psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER).withParent(CfmlAttribute.class).withLanguage(
      CfmlLanguage.INSTANCE),
           new CfmlAttributeNamesCompletionProvider());
    // attribute completion for script property
    extend(BASIC,
           PlatformPatterns.psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER).withParent(CfmlProperty.class).withLanguage(
             CfmlLanguage.INSTANCE),
           new CfmlAttributeNamesCompletionProvider());
    //return type completion in script function definition
    extend(BASIC,
           PlatformPatterns.psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER).withSuperParent(2, CfmlFunctionImpl.class)
             .withLanguage(
               CfmlLanguage.INSTANCE).with(
             new PatternCondition<PsiElement>("") {
               public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
                 return (psiElement.getParent() != null && psiElement.getParent().getNode().getElementType() == CfmlElementTypes.TYPE);
               }
             }),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               String[] attributeValues = CfmlUtil.getAttributeValues("cffunction", "returntype", parameters.getPosition().getProject());
               Set lookupResult = ContainerUtil.map2Set(attributeValues, new Function<String, LookupElement>() {
                 public LookupElementBuilder fun(final String argumentValue) {
                   return LookupElementBuilder.create(argumentValue).withCaseSensitivity(false);
                 }
               });
               result.addAllElements(lookupResult);
             }
           });
    // property word completion for script
    extend(BASIC,
           psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER).withSuperParent(2, CfmlComponent.class)
             .withLanguage(
               CfmlLanguage.INSTANCE).with(new PropertyPatternCondition()),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               result.addElement(LookupElementBuilder.create("property").withCaseSensitivity(false));
             }
           });
    //attribute completion for script property without any attributes and ';' character//
    extend(BASIC, PlatformPatterns.psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER),
           new CfmlAttributeNamesCompletionProvider());
    //  cfml createObject attribute completion
    extend(BASIC, PlatformPatterns.psiElement().withElementType(CfmlTokenTypes.STRING_TEXT).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlArgumentValuesCompletionProvider());
    // predefined attributes values completion
    extend(BASIC, PlatformPatterns.psiElement().withElementType(CfmlTokenTypes.STRING_TEXT).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlAttributeValuesCompletionProvider());
    // java class names completion
    extend(BASIC, PlatformPatterns.psiElement().withElementType(CfmlTokenTypes.STRING_TEXT).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlJavaClassNamesCompletion());
    // predefined and user defined function names completion
    extend(BASIC, PlatformPatterns.psiElement().
      withLanguage(CfmlLanguage.INSTANCE).
      withElementType(CfscriptTokenTypes.IDENTIFIER).
      with(new PatternCondition<PsiElement>("") {
        public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
          return !(psiElement.getParent() instanceof CfmlAttribute) && (psiElement.getPrevSibling() == null ||
                                                                        psiElement.getPrevSibling().getNode().getElementType() !=
                                                                        CfscriptTokenTypes.POINT);
        }
      }), new CfmlFunctionNamesCompletionProvider());
    // predefined variables completion
    extend(BASIC, PlatformPatterns.psiElement().withElementType(CfscriptTokenTypes.IDENTIFIER).withLanguage(CfmlLanguage.INSTANCE),
           new CfmlPredefinedVariablesCompletion());
  }

  private static class PropertyPatternCondition extends PatternCondition<PsiElement> {
    public PropertyPatternCondition() {
      super("");
    }

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
