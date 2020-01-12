// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindUsagesTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "/findUsages";
  }

  public void testPrivateComponentField() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("f<caret>oo",
                "foo <private.html:(3,6):(0,3)>",
                "foo <private.html:(69,72):(0,3)>",
                "this.foo <private.ts:(350,358):(5,8)>");
  }

  public void testPrivateComponentMethod() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("b<caret>ar",
                "bar() <private.html:(13,16):(0,3)>",
                "bar() <private.html:(49,52):(0,3)>",
                "this.bar() <private.ts:(369,377):(5,8)>");
  }

  public void testPrivateConstructorField() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("fooB<caret>ar",
                "foo + fooBar <private.html:(120,126):(0,6)>",
                "fooBar <private.html:(25,31):(0,6)>",
                "this.fooBar <private.ts:(385,396):(5,11)>");
  }

  public void testSlotComponentElementSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json");
    checkUsages("tag<caret>-slot",
                "\"tag-slot\" <slots.component.ts:(27,35):(0,8)>",
                "tag-slot <slots.test.component.html:(20,70):(1,9)>",
                "tag-slot <slots.test.component.html:(20,70):(41,49)>");
  }

  public void testSlotComponentAttributeSelector() {
    myFixture.configureByFiles("slots.component.ts", "slots.test.component.html", "slots.test.component.ts", "package.json");
    checkUsages("attr<caret>-slot",
                "\"[attr-slot]\" <slots.component.ts:(77,88):(1,10)>",
                "attr-slot <slots.test.component.html:(78,87):(0,9)>");
  }

  private void checkUsages(@NotNull String signature, String @NotNull ... usages) {
    AngularTestUtil.moveToOffsetBySignature(signature, myFixture);
    assertEquals(Stream.of(usages)
                   .sorted()
                   .collect(Collectors.toList()),
                 myFixture.findUsages(myFixture.getElementAtCaret())
                   .stream()
                   .map(usage -> getElementText(usage.getElement()) +
                                 " <" + usage.getFile().getName() +
                                 ":" + usage.getElement().getTextRange() +
                                 ":" + usage.getRangeInElement() +
                                 ">")
                   .sorted()
                   .collect(Collectors.toList())
    );
  }

  private static String getElementText(PsiElement element) {
    if (element instanceof XmlTag) {
      return ((XmlTag)element).getName();
    }
    else if (element instanceof XmlAttribute) {
      return element.getText();
    }
    return element.getParent().getText();
  }
}
