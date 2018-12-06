// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.lang.resharper.ReSharperTestUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Angular2HtmlCodeCompletionTest extends Angular2ReSharperCompletionTestBase {

  private static final String SUB_PATH = "Angular2Html";
  private static final String[] TESTS_TO_SKIP = new String[]{
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
    "test015",
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

  @Override
  protected List<String> doGetExtraFiles() {
    List<String> extraFiles = super.doGetExtraFiles();
    extraFiles.add("style.css");
    return extraFiles;
  }

  @Override
  protected String[] getItemsToSkip() {
    return TESTS_TO_SKIP;
  }
}
