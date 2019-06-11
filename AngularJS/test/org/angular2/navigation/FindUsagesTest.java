// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation;

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
                "foo <private.html:(3,6)>",
                "foo <private.html:(69,72)>",
                "this.foo <private.ts:(350,358)>");
  }

  public void testPrivateComponentMethod() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("b<caret>ar",
                "bar() <private.html:(13,16)>",
                "bar() <private.html:(49,52)>",
                "this.bar() <private.ts:(369,377)>");
  }

  public void testPrivateConstructorField() {
    myFixture.configureByFiles("private.ts", "private.html", "package.json");
    checkUsages("fooB<caret>ar",
                "foo + fooBar <private.html:(120,126)>",
                "fooBar <private.html:(25,31)>",
                "this.fooBar <private.ts:(385,396)>");
  }

  private void checkUsages(@NotNull String signature, @NotNull String... usages) {
    AngularTestUtil.moveToOffsetBySignature(signature, myFixture);
    assertEquals(Stream.of(usages)
                   .sorted()
                   .collect(Collectors.toList()),
                 myFixture.findUsages(myFixture.getElementAtCaret())
                   .stream()
                   .map(usage -> usage.getElement().getParent().getText() +
                                 " <" + usage.getFile().getName() +
                                 ":" + usage.getElement().getTextRange() +
                                 ">")
                   .sorted()
                   .collect(Collectors.toList())
    );
  }
}
