// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.resharper;

import com.intellij.testFramework.TestDataPath;
import com.intellij.util.containers.ContainerUtil;

import java.util.Set;

@TestDataPath("List")
public class AngularJSAttributesCompletionListTest extends AngularJSBaseCompletionTest {

  private static final Set<String> IGNORED_TESTS = ContainerUtil.newHashSet(
    "ShowAbbreviationsWithMatchingPrefix", //no support for data- and x- prefixes, no abbreviations
    "ShowAbbreviationsWithNoPrefix", //no abbreviations
    "ShowItemsWithCaretInMiddleOfCompletionPrefix", //no support for data- prefixes
    "ShowItemsWithExactAbbreviationMatch", //more items in WebStorm, e.g. ng-maxlength should not be on the list
    "ShowItemsWithPatternAbbreviationMatch", //no support for data-, no abbreviations
    "ShowItemsWithPatternIncludingAbbreviation", //no support for data-
    "ShowItemsWithPatternNotIncludingAbbreviation" //no support for data- and x- prefixes
  );

  @Override
  protected boolean isExcluded() {
    return IGNORED_TESTS.contains(getName());
  }


}
