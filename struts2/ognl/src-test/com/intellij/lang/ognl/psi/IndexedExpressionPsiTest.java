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

import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.psi.PsiTypes;
import org.intellij.lang.annotations.Language;

/**
 * {@link OgnlIndexedExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class IndexedExpressionPsiTest extends PsiTestCase {

  public void testIdentifierSimpleIntegerIndex() {
    final OgnlIndexedExpression indexedExpression = parse("identifier[0]");
    final OgnlExpression qualifier = indexedExpression.getReferenceQualifier();
    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, qualifier);
    assertNull(qualifier.getType());

    final OgnlExpression index = indexedExpression.getIndexExpression();
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, index);
    final OgnlLiteralExpression expression = assertInstanceOf(index, OgnlLiteralExpression.class);
    assertEquals("0", expression.getText());
    assertEquals(PsiTypes.intType(), expression.getType());
  }

  public void testIdentifierExpressionIntegerIndex() {
    final OgnlIndexedExpression indexedExpression = parse("identifier[1+2]");
    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, indexedExpression.getReferenceQualifier());

    final OgnlExpression index = indexedExpression.getIndexExpression();
    assertElementType(OgnlTypes.BINARY_EXPRESSION, index);
    assertEquals(PsiTypes.intType(), index.getType());
  }

  public void testVarSimpleIntegerIndex() {
    final OgnlIndexedExpression indexedExpression = parse("#var[0]");
    assertElementType(OgnlTypes.VARIABLE_EXPRESSION, indexedExpression.getReferenceQualifier());

    assertElementType(OgnlTypes.LITERAL_EXPRESSION, indexedExpression.getIndexExpression());
  }

  public void testVarExpressionIntegerIndex() {
    final OgnlIndexedExpression indexedExpression = parse("#var[1+2]");
    assertElementType(OgnlTypes.VARIABLE_EXPRESSION, indexedExpression.getReferenceQualifier());

    assertElementType(OgnlTypes.BINARY_EXPRESSION, indexedExpression.getIndexExpression());
  }

  public void testReferenceIndex() {
    final OgnlIndexedExpression indexedExpression = parse("identifier[length]");
    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, indexedExpression.getReferenceQualifier());

    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, indexedExpression.getIndexExpression());
  }

  public void testPropertyReferenceIndex() {
    final OgnlIndexedExpression indexedExpression = parse("identifier[\"length\"]");
    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, indexedExpression.getReferenceQualifier());

    assertElementType(OgnlTypes.LITERAL_EXPRESSION, indexedExpression.getIndexExpression());
  }

  public void testPropertyExpressionReferenceIndex() {
    final OgnlIndexedExpression indexedExpression = parse("identifier[\"len\" + \"gth\"]");
    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, indexedExpression.getReferenceQualifier());

    assertElementType(OgnlTypes.BINARY_EXPRESSION, indexedExpression.getIndexExpression());
  }

  private OgnlIndexedExpression parse(@Language(value = OgnlLanguage.ID,
                                                prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlIndexedExpression)parseSingleExpression(expression);
  }
}