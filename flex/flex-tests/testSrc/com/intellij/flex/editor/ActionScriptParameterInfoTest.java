// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.editor;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.JSAnnotationParameterInfoHandler;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSParameterInfoFixture;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext;
import com.intellij.xml.XmlElementDescriptor;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSParameterInfoFixture.jsParameterInfoFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public final class ActionScriptParameterInfoTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSParameterInfoFixture> js =
    jsParameterInfoFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("parameterInfo/"), ".js"));

  @Test
  public void testRest() {
    final String s = js.get().defaultTestForFiles(js.get().getTestName(false) + ".js2");
    assertEquals("... rest", s);
  }

  @Test
  public void testShortType() {
    final String s = js.get().defaultTestForFiles(js.get().getTestName(false) + ".js2");
    assertEquals("x:TTT", s);
  }

  @Test
  public void testTypesInParameterInfo() {
    final MockParameterInfoUIContext context = js.get().defaultTestGetContext(js.get().getTestName(false) + ".js2");
    assertEquals("a:Object, b:Number, c:int = 1", js.get().getPlainText(context));
    assertEquals(0, context.getHighlightStart());
  }

  @Test
  public void testAnnotationParameters() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    Object[] objects = js.get().doTest("js2", true, js.get().getTestName(false) + ".js2");
    assertEquals(1, objects.length);
    assertTrue(objects[0] instanceof XmlElementDescriptor);

    XmlElementDescriptor attr = (XmlElementDescriptor)objects[0];
    final JSAnnotationParameterInfoHandler parameterInfoHandler = new JSAnnotationParameterInfoHandler();

    JSAttribute attribute =
      ReadAction.computeBlocking(() -> PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getCaretOffset()), JSAttribute.class));
    MockParameterInfoUIContext context = new MockParameterInfoUIContext<>(attribute);
    ReadAction.run(() -> parameterInfoHandler.updateUI(attr, context));
    assertEquals(
      " frameRate,   backgroundColor, height, heightPercent, pageTitle, quality, scaleMode, scriptRecursionLimit, scriptTimeLimit, width, widthPercent",
      js.get().getPlainText(context));
    assertEquals(12, context.getHighlightStart());
  }

  @Test
  public void testParameterInfoForSuperInvokation() {
    final MockParameterInfoUIContext context = js.get().defaultTestGetContext(js.get().getTestName(false) + ".js2");

    assertEquals("a:String", js.get().getPlainText(context));
    assertEquals(0, context.getHighlightStart());
  }

  @Test
  public void testGenericsAndQuotes() {
    js.get().checkParameterInfo("a:String = '', b:String = \"\", <b>v:Vector.&lt;String&gt; = null,</b> v2:Vector.&lt;int&gt; = null", ".as");
  }
}
