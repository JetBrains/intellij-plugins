/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.intellij.terraform.hil;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightPlatformTestCase;
import org.intellij.terraform.config.model.Type;
import org.intellij.terraform.config.model.Types;
import org.intellij.terraform.hil.psi.ILExpression;
import org.intellij.terraform.hil.psi.TypeCachedValueProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HILTypesResolvingTest extends LightPlatformTestCase {

  public void testExpressionHolder() throws Exception {
    doTypeResolveTest("${}", Types.INSTANCE.getAny());
    doTypeResolveTest("${1}", Types.INSTANCE.getNumber());
    doTypeResolveTest("${\"text\"}", Types.INSTANCE.getString());
  }

  public void testSimpleLiterals() throws Exception {
    doTypeResolveTest("1", Types.INSTANCE.getNumber());
    doTypeResolveTest("1.0", Types.INSTANCE.getNumber());
    doTypeResolveTest("true", Types.INSTANCE.getBoolean());
    doTypeResolveTest("false", Types.INSTANCE.getBoolean());
    doTypeResolveTest("\"\"", Types.INSTANCE.getString());
    doTypeResolveTest("\"string\"", Types.INSTANCE.getString());
  }

  public void testUnaryExpression() throws Exception {
    doTypeResolveTest("!true", Types.INSTANCE.getBoolean());
    doTypeResolveTest("!false", Types.INSTANCE.getBoolean());
    doTypeResolveTest("!!true", Types.INSTANCE.getBoolean());
    doTypeResolveTest("!!false", Types.INSTANCE.getBoolean());

    doTypeResolveTest("-1", Types.INSTANCE.getNumber());
    doTypeResolveTest("+1", Types.INSTANCE.getNumber());
    // TODO: Should we allow that: ?
    doTypeResolveTest("--1", Types.INSTANCE.getNumber());
    doTypeResolveTest("++1", Types.INSTANCE.getNumber());

    // Unfinished
    doTypeResolveTest("true || ", Types.INSTANCE.getBoolean());
  }

  public void testBinaryLogicalExpression() throws Exception {
    doTypeResolveTest("true || false", Types.INSTANCE.getBoolean());
    doTypeResolveTest("true && true", Types.INSTANCE.getBoolean());

    // Unfinished
    doTypeResolveTest("true || ", Types.INSTANCE.getBoolean());
  }

  public void testBinaryArithmeticalExpression() throws Exception {
    doTypeResolveTest("1 + 1", Types.INSTANCE.getNumber());
    doTypeResolveTest("1 - 1", Types.INSTANCE.getNumber());
    doTypeResolveTest("5 * 2", Types.INSTANCE.getNumber());
    doTypeResolveTest("10 / 2", Types.INSTANCE.getNumber());
    doTypeResolveTest("10 % 5", Types.INSTANCE.getNumber());
    doTypeResolveTest("1 + 1.0", Types.INSTANCE.getNumber());
    doTypeResolveTest("1 - 1.0", Types.INSTANCE.getNumber());
    doTypeResolveTest("5 * 2.0", Types.INSTANCE.getNumber());
    doTypeResolveTest("10 / 2.0", Types.INSTANCE.getNumber());

    doTypeResolveTest("1 + 1 * 2", Types.INSTANCE.getNumber());

    // Unfinished
    doTypeResolveTest("1 + ", Types.INSTANCE.getNumber());
  }

  public void testBinaryNumericalCompareExpression() throws Exception {
    doTypeResolveTest("1 < 2", Types.INSTANCE.getBoolean());
    doTypeResolveTest("1 > 2", Types.INSTANCE.getBoolean());
    doTypeResolveTest("1 <= 2", Types.INSTANCE.getBoolean());
    doTypeResolveTest("1 >= 2", Types.INSTANCE.getBoolean());

    // Unfinished
    doTypeResolveTest("1 >= ", Types.INSTANCE.getBoolean());
  }

  public void testBinaryEqualsExpression() throws Exception {
    doTypeResolveTest("1 == 1", Types.INSTANCE.getBoolean());
    doTypeResolveTest("1 != 2", Types.INSTANCE.getBoolean());
    doTypeResolveTest("true == false", Types.INSTANCE.getBoolean());
    doTypeResolveTest("true != false", Types.INSTANCE.getBoolean());
    doTypeResolveTest("1 == \"1\"", Types.INSTANCE.getBoolean());
    doTypeResolveTest("1 != \"1\"", Types.INSTANCE.getBoolean());
    doTypeResolveTest("\"1\" == \"1\"", Types.INSTANCE.getBoolean());
    doTypeResolveTest("\"1\" != \"2\"", Types.INSTANCE.getBoolean());

    // Unfinished
    doTypeResolveTest("1 == ", Types.INSTANCE.getBoolean());
  }

  public void testConditionalExpression() throws Exception {
    doTypeResolveTest("true?true:false", Types.INSTANCE.getBoolean());
    doTypeResolveTest("true?1:2", Types.INSTANCE.getNumber());
    doTypeResolveTest("true?\"1\":\"2\"", Types.INSTANCE.getString());

    // Weird type conversion:
    doTypeResolveTest("true?1:\"2\"", Types.INSTANCE.getNumber()); // False converted to type of true
    doTypeResolveTest("true?\"1\":2", Types.INSTANCE.getNumber()); // True converted to type of false since true type is string

    // Unfinished
    doTypeResolveTest("true?:", null);
    doTypeResolveTest("true?1:", Types.INSTANCE.getNumber());
    doTypeResolveTest("true?:1", Types.INSTANCE.getNumber());
  }

  public void testParenthesesAroundSimpleExpressions() throws Exception {
    doTypeResolveTest("()", Types.INSTANCE.getAny());
    doTypeResolveTest("(\"\")", Types.INSTANCE.getString());
    doTypeResolveTest("(1)", Types.INSTANCE.getNumber());
    doTypeResolveTest("(true)", Types.INSTANCE.getBoolean());
  }

  public void testParenthesesAroundComplexExpressions() throws Exception {
    doTypeResolveTest("(1 + 1)", Types.INSTANCE.getNumber());
    doTypeResolveTest("(1 == 1)", Types.INSTANCE.getBoolean());
    doTypeResolveTest("(true || false)", Types.INSTANCE.getBoolean());
  }

  private void doTypeResolveTest(@NotNull String text, @Nullable final Type expected) {
    if (!text.startsWith("${") && !text.endsWith("}")) {
      text = "${" + text + "}";
    }
    final PsiFile psiFile = PsiFileFactory.getInstance(getProject()).createFileFromText("a.hil", HILFileType.INSTANCE, text);
    assertEquals(HILLanguage.INSTANCE, psiFile.getLanguage());
    PsiElement root = psiFile.getFirstChild();
    assertNotNull(root);
    assertInstanceOf(root, ILExpression.class);
    Type type = TypeCachedValueProvider.Companion.getType((ILExpression) root);
    assertEquals(type, expected);
  }
}
