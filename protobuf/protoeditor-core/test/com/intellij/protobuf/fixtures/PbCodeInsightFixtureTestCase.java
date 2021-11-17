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
package com.intellij.protobuf.fixtures;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Disposer;
import com.intellij.protobuf.TestUtils;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/** Code insight test fixture for protoeditor tests. */
public abstract class PbCodeInsightFixtureTestCase extends BasePlatformTestCase {

  protected final Disposable testDisposable = new TestDisposable();

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + TestUtils.getTestdataPath();
  }

  @Override
  protected void tearDown() throws Exception {
    Disposer.dispose(testDisposable);
    super.tearDown();
  }

  public Editor getEditor() {
    return myFixture.getEditor();
  }

  public PsiFile getFile() {
    return myFixture.getFile();
  }
}
