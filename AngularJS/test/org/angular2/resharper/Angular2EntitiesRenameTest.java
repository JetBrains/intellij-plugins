// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.resharper.ReSharperParameterizedTestCase;
import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.testFramework.Parameterized;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webSymbols.WebTestUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(value = Parameterized.class)
public class Angular2EntitiesRenameTest extends ReSharperParameterizedTestCase {

  private static final Set<String> TESTS_TO_SKIP = ContainerUtil.newHashSet(
    "test023", //insufficient input/output refactoring support
    "test024", //insufficient input/output refactoring support
    "test025", //insufficient input/output refactoring support
    "test026", //insufficient input/output refactoring support
    "test027", //insufficient input/output refactoring support
    "test028", //insufficient input/output refactoring support
    "test029", //insufficient input/output refactoring support
    "test030", //insufficient input/output refactoring support
    "test031", //insufficient input/output refactoring support
    "test032", //insufficient input/output refactoring support
    "test039", //insufficient input/output refactoring support
    "test041", //insufficient input/output refactoring support
    "test043"  //insufficient input/output refactoring support
  );

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
  public static List<String> testNames(@NotNull Class<?> klass) throws Exception {
    return ReSharperTestUtil.getTestParamsFromSubPath(callFindTestData(klass))
      .stream()
      .map(name -> StringUtil.split(name, ".").get(0))
      .distinct()
      .collect(Collectors.toList());
  }

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static String findTestData(@NotNull Class<?> klass) {
    return AngularTestUtil.getBaseTestDataPath(klass)
           + "/Refactorings/Rename/Angular2";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    AngularTestUtil.enableAstLoadingFilter(this);
  }

  @Override
  protected boolean isExcluded() {
    return TESTS_TO_SKIP.contains(getName());
  }

  @Override
  protected void doSingleTest(String testFile, String path) throws IOException {
    myFixture.copyFileToProject("../../../package.json", "package.json");
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
    if (WebTestUtil.canRenameWebSymbolAtCaret(myFixture)) {
      WebTestUtil.renameWebSymbol(myFixture, "zzz");
    }
    else {
      PsiElement targetElement = TargetElementUtil.findTargetElement(
        myFixture.getEditor(),
        TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
      targetElement = RenamePsiElementProcessor.forElement(targetElement).substituteElementToRename(targetElement, myFixture.getEditor());
      RenameProcessor renameProcessor =
        new RenameProcessor(myFixture.getProject(), targetElement, "zzz", false, false);
      renameProcessor.run();

      WriteCommandAction
        .runWriteCommandAction(getProject(), () -> PostprocessReformattingAspect.getInstance(getProject()).doPostponedFormatting());

      FileDocumentManager.getInstance().saveAllDocuments();
    }

    for (File f : testDir.listFiles()) {
      if (f.getName().startsWith(testNamePrefix) && f.getName().endsWith(".gold")) {
        myFixture.checkResultByFile(StringUtil.trimEnd(f.getName(), ".gold"),
                                    f.getName(), true);
      }
    }
  }
}
