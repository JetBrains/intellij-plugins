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
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyPsiFacade;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class FlaskTypeProvider extends PyTypeProviderBase {
  @Override
  public PyType getReferenceType(@NotNull PsiElement referenceTarget, TypeEvalContext context, @Nullable PsiElement anchor) {
    if (referenceTarget instanceof PyTargetExpression) {
      PyTargetExpression target = (PyTargetExpression)referenceTarget;
      if (FlaskNames.REQUEST.equals(target.getName()) && target.getContainingFile().getName().equals(FlaskNames.GLOBALS_PY)) {
        PyPsiFacade pyPsiFacade = PyPsiFacade.getInstance(referenceTarget.getProject());
        PyClass requestClass = pyPsiFacade.findClass(FlaskNames.REQUEST_CLASS);
        if (requestClass != null) {
          return pyPsiFacade.createClassType(requestClass, false);
        }
      }
    }
    return super.getReferenceType(referenceTarget, context, anchor);
  }
}
