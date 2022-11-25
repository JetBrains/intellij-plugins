// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.selector;


import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.mscharhag.oleaster.runner.OleasterRunner;
import org.angular2.lang.OleasterTestUtil;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector.ParseException;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static com.intellij.openapi.util.Pair.pair;
import static com.mscharhag.oleaster.matcher.Matchers.expect;
import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;

@SuppressWarnings({"CodeBlock2Expr", "JUnitTestCaseWithNoTests"})
@RunWith(OleasterRunner.class)
public class Angular2DirectiveSimpleSelectorSpecTest {

  private static Angular2SelectorMatcher<Integer> matcher;
  private static BiConsumer<Angular2DirectiveSimpleSelector, Integer> selectableCollector;
  private static List<Angular2DirectiveSimpleSelector> s1, s2, s3, s4;
  private static List<Object> matched;

  private static void reset() {
    matched = new ArrayList<>();
  }


  private static Angular2DirectiveSimpleSelector getSelectorFor(@NotNull String tag) {
    return getSelectorFor(tag, "");
  }


  @SafeVarargs
  private static Angular2DirectiveSimpleSelector getSelectorFor(Pair<String, String> @NotNull ... attrs) {
    return getSelectorFor("", "", attrs);
  }

  private static Angular2DirectiveSimpleSelector getSelectorForClasses(@NotNull String classes) {
    return getSelectorFor("", classes);
  }

  @SafeVarargs
  private static Angular2DirectiveSimpleSelector getSelectorFor(@NotNull String tag,
                                                                Pair<String, String> @NotNull ... attrs) {
    return getSelectorFor(tag, "", attrs);
  }

  @SafeVarargs
  private static Angular2DirectiveSimpleSelector getSelectorFor(@NotNull String tag,
                                                                @NotNull String classes,
                                                                Pair<String, String> @NotNull ... attrs) {
    Angular2DirectiveSimpleSelector selector = new Angular2DirectiveSimpleSelector();
    selector.setElement(tag);

    for (Pair<String, String> nameValue : attrs) {
      selector.addAttribute(nameValue.first, nameValue.second);
    }

    StringUtil.split(classes, " ").forEach(selector::addClassName);

    return selector;
  }

  static {
    describe("SelectorMatcher", () -> {
      OleasterTestUtil.bootstrapLightPlatform();

      beforeEach(() -> {
        reset();
        s1 = s2 = s3 = s4 = null;
        selectableCollector = (selector, context) -> {
          matched.add(selector);
          matched.add(context);
        };
        matcher = new Angular2SelectorMatcher<>();
      });

      it("should select by element name case sensitive", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("someTag"), 1);

        expect(matcher.match(getSelectorFor("SOMEOTHERTAG"), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(getSelectorFor("SOMETAG"), selectableCollector)).toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(getSelectorFor("someTag"), selectableCollector)).toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1));
      });

      it("should select by class name case insensitive", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse(".someClass"), 1);
        matcher.addSelectables(s2 = Angular2DirectiveSimpleSelector.parse(".someClass.class2"), 2);

        expect(matcher.match(getSelectorForClasses("SOMEOTHERCLASS"), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(getSelectorForClasses("SOMECLASS"), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1));

        reset();
        expect(matcher.match(getSelectorForClasses("someClass class2"), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1, s2.get(0), 2));
      });

      it("should not throw for class name \"constructor\"", () -> {
        expect(matcher.match(getSelectorForClasses("constructor"), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());
      });

      it("should select by attr name case sensitive independent of the value", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("[someAttr]"), 1);
        matcher.addSelectables(s2 = Angular2DirectiveSimpleSelector.parse("[someAttr][someAttr2]"), 2);

        expect(matcher.match(getSelectorFor(pair("SOMEOTHERATTR", "")), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(getSelectorFor(pair("SOMEATTR", "")), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(
          matcher.match(getSelectorFor(pair("SOMEATTR", "someValue")), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(getSelectorFor(pair("someAttr", ""), pair("someAttr2", "")), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1, s2.get(0), 2));

        reset();
        expect(matcher.match(getSelectorFor(pair("someAttr", "someValue"), pair("someAttr2", "")), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1, s2.get(0), 2));

        reset();
        expect(matcher.match(getSelectorFor(pair("someAttr2", ""), pair("someAttr", "someValue")), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1, s2.get(0), 2));

        reset();
        expect(matcher.match(getSelectorFor(pair("someAttr2", "someValue"), pair("someAttr", "")), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1, s2.get(0), 2));
      });

      it("should support \".\" in attribute names", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("[foo.bar]"), 1);

        expect(matcher.match(getSelectorFor(pair("barfoo", "")), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        reset();
        expect(matcher.match(getSelectorFor(pair("foo.bar", "")), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1));
      });

      it("should select by attr name only once if the value is from the DOM", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("[some-decor]"), 1);

        Angular2DirectiveSimpleSelector elementSelector = new Angular2DirectiveSimpleSelector();
        //const element = el("<div attr></div>");
        //const empty = getDOM().getAttribute(element, "attr") !;
        elementSelector.addAttribute("some-decor", null);
        matcher.match(elementSelector, selectableCollector);
        expect(matched).toEqual(List.of(s1.get(0), 1));
      });

      it("should select by attr name case sensitive and value case insensitive", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("[someAttr=someValue]"), 1);

        expect(matcher.match(getSelectorFor(pair("SOMEATTR", "SOMEOTHERATTR")), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(getSelectorFor(pair("SOMEATTR", "SOMEVALUE")), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(getSelectorFor(pair("someAttr", "SOMEVALUE")), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1));
      });

      it("should select by element name, class name and attribute name with value", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("someTag.someClass[someAttr=someValue]"), 1);

        expect(matcher.match(getSelectorFor("someOtherTag", "someOtherClass", pair("someOtherAttr", "")), selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(
          getSelectorFor(
            "someTag", "someOtherClass", pair("someOtherAttr", "")),
          selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(
          getSelectorFor(
            "someTag", "someClass", pair("someOtherAttr", "")),
          selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(
          getSelectorFor("someTag", "someClass", pair("someAttr", "")),
          selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());

        expect(matcher.match(
          getSelectorFor(
            "someTag", "someClass", pair("someAttr", "someValue")),
          selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1));
      });

      it("should select by many attributes and independent of the value", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("input[type=text][control]"), 1);

        Angular2DirectiveSimpleSelector cssSelector = new Angular2DirectiveSimpleSelector();
        cssSelector.setElement("input");
        cssSelector.addAttribute("type", "text");
        cssSelector.addAttribute("control", "one");

        expect(matcher.match(cssSelector, selectableCollector)).toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1));
      });

      it("should select independent of the order in the css selector", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("[someAttr].someClass"), 1);
        matcher.addSelectables(s2 = Angular2DirectiveSimpleSelector.parse(".someClass[someAttr]"), 2);
        matcher.addSelectables(s3 = Angular2DirectiveSimpleSelector.parse(".class1.class2"), 3);
        matcher.addSelectables(s4 = Angular2DirectiveSimpleSelector.parse(".class2.class1"), 4);

        expect(matcher.match(Angular2DirectiveSimpleSelector.parse("[someAttr].someClass").get(0), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1, s2.get(0), 2));

        reset();
        expect(matcher.match(Angular2DirectiveSimpleSelector.parse(".someClass[someAttr]").get(0), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1, s2.get(0), 2));

        reset();
        expect(matcher.match(Angular2DirectiveSimpleSelector.parse(".class1.class2").get(0), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s3.get(0), 3, s4.get(0), 4));

        reset();
        expect(matcher.match(Angular2DirectiveSimpleSelector.parse(".class2.class1").get(0), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s4.get(0), 4, s3.get(0), 3));
      });

      it("should not select with a matching :not selector", () -> {
        matcher.addSelectables(Angular2DirectiveSimpleSelector.parse("p:not(.someClass)"), 1);
        matcher.addSelectables(Angular2DirectiveSimpleSelector.parse("p:not([someAttr])"), 2);
        matcher.addSelectables(Angular2DirectiveSimpleSelector.parse(":not(.someClass)"), 3);
        matcher.addSelectables(Angular2DirectiveSimpleSelector.parse(":not(p)"), 4);
        matcher.addSelectables(Angular2DirectiveSimpleSelector.parse(":not(p[someAttr])"), 5);

        expect(matcher.match(
          getSelectorFor("p", "someClass", pair("someAttr", "")),
          selectableCollector))
          .toEqual(false);
        expect(matched).toEqual(Collections.emptyList());
      });

      it("should select with a non matching :not selector", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("p:not(.someClass)"), 1);
        matcher.addSelectables(s2 = Angular2DirectiveSimpleSelector.parse("p:not(.someOtherClass[someAttr])"), 2);
        matcher.addSelectables(s3 = Angular2DirectiveSimpleSelector.parse(":not(.someClass)"), 3);
        matcher.addSelectables(s4 = Angular2DirectiveSimpleSelector.parse(":not(.someOtherClass[someAttr])"), 4);

        expect(
          matcher.match(
            getSelectorFor("p", "someOtherClass", pair("someOtherAttr", "")),
            selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1, s2.get(0), 2, s3.get(0), 3, s4.get(0), 4));
      });

      it("should match * with :not selector", () -> {
        matcher.addSelectables(Angular2DirectiveSimpleSelector.parse(":not([a])"), 1);
        expect(matcher.match(getSelectorFor("div"), (a, b) -> {
        })).toEqual(true);
      });

      it("should match with multiple :not selectors", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("div:not([a]):not([b])"), 1);
        expect(matcher.match(getSelectorFor("div", pair("a", "")), selectableCollector))
          .toBeFalse();
        expect(matcher.match(getSelectorFor("div", pair("b", "")), selectableCollector))
          .toBeFalse();
        expect(matcher.match(getSelectorFor("div", pair("c", "")), selectableCollector))
          .toBeTrue();
      });

      it("should select with one match in a list", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("input[type=text], textbox"), 1);

        expect(matcher.match(getSelectorFor("textbox"), selectableCollector)).toEqual(true);
        expect(matched).toEqual(List.of(s1.get(1), 1));

        reset();
        expect(matcher.match(
          getSelectorFor("input", pair("type", "text")), selectableCollector))
          .toEqual(true);
        expect(matched).toEqual(List.of(s1.get(0), 1));
      });

      it("should not select twice with two matches in a list", () -> {
        matcher.addSelectables(s1 = Angular2DirectiveSimpleSelector.parse("input, .someClass"), 1);

        expect(
          matcher.match(getSelectorFor("input", "someclass"), selectableCollector))
          .toEqual(true);
        expect(matched.size()).toEqual(2);
        expect(matched).toEqual(List.of(s1.get(0), 1));
      });
    });

    describe("CssSelector.parse", () -> {
      it("should detect element names", () -> {
        Angular2DirectiveSimpleSelector cssSelector = Angular2DirectiveSimpleSelector.parse("sometag").get(0);
        expect(cssSelector.element).toEqual("sometag");
        expect(cssSelector.toString()).toEqual("sometag");
      });

      it("should detect class names", () -> {
        Angular2DirectiveSimpleSelector cssSelector = Angular2DirectiveSimpleSelector.parse(".someClass").get(0);
        expect(cssSelector.classNames).toEqual(List.of("someclass"));

        expect(cssSelector.toString()).toEqual(".someclass");
      });

      it("should detect attr names", () -> {
        Angular2DirectiveSimpleSelector cssSelector = Angular2DirectiveSimpleSelector.parse("[attrname]").get(0);
        expect(cssSelector.attrs).toEqual(List.of("attrname", ""));

        expect(cssSelector.toString()).toEqual("[attrname]");
      });

      it("should detect attr values", () -> {
        Angular2DirectiveSimpleSelector cssSelector = Angular2DirectiveSimpleSelector.parse("[attrname=attrvalue]").get(0);
        expect(cssSelector.attrs).toEqual(List.of("attrname", "attrvalue"));
        expect(cssSelector.toString()).toEqual("[attrname=attrvalue]");
      });

      it("should detect attr values with double quotes", () -> {
        Angular2DirectiveSimpleSelector cssSelector = Angular2DirectiveSimpleSelector.parse("[attrname=\"attrvalue\"]").get(0);
        expect(cssSelector.attrs).toEqual(List.of("attrname", "attrvalue"));
        expect(cssSelector.toString()).toEqual("[attrname=attrvalue]");
      });

      it("should detect attr values with single quotes", () -> {
        Angular2DirectiveSimpleSelector cssSelector = Angular2DirectiveSimpleSelector.parse("[attrname='attrvalue']").get(0);
        expect(cssSelector.attrs).toEqual(List.of("attrname", "attrvalue"));
        expect(cssSelector.toString()).toEqual("[attrname=attrvalue]");
      });

      it("should detect multiple parts", () -> {
        Angular2DirectiveSimpleSelector cssSelector = Angular2DirectiveSimpleSelector.parse("sometag[attrname=attrvalue].someclass").get(0);
        expect(cssSelector.element).toEqual("sometag");
        expect(cssSelector.attrs).toEqual(List.of("attrname", "attrvalue"));
        expect(cssSelector.classNames).toEqual(List.of("someclass"));

        expect(cssSelector.toString()).toEqual("sometag.someclass[attrname=attrvalue]");
      });

      it("should detect multiple attributes", () -> {
        Angular2DirectiveSimpleSelector cssSelector = Angular2DirectiveSimpleSelector.parse("input[type=text][control]").get(0);
        expect(cssSelector.element).toEqual("input");
        expect(cssSelector.attrs).toEqual(List.of("type", "text", "control", ""));

        expect(cssSelector.toString()).toEqual("input[type=text][control]");
      });

      it("should detect :not", () -> {
        Angular2DirectiveSimpleSelector
          cssSelector = Angular2DirectiveSimpleSelector.parse("sometag:not([attrname=attrvalue].someclass)").get(0);
        expect(cssSelector.element).toEqual("sometag");
        expect(cssSelector.attrs.size()).toEqual(0);
        expect(cssSelector.classNames.size()).toEqual(0);

        Angular2DirectiveSimpleSelector notSelector = cssSelector.notSelectors.get(0);
        expect(notSelector.element).toEqual(null);
        expect(notSelector.attrs).toEqual(List.of("attrname", "attrvalue"));
        expect(notSelector.classNames).toEqual(List.of("someclass"));

        expect(cssSelector.toString()).toEqual("sometag:not(.someclass[attrname=attrvalue])");
      });

      it("should detect :not without truthy", () -> {
        Angular2DirectiveSimpleSelector cssSelector = Angular2DirectiveSimpleSelector.parse(":not([attrname=attrvalue].someclass)").get(0);
        expect(cssSelector.element).toEqual("*");

        Angular2DirectiveSimpleSelector notSelector = cssSelector.notSelectors.get(0);
        expect(notSelector.attrs).toEqual(List.of("attrname", "attrvalue"));
        expect(notSelector.classNames).toEqual(List.of("someclass"));

        expect(cssSelector.toString()).toEqual("*:not(.someclass[attrname=attrvalue])");
      });

      it("should throw when nested :not", () -> {
        expect(() -> {
          Angular2DirectiveSimpleSelector.parse("sometag:not(:not([attrname=attrvalue].someclass))");
        }).toThrow(ParseException.class, "Nested :not is not allowed in selectors");
      });

      it("should throw when multiple selectors in :not", () -> {
        expect(() -> {
          Angular2DirectiveSimpleSelector.parse("sometag:not(a,b)");
        }).toThrow(ParseException.class, "Multiple selectors in :not are not supported");
      });

      it("should detect lists of selectors", () -> {
        List<Angular2DirectiveSimpleSelector> cssSelectors = Angular2DirectiveSimpleSelector
          .parse(".someclass,[attrname=attrvalue], sometag");
        expect(cssSelectors.size()).toEqual(3);

        expect(cssSelectors.get(0).classNames).toEqual(List.of("someclass"));
        expect(cssSelectors.get(1).attrs).toEqual(List.of("attrname", "attrvalue"));
        expect(cssSelectors.get(2).element).toEqual("sometag");
      });

      it("should detect lists of selectors with :not", () -> {
        List<Angular2DirectiveSimpleSelector> cssSelectors =
          Angular2DirectiveSimpleSelector.parse("input[type=text], :not(textarea), textbox:not(.special)");
        expect(cssSelectors.size()).toEqual(3);

        expect(cssSelectors.get(0).element).toEqual("input");
        expect(cssSelectors.get(0).attrs).toEqual(List.of("type", "text"));

        expect(cssSelectors.get(1).element).toEqual("*");
        expect(cssSelectors.get(1).notSelectors.get(0).element).toEqual("textarea");

        expect(cssSelectors.get(2).element).toEqual("textbox");
        expect(cssSelectors.get(2).notSelectors.get(0).classNames).toEqual(List.of("special"));
      });
    });

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
