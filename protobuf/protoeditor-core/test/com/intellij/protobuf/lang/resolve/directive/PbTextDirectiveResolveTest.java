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
package com.intellij.protobuf.lang.resolve.directive;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.psi.PbField;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbMessageType;

import static com.google.common.truth.Truth.assertThat;

/** Tests for references when using comment-based format directives in text format files. */
public class PbTextDirectiveResolveTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.addTestFileResolveProvider(getProject(), getTestRootDisposable());
  }

  private PsiElement resolve() {
    String filename = "lang/resolve/directive/" + getTestName(false) + ".pb";
    PsiReference ref = myFixture.getReferenceAtCaretPosition(filename);
    assertThat(ref).isNotNull();
    return ref.resolve();
  }

  public void testFileRef() {
    myFixture.configureByFile("lang/resolve/root_message.proto");
    PsiElement target = resolve();
    assertThat(target).isInstanceOf(PbFile.class);
  }

  public void testMessageRef() {
    myFixture.configureByFile("lang/resolve/root_message.proto");
    PsiElement target = resolve();
    assertThat(target).isInstanceOf(PbMessageType.class);
    QualifiedName qualifiedName = ((PbMessageType) target).getQualifiedName();
    assertThat(qualifiedName).isNotNull();
    assertThat(qualifiedName.toString()).isEqualTo("foo.bar.Message");
  }

  public void testPublicMessageRef() {
    myFixture.configureByFile("lang/resolve/root_message.proto");
    myFixture.configureByFile("lang/resolve/public_message.proto");
    PsiElement target = resolve();
    assertThat(target).isInstanceOf(PbMessageType.class);
    QualifiedName qualifiedName = ((PbMessageType) target).getQualifiedName();
    assertThat(qualifiedName).isNotNull();
    assertThat(qualifiedName.toString()).isEqualTo("foo.bar.Message");
  }

  public void testFieldRef() {
    myFixture.configureByFile("lang/resolve/root_message.proto");
    PsiElement target = resolve();
    assertThat(target).isInstanceOf(PbField.class);
    QualifiedName qualifiedName = ((PbField) target).getQualifiedName();
    assertThat(qualifiedName).isNotNull();
    assertThat(qualifiedName.toString()).isEqualTo("foo.bar.Message.number");
  }
}
