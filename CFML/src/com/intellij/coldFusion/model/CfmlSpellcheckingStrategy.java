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
package com.intellij.coldFusion.model;

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.coldFusion.model.psi.impl.CfmlNamedAttributeImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagComponentImpl;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.spellchecker.inspections.TextSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.spellchecker.tokenizer.TokenizerBase;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 */
public class CfmlSpellcheckingStrategy extends SpellcheckingStrategy {
  private final Tokenizer<LeafPsiElement> myCfmlCommentTokenizer = TokenizerBase.create(CfmlCommentSplitter.INSTANCE);

  @NotNull
  @Override
  public Tokenizer getTokenizer(PsiElement element) {
    if (element instanceof CfmlStringLiteralExpression) {
      return new Tokenizer() {
        @Override
        public void tokenize(@NotNull final PsiElement element, @NotNull TokenConsumer consumer) {
          consumer.consumeToken(element, new TextSplitter() {
            @Override
            public void split(@Nullable String text, @NotNull TextRange range, Consumer<TextRange> consumer) {
              int elementOffset = element.getTextRange().getStartOffset();
              for (PsiElement e = element.getFirstChild(); e != null; e = e.getNextSibling()) {
                ASTNode astNode = e.getNode();
                if (astNode == null) continue;
                IElementType type = astNode.getElementType();
                // cut out all sharped expressions and CIDs
                if (type == CfmlTokenTypes.STRING_TEXT) {
                  doSplit(text, astNode.getTextRange().shiftRight(-elementOffset), consumer);
                }
              }
            }
          });
        }
      };
    }
    else if (element instanceof CfmlReferenceExpression) {
      final PsiElement parent = element.getParent();
      if (parent instanceof CfmlAssignmentExpression) {
        CfmlAssignmentExpression assignment = (CfmlAssignmentExpression)parent;
        CfmlVariable var = assignment.getAssignedVariable();
        if (var != null && assignment.getAssignedVariableElement() == element) {
          return TEXT_TOKENIZER;
        }
      }
    }
    else if (element instanceof CfmlTagComponentImpl) {
      return EMPTY_TOKENIZER;
    } else if (element instanceof CfmlNamedAttributeImpl && 
               "name".equals(((CfmlNamedAttributeImpl)element).getAttributeName())) {
      return new TokenizerBase(TextSplitter.getInstance());
    }

    else if (element instanceof LeafPsiElement && ((LeafPsiElement)element).getElementType() == CfmlTokenTypes.COMMENT) {
      return myCfmlCommentTokenizer;
    }
    return super.getTokenizer(element);
  }
}
