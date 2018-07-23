package com.intellij.tapestry.tests;

import com.intellij.codeInsight.hint.api.impls.XmlParameterInfoHandler;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.utils.parameterInfo.MockCreateParameterInfoContext;
import com.intellij.testFramework.utils.parameterInfo.MockParameterInfoUIContext;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.util.XmlUtil;

/**
 * @author Alexey.Chmutov
 */
public class TapestryParamInfoTest extends TapestryBaseTestCase {
  public void testTmlTagAttrs() {
    addComponentToProject("Count");
    doTest();
  }

  public void testHtmlTagAttrs() {
    final ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();
    final String doctype = manager.getDefaultHtmlDoctype(myFixture.getProject());
    manager.setDefaultHtmlDoctype(XmlUtil.XHTML_URI, myFixture.getProject());
    try {
      addComponentToProject("Count");
      doTest();
    }
    finally {
      manager.setDefaultHtmlDoctype(doctype, myFixture.getProject());
    }
  }

  private void doTest() {
    initByComponent();

    XmlParameterInfoHandler handler = new XmlParameterInfoHandler();
    MockCreateParameterInfoContext createContext = new MockCreateParameterInfoContext(myFixture.getEditor(), myFixture.getFile());
    XmlTag tag = handler.findElementForParameterInfo(createContext);
    assertNotNull(tag);
    handler.showParameterInfo(tag, createContext);
    Object[] items = createContext.getItemsToShow();
    assertTrue(items != null);
    assertTrue(items.length > 0);
    final XmlElementDescriptor descriptor = (XmlElementDescriptor)items[0];
    MockParameterInfoUIContext context = new MockParameterInfoUIContext<PsiElement>(tag);
    handler.updateUI(descriptor, context);
  }

  @Override
  protected String getBasePath() {
    return "parameterInfo/";
  }
}