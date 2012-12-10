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
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.flask.codeInsight.FlaskNames;
import com.jetbrains.flask.codeInsight.FlaskTemplateManager;
import com.jetbrains.flask.codeInsight.WerkzeugRoutingRule;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.templateLanguages.PyTemplateFileReferenceSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class FlaskReferenceProvider extends PsiReferenceProvider {

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    PyStringLiteralExpression stringLiteral = (PyStringLiteralExpression)element;
    if (FlaskTemplateManager.isTemplateReference(stringLiteral)) {
      return new PyTemplateFileReferenceSet(stringLiteral).getAllReferences();
    }
    else if (FlaskTemplateManager.isCallArgument(stringLiteral, 0, FlaskNames.URL_FOR, FlaskNames.HELPERS_PY)) {
      return new PsiReference[] { new FlaskViewMethodReference(stringLiteral) };
    }
    else if (isRouteDecoratorArgument(stringLiteral)) {
      WerkzeugRoutingRule rule = WerkzeugRoutingRule.parse(stringLiteral);
      List<PsiReference> refs = new ArrayList<PsiReference>();
      for (WerkzeugRoutingRule.Parameter parameter : rule.parameters) {
        refs.add(new FlaskRouteParameterReference(stringLiteral, parameter));
      }
      return refs.toArray(new PsiReference[refs.size()]);
    }
    return PsiReference.EMPTY_ARRAY;
  }

  private static boolean isRouteDecoratorArgument(PyStringLiteralExpression literal) {
    PyDecorator decorator = PsiTreeUtil.getParentOfType(literal, PyDecorator.class);
    if (decorator != null && FlaskViewMethodReference.isRouteDecorator(decorator)) {
      PyExpression[] arguments = decorator.getArguments();
      if (arguments.length > 0 && literal == arguments[0]) {
        return true;
      }
    }
    return false;
  }
}
