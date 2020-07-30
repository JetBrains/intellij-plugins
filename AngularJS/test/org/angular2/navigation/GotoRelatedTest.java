// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation;

import com.intellij.ide.actions.GotoRelatedSymbolAction;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import one.util.streamex.StreamEx;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;

@RunWith(com.intellij.testFramework.Parameterized.class)
public class GotoRelatedTest extends Angular2CodeInsightFixtureTestCase {

  @Parameterized.Parameter
  public String myTestDir;

  @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
  public static List<String> testNames(@NotNull Class<?> klass) {
    return StreamEx.of(new File(AngularTestUtil.getBaseTestDataPath(klass), "related").listFiles())
      .map(File::getName)
      .sorted()
      .toList();
  }

  @org.junit.runners.Parameterized.Parameters
  public static Collection<Object> data() {
    return new ArrayList<>();
  }

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "related/" + myTestDir;
  }

  @Test
  public void singleTest() {
    myFixture.setCaresAboutInjection(false);
    myFixture.copyDirectoryToProject(".", ".");
    VirtualFile testFile = myFixture.findFileInTempDir("test.txt");
    List<String> result = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(testFile.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty() || line.startsWith(" ")) {
          continue;
        }
        result.add(line);
        List<String> input = StringUtil.split(line, "#");
        assert !input.isEmpty();
        myFixture.configureFromTempProjectFile(input.get(0));
        if (input.size() > 1) {
          AngularTestUtil.moveToOffsetBySignature(input.get(1).replace("{caret}", "<caret>"), myFixture);
        }
        StreamEx.of(getStringifiedRelatedItems())
          .map(str -> " " + str)
          .into(result);
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    myFixture.configureFromTempProjectFile("test.txt");
    WriteAction.runAndWait(
      () -> myFixture.getDocument(myFixture.getFile()).setText(StringUtil.join(result, "\n")));
    myFixture.checkResultByFile("test.txt");
  }

  @NotNull
  private List<String> getStringifiedRelatedItems() {
    return GotoRelatedSymbolAction.getItems(myFixture.getFile(), myFixture.getEditor(), null)
      .stream()
      .filter(item -> item.getGroup().equals("Angular Component"))
      .map(item -> {
        ItemPresentation presentation = getPresentation(item.getElement());
        PsiFile file = item.getElement().getContainingFile();
        String name = notNull(item.getCustomName(), SymbolPresentationUtil.getSymbolPresentableText(item.getElement()));
        String location = file != null && !name.equals(file.getName())
                          ? "(" + file.getName() + ")"
                          : null;
        return item.getMnemonic() + ". "
               + name + " "
               + notNull(item.getCustomContainerName(), location) + " <"
               + presentation.getPresentableText() + ", "
               + doIfNotNull(presentation.getLocationString(), FileUtil::toSystemIndependentName) + ">";
      })
      .collect(Collectors.toList());
  }

  @NotNull
  private static ItemPresentation getPresentation(@NotNull PsiElement element) {
    if (element instanceof NavigationItem) {
      return ((NavigationItem)element).getPresentation();
    }
    return (ItemPresentation)element;
  }
}
