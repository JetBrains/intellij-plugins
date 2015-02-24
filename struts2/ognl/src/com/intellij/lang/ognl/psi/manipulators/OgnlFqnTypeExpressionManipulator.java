/*
 * Copyright 2015 The authors
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

package com.intellij.lang.ognl.psi.manipulators;

import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.psi.OgnlFqnTypeExpression;
import com.intellij.lang.ognl.psi.OgnlNewExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class OgnlFqnTypeExpressionManipulator extends AbstractElementManipulator<OgnlFqnTypeExpression> {

  @Override
  public OgnlFqnTypeExpression handleContentChange(@NotNull OgnlFqnTypeExpression element, @NotNull TextRange range, String newContent)
    throws IncorrectOperationException {

    String newQualifiedText = range.replace(element.getText(), newContent);
    PsiFile file = PsiFileFactory.getInstance(element.getProject())
      .createFileFromText("foo", OgnlFileType.INSTANCE,
                          OgnlLanguage.EXPRESSION_PREFIX +
                          "new " + newQualifiedText + "()" +
                          OgnlLanguage.EXPRESSION_SUFFIX);
    final PsiElement newExpression = file.getChildren()[1];
    final OgnlFqnTypeExpression newFqnExpression = ((OgnlNewExpression)newExpression).getObjectType();
    assert newFqnExpression != null;

    return (OgnlFqnTypeExpression)element.replace(newFqnExpression);
  }
}
