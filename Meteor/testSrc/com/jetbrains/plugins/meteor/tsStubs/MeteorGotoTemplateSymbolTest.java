package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.util.ArrayUtil;

import java.util.Arrays;


public class MeteorGotoTemplateSymbolTest extends MeteorProjectTestBase {
  public void testGoto() {
    runGotoTest("myTemplateForHelper1");
  }

  private void runGotoTest(final String... expectedSymbols) {
    ApplicationManager.getApplication().runWriteAction(() -> {
      ChooseByNameModel model2 = new GotoSymbolModel2(getProject());
      String[] names = model2.getNames(false);

      for (String expectedSymbol : expectedSymbols) {
        assertTrue(expectedSymbol+": "+Arrays.asList(names), ArrayUtil.contains(expectedSymbol, names));
      }
    });
  }

  @Override
  protected void tuneFixture(ModuleFixtureBuilder moduleBuilder) {
    super.tuneFixture(moduleBuilder);
    moduleBuilder.addSourceContentRoot(getTestDataPath() + "/testGotoSymbol/module");
  }
}
