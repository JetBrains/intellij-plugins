// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.resharper;

import com.intellij.lang.resharper.ReSharperCompletionTestCase;
import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.TestDataPath;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.*;

public abstract class AngularJSBaseCompletionTest extends ReSharperCompletionTestCase {

  private static final Map<String, String> VERSIONS = Map.of(
    "12","angular.1.2.28.js",
    "13","angular.1.3.15.js",
    "14","angular.1.4.0.js");

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static String findTestData(@NotNull Class<?> klass) {
    return AngularTestUtil.getBaseTestDataPath(klass)
           + "/CodeCompletion/"
           + StringUtil.trimStart(klass.getAnnotation(TestDataPath.class).value(),
                                  "$R#_COMPLETION_TEST_ROOT");
  }

  @com.intellij.testFramework.Parameterized.Parameters(name = "{0} - {1}")
  public static List<String[]> testNames2(@NotNull Class<?> klass) {
    ArrayList<String[]> result = new ArrayList<>();
    File testDataRoot = new File(callFindTestData(klass));
    for (File file : testDataRoot.listFiles()) {
      if (new File(file.getParentFile(), file.getName() + "14.gold").exists()) {
        for (String version: new String[] {"12","13","14"}) {
          result.add(new String[]{file.getName(), version});
        }
      }
    }
    result.sort(Comparator.comparing(item -> item[0]));
    return result;
  }

  @Parameterized.Parameter(1)
  public String myAngularVersion;

  @NotNull
  @Override
  protected List<String> getGoldSuffix() {
    return Collections.singletonList(myAngularVersion + ".gold");
  }

  @Override
  protected void doSingleTest(@NotNull String testFile, @NotNull String path) throws Exception {
    WriteAction.runAndWait(() -> {
      VirtualFile angularFile = ReSharperTestUtil.fetchVirtualFile(
        AngularTestUtil.getBaseTestDataPath(getClass()),VERSIONS.get(myAngularVersion),getTestRootDisposable());
      PsiTestUtil.addSourceContentToRoots(getModule(), angularFile);
      Disposer.register(myFixture.getTestRootDisposable(),
                        () -> PsiTestUtil.removeContentEntry(getModule(), angularFile));
    });
    super.doSingleTest(testFile, path);
  }

}
