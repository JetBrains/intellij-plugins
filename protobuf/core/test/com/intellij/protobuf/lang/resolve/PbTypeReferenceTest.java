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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.protobuf.lang.psi.PbPackageName;
import org.junit.Assert;

/** Tests for {@link ProtoSymbolPathReference}. */
public class PbTypeReferenceTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.addFileToProject(
            TestUtils.OPENSOURCE_DESCRIPTOR_PATH, TestUtils.getOpensourceDescriptorText());
    TestUtils.addTestFileResolveProvider(
            getProject(), TestUtils.OPENSOURCE_DESCRIPTOR_PATH, getTestRootDisposable());
    TestUtils.registerTestdataFileExtension();
  }

  private PsiReference resolveRef() {
    String filename = "lang/resolve/" + getTestName(false) + ".proto.testdata";
    return myFixture.getReferenceAtCaretPosition(filename);
  }

  private PsiElement resolve() {
    PsiReference ref = resolveRef();
    assertNotNull(ref);
    return ref.resolve();
  }

  public void testNonQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  public void testFullyQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  public void testPartiallyQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Baz.Foo.Tom");
  }

  public void testPartialPackageNameType() {
    assertIsMessageType(resolve(), "foo.bar.Foo.Tom");
  }

  public void testIntermediateFullyQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  public void testIntermediateFullyQualifiedPackage() {
    assertIsPackage(resolve(), "foo.bar");
  }

  public void testIntermediatePartiallyQualifiedType() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  public void testIntermediatePartiallyQualifiedPackage() {
    assertIsPackage(resolve(), "foo.bar");
  }

  public void testIntermediateTypeOfUnresolvedType() {
    assertIsMessageType(resolve(), "foo.bar.baz.Foo");
  }

  public void testIntermediatePackageOfUnresolvedType() {
    assertIsPackage(resolve(), "foo.bar.baz");
  }

  public void testSameTypeAndFieldName() {
    assertIsMessageType(resolve(), "foo.bar.Foo");
  }

  public void testTypeDeclaredInOneof() {
    assertIsMessageType(resolve(), "foo.bar.Bar.SomeGroup");
  }

  public void testGeneratedMapEntry() {
    assertIsMessageType(resolve(), "foo.bar.Foo.SomeMapEntry");
  }

  public void testInnerMostSymbolWins() {
    assertNull("Reference should be unresolvable", resolve());
  }

  public void testFullyQualifiedBuiltInTypeName() {
    assertIsMessageType(resolve(), "string");
  }

  public void testUnqualifiedBuiltInTypeName() {
    assertNull("Built-in type should be unresolvable", resolveRef());
  }

  private static void assertIsMessageType(PsiElement target, String name) {
    Assert.assertTrue(target instanceof PbMessageType);
    QualifiedName qualifiedName = ((PbMessageType) target).getQualifiedName();
    Assert.assertNotNull(qualifiedName);
    Assert.assertEquals(name, qualifiedName.toString());
  }

  private static void assertIsPackage(PsiElement target, String name) {
    Assert.assertTrue(target instanceof PbPackageName);
    QualifiedName qualifiedName = ((PbPackageName) target).getQualifiedName();
    Assert.assertNotNull(qualifiedName);
    Assert.assertTrue(qualifiedName.matchesPrefix(QualifiedName.fromDottedString(name)));
  }
}
