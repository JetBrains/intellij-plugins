// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.resharper;

import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;

import java.util.Set;

@TestDataPath("List")
public class AngularJSAttributesCompletionListTest extends AngularJSBaseCompletionTest {

  private static final Set<String> IGNORED_TESTS = ContainerUtil.newHashSet(
    "ShowAbbreviationsWithMatchingPrefix",
    "ShowAbbreviationsWithNoPrefix",
    "ShowItemsWithCaretInMiddleOfCompletionPrefix",
    "ShowItemsWithExactAbbreviationMatch",
    "ShowItemsWithPatternAbbreviationMatch",
    "ShowItemsWithPatternIncludingAbbreviation",
    "ShowItemsWithPatternNotIncludingAbbreviation"
  );

  @Override
  protected boolean isExcluded() {
    return IGNORED_TESTS.contains(getName());
  }
}
