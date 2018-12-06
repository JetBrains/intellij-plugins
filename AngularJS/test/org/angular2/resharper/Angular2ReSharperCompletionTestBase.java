// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper;

import com.intellij.lang.javascript.resharper.JSReSharperCompletionTestBase;
import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.util.ArrayUtil;
import org.angularjs.AngularTestUtil;

import java.util.*;

public abstract class Angular2ReSharperCompletionTestBase extends JSReSharperCompletionTestBase {

  public static final String BASE_PATH = "CodeCompletion/";

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  @Override
  protected String getExtension() {
    String basePath = getBasePath() + getName() + ".";
    for (String ext : new String[]{"ts", "html"}) {
      if (ReSharperTestUtil.fetchVirtualFile(getTestDataPath(), basePath + ext + ".gold", getTestRootDisposable(), false) != null) {
        return ext;
      }
    }
    throw new IllegalArgumentException("Gold file not found");
  }

  @Override
  protected void doTest() throws Exception {
    myFixture.copyFileToProject("package.json");
    super.doTest();
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

  @Override
  protected final Map<String, String[]> getExtraFiles() {
    return Collections.singletonMap(getName(), ArrayUtil.toStringArray(doGetExtraFiles()));
  }

  @Override
  protected boolean skipTestForData(Set<String> expectedNames, Set<String> realNames, Set<String> diff) {
    if (!super.skipTestForData(expectedNames, realNames, diff)) {
      diff.remove("$any");
      if (diff.isEmpty()) {
        return true;
      }
    }
    return false;
  }
}
