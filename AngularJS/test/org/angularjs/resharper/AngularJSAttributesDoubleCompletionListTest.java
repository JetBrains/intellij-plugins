// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.resharper;

import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@TestDataPath("Double/List")
public class AngularJSAttributesDoubleCompletionListTest extends AngularJSBaseCompletionTest {

  private static final Set<String> IGNORED_TESTS = ContainerUtil.newHashSet(
    "ShowAllItemsOnDoubleCompletionWithNoPrefix",
    "ShowMatchingItemsOnDoubleCompletionWithPrefix"

  );

  @Override
  protected boolean isExcluded(@NotNull String testName) {
    return IGNORED_TESTS.contains(testName);
  }
}
