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
package com.intellij.protobuf.lang;

import com.intellij.psi.PsiFile;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** Test cases for {@link PbTextFileType}. */
public class PbTextFileTypeTest extends PbCodeInsightFixtureTestCase {

  // This should match the list of registered extensions in PbTextFileTypeFactory.
  private static final List<String> EXTENSIONS =
      Arrays.asList("pb", "textproto");

  public void testRegisteredExtensions() {
    for (String extension : EXTENSIONS) {
      PsiFile file = myFixture.configureByText("test." + extension, "");
      assertThat(file.getLanguage()).isInstanceOf(PbTextLanguage.class);
    }
  }
}
