// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract public class DartCodeInsightFixtureTestCase extends BasePlatformTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(getModule(), myFixture.getProjectDisposable(), false);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      VirtualFile root = ModuleRootManager.getInstance(getModule()).getContentRoots()[0];
      VirtualFile pubspec = root.findChild("pubspec.yaml");
      if (pubspec != null) {
        WriteAction.run(() -> pubspec.delete(this));
        List<String> toUnexclude = Arrays.asList(root.getUrl() + "/build", root.getUrl() + "/.pub", root.getUrl() + "/.dart_tool");
        ModuleRootModificationUtil.updateExcludedFolders(getModule(), root, toUnexclude, Collections.emptyList());
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
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  public void addStandardPackage(@NotNull final String packageName) {
    myFixture.copyDirectoryToProject("../packages/" + packageName, "packages/" + packageName);
  }
}
