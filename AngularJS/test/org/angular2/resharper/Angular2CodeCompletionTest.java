// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@TestDataPath("$R#_COMPLETION_TEST_ROOT/Angular2")
public class Angular2CodeCompletionTest extends Angular2ReSharperCompletionTestBase {

  private static final Set<String> TESTS_TO_SKIP = ContainerUtil.newHashSet(
    "test004" // improve ngFor completions
  );

  private static final Set<String> HIGH_PRIORITY_ONLY = ContainerUtil.newHashSet(
    "external/test003"
  );

  private static final Set<String> CAMEL_CASE_MATCH_ONLY = ContainerUtil.newHashSet(
    "external/test002",
    "external/test006"
  );

  @NotNull
  private VirtualFile getNodeModules() {
    VirtualFile nodeModules = ReSharperTestUtil.fetchVirtualFile(
      getTestDataPath(), getBasePath() + "/external/node_modules", getTestRootDisposable());
    assert nodeModules != null;
    return nodeModules;
  }

  @Override
  protected boolean shouldSkipItem(@NotNull LookupElement element) {
    if (element.getLookupString().startsWith("[(")) {
      return true;
    }
    if (HIGH_PRIORITY_ONLY.contains(getName())) {
      return !(element instanceof PrioritizedLookupElement)
             || ((PrioritizedLookupElement)element).getPriority() < Angular2AttributeDescriptor.AttributePriority.HIGH.getValue();
    }
    if (CAMEL_CASE_MATCH_ONLY.contains(getName())) {
      PsiElement el = myFixture.getFile().findElementAt(myFixture.getCaretOffset() - 1);
      String prefix = el.getText();
      if (new CamelHumpMatcher(prefix).matchingDegree(element.getLookupString()) < 800) {
        return true;
      }
    }
    return super.shouldSkipItem(element);
  }

  @Override
  protected boolean isExcluded() {
    return TESTS_TO_SKIP.contains(getName());
  }

  @Override
  protected void doSingleTest(@NotNull String testFile, @NotNull String path) throws Exception {
    if (getName().startsWith("external")) {
      WriteAction.runAndWait(() -> {
        VirtualFile nodeModules = getNodeModules();
        PsiTestUtil.addSourceContentToRoots(myModule, nodeModules);
        Disposer.register(myFixture.getTestRootDisposable(),
                          () -> PsiTestUtil.removeContentEntry(myModule, nodeModules));
      });
    }
    myFixture.copyFileToProject("../../package.json", "package.json");
    super.doSingleTest(testFile, path);
  }

  @Override
  protected List<String> doGetExtraFiles() {
    List<String> extraFiles = super.doGetExtraFiles();
    if (getName().startsWith("external")) {
      extraFiles.add("external/module.ts");
    }
    return extraFiles;
  }
}
