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

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlFunctionCallExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.util.containers.ContainerUtil.map2Set;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 11/23/11
 */
public class CfmlArgumentValuesCompletionProvider extends CompletionProvider<CompletionParameters> {

  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters,
                                ProcessingContext context,
                                @NotNull CompletionResultSet result) {
    PsiElement element = parameters.getPosition();
    CfmlFunctionCallExpression parentOfType = PsiTreeUtil.getParentOfType(element, CfmlFunctionCallExpression.class);
    if (parentOfType == null || !parentOfType.isCreateObject()) {
      return;
    }

    PsiElement parent = element.getParent();
    PsiElement parentSibling = parent != null ? parent.getPrevSibling() : null;
    while (parentSibling != null && parentSibling.getNode().getElementType() == TokenType.WHITE_SPACE) {
      parentSibling = parentSibling.getPrevSibling();
    }
    if (parentSibling != null &&
        parent.getNode().getElementType() == CfmlElementTypes.STRING_LITERAL &&
        parentSibling.getNode().getElementType() == CfscriptTokenTypes.L_BRACKET) {
      result.addAllElements(map2Set(CfmlUtil.getCreateObjectArgumentValues(), new Function<String, LookupElement>() {
        public LookupElementBuilder fun(final String argumentValue) {
          return LookupElementBuilder.create(argumentValue).withCaseSensitivity(false);
        }
      })

      );
    }
  }
}
