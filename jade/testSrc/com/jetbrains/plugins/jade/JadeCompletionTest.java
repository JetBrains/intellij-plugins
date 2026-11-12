// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.idea.IJIgnore;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestApplication
public class JadeCompletionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;
  private String myTestNameUpper;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    String methodName = testInfo.getTestMethod().orElseThrow().getName();
    myTestName = PlatformTestUtil.getTestName(methodName, true);
    myTestNameUpper = PlatformTestUtil.getTestName(methodName, false);
    fixture.get().setTestDataPath(JadeTestUtil.getBaseTestDataPath() + "/completion/");
  }

  @Test
  public void testTag1() {
    defaultTest(null);
  }

  @Test
  public void testDoctype() {
    configureByFile(myTestName + ".jade");
    complete();
    assertCompletionContains(true, "html", "plist", "transitional", "strict", "frameset", "1.1", "mobile");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testVariable1() {
    defaultTest(null);
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testVariable2() {
    defaultTest(null);
  }

  @Test
  public void testNoTagClosing() {
    defaultTest(null);
  }

  @Test
  public void testStatement1() {
    configureByFile(myTestName + ".jade");
    complete();
    assertCompletionContains(true, "if", "else", "else if", "until", "while", "unless", "each", "for", "case", "when", "extends", "include",
                             "default", "yield");
    selectItem("if", (char)0);
    fixture.get().checkResultByFile(myTestName + "_after.jade");
  }

  @Test
  public void testStatement2() {
    defaultTest("else if");
  }

  @Test
  public void testStatement3() {
    defaultTest("else");
  }

  @Test
  public void testStatement4() {
    defaultTest("each", ' ');
  }

  @Test
  public void testTagInsideStatement() {
    defaultTest(null);
  }

  @Test
  public void testExtends1() {
    CodeInsightTestFixture myFixture = fixture.get();
    configureByFile(myTestName + ".jade");
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
    assertCompletionContains(false, "c", myTestNameUpper);
    selectItem("foo", (char)0);
    myFixture.checkResultByFile(myTestName + "_after.jade");
  }

  @Test
  public void testInclude1() {
    CodeInsightTestFixture myFixture = fixture.get();
    configureByFile(myTestName + ".jade");
    EdtTestUtil.runInEdtAndWait(() -> ApplicationManager.getApplication().runWriteAction(() -> {
      PsiDirectory dir = myFixture.getFile().getParent();
      assert dir != null;
      dir.createSubdirectory("foo");
      dir.createSubdirectory("bar");
      dir.createFile("a.jade");
      dir.createFile("z123.jade");
      dir.createFile("c.txt");
      dir.createFile("ppp.js");
    }));

    complete();
    assertCompletionContains(true, "foo", "bar", "a", "z123", "c.txt", "ppp.js");
    assertCompletionContains(false, myTestNameUpper);
    selectItem("ppp.js", (char)0);
    myFixture.checkResultByFile(myTestName + "_after.jade");
  }

  @Test
  public void testMixinName() {
    defaultTest(null);
  }

  @Test
  public void testAttribute1() {
    defaultTest(null);
  }

  @Test
  public void testAttribute2() {
    defaultTest(null);
  }

  @Test
  public void testClassSelector() {
    configureByFile(myTestName + ".jade");
    complete();
    assertCompletionContains(true, "qe45", "someCls");
    selectItem("qe45", (char)0);
    fixture.get().checkResultByFile(myTestName + "_after.jade");
  }

  private void defaultTest(@Nullable String itemToSelect) {
    defaultTest(itemToSelect, (char)0);
  }

  protected void configureByFile(final String filePath) {
    fixture.get().configureByFile(filePath);
  }

  private void complete() {
    fixture.get().complete(CompletionType.BASIC);
  }

  private void defaultTest(@Nullable String itemToSelect, char ch) {
    CodeInsightTestFixture myFixture = fixture.get();
    configureByFile(myTestName + ".jade");
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
    myFixture.checkResultByFile(myTestName + "_after.jade");
  }

  private void assertCompletionContains(boolean include, String... items) {
    List<String> strings = fixture.get().getLookupElementStrings();
    assertNotNull(strings, "No lookups, probably did not invoke completion");
    for (String item : items) {
      assertTrue(include == strings.contains(item),
                 "Completion list " + (include ? "should" : "should not") + " contain '" + item + "' item");
    }
  }

  private void selectItem(String lookupString, char ch) {
    CodeInsightTestFixture myFixture = fixture.get();
    final LookupElement[] lookupElements = myFixture.getLookupElements();
    assertNotNull(lookupElements, "No lookups, probably did not invoke completion");

    for (LookupElement item : lookupElements) {
      if (lookupString.equals(item.getLookupString())) {
        EdtTestUtil.runInEdtAndWait(() -> {
          myFixture.getLookup().setCurrentItem(item);
          myFixture.finishLookup(ch);
        });
        return;
      }
    }

    fail("item '" + lookupString + "' not found");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testScript1() {
    defaultTest("length");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testScript2() {
    defaultTest("length");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testScript3() {
    defaultTest("length");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testScript4() {
    defaultTest("length");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testScriptTopLevel1() {
    defaultTest("console");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testScriptTopLevel2() {
    defaultTest("console");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testFilterTopLevel() {
    defaultTest("console");
  }

  @Test
  public void testScriptNoClassAuto() {
    CodeInsightTestFixture myFixture = fixture.get();
    configureByFile(myTestName + ".jade");
    myFixture.complete(CompletionType.BASIC, 0);
    assertCompletionContains(false, "myClass1", "myClass2");
    myFixture.complete(CompletionType.BASIC, 1);
    assertCompletionContains(true, "myClass1", "myClass2");
  }

  @Test
  public void testClassSelectorPug() {
    configureByFile("classSelector.pug");
    complete();
    assertCompletionContains(true, "qe45", "someCls");
    selectItem("qe45", (char)0);
    fixture.get().checkResultByFile("classSelector_after.pug");
  }

  @Test
  public void testBlock() {
    configureByFile("block.pug");
    complete();
    assertCompletionContains(true, "block");
    selectItem("block", (char)0);
    fixture.get().checkResultByFile("block_after.pug");
  }

  @Test
  public void testInBlock() {
    configureByFile("inBlock.pug");
    complete();
    assertCompletionContains(true, "title", "div");
    selectItem("div", (char)0);
    fixture.get().checkResultByFile("inBlock_after.pug");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testMixinsObjectDestructuring() {
    configureByFile(myTestName + ".jade");
    complete();
    assertCompletionContains(true, "title", "subtitle");
    selectItem("subtitle", (char)0);
    fixture.get().checkResultByFile(myTestName + "_after.jade");
  }
}
