// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.selector

import com.intellij.openapi.util.text.StringUtil
import com.mscharhag.oleaster.matcher.Matchers
import com.mscharhag.oleaster.runner.OleasterRunner
import com.mscharhag.oleaster.runner.StaticRunnerSupport.*
import org.angular2.lang.OleasterTestUtil.bootstrapLightPlatform
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector.Companion.parse
import org.junit.runner.RunWith

@RunWith(OleasterRunner::class)
class Angular2DirectiveSimpleSelectorSpecTest {
  private lateinit var matcher: Angular2SelectorMatcher<Int>
  private var selectableCollector: ((Angular2DirectiveSimpleSelector, Int?) -> Unit)? = null
  private var s1: List<Angular2DirectiveSimpleSelector>? = null
  private var s2: List<Angular2DirectiveSimpleSelector>? = null
  private var s3: List<Angular2DirectiveSimpleSelector>? = null
  private var s4: List<Angular2DirectiveSimpleSelector>? = null
  private var matched: MutableList<Any?>? = null
  private fun reset() {
    matched = ArrayList()
  }

  private fun getSelectorFor(tag: String): Angular2DirectiveSimpleSelector {
    return getSelectorFor(tag, "")
  }

  @SafeVarargs
  private fun getSelectorFor(vararg attrs: Pair<String, String?>): Angular2DirectiveSimpleSelector {
    return getSelectorFor("", "", *attrs)
  }

  private fun getSelectorForClasses(classes: String): Angular2DirectiveSimpleSelector {
    return getSelectorFor("", classes)
  }

  @SafeVarargs
  private fun getSelectorFor(tag: String,
                             vararg attrs: Pair<String, String?>): Angular2DirectiveSimpleSelector {
    return getSelectorFor(tag, "", *attrs)
  }

  @SafeVarargs
  private fun getSelectorFor(tag: String,
                             classes: String,
                             vararg attrs: Pair<String, String?>): Angular2DirectiveSimpleSelector {
    return Angular2DirectiveSimpleSelector(
      tag,
      attrs.toList().toMap(),
      StringUtil.split(classes, " "), emptyList())
  }

  init {
    describe("SelectorMatcher") {
      bootstrapLightPlatform()
      beforeEach {
        reset()
        s4 = null
        s3 = null
        s2 = null
        s1 = null
        selectableCollector = { selector: Angular2DirectiveSimpleSelector?, context: Int? ->
          matched!!.add(selector)
          matched!!.add(context)
          Unit
        }
        matcher = Angular2SelectorMatcher()
      }
      it("should select by element name case sensitive") {
        matcher.addSelectables(parse("someTag").also { s1 = it }, 1)
        Matchers.expect(matcher.match(getSelectorFor("SOMEOTHERTAG"), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(getSelectorFor("SOMETAG"), selectableCollector)).toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(getSelectorFor("someTag"), selectableCollector)).toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1))
      }
      it("should select by class name case insensitive") {
        matcher.addSelectables(parse(".someClass").also { s1 = it }, 1)
        matcher.addSelectables(parse(".someClass.class2").also { s2 = it }, 2)
        Matchers.expect(matcher.match(getSelectorForClasses("SOMEOTHERCLASS"), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(getSelectorForClasses("SOMECLASS"), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1))
        reset()
        Matchers.expect(matcher.match(getSelectorForClasses("someClass class2"), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1, s2!![0], 2))
      }
      it("should not throw for class name \"constructor\"") {
        Matchers.expect(matcher.match(getSelectorForClasses("constructor"), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
      }
      it("should select by attr name case sensitive independent of the value") {
        matcher.addSelectables(parse("[someAttr]").also { s1 = it }, 1)
        matcher.addSelectables(parse("[someAttr][someAttr2]").also { s2 = it }, 2)
        Matchers.expect(matcher.match(getSelectorFor(Pair("SOMEOTHERATTR", "")), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(getSelectorFor(Pair("SOMEATTR", "")), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(getSelectorFor(Pair("SOMEATTR", "someValue")), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(getSelectorFor(Pair("someAttr", ""), Pair("someAttr2", "")), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1, s2!![0], 2))
        reset()
        Matchers.expect(matcher.match(getSelectorFor(Pair("someAttr", "someValue"), Pair("someAttr2", "")), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1, s2!![0], 2))
        reset()
        Matchers.expect(matcher.match(getSelectorFor(Pair("someAttr2", ""), Pair("someAttr", "someValue")), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1, s2!![0], 2))
        reset()
        Matchers.expect(matcher.match(getSelectorFor(Pair("someAttr2", "someValue"), Pair("someAttr", "")), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1, s2!![0], 2))
      }
      it("should support \".\" in attribute names") {
        matcher.addSelectables(parse("[foo.bar]").also { s1 = it }, 1)
        Matchers.expect(matcher.match(getSelectorFor(Pair("barfoo", "")), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        reset()
        Matchers.expect(matcher.match(getSelectorFor(Pair("foo.bar", "")), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1))
      }
      it("should select by attr name only once if the value is from the DOM") {
        matcher.addSelectables(parse("[some-decor]").also { s1 = it }, 1)
        val elementSelector: Angular2DirectiveSimpleSelector = getSelectorFor(Pair("some-decor", null))
        //const element = el("<div attr></div>");
        //const empty = getDOM().getAttribute(element, "attr") !;
        matcher.match(elementSelector, selectableCollector)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1))
      }
      it("should select by attr name case sensitive and value case insensitive") {
        matcher.addSelectables(parse("[someAttr=someValue]").also { s1 = it }, 1)
        Matchers.expect(matcher.match(getSelectorFor(Pair("SOMEATTR", "SOMEOTHERATTR")), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(getSelectorFor(Pair("SOMEATTR", "SOMEVALUE")), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(getSelectorFor(Pair("someAttr", "SOMEVALUE")), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1))
      }
      it("should select by element name, class name and attribute name with value") {
        matcher.addSelectables(parse("someTag.someClass[someAttr=someValue]").also { s1 = it }, 1)
        Matchers.expect(matcher.match(getSelectorFor("someOtherTag", "someOtherClass", Pair("someOtherAttr", "")), selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(
          getSelectorFor(
            "someTag", "someOtherClass", Pair("someOtherAttr", "")),
          selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(
          getSelectorFor(
            "someTag", "someClass", Pair("someOtherAttr", "")),
          selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(
          getSelectorFor("someTag", "someClass", Pair("someAttr", "")),
          selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
        Matchers.expect(matcher.match(
          getSelectorFor(
            "someTag", "someClass", Pair("someAttr", "someValue")),
          selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1))
      }
      it("should select by many attributes and independent of the value") {
        matcher.addSelectables(parse("input[type=text][control]").also { s1 = it }, 1)
        val cssSelector = getSelectorFor("input", Pair("type", "text"), Pair("control", "one"))
        Matchers.expect(matcher.match(cssSelector, selectableCollector)).toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1))
      }
      it("should select independent of the order in the css selector") {
        matcher.addSelectables(parse("[someAttr].someClass").also { s1 = it }, 1)
        matcher.addSelectables(parse(".someClass[someAttr]").also { s2 = it }, 2)
        matcher.addSelectables(parse(".class1.class2").also { s3 = it }, 3)
        matcher.addSelectables(parse(".class2.class1").also { s4 = it }, 4)
        Matchers.expect(matcher.match(parse("[someAttr].someClass")[0], selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1, s2!![0], 2))
        reset()
        Matchers.expect(matcher.match(parse(".someClass[someAttr]")[0], selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1, s2!![0], 2))
        reset()
        Matchers.expect(matcher.match(parse(".class1.class2")[0], selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s3!![0], 3, s4!![0], 4))
        reset()
        Matchers.expect(matcher.match(parse(".class2.class1")[0], selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s4!![0], 4, s3!![0], 3))
      }
      it("should not select with a matching :not selector") {
        matcher.addSelectables(parse("p:not(.someClass)"), 1)
        matcher.addSelectables(parse("p:not([someAttr])"), 2)
        matcher.addSelectables(parse(":not(.someClass)"), 3)
        matcher.addSelectables(parse(":not(p)"), 4)
        matcher.addSelectables(parse(":not(p[someAttr])"), 5)
        Matchers.expect(matcher.match(
          getSelectorFor("p", "someClass", Pair("someAttr", "")),
          selectableCollector))
          .toEqual(false)
        Matchers.expect(matched).toEqual(emptyList<Any>())
      }
      it("should select with a non matching :not selector") {
        matcher.addSelectables(parse("p:not(.someClass)").also { s1 = it }, 1)
        matcher.addSelectables(parse("p:not(.someOtherClass[someAttr])").also { s2 = it }, 2)
        matcher.addSelectables(parse(":not(.someClass)").also { s3 = it }, 3)
        matcher.addSelectables(parse(":not(.someOtherClass[someAttr])").also { s4 = it }, 4)
        Matchers.expect(matcher.match(
          getSelectorFor("p", "someOtherClass", Pair("someOtherAttr", "")),
          selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1, s2!![0], 2, s3!![0], 3, s4!![0], 4))
      }
      it("should match * with :not selector") {
        matcher.addSelectables(parse(":not([a])"), 1)
        Matchers.expect(matcher.match(getSelectorFor("div")) { _, _ -> }).toEqual(true)
      }
      it("should match with multiple :not selectors") {
        matcher.addSelectables(parse("div:not([a]):not([b])").also { s1 = it }, 1)
        Matchers.expect(matcher.match(getSelectorFor("div", Pair("a", "")), selectableCollector))
          .toBeFalse()
        Matchers.expect(matcher.match(getSelectorFor("div", Pair("b", "")), selectableCollector))
          .toBeFalse()
        Matchers.expect(matcher.match(getSelectorFor("div", Pair("c", "")), selectableCollector))
          .toBeTrue()
      }
      it("should select with one match in a list") {
        matcher.addSelectables(parse("input[type=text], textbox").also { s1 = it }, 1)
        Matchers.expect(matcher.match(getSelectorFor("textbox"), selectableCollector)).toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![1], 1))
        reset()
        Matchers.expect(matcher.match(
          getSelectorFor("input", Pair("type", "text")), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1))
      }
      it("should not select twice with two matches in a list") {
        matcher.addSelectables(parse("input, .someClass").also { s1 = it }, 1)
        Matchers.expect(matcher.match(getSelectorFor("input", "someclass"), selectableCollector))
          .toEqual(true)
        Matchers.expect(matched!!.size).toEqual(2)
        Matchers.expect(matched).toEqual(listOf(s1!![0], 1))
      }
    }
    describe("CssSelector.parse") {
      it("should detect element names") {
        val cssSelector = parse("sometag")[0]
        Matchers.expect(cssSelector.elementName).toEqual("sometag")
        Matchers.expect(cssSelector.toString()).toEqual("sometag")
      }
      it("should detect class names") {
        val cssSelector = parse(".someClass")[0]
        Matchers.expect(cssSelector.classNames).toEqual(listOf("someclass"))
        Matchers.expect(cssSelector.toString()).toEqual(".someclass")
      }
      it("should detect attr names") {
        val cssSelector = parse("[attrname]")[0]
        Matchers.expect(cssSelector.attrsAndValues).toEqual(listOf("attrname", ""))
        Matchers.expect(cssSelector.toString()).toEqual("[attrname]")
      }
      it("should detect attr values") {
        val cssSelector = parse("[attrname=attrvalue]")[0]
        Matchers.expect(cssSelector.attrsAndValues).toEqual(listOf("attrname", "attrvalue"))
        Matchers.expect(cssSelector.toString()).toEqual("[attrname=attrvalue]")
      }
      it("should detect attr values with double quotes") {
        val cssSelector = parse("[attrname=\"attrvalue\"]")[0]
        Matchers.expect(cssSelector.attrsAndValues).toEqual(listOf("attrname", "attrvalue"))
        Matchers.expect(cssSelector.toString()).toEqual("[attrname=attrvalue]")
      }
      it("should detect attr values with single quotes") {
        val cssSelector = parse("[attrname='attrvalue']")[0]
        Matchers.expect(cssSelector.attrsAndValues).toEqual(listOf("attrname", "attrvalue"))
        Matchers.expect(cssSelector.toString()).toEqual("[attrname=attrvalue]")
      }
      it("should detect multiple parts") {
        val cssSelector = parse("sometag[attrname=attrvalue].someclass")[0]
        Matchers.expect(cssSelector.elementName).toEqual("sometag")
        Matchers.expect(cssSelector.attrsAndValues).toEqual(listOf("attrname", "attrvalue"))
        Matchers.expect(cssSelector.classNames).toEqual(listOf("someclass"))
        Matchers.expect(cssSelector.toString()).toEqual("sometag.someclass[attrname=attrvalue]")
      }
      it("should detect multiple attributes") {
        val cssSelector = parse("input[type=text][control]")[0]
        Matchers.expect(cssSelector.elementName).toEqual("input")
        Matchers.expect(cssSelector.attrsAndValues).toEqual(listOf("type", "text", "control", ""))
        Matchers.expect(cssSelector.toString()).toEqual("input[type=text][control]")
      }
      it("should detect :not") {
        val cssSelector = parse("sometag:not([attrname=attrvalue].someclass)")[0]
        Matchers.expect(cssSelector.elementName).toEqual("sometag")
        Matchers.expect(cssSelector.attrsAndValues.size).toEqual(0)
        Matchers.expect(cssSelector.classNames.size).toEqual(0)
        val notSelector = cssSelector.notSelectors[0]
        Matchers.expect(notSelector.elementName).toEqual(null)
        Matchers.expect(notSelector.attrsAndValues).toEqual(listOf("attrname", "attrvalue"))
        Matchers.expect(notSelector.classNames).toEqual(listOf("someclass"))
        Matchers.expect(cssSelector.toString()).toEqual("sometag:not(.someclass[attrname=attrvalue])")
      }
      it("should detect :not without truthy") {
        val cssSelector = parse(":not([attrname=attrvalue].someclass)")[0]
        Matchers.expect(cssSelector.elementName).toEqual("*")
        val notSelector = cssSelector.notSelectors[0]
        Matchers.expect(notSelector.attrsAndValues).toEqual(listOf("attrname", "attrvalue"))
        Matchers.expect(notSelector.classNames).toEqual(listOf("someclass"))
        Matchers.expect(cssSelector.toString()).toEqual("*:not(.someclass[attrname=attrvalue])")
      }
      it("should throw when nested :not") {
        Matchers.expect { parse("sometag:not(:not([attrname=attrvalue].someclass))") }.toThrow(
          Angular2DirectiveSimpleSelector.ParseException::class.java, "Nested :not is not allowed in selectors")
      }
      it("should throw when multiple selectors in :not") {
        Matchers.expect { parse("sometag:not(a,b)") }.toThrow(
          Angular2DirectiveSimpleSelector.ParseException::class.java, "Multiple selectors in :not are not supported")
      }
      it("should detect lists of selectors") {
        val cssSelectors = parse(".someclass,[attrname=attrvalue], sometag")
        Matchers.expect(cssSelectors.size).toEqual(3)
        Matchers.expect(cssSelectors[0].classNames).toEqual(listOf("someclass"))
        Matchers.expect(
          cssSelectors[1].attrsAndValues).toEqual(listOf("attrname", "attrvalue"))
        Matchers.expect(cssSelectors[2].elementName).toEqual("sometag")
      }
      it("should detect lists of selectors with :not") {
        val cssSelectors = parse("input[type=text], :not(textarea), textbox:not(.special)")
        Matchers.expect(cssSelectors.size).toEqual(3)
        Matchers.expect(cssSelectors[0].elementName).toEqual("input")
        Matchers.expect(
          cssSelectors[0].attrsAndValues).toEqual(listOf("type", "text"))
        Matchers.expect(cssSelectors[1].elementName).toEqual("*")
        Matchers.expect(cssSelectors[1].notSelectors[0].elementName).toEqual("textarea")
        Matchers.expect(cssSelectors[2].elementName).toEqual("textbox")
        Matchers.expect(
          cssSelectors[2].notSelectors[0].classNames).toEqual(listOf("special"))
      }
    }

    //describe("CssSelector.getMatchingElementTemplate", () -> {
    //  it("should create an element with a tagName, classes, and attributes with the correct casing",
    //     () -> {
    //       CssSelector selector = CssSelector.parse("Blink.neon.hotpink[Sweet][Dismissable=false]").get(0);
    //       String template = selector.getMatchingElementTemplate();
    //
    //       expect(template).toEqual("<Blink class=\"neon hotpink\" Sweet Dismissable=\"false\"></Blink>");
    //     });
    //
    //  it("should create an element without a tag name", () -> {
    //    CssSelector selector = CssSelector.parse("[fancy]").get(0);
    //    String template = selector.getMatchingElementTemplate();
    //
    //    expect(template).toEqual("<div fancy></div>");
    //  });
    //
    //  it("should ignore :not selectors", () -> {
    //    CssSelector selector = CssSelector.parse("grape:not(.red)").get(0);
    //    String template = selector.getMatchingElementTemplate();
    //
    //    expect(template).toEqual("<grape></grape>");
    //  });
    //
    //  it("should support void tags", () -> {
    //    CssSelector selector = CssSelector.parse("input[fancy]").get(0);
    //    String template = selector.getMatchingElementTemplate();
    //    expect(template).toEqual("<input fancy/>");
    //  });
    //});
  }
}
