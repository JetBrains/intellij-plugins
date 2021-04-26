/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.highlighter;

import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

public class PbHighlightingAnnotatorTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.addFileToProject(
        TestUtils.OPENSOURCE_DESCRIPTOR_PATH, TestUtils.getOpensourceDescriptorText());
    TestUtils.addTestFileResolveProvider(
        getProject(), TestUtils.OPENSOURCE_DESCRIPTOR_PATH, getTestRootDisposable());
    TestUtils.registerTestdataFileExtension();
  }

  @Override
  public String getTestDataPath() {
    return super.getTestDataPath();
  }

  public void testAnnotator() {
    myFixture.configureByFile("ide/highlighter/TopLevelString.proto.testdata");
    myFixture.testFile("ide/highlighter/Highlighted.proto.testdata").checkSymbolNames().test();
  }
}
