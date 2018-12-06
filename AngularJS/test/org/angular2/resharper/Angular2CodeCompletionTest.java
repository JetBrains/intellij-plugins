// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Angular2CodeCompletionTest extends Angular2ReSharperCompletionTestBase {

  public static final String SUB_PATH = "Angular2";

  private static final String[] TESTS_TO_SKIP = new String[]{
    "external/test001",
    "external/test002",
    "external/test003",
    "external/test004",
    "external/test005",
    "external/test006",
    "test004",
    "test006",
    "test007",
    "test008",
  };

  @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
  public static List<String> data(@NotNull Class<?> klass) throws Exception {
    return ReSharperTestUtil.getTestParamsFromSubPath(SUB_PATH, BASE_PATH, null,
                                                      AngularTestUtil.getBaseTestDataPath(klass));
  }

  @Override
  protected String getBasePath() {
    return ReSharperTestUtil.getRelativeTestPath(SUB_PATH, BASE_PATH);
  }

  @NotNull
  private VirtualFile getNodeModules() {
    VirtualFile nodeModules = ReSharperTestUtil.fetchVirtualFile(
      getTestDataPath(), getBasePath() + "/external/node_modules", getTestRootDisposable());
    assert nodeModules != null;
    return nodeModules;
  }

  @Override
  protected void doTest() throws Exception {
    if (getName().startsWith("external")) {
      PsiTestUtil.addSourceContentToRoots(myModule, getNodeModules());
    }
    super.doTest();
  }

  @Override
  public void tearDown() throws Exception {
    try {
      if (getName().startsWith("external")) {
        PsiTestUtil.removeContentEntry(myModule, getNodeModules());
      }
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  @Override
  protected List<String> doGetExtraFiles() {
    List<String> extraFiles = super.doGetExtraFiles();
    if (getName().startsWith("external")) {
      extraFiles.add("external/module.ts");
    }
    return extraFiles;
  }

  @Override
  protected String[] getItemsToSkip() {
    return TESTS_TO_SKIP;
  }
}
