// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.codeInsight.hint.ImplementationViewComponent;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

public class DartImplementationsViewTest extends DartCodeInsightFixtureTestCase {
  private void doTest(String fileText, String expected) {
    myFixture.configureByText("foo.dart", fileText);
    PsiElement element = TargetElementUtil.findTargetElement(myFixture.getEditor(), TargetElementUtil.getInstance().getAllAccepted());
    assertNotNull(element);
    assertEquals(expected, ImplementationViewComponent.getNewText(element));
  }

  public void testClass() {
    doTest("class\nFoo<caret> {\nvar a;\n}; //comment1\n//comment2", "class\nFoo {\nvar a;\n}; //comment1");
  }

  public void testVarInit() {
    doTest("class Foo {\n" +
           "  static final Bar _bar<caret> = const Bar(\n" +
           "    zoom: 11.0,\n" +
           "  );\n" +
           "}",
           "  static final Bar _bar = const Bar(\n" +
           "    zoom: 11.0,\n" +
           "  );");
  }

  public void testVarInit2() {
    doTest("class Foo {\n" +
           "  static var a,\n" +
           "      <caret>b\n" +
           "      =\n" +
           "      1\n" +
           "  ;\n" +
           "}\n",
           "      b\n" +
           "      =\n" +
           "      1");
  }
}
