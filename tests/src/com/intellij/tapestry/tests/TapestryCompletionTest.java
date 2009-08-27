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
    doTestBasicCompletionVariants(mergeArrays(CORE_5_1_0_5_TAG_NAMES, "head", "body", getElementTagName()));
  }

  public void testTagNameInTmlParent() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(
        mergeArrays(CORE_5_1_0_5_TAG_NAMES, "base", "isindex", "link", "meta", "object", "script", "style", "title", getElementTagName()));

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
    doTestBasicCompletionVariants(mergeArrays(HTML_AND_CORE_5_1_0_5_TAG_NAMES, getElementTagName()));
  }

  public void testTagNameWithinTmlRootTag() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(mergeArrays(HTML_AND_CORE_5_1_0_5_TAG_NAMES, getElementTagName()));
  }

  public void testTagNameWithoutHtmlContext() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(mergeArrays(HTML_AND_CORE_5_1_0_5_TAG_NAMES, getElementTagName()));
  }

  public void testInvalidTagName() throws Throwable {
    initByComponent();
    UsefulTestCase.assertEmpty(myFixture.complete(CompletionType.BASIC));
    UsefulTestCase.assertEmpty(myFixture.getLookupElementStrings());
  }

  public void testTypeAttrValue() throws Throwable {
    initByComponent();
    addComponentToProject("mycomps.Count");
    doTestBasicCompletionVariants(mergeArrays(CORE_5_1_0_5_ELEMENT_NAMES, getLowerCaseElementName(), "mycomps.count"));
  }

  public void testPageAttrValue() throws Throwable {
    addPageToProject("StartPage");
    initByComponent();
    doTestBasicCompletionVariants(mergeArrays(CORE_5_1_0_5_PAGE_NAMES, "startpage"));
  }

  public void testIdAttrValue() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    doTestBasicCompletionVariants("index", "link2", "link3");
  }

  public void testAttrValueWithPropPrefix() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    doTestBasicCompletionVariants("prop:strProp.chars", "prop:strProp.bytes");
  }

  public void testTapestryAttrValue() throws Throwable {
    addComponentToProject("Count");
    initByComponent();
    doTestBasicCompletionVariants("dateProp", "strProp", "intFieldProp");
  }

  public void testTagNameWithDoctypePresent() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(mergeArrays(CORE_5_1_0_5_TAG_NAMES, "body", "head", getElementTagName()));
  }

  public void testTagNameWithDoctypeAndExplicitHtmlNSPresent() throws Throwable {
    initByComponent();
    doTestBasicCompletionVariants(mergeArrays(CORE_5_1_0_5_TAG_NAMES, "body", "head", getElementTagName()));
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

  public static final String[] CORE_5_1_0_5_PAGE_NAMES =
      {"exceptionreport", "propertydisplayblocks", "propertyeditblocks", "servicestatus"};
  public static final String[] CORE_5_1_0_5_ELEMENT_NAMES =
      {"actionlink", "addrowlink", "ajaxformloop", "any", "beandisplay", "beaneditform", "beaneditor", "block", "body", "checkbox",
          "container", "datefield", "delegate", "errors", "eventlink", "exceptiondisplay", "form", "formfragment", "forminjector", "grid",
          "gridcell", "gridcolumns", "gridpager", "gridrows", "hidden", "if", "label", "linksubmit", "loop", "output", "outputraw",
          "pagelink", "palette", "parameter", "passwordfield", "progressivedisplay", "propertydisplay", "propertyeditor", "radio",
          "radiogroup", "removerowlink", "renderobject", "select", "submit", "submitnotifier", "textarea", "textfield", "textoutput",
          "unless", "zone"};
  public static final String[] CORE_5_1_0_5_TAG_NAMES;

  static {
    CORE_5_1_0_5_TAG_NAMES = new String[CORE_5_1_0_5_ELEMENT_NAMES.length];
    for (int i = 0; i < CORE_5_1_0_5_TAG_NAMES.length; i++) {
      CORE_5_1_0_5_TAG_NAMES[i] = "t:" + CORE_5_1_0_5_ELEMENT_NAMES[i];
    }
  }

  public static final String[] HTML_TAG_NAMES =
      {"a", "abbr", "acronym", "address", "applet", "area", "b", "base", "basefont", "bdo", "big", "blockquote", "body", "br", "button",
          "caption", "center", "cite", "code", "col", "colgroup", "dd", "del", "dfn", "dir", "div", "dl", "dt", "em", "fieldset", "font",
          "form", "h1", "h2", "h3", "h4", "h5", "h6", "head", "hr", "html", "i", "iframe", "img", "input", "ins", "isindex", "kbd", "label",
          "legend", "li", "link", "map", "menu", "meta", "noframes", "noscript", "object", "ol", "optgroup", "option", "p", "param", "pre",
          "q", "s", "samp", "script", "select", "small", "span", "strike", "strong", "style", "sub", "sup", "table", "tbody", "td",
          "textarea", "tfoot", "th", "thead", "title", "tr", "tt", "u", "ul", "var"};

  public static final String[] HTML_AND_CORE_5_1_0_5_TAG_NAMES = mergeArrays(CORE_5_1_0_5_TAG_NAMES, HTML_TAG_NAMES);
}
