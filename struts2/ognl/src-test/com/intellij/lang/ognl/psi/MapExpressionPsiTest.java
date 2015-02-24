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

package com.intellij.lang.ognl.psi;

import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.psi.PsiClassType;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.lang.annotations.Language;

import java.util.List;

/**
 * {@link com.intellij.lang.ognl.psi.OgnlSequenceExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class MapExpressionPsiTest extends PsiTestCase {

  public void testSingleMapEntryElement() {
    final OgnlMapExpression expression = parse("#{'key':aaa}");
    assertSize(1, expression.getMapEntryElementList());

    final OgnlMapEntryElement mapEntryElement = ContainerUtil.getFirstItem(expression.getMapEntryElementList());
    assertNotNull(mapEntryElement);
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, mapEntryElement.getKeyExpression());
    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, mapEntryElement.getValueExpression());
  }

  public void testSingleMapEntryElementWithSequenceValues() {
    final OgnlMapExpression expression = parse("#{'key':{1,2,3}}");
    assertSize(1, expression.getMapEntryElementList());

    final OgnlMapEntryElement mapEntryElement = ContainerUtil.getFirstItem(expression.getMapEntryElementList());
    assertNotNull(mapEntryElement);
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, mapEntryElement.getKeyExpression());
    final OgnlExpression valueExpression = mapEntryElement.getValueExpression();
    assertElementType(OgnlTypes.SEQUENCE_EXPRESSION, valueExpression);
    final OgnlSequenceExpression sequenceExpression = assertInstanceOf(valueExpression, OgnlSequenceExpression.class);
    assertSize(3, sequenceExpression.getElementsList());
  }

  public void testTwoMapEntryElements() {
    final OgnlMapExpression expression = parse("#{'key':aaa, 1 + 2:'value'}");
    final List<OgnlMapEntryElement> mapEntryElements = expression.getMapEntryElementList();
    assertSize(2, mapEntryElements);

    final OgnlMapEntryElement mapEntryElement = mapEntryElements.get(0);
    assertNotNull(mapEntryElement);
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, mapEntryElement.getKeyExpression());
    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, mapEntryElement.getValueExpression());

    final OgnlMapEntryElement mapEntryElement2 = mapEntryElements.get(1);
    assertNotNull(mapEntryElement2);
    assertElementType(OgnlTypes.BINARY_EXPRESSION, mapEntryElement2.getKeyExpression());
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, mapEntryElement2.getValueExpression());
  }

  public void testTypedMapExpression() {
    final OgnlMapExpression expression = parse("#@java.util.LinkedHashMap@{'key':aaa}");
    assertSize(1, expression.getMapEntryElementList());
    final OgnlFqnTypeExpression mapTypeExpression = expression.getMapType();
    assertNotNull(mapTypeExpression);
    final PsiClassType mapType = assertInstanceOf(mapTypeExpression.getType(), PsiClassType.class);
    assertEquals("java.util.LinkedHashMap", mapType.getCanonicalText());
  }

  private OgnlMapExpression parse(@Language(value = OgnlLanguage.ID,
                                            prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                            suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlMapExpression)parseSingleExpression(expression);
  }
}