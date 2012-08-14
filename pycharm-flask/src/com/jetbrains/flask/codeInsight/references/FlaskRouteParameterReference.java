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

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.flask.codeInsight.FlaskNames;
import com.jetbrains.flask.codeInsight.WerkzeugRoutingRule;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class FlaskRouteParameterReference extends PsiReferenceBase<PyStringLiteralExpression> {
  private final WerkzeugRoutingRule.Parameter myParameter;

  public FlaskRouteParameterReference(PyStringLiteralExpression literal, WerkzeugRoutingRule.Parameter parameter) {
    super(literal, parameter.psiTextRange);
    myParameter = parameter;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    PyFunction function = PsiTreeUtil.getParentOfType(getElement(), PyFunction.class);
    if (function == null) {
      return null;
    }
    return function.getParameterList().findParameterByName(myParameter.parameterName);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    String canonicalText = getCanonicalText();
    int index = canonicalText.indexOf(':');
    String existingConverter = "";
    if (index >= 0) {
      existingConverter = canonicalText.substring(0, index+1);
    }
    List<Object> variants = new ArrayList<Object>();
    PyFunction function = PsiTreeUtil.getParentOfType(getElement(), PyFunction.class);
    if (function != null) {
      for (PyParameter parameter : function.getParameterList().getParameters()) {
        if (parameter instanceof PyNamedParameter) {
          variants.add(LookupElementBuilder.create(parameter, existingConverter + parameter.getName())
                         .withIcon(parameter.getIcon(Iconable.ICON_FLAG_CLOSED)));
        }
      }
    }
    PyPsiFacade pyPsiFacade = PyPsiFacade.getInstance(getElement().getProject());
    PsiElement routingModule = pyPsiFacade.qualifiedNameResolver("werkzeug.routing").fromElement(getElement()).firstResult();
    if (routingModule instanceof PyFile) {
      PyTargetExpression converters = ((PyFile)routingModule).findTopLevelAttribute(FlaskNames.DEFAULT_CONVERTERS);
      if (converters != null) {
        PyExpression value = converters.findAssignedValue();
        if (value instanceof PyDictLiteralExpression) {
          for (PyKeyValueExpression expression : ((PyDictLiteralExpression)value).getElements()) {
            String converterName = PyPsiUtils.strValue(expression.getKey());
            if (converterName != null) {
              variants.add(TailTypeDecorator.withTail(LookupElementBuilder.create(converterName), TailType.createSimpleTailType(':')));
            }
          }
        }
      }
    }
    return variants.toArray();
  }
}
