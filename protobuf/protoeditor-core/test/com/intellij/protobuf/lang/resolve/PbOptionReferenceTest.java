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

/** Tests for {@link PbOptionNameReference}. */
@TestApplication
public class PbOptionReferenceTest {
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
  public void testCustomExtendedFieldOption() {
    assertIsField(resolve(), "foo.bar.crazy_extended_option_field");
  }

  @Test
  public void testCustomFieldOption() {
    assertIsField(resolve(), "foo.bar.MyType.crazy_option_field");
  }

  @Test
  public void testEnumOption() {
    assertIsField(resolve(), "google.protobuf.EnumOptions.allow_alias");
  }

  @Test
  public void testEnumValueOption() {
    assertIsField(resolve(), "google.protobuf.EnumValueOptions.deprecated");
  }

  @Test
  public void testFieldOption() {
    assertIsField(resolve(), "google.protobuf.FieldOptions.weak");
  }

  @Test
  public void testFileOption() {
    assertIsField(resolve(), "google.protobuf.FileOptions.java_package");
  }

  @Test
  public void testMessageOption() {
    assertIsField(resolve(), "google.protobuf.MessageOptions.deprecated");
  }

  @Test
  public void testMethodOption() {
    assertIsField(resolve(), "google.protobuf.MethodOptions.deprecated");
  }

  @Test
  public void testOneofCustomOption() {
    assertIsField(resolve(), "foo.bar.test_oneof_option");
  }

  @Test
  public void testServiceOption() {
    assertIsField(resolve(), "google.protobuf.ServiceOptions.deprecated");
  }

  @Test
  public void testMapKeyOption() {
    assertIsField(resolve(), "foo.bar.TestMessage.SomeMapEntry.key");
  }

  @Test
  public void testMapValueOption() {
    assertIsField(resolve(), "foo.bar.TestMessage.SomeMapEntry.value");
  }

  @Test
  public void testGroupDefinitionFieldOption() {
    assertIsField(resolve(), "foo.bar.mygroupoption");
  }

  @Test
  public void testGroupDefinitionMemberOption() {
    assertIsField(resolve(), "foo.bar.MyGroupOption.zz");
  }

  @Test
  public void testEnumValueReference() {
    assertIsEnumValue(resolve(), "google.protobuf.FieldOptions.STRING_PIECE");
  }

  @Test
  public void testCustomOptionEnumValueReference() {
    assertIsEnumValue(resolve(), "foo.bar.FOO");
  }

  @Test
  public void testCustomOptionExtensionNameEnumValueReference() {
    assertIsEnumValue(resolve(), "foo.bar.FOO");
  }

  @Test
  public void testCustomOptionEnumKeywordNameReference() {
    assertIsEnumValue(resolve(), "foo.bar.inf");
  }

  @Test
  public void testOptionNameDoesNotResolveExtensionField() {
    // A non-extension option name should not resolve extension fields in the target type.
    assertNull(resolve());
  }

  @Test
  public void testFirstOfMultipleExtensionNames() {
    assertIsField(resolve(), "foo.bar.opt");
  }

  @Test
  public void testExtensionRangeOption() {
    assertIsField(resolve(), "foo.bar.value_ext");
  }

  private static void assertIsField(PsiElement target, String name) {
    assertInstanceOf(PbField.class, target);
    QualifiedName qualifiedName = ReadAction.computeBlocking(() -> ((PbField) target).getQualifiedName());
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
