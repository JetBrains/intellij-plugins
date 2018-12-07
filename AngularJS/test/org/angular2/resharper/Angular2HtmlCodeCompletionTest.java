// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@TestDataPath("$R#_COMPLETION_TEST_ROOT/Angular2Html")
public class Angular2HtmlCodeCompletionTest extends Angular2ReSharperCompletionTestBase {

  private static final Set<String> TESTS_TO_SKIP = ContainerUtil.newHashSet(
    "test001",
    "test002",
    "test003",
    "test004",
    "test005",
    "test006",
    "test007",
    "test008",
    "test009",
    "test010",
    "test011",
    "test012",
    "test013",
    "test014",
    "test015"
  );

  @Override
  protected List<String> doGetExtraFiles() {
    List<String> extraFiles = super.doGetExtraFiles();
    extraFiles.add("style.css");
    return extraFiles;
  }

  @Override
  protected boolean isExcluded(@NotNull String testName) {
    return TESTS_TO_SKIP.contains(testName);
  }

  @Override
  protected void doSingleTest(@NotNull String testFile, @NotNull String path) throws Exception {
    myFixture.copyFileToProject("../../package.json", "package.json");
    super.doSingleTest(testFile, path);
  }
}
