// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.editor;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.JSAnnotationParameterInfoHandler;
import com.intellij.lang.javascript.JSParameterInfoTestBase;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext;
import com.intellij.xml.XmlElementDescriptor;

public final class ActionScriptParameterInfoTest extends JSParameterInfoTestBase {

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("parameterInfo/");
  }

  public void testRest() {
    final String s = defaultTestForFiles(getTestName(false) + ".js2");
    assertEquals("... rest", s);
  }

  public void testShortType() {
    final String s = defaultTestForFiles(getTestName(false) + ".js2");
    assertEquals("x:TTT", s);
  }

  public void testTypesInParameterInfo() {
    final MockParameterInfoUIContext context = defaultTestGetContext(getTestName(false) + ".js2");
    assertEquals("a:Object, b:Number, c:int = 1", getPlainText(context));
    assertEquals(0, context.getHighlightStart());
  }

  public void testAnnotationParameters() {
    Object[] objects = doTest("js2", true, getTestName(false) + ".js2");
    assertEquals(1, objects.length);
    assertTrue(objects[0] instanceof XmlElementDescriptor);

    XmlElementDescriptor attr = (XmlElementDescriptor)objects[0];
    final JSAnnotationParameterInfoHandler parameterInfoHandler = new JSAnnotationParameterInfoHandler();

    JSAttribute attribute = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getCaretOffset()), JSAttribute.class);
    MockParameterInfoUIContext context = new MockParameterInfoUIContext<>(attribute);
    parameterInfoHandler.updateUI(attr, context);
    assertEquals(
      " frameRate,   backgroundColor, height, heightPercent, pageTitle, quality, scaleMode, scriptRecursionLimit, scriptTimeLimit, width, widthPercent",
      getPlainText(context));
    assertEquals(12, context.getHighlightStart());
  }

  public void testParameterInfoForSuperInvokation() {
    final MockParameterInfoUIContext context = defaultTestGetContext(getTestName(false) + ".js2");

    assertEquals("a:String", getPlainText(context));
    assertEquals(0, context.getHighlightStart());
  }

  public void testGenericsAndQuotes() {
    checkParameterInfo("a:String = '', b:String = \"\", <b>v:Vector.&lt;String&gt; = null,</b> v2:Vector.&lt;int&gt; = null", ".as");
  }
}
