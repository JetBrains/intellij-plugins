// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.navigation;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.util.ArrayUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.ide.DartSymbolContributor;

import java.util.ArrayList;
import java.util.Collection;

public class DartGoToSymbolTest extends DartCodeInsightFixtureTestCase {

  private void assertNamesFound(ChooseByNameContributor contributor, String... names) {
    Collection<String> actual = new ArrayList<>();
    for (String name : names) {
      for (NavigationItem item : contributor.getItemsByName(name, "", getProject(), false)) {
        actual.add(item.getName());
      }
    }
    assertSameElements(actual, names);
  }

  public void testGoToSymbol() {
    myFixture.addFileToProject("foo.dart",
                               """
                                 fooBarBaz1() {}
                                 class fooBarBaz2 {
                                   var fooBarBaz3;
                                   fooBarBaz4(){}
                                 }
                                 mixin fooBarBaz5 {
                                   var fooBarBaz6;
                                   fooBarBaz7(){}
                                 }
                                 """);

    DartSymbolContributor contributor = new DartSymbolContributor();
    String[] allNames = contributor.getNames(getProject(), false);
    assertFalse(ArrayUtil.contains("fooBarBaz0", allNames));
    assertFalse(ArrayUtil.contains("FooBarBaz1", allNames));
    assertFalse(ArrayUtil.contains("fooBarBaz8", allNames));
    assertNamesFound(contributor, "fooBarBaz1", "fooBarBaz2", "fooBarBaz3", "fooBarBaz4", "fooBarBaz5", "fooBarBaz6", "fooBarBaz7");
  }
}
