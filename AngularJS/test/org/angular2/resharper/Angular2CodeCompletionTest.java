// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@TestDataPath("$R#_COMPLETION_TEST_ROOT/Angular2")
public class Angular2CodeCompletionTest extends Angular2ReSharperCompletionTestBase {

  private static final Set<String> TESTS_TO_SKIP = ContainerUtil.newHashSet(
    "external/test002",
    "external/test003",
    "external/test004",
    "external/test005",
    "external/test006",
    "test004",
    "test005",
    "test006",
    "test007",
    "test008"
  );

  @NotNull
  private VirtualFile getNodeModules() {
    VirtualFile nodeModules = ReSharperTestUtil.fetchVirtualFile(
      getTestDataPath(), getBasePath() + "/external/node_modules", getTestRootDisposable());
    assert nodeModules != null;
    return nodeModules;
  }

  @Override
  protected boolean isExcluded(@NotNull String testName) {
    return TESTS_TO_SKIP.contains(testName);
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
