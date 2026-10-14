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
package com.intellij.protobuf.lang.resolve;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.psi.PbEnumValue;
import com.intellij.protobuf.lang.psi.PbField;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.QualifiedName;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Tests for injected text format references. */
@TestApplication
public class PbTextReferenceTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;
  private String testName;

  @BeforeEach
  void setUp(TestInfo testInfo, @TestDisposable Disposable disposable) throws Exception {
    myFixture = codeInsightFixture.get();
    testName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    myFixture.addFileToProject(
        TestUtils.OPENSOURCE_DESCRIPTOR_PATH, TestUtils.getOpensourceDescriptorText());
    TestUtils.addTestFileResolveProvider(
        myFixture.getProject(), TestUtils.OPENSOURCE_DESCRIPTOR_PATH, disposable);
    EdtTestUtil.runInEdtAndWait(TestUtils::registerTestdataFileExtension);
  }

  private PsiElement resolve() {
    String filename = "lang/options/" + testName + ".proto.testdata";
    PsiReference ref = myFixture.getReferenceAtCaretPosition(filename);
    assertNotNull(ref);
    return ReadAction.computeBlocking(() -> ref.resolve());
  }

  @Test
  public void testTextTopLevelFieldReference() {
    assertIsField(resolve(), "foo.bar.MyType.nested");
  }

  @Test
  public void testTextNestedFieldReference() {
    assertIsField(resolve(), "foo.bar.MyNestedType.str");
  }

  @Test
  public void testTextEnumValueReference() {
    assertIsEnumValue(resolve(), "foo.bar.FOO");
  }

  @Test
  public void testTextEnumNumberReference() {
    assertIsEnumValue(resolve(), "foo.bar.FOO");
  }

  @Test
  public void testTextExtensionNameReference() {
    assertIsField(resolve(), "foo.bar.ext_opt");
  }

  @Test
  public void testTextAnyTypeReference() {
    myFixture.configureByFile("lang/options/any.proto");
    assertIsMessage(resolve(), "foo.bar.MyType");
  }

  @Test
  public void testTextAnyValueReference() {
    myFixture.configureByFile("lang/options/any.proto");
    assertIsField(resolve(), "foo.bar.MyType.value");
  }

  @Test
  public void testTextAnyDirectValueReference() {
    myFixture.configureByFile("lang/options/any.proto");
    assertIsField(resolve(), "foo.bar.MyType.value");
  }

  @Test
  public void testTextNestedAnyValueReference() {
    myFixture.configureByFile("lang/options/any.proto");
    assertIsField(resolve(), "foo.bar.AnyType2.bar");
  }

  @Test
  public void testTextFieldDoesNotResolveExtensionField() {
    // A non-extension field name should not resolve extension fields in the target type.
    assertNull(resolve());
  }

  @Test
  public void testAggregateExtensionImport() {
    myFixture.configureByFile("lang/options/constraints.proto");
    myFixture.configureByFile("lang/options/validation.proto");
    assertIsField(resolve(), "ext_bug.validation.InRange.min");
  }

  @Test
  public void testTextGroupDefinitionFieldOption() {
    assertIsField(resolve(), "foo.bar.OptionType.mygroupoption");
  }

  @Test
  public void testTextGroupDefinitionFieldOptionUnresolved() {
    assertNull(resolve());
  }

  @Test
  public void testTextGroupDefinitionMemberOption() {
    assertIsField(resolve(), "foo.bar.OptionType.MyGroupOption.zz");
  }

  private static void assertIsField(PsiElement target, String name) {
    assertInstanceOf(PbField.class, target);
    QualifiedName qualifiedName = ReadAction.computeBlocking(() -> ((PbField) target).getQualifiedName());
    assertNotNull(qualifiedName);
    assertEquals(name, qualifiedName.toString());
  }

  private static void assertIsMessage(PsiElement target, String name) {
    assertInstanceOf(PbMessageType.class, target);
    QualifiedName qualifiedName = ReadAction.computeBlocking(() -> ((PbMessageType) target).getQualifiedName());
    assertNotNull(qualifiedName);
    assertEquals(name, qualifiedName.toString());
  }

  private static void assertIsEnumValue(PsiElement target, String name) {
    assertInstanceOf(PbEnumValue.class, target);
    QualifiedName qualifiedName = ReadAction.computeBlocking(() -> ((PbEnumValue) target).getQualifiedName());
    assertNotNull(qualifiedName);
    assertEquals(name, qualifiedName.toString());
  }
}
