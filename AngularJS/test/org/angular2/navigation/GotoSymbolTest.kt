// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation;

import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.impl.PsiManagerEx;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static java.util.Arrays.asList;

public class GotoSymbolTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "symbol/" + getTestName(true);
  }

  public void testElementSelector() {
    myFixture.configureByFiles("my-table.component.ts", "package.json");
    doTest("app-my-table", true,
           "app-my-table (MyTableComponent, my-table.component.ts) - my-table.component.ts [342]");
  }

  public void testAttributeSelector() {
    myFixture.configureByFiles("my-table.component.ts", "package.json");
    doTest("app-my-table", true,
           "app-my-table (MyTableComponent, my-table.component.ts) - my-table.component.ts [343]");
  }

  public void testAttrAndElementSelector() {
    myFixture.configureByFiles("my-table.component.ts", "package.json");
    doTest("app-my-table", true,
           "app-my-table (MyTableComponent, my-table.component.ts) - my-table.component.ts [342]",
           "app-my-table (MyTableComponent, my-table.component.ts) - my-table.component.ts [355]");
  }

  public void testPipe() {
    myFixture.configureByFiles("foo.pipe.ts", "package.json");
    doTest("foo", false,
           "foo (foo.pipe.ts)");
  }

  private void doTest(@NotNull String name, boolean detailed, String @NotNull ... expectedItems) {
    ((PsiManagerEx)myFixture.getPsiManager()).setAssertOnFileLoadingFilter(VirtualFileFilter.ALL, myFixture.getTestRootDisposable());

    GotoSymbolModel2 model = new GotoSymbolModel2(myFixture.getProject(), myFixture.getTestRootDisposable());

    assertContainsElements(asList(model.getNames(false)), name);
    final ArrayList<String> actual = new ArrayList<>();
    for (Object o : model.getElementsByName(name, false, "")) {
      if (o instanceof NavigationItem) {
        final ItemPresentation presentation = ((NavigationItem)o).getPresentation();
        assertNotNull(presentation);
        actual.add(presentation.getPresentableText() + " " + presentation.getLocationString() +
                   (detailed ? " - " + o : ""));
      }
    }
    assertSameElements(actual, expectedItems);
  }
}
