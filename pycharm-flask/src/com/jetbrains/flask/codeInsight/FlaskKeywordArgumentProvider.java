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

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.flask.codeInsight.references.FlaskViewMethodReference;
import com.jetbrains.python.PyNames;
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
      return getUrlForKeywordArguments(callExpr);
    }
    else if (FlaskNames.ROUTE.equals(function.getName())) {
      PyClass aClass = function.getContainingClass();
      if (aClass != null && FlaskNames.FLASK_CLASS.equals(aClass.getName())) {
        return getRouteKeywordArguments(function.getProject());
      }
    }
    return Collections.emptyList();
  }

  public static List<String> getRouteKeywordArguments(Project project) {
    PyClass ruleClass = PyPsiFacade.getInstance(project).findClass(FlaskNames.RULE_CLASS);
    if (ruleClass != null) {
      PyFunction initMethod = ruleClass.findMethodByName(PyNames.INIT, false);
      if (initMethod != null) {
        List<String> defaults = new ArrayList<String>();
        for (PyParameter parameter: initMethod.getParameterList().getParameters()) {
          if (parameter.getDefaultValue() != null) {
            defaults.add(parameter.getName());
          }
        }
        return defaults;
      }
    }
    return Collections.emptyList();
  }

  private static List<String> getUrlForKeywordArguments(PyCallExpression callExpr) {
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
    return Collections.emptyList();
  }
}
