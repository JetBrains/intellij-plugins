/*
 * Copyright 2014 The authors
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

package com.intellij.lang.ognl.psi;

import com.intellij.lang.ognl.OgnlFile;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.OgnlTestUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.util.IncorrectOperationException;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;

/**
 * Basic PSI-building "happy-path" test case.
 *
 * @author Yann C&eacute;bron
 */
public abstract class PsiTestCase extends LightIdeaTestCase {

  protected OgnlExpression parseSingleExpression(@Language(value = OgnlLanguage.ID,
                                                           prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                           suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String text) {
    final PsiElement expression = doParse(text);

    if (!(expression instanceof OgnlExpression) ||
        PsiTreeUtil.getChildrenOfType(expression, PsiErrorElement.class) != null) {
      fail(text + " parsed with errors:\n" + DebugUtil.psiToString(expression, false));
    }

    return assertInstanceOf(expression, OgnlExpression.class);
  }

  private PsiElement doParse(@Language(value = OgnlLanguage.ID,
                                              prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                              suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String text) {
    final OgnlFile ognlFile = createFile(OgnlTestUtils.createExpression(text));
    assertNotNull(ognlFile);

    final PsiElement firstChild = ognlFile.getFirstChild();
    assertNotNull(firstChild);
    assertEquals(OgnlLanguage.INSTANCE, firstChild.getLanguage());

    final PsiElement expression = firstChild.getNextSibling();
    assertNotNull(expression);
    return expression;
  }

  @Contract("_, null -> fail")
  protected static void assertElementType(final IElementType expectedType,
                                          final OgnlExpression expression) {
    assertNotNull(expression);
    assertEquals(expectedType, expression.getNode().getElementType());
  }

  private OgnlFile createFile(final String text) throws IncorrectOperationException {
    return (OgnlFile)PsiFileFactory.getInstance(getProject())
      .createFileFromText("test.ognl", OgnlLanguage.INSTANCE, text);
  }
}