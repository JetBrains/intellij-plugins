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

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyRecursiveElementVisitor;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import com.jetbrains.python.psi.resolve.PyResolveContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class FlaskTemplateManager {
  public static boolean isTemplateReference(PyStringLiteralExpression expr) {
    return isCallArgument(expr, 0, FlaskNames.RENDER_TEMPLATE, "templating.py");
  }

  public static boolean isCallArgument(PyStringLiteralExpression expr,
                                        int parameterIndex,
                                        String methodName,
                                        String filename) {
    PyCallExpression call = PsiTreeUtil.getParentOfType(expr, PyCallExpression.class);
    if (call != null) {
      int index = PyPsiUtils.findArgumentIndex(call, expr);
      if (index == parameterIndex && call.isCalleeText(methodName)) {
        PyCallExpression.PyMarkedCallee callee = call.resolveCallee(PyResolveContext.noImplicits());
        if (callee != null && callee.getCallable().getContainingFile().getName().equals(filename)) {
          return true;
        }
      }
    }
    return false;
  }

  public static List<PyStringLiteralExpression> collectTemplateReferences(PyFunction function) {
    final List<PyStringLiteralExpression> result = new ArrayList<PyStringLiteralExpression>();
    function.acceptChildren(new PyRecursiveElementVisitor() {
      @Override
      public void visitPyStringLiteralExpression(PyStringLiteralExpression node) {
        if (isTemplateReference(node)) {
          result.add(node);
        }
      }
    });
    return result;
  }

  public static PsiDirectory getTemplatesDirectory(PsiElement element) {
    PsiDirectory directory = element.getContainingFile().getContainingDirectory();
    return directory.findSubdirectory(FlaskNames.TEMPLATES);
  }
}
