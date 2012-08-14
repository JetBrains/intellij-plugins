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
package com.jetbrains.flask.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.flask.codeInsight.references.FlaskViewMethodReference;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyKeywordArgumentProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yole
 */
public class FlaskKeywordArgumentProvider implements PyKeywordArgumentProvider {
  @Override
  public List<String> getKeywordArguments(PyFunction function, PyCallExpression callExpr) {
    if (FlaskNames.URL_FOR.equals(function.getName()) && function.getContainingFile().getName().equals(FlaskNames.HELPERS_PY)) {
      PyExpression[] arguments = callExpr.getArguments();
      if (arguments.length > 0) {
        PsiReference[] references = arguments[0].getReferences();
        for (PsiReference reference : references) {
          if (reference instanceof FlaskViewMethodReference) {
            PsiElement result = reference.resolve();
            if (result instanceof PyFunction) {
              PyFunction viewFunction = (PyFunction)result;
              List<String> args = new ArrayList<String>();
              for (PyParameter parameter : viewFunction.getParameterList().getParameters()) {
                if (parameter instanceof PyNamedParameter) {
                  args.add(parameter.getName());
                }
              }
              return args;
            }
          }
        }
      }
    }
    return Collections.emptyList();
  }
}
