// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.names.JSNameSuggestionsUtil;
import com.intellij.lang.javascript.names.JSNamedEntityKind;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import org.hamcrest.core.Is;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.Collections;
import java.util.List;

public class ActionScriptNameSuggestionTest extends BasePlatformTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(FlexTestUtils.getTestDataPath("/refactoring/introduce/nameSuggestion"));
  }

  public void testIntroduceSuggestNiceNames() {
    String[] strings = getNamesForIntroduce("js2");
    assertTrue(strings != null && strings.length > 0);
    final String selectedName = "year2";
    assertTrue(ArrayUtil.indexOf(strings, selectedName) >= 0);
    assertTrue(ArrayUtil.indexOf(strings, "fullYear2") >= 0);
    assertTrue(ArrayUtil.indexOf(strings, "2") < 0);
  }

  public void testIntroduceSuggestNiceNames2() {
    String[] strings = getNamesForIntroduce("js2");
    final String selectedName = "queryBuilder";
    assertTrue(ArrayUtil.indexOf(strings, selectedName) >= 0);
    assertTrue(ArrayUtil.indexOf(strings, "builder") >= 0);
    assertTrue(ArrayUtil.indexOf(strings, "simpleQueryBuilder") >= 0);
  }

  public void testIntroduceSuggestNiceNames3() {
    Assert.assertThat(ArrayUtil.getFirstElement(getNamesForIntroduce("js2")), Is.is("listOfFunctions"));
  }

  public void testIntroduceSuggestNiceNames4() {
    Assert.assertThat(getNamesForIntroduce("js2")[1], Is.is("xxx1"));
  }

  public void testIntroduceSuggestNiceNames4_2() {
    Assert.assertThat(getNamesForIntroduce("js2")[1], Is.is("xxx"));
  }

  public void testIntroduceSuggestNiceNames5() {
    Assert.assertThat(getNamesForIntroduce("js2")[0], Is.is("strings"));
  }

  public void testIntroduceSuggestNiceNames6() {
    Assert.assertThat(getNamesForIntroduce("js2")[0], Is.is("xxx"));
  }

  public void testIntroduceSuggestNiceNames8() {
    Assert.assertThat(getNamesForIntroduce("js2")[0], Is.is("bar"));
  }

  public void testIntroduceSuggestNiceNames8_2() {
    Assert.assertThat(getNamesForIntroduce("js2")[0], Is.is("bar"));
  }

  public void testIntroduceSuggestNiceNames9() {
    String[] strings = getNamesForIntroduce("as");
    assertSameElements(strings, "iosHandler", "handler", "url");
  }

  private String[] getNamesForIntroduce(String extension) {
    return getNamesForIntroduce(extension, "", JSNamedEntityKind.Variable);
  }

  private String @NotNull [] getNamesForIntroduce(String extension, String prefix, JSNamedEntityKind kind) {
    myFixture.configureByFile(getTestName(false) + "." + extension);
    JSExpression expression = getSelectedExpression();
    JSElement scope = getScope(expression);
    List<String> strings =
      JSNameSuggestionsUtil.generateVariableNamesFromExpression(expression, scope, prefix, Collections.emptyList(),
                                                                kind);
    return ArrayUtilRt.toStringArray(strings);
  }

  private static JSElement getScope(JSExpression expression) {
    return PsiTreeUtil.getParentOfType(expression, JSFunction.class, JSFile.class);
  }

  private @NotNull JSExpression getSelectedExpression() {
    int selectionStart = myFixture.getEditor().getSelectionModel().getSelectionStart();
    int selectionEnd = myFixture.getEditor().getSelectionModel().getSelectionEnd();
    JSExpression expression = PsiTreeUtil.findElementOfClassAtRange(myFixture.getFile(), selectionStart, selectionEnd, JSExpression.class);
    Assert.assertNotNull(String.format("Expected expression between selection markers at offsets %s and %s, but was null", selectionStart, selectionEnd), expression);
    return expression;
  }
}
