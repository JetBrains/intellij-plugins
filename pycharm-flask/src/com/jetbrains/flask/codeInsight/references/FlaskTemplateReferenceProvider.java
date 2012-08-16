/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.jetbrains.flask.codeInsight.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.flask.codeInsight.FlaskNames;
import com.jetbrains.flask.codeInsight.FlaskTemplateManager;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.templateLanguages.psi.TemplateFunctionCall;
import com.jetbrains.python.templateLanguages.psi.TemplateStringLiteral;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author yole
 */
public class FlaskTemplateReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    TemplateStringLiteral literal = (TemplateStringLiteral) element;
    TemplateFunctionCall call = PsiTreeUtil.getParentOfType(literal, TemplateFunctionCall.class);
    if (call != null) {
      PsiElement callee = call.getCallee();
      if (callee != null && callee.getText().equals(FlaskNames.URL_FOR)) {
        FlaskViewMethodReference viewMethodReference = new FlaskViewMethodReference(literal) {
          @Override
          protected PsiFile getViewFunctionsFile() {
            PsiFile templateFile = getElement().getContainingFile().getOriginalFile();
            List<PyStringLiteralExpression> references = FlaskTemplateManager.findTemplateReferences(templateFile);
            if (references.size() > 0) {
              return references.get(0).getContainingFile();
            }
            return super.getViewFunctionsFile();
          }
        };
        return new PsiReference[] { viewMethodReference };
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }
}
