// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JadeCompletionTest extends BasePlatformTestCase {

  @Override
  protected String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/completion/";
  }

  public void testTag1() {
    defaultTest(null);
  }

  public void testDoctype() {
    configureByFile(getTestName(true) + ".jade");
    complete();
    assertCompletionContains(true, "html", "plist", "transitional", "strict", "frameset", "1.1", "mobile");
  }

  public void testVariable1() {
    defaultTest(null);
  }

  public void testVariable2() {
    defaultTest(null);
  }

  public void testNoTagClosing() {
    defaultTest(null);
  }

  public void testStatement1() {
    configureByFile(getTestName(true) + ".jade");
    complete();
    assertCompletionContains(true, "if", "else", "else if", "until", "while", "unless", "each", "for", "case", "when", "extends", "include",
                             "default", "yield");
    selectItem("if", (char)0);
    myFixture.checkResultByFile(getTestName(true) + "_after.jade");
  }

  public void testStatement2() {
    defaultTest("else if");
  }

  public void testStatement3() {
    defaultTest("else");
  }

  public void testStatement4() {
    defaultTest("each", ' ');
  }

  public void testTagInsideStatement() {
    defaultTest(null);
  }

  public void testExtends1() {
    configureByFile(getTestName(true) + ".jade");
    WriteCommandAction.runWriteCommandAction(null, () -> {
      PsiDirectory dir = myFixture.getFile().getParent();
      assert dir != null;
      dir.createSubdirectory("foo");
      dir.createSubdirectory("bar");
      dir.createFile("a.jade");
      dir.createFile("z123.jade");
      dir.createFile("c.txt");
    });

    complete();
    assertCompletionContains(true, "foo", "bar", "a", "z123");
    assertCompletionContains(false, "c", getTestName(false));
    selectItem("foo", (char)0);
    myFixture.checkResultByFile(getTestName(true) + "_after.jade");
  }

  public void testInclude1() {
    configureByFile(getTestName(true) + ".jade");
    ApplicationManager.getApplication().runWriteAction(() -> {
      PsiDirectory dir = myFixture.getFile().getParent();
      assert dir != null;
      dir.createSubdirectory("foo");
      dir.createSubdirectory("bar");
      dir.createFile("a.jade");
      dir.createFile("z123.jade");
      dir.createFile("c.txt");
      dir.createFile("ppp.js");
    });

    complete();
    assertCompletionContains(true, "foo", "bar", "a", "z123", "c.txt", "ppp.js");
    assertCompletionContains(false, getTestName(false));
    selectItem("ppp.js", (char)0);
    myFixture.checkResultByFile(getTestName(true) + "_after.jade");
  }

  public void testMixinName() {
    defaultTest(null);
  }

  public void testAttribute1() {
    defaultTest(null);
  }

  public void testAttribute2() {
    defaultTest(null);
  }

  public void testClassSelector() {
    configureByFile(getTestName(true) + ".jade");
    complete();
    assertCompletionContains(true, "qe45", "someCls");
    selectItem("qe45", (char)0);
    myFixture.checkResultByFile(getTestName(true) + "_after.jade");
  }

  private void defaultTest(@Nullable String itemToSelect) {
    defaultTest(itemToSelect, (char)0);
  }

  protected void configureByFile(final String filePath) {
    myFixture.configureByFile(filePath);
  }

  private void complete() {
    myFixture.complete(CompletionType.BASIC);
  }

  private void defaultTest(@Nullable String itemToSelect, char ch) {
    configureByFile(getTestName(true) + ".jade");
    complete();
    if (itemToSelect != null) {
      selectItem(itemToSelect, ch);
    }
    else {
      LookupElement[] elements = myFixture.getLookupElements();
      if (elements != null) {
        fail("{" + StringUtil.join(elements, element -> element.getLookupString(), ", ") + "}");
      }
    }
    myFixture.checkResultByFile(getTestName(true) + "_after.jade");
  }

  private void assertCompletionContains(boolean include, String... items) {
    List<String> strings = myFixture.getLookupElementStrings();
    assertNotNull("No lookups, probably did not invoke completion", strings);
    for (String item : items) {
      assertTrue("Completion list " + (include ? "should" : "should not") + " contain '" + item + "' item",
                 include == strings.contains(item));
    }
  }

  private void selectItem(String lookupString, char ch) {
    final LookupElement[] lookupElements = myFixture.getLookupElements();
    assertNotNull("No lookups, probably did not invoke completion", lookupElements);

    for (LookupElement item : lookupElements) {
      if (lookupString.equals(item.getLookupString())) {
        myFixture.getLookup().setCurrentItem(item);
        myFixture.finishLookup(ch);
        return;
      }
    }

    fail("item '" + lookupString + "' not found");
  }

  public void testScript1() {
    defaultTest("length");
  }

  public void testScript2() {
    defaultTest("length");
  }

  public void testScript3() {
    defaultTest("length");
  }

  public void testScript4() {
    defaultTest("length");
  }

  public void testScriptTopLevel1() {
    defaultTest("console");
  }

  public void testScriptTopLevel2() {
    defaultTest("console");
  }

  public void testFilterTopLevel() {
    defaultTest("console");
  }

  public void testScriptNoClassAuto() {
    configureByFile(getTestName(true) + ".jade");
    myFixture.complete(CompletionType.BASIC, 0);
    assertCompletionContains(false, "myClass1", "myClass2");
    myFixture.complete(CompletionType.BASIC, 1);
    assertCompletionContains(true, "myClass1", "myClass2");
  }

  public void testClassSelectorPug() {
    configureByFile("classSelector.pug");
    complete();
    assertCompletionContains(true, "qe45", "someCls");
    selectItem("qe45", (char)0);
    myFixture.checkResultByFile("classSelector_after.pug");
  }

  public void testBlock() {
    configureByFile("block.pug");
    complete();
    assertCompletionContains(true, "block");
    selectItem("block", (char)0);
    myFixture.checkResultByFile("block_after.pug");
  }

  public void testInBlock() {
    configureByFile("inBlock.pug");
    complete();
    assertCompletionContains(true, "title", "div");
    selectItem("div", (char)0);
    myFixture.checkResultByFile("inBlock_after.pug");
  }

  public void testMixinsObjectDestructuring() {
    configureByFile(getTestName(true) + ".jade");
    complete();
    assertCompletionContains(true, "title", "subtitle");
    selectItem("subtitle", (char)0);
    myFixture.checkResultByFile(getTestName(true) + "_after.jade");
  }
}
