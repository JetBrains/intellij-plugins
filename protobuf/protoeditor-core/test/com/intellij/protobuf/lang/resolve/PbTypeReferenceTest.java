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
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.protobuf.lang.psi.PbPackageName;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link ProtoSymbolPathReference}. */
@TestApplication
public class PbTypeReferenceTest {
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

  private PsiReference resolveRef() {
    String filename = "lang/resolve/" + testName + ".proto.testdata";
    return myFixture.getReferenceAtCaretPosition(filename);
  }

  private PsiElement resolve() {
    PsiReference ref = resolveRef();
    assertNotNull(ref);
    return ReadAction.computeBlocking(() -> ref.resolve());
  }

  @Test
  public void testNonQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  @Test
  public void testFullyQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  @Test
  public void testPartiallyQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Baz.Foo.Tom");
  }

  @Test
  public void testPartialPackageNameType() {
    assertIsMessageType(resolve(), "foo.bar.Foo.Tom");
  }

  @Test
  public void testIntermediateFullyQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  @Test
  public void testIntermediateFullyQualifiedPackage() {
    assertIsPackage(resolve(), "foo.bar");
  }

  @Test
  public void testIntermediatePartiallyQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  @Test
  public void testIntermediatePartiallyQualifiedPackage() {
    assertIsPackage(resolve(), "foo.bar");
  }

  @Test
  public void testIntermediateTypeOfUnresolvedType() {
    assertIsMessageType(resolve(), "foo.bar.baz.Foo");
  }

  @Test
  public void testIntermediatePackageOfUnresolvedType() {
    assertIsPackage(resolve(), "foo.bar.baz");
  }

  @Test
  public void testSameTypeAndFieldName() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  @Test
  public void testTypeDeclaredInOneof() {
    assertIsMessageType(resolve(), "foo.bar.Bar.SomeGroup");
  }

  @Test
  public void testGeneratedMapEntry() {
    assertIsMessageType(resolve(), "foo.bar.Foo.SomeMapEntry");
  }

  @Test
  public void testInnerMostSymbolWins() {
    assertNull(resolve(), "Reference should be unresolvable");
  }

  @Test
  public void testFullyQualifiedBuiltInTypeName() {
    assertIsMessageType(resolve(), "string");
  }

  @Test
  public void testUnqualifiedBuiltInTypeName() {
    assertNull(resolveRef(), "Built-in type should be unresolvable");
  }

  private static void assertIsMessageType(PsiElement target, String name) {
    assertInstanceOf(PbMessageType.class, target);
    QualifiedName qualifiedName = ReadAction.computeBlocking(() -> ((PbMessageType) target).getQualifiedName());
    assertNotNull(qualifiedName);
    assertEquals(name, qualifiedName.toString());
  }

  private static void assertIsPackage(PsiElement target, String name) {
    assertInstanceOf(PbPackageName.class, target);
    QualifiedName qualifiedName = ReadAction.computeBlocking(() -> ((PbPackageName) target).getQualifiedName());
    assertNotNull(qualifiedName);
    assertTrue(qualifiedName.matchesPrefix(QualifiedName.fromDottedString(name)));
  }
}
