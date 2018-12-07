// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.lang.javascript.resharper.TypeScriptReSharperCompletionTestBase;
import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.util.ArrayUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Angular2ReSharperCompletionTestBase extends TypeScriptReSharperCompletionTestBase {

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static String findTestData(@NotNull Class<?> klass) {
    return AngularTestUtil.getBaseTestDataPath(klass)
           + "/CodeCompletion/"
           + StringUtil.trimStart(klass.getAnnotation(TestDataPath.class).value(),
                                  "$R#_COMPLETION_TEST_ROOT");
  }

  protected List<String> doGetExtraFiles() {
    List<String> extraFiles = new ArrayList<>();
    String basePath = getBasePath() + getName() + ".";
    for (String ext : new String[]{"ts", "html"}) {
      if (ReSharperTestUtil.fetchVirtualFile(getTestDataPath(), basePath + ext + ".gold", getTestRootDisposable(), false) == null
          && ReSharperTestUtil.fetchVirtualFile(getTestDataPath(), basePath + ext, getTestRootDisposable(), false) != null) {
        extraFiles.add(getName() + "." + ext);
      }
    }
    return extraFiles;
  }

  @NotNull
  @Override
  protected final Map<String, String[]> getExtraFiles() {
    return Collections.singletonMap(getName(), ArrayUtil.toStringArray(doGetExtraFiles()));
  }

  @Override
  protected boolean skipTestForData(@NotNull Set<String> expectedNames, @NotNull Set<String> realNames, @NotNull Set<String> diff) {
    if (!super.skipTestForData(expectedNames, realNames, diff)) {
      diff.remove("$any");
      if (diff.isEmpty()) {
        return true;
      }
    }
    return false;
  }
}
