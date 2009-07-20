/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.tapestry.tests;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.testFramework.UsefulTestCase;
import junit.framework.Assert;
import org.jetbrains.annotations.NonNls;

/**
 * @author Alexey Chmutov
 */
public class TapestryCompletionTest extends TapestryBaseTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testTagNameInHtmlParent() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(mergeArrays(CORE_5_1_0_5_ELEMENT_NAMES, "head", "body", getElementTagName()));
  }

  public void testTagNameInTmlParent() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(
        mergeArrays(CORE_5_1_0_5_ELEMENT_NAMES, "base", "isindex", "link", "meta", "object", "script", "style", "title",
                    getElementTagName()));

  }

  public void testAttrNameInHtmlParent() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants("accesskey", "charset", "class", "coords", "dir", "href", "hreflang", "id", "lang", "name", "onblur",
                                  "onclick", "ondblclick", "onfocus", "onkeydown", "onkeypress", "onkeyup", "onmousedown", "onmousemove",
                                  "onmouseout", "onmouseover", "onmouseup", "rel", "rev", "shape", "style", "tabindex", "target", "title",
                                  "type", "t:type", "t:id");

  }

  public void testAttrNameInTmlParent() throws Throwable {
    initByComponent();
    addComponentToProject("Count");
    doTestBasicCompletionVariants("class", "dir", "end", "id", "lang", "onclick", "ondblclick", "onkeydown", "onkeypress", "onkeyup",
                                  "onmousedown", "onmousemove", "onmouseout", "onmouseover", "onmouseup", "start", "style", "title",
                                  "value");

  }

  public void testRootTagName() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(mergeArrays(HTML_AND_CORE_5_1_0_5_ELEMENT_NAMES, getElementTagName()));
  }

  public void testTagNameWithinTmlRootTag() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(mergeArrays(HTML_AND_CORE_5_1_0_5_ELEMENT_NAMES, getElementTagName()));
  }

  public void testTagNameWithoutHtmlContext() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(mergeArrays(HTML_AND_CORE_5_1_0_5_ELEMENT_NAMES, getElementTagName()));
  }

  public void testInvalidTagName() throws Throwable {
    initByComponent();
    UsefulTestCase.assertEmpty(myFixture.complete(CompletionType.BASIC));
    UsefulTestCase.assertEmpty(myFixture.getLookupElementStrings());
  }

  public void testTagNameWithDoctypePresent() throws Throwable {
    //initByComponent();
    //doTestBasicCompletionVariants(mergeArrays(CORE_5_1_0_5_ELEMENT_NAMES, "comment", getElementTagName()));
  }

  private void doTestBasicCompletionVariants(@NonNls String... expectedItems) throws Throwable {
    doTestCompletionVariants(CompletionType.BASIC, expectedItems);
  }

  private void doTestCompletionVariants(final CompletionType type, @NonNls String... expectedItems) throws Throwable {
    final LookupElement[] items = myFixture.complete(type);
    Assert.assertNotNull("No lookup was shown, probably there was only one lookup element that was inserted automatically", items);
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings(), expectedItems);
  }

  @Override
  protected String getBasePath() {
    return "completion/";
  }

  public static final String[] CORE_5_1_0_5_ELEMENT_NAMES =
      {"t:actionlink", "t:addrowlink", "t:ajaxformloop", "t:any", "t:beandisplay", "t:beaneditform", "t:beaneditor", "t:checkbox",
          "t:datefield", "t:delegate", "t:errors", "t:eventlink", "t:exceptiondisplay", "t:form", "t:formfragment", "t:forminjector",
          "t:grid", "t:gridcell", "t:gridcolumns", "t:gridpager", "t:gridrows", "t:hidden", "t:if", "t:label", "t:linksubmit", "t:loop",
          "t:output", "t:outputraw", "t:pagelink", "t:palette", "t:passwordfield", "t:progressivedisplay", "t:propertydisplay",
          "t:propertyeditor", "t:radio", "t:radiogroup", "t:removerowlink", "t:renderobject", "t:select", "t:submit", "t:submitnotifier",
          "t:textarea", "t:textfield", "t:textoutput", "t:unless", "t:zone"};

  public static final String[] HTML_ELEMENT_NAMES =
      {"a", "abbr", "acronym", "address", "applet", "area", "b", "base", "basefont", "bdo", "big", "blockquote", "body", "br", "button",
          "caption", "center", "cite", "code", "col", "colgroup", "dd", "del", "dfn", "dir", "div", "dl", "dt", "em", "fieldset", "font",
          "form", "h1", "h2", "h3", "h4", "h5", "h6", "head", "hr", "html", "i", "iframe", "img", "input", "ins", "isindex", "kbd", "label",
          "legend", "li", "link", "map", "menu", "meta", "noframes", "noscript", "object", "ol", "optgroup", "option", "p", "param", "pre",
          "q", "s", "samp", "script", "select", "small", "span", "strike", "strong", "style", "sub", "sup", "table", "tbody", "td",
          "textarea", "tfoot", "th", "thead", "title", "tr", "tt", "u", "ul", "var"};

  public static final String[] HTML_AND_CORE_5_1_0_5_ELEMENT_NAMES = mergeArrays(CORE_5_1_0_5_ELEMENT_NAMES, HTML_ELEMENT_NAMES);
}
