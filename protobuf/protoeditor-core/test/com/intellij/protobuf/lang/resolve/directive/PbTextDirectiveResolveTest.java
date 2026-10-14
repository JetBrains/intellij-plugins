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

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.psi.PbField;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.QualifiedName;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.google.common.truth.Truth.assertThat;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

/** Tests for references when using comment-based format directives in text format files. */
@TestApplication
public class PbTextDirectiveResolveTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;
  private String testName;

  @BeforeEach
  void setUp(TestInfo testInfo, @TestDisposable Disposable disposable) {
    myFixture = codeInsightFixture.get();
    testName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    TestUtils.addTestFileResolveProvider(myFixture.getProject(), disposable);
  }

  private PsiElement resolve() {
    String filename = "lang/resolve/directive/" + testName + ".pb";
    PsiReference ref = myFixture.getReferenceAtCaretPosition(filename);
    assertThat(ref).isNotNull();
    return ReadAction.computeBlocking(() -> ref.resolve());
  }

  @Test
  public void testFileRef() {
    myFixture.configureByFile("lang/resolve/root_message.proto");
    PsiElement target = resolve();
    assertThat(target).isInstanceOf(PbFile.class);
  }

  @Test
  public void testMessageRef() {
    myFixture.configureByFile("lang/resolve/root_message.proto");
    PsiElement target = resolve();
    assertThat(target).isInstanceOf(PbMessageType.class);
    QualifiedName qualifiedName = ReadAction.computeBlocking(() -> ((PbMessageType) target).getQualifiedName());
    assertThat(qualifiedName).isNotNull();
    assertThat(qualifiedName.toString()).isEqualTo("foo.bar.Message");
  }

  @Test
  public void testPublicMessageRef() {
    myFixture.configureByFile("lang/resolve/root_message.proto");
    myFixture.configureByFile("lang/resolve/public_message.proto");
    PsiElement target = resolve();
    assertThat(target).isInstanceOf(PbMessageType.class);
    QualifiedName qualifiedName = ReadAction.computeBlocking(() -> ((PbMessageType) target).getQualifiedName());
    assertThat(qualifiedName).isNotNull();
    assertThat(qualifiedName.toString()).isEqualTo("foo.bar.Message");
  }

  @Test
  public void testFieldRef() {
    myFixture.configureByFile("lang/resolve/root_message.proto");
    PsiElement target = resolve();
    assertThat(target).isInstanceOf(PbField.class);
    QualifiedName qualifiedName = ReadAction.computeBlocking(() -> ((PbField) target).getQualifiedName());
    assertThat(qualifiedName).isNotNull();
    assertThat(qualifiedName.toString()).isEqualTo("foo.bar.Message.number");
  }
}
