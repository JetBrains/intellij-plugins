/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.ognl.psi.OgnlReferenceExpression;
import com.intellij.lang.ognl.psi.OgnlVariableExpression;
import com.intellij.psi.PsiElement;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlDocumentationProvider extends AbstractDocumentationProvider {

  @Override
  public String getQuickNavigateInfo(final PsiElement element, final PsiElement originalElement) {
    if (element instanceof OgnlReferenceExpression) {
      return "Reference " + ((OgnlReferenceExpression) element).getText();
    }

    // TODO fix
    if (element instanceof OgnlVariableExpression) {
      return "Variable " + ((OgnlVariableExpression) element).getText();
    }

    return null;
  }

}