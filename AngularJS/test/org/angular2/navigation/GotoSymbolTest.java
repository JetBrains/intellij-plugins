// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation;

import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static java.util.Arrays.asList;

public class GotoSymbolTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "symbol/" + getTestName(true);
  }

  public void testElementSelector() {
    myFixture.configureByFiles("my-table.component.ts", "package.json");
    doTest("app-my-table", true,
           "MyTableComponent (my-table.component.ts) - TypeScriptClass",
           "app-my-table (MyTableComponent, my-table.component.ts) - ElementDirectiveSelector<app-my-table>");
  }

  public void testAttributeSelector() {
    myFixture.configureByFiles("my-table.component.ts", "package.json");
    doTest("app-my-table", true,
           "MyTableComponent (my-table.component.ts) - TypeScriptClass",
           "app-my-table (MyTableComponent, my-table.component.ts) - AttributeDirectiveSelector<app-my-table>");
  }

  public void testAttrAndElementSelector() {
    myFixture.configureByFiles("my-table.component.ts", "package.json");
    doTest("app-my-table", true,
           "MyTableComponent (my-table.component.ts) - TypeScriptClass",
           "app-my-table (MyTableComponent, my-table.component.ts) - AttributeDirectiveSelector<app-my-table>",
           "app-my-table (MyTableComponent, my-table.component.ts) - ElementDirectiveSelector<app-my-table>");
  }

  public void testPipe() {
    myFixture.configureByFiles("foo.pipe.ts", "package.json");
    doTest("foo", false,
           "foo (foo.pipe.ts)");
  }

  private void doTest(@NotNull String name, boolean detailed, @NotNull String... expectedItems) {
    ((PsiManagerEx)myFixture.getPsiManager()).setAssertOnFileLoadingFilter(VirtualFileFilter.ALL, myFixture.getTestRootDisposable());

    GotoSymbolModel2 model = new GotoSymbolModel2(myFixture.getProject());

    assertContainsElements(asList(model.getNames(false)), name);
    final ArrayList<String> actual = ContainerUtil.newArrayList();
    for (Object o : model.getElementsByName(name, false, "")) {
      if (o instanceof NavigationItem) {
        final ItemPresentation presentation = ((NavigationItem)o).getPresentation();
        assertNotNull(presentation);
        actual.add(presentation.getPresentableText() + " " + presentation.getLocationString() +
                   (detailed ? " - " + o.toString() : ""));
      }
    }
    assertSameElements(actual, expectedItems);
  }
}
