// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.Parameterized;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ArrayUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(value = Parameterized.class)
public class Angular2EntitiesRenameTest extends LightPlatformCodeInsightFixtureTestCase {

  private static final String[] TESTS_TO_SKIP = new String[]{
    "test019",
    "test021",
    "test023",
    "test024",
    "test025",
    "test026",
    "test027",
    "test028",
    "test029",
    "test030",
    "test031",
    "test032",
    "test039",
    "test041",
    "test043",
  };

  @org.junit.runners.Parameterized.Parameter
  public String myChild;

  @org.junit.runners.Parameterized.Parameters
  public static List<Object> unused() {
    return Collections.emptyList();
  }

  @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
  public static List<String> data(@NotNull Class<?> klass) throws Exception {
    return ReSharperTestUtil.getTestParamsFromSubPath("Angular2", "Refactorings/Rename/", null, getTestBase(klass))
      .stream()
      .map(name -> StringUtil.split(name, ".").get(0))
      .distinct()
      .collect(Collectors.toList());
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  @After
  public void tearDown() {
    EdtTestUtil.runInEdtAndWait(() -> super.tearDown());
  }

  @Override
  public String getName() {
    return myChild;
  }

  @Test
  public void test() {
    EdtTestUtil.runInEdtAndWait(() -> doTest());
  }

  @Override
  protected String getTestDataPath() {
    return getTestBase(getClass()) + "Refactorings/Rename/Angular2";
  }

  protected void doTest() {
    myFixture.copyFileToProject("../../../package.json", "package.json");
    try {
      File testDir = new File(getTestDataPath());
      String testNamePrefix = getName() + ".";
      String fileWithCaret = null;
      for (File f : testDir.listFiles()) {
        if (f.getName().startsWith(testNamePrefix) && !f.getName().endsWith(".gold")) {
          String text = ReSharperTestUtil.loadAndConvertCaret(f.getName(), testDir.getPath());
          if (text.contains("<caret>")) {
            fileWithCaret = f.getName();
          }
          else {
            myFixture.configureByText(f.getName(), text);
          }
        }
      }
      assert fileWithCaret != null;
      myFixture.configureByText(fileWithCaret, ReSharperTestUtil.loadAndConvertCaret(fileWithCaret, testDir.getPath()));

      PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments();

      //perform rename
      PsiElement targetElement = TargetElementUtil.findTargetElement(
        myFixture.getEditor(),
        TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
      targetElement = RenamePsiElementProcessor.forElement(targetElement).substituteElementToRename(targetElement, myFixture.getEditor());
      RenameProcessor renameProcessor =
        new RenameProcessor(myFixture.getProject(), targetElement, "zzz", false, false);
      renameProcessor.run();

      WriteCommandAction
        .runWriteCommandAction(getProject(), () -> getProject().getComponent(PostprocessReformattingAspect.class).doPostponedFormatting());

      FileDocumentManager.getInstance().saveAllDocuments();

      for (File f : testDir.listFiles()) {
        if (f.getName().startsWith(testNamePrefix) && f.getName().endsWith(".gold")) {
          myFixture.checkResultByFile(StringUtil.trimEnd(f.getName(), ".gold"),
                                      f.getName(), true);
        }
      }
    }
    catch (AssertionError | RuntimeException ex) {
      if (ArrayUtil.contains(getName(), TESTS_TO_SKIP)) {
        Assume.assumeTrue("This test is ignored", false); // causes test to be ignored
      }
      throw ex;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (ArrayUtil.contains(getName(), TESTS_TO_SKIP)) {
      Assert.fail("Test is ignored but passed successfully");
    }
  }

  private static String getTestBase(Class<?> klass) {
    return AngularTestUtil.getBaseTestDataPath(klass);
  }
}
