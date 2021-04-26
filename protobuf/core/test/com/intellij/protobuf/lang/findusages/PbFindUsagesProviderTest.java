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
package com.intellij.protobuf.lang.findusages;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.psi.PbSymbol;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;
import static com.intellij.protobuf.TestUtils.notNull;

/** Tests for {@link PbFindUsagesProvider}. */
public class PbFindUsagesProviderTest extends PbCodeInsightFixtureTestCase {

  private static final UsageTypeProvider USAGE_TYPE_PROVIDER = new PbUsageTypeProvider();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.addTestFileResolveProvider(getProject(), getTestRootDisposable());
    TestUtils.registerTestdataFileExtension();
  }

  private String syntaxForTest = "proto3";
  private String packageForTest = "plugin.test";

  /** Override the default syntax for the test. */
  private void withSyntax(String syntaxForTest) {
    this.syntaxForTest = syntaxForTest;
  }

  /** Override the default package for the test. */
  private void withPackage(String packageForTest) {
    this.packageForTest = packageForTest;
  }

  public void testMessageNonQualified() {
    PsiFile fileUser =
        createFile(
            "user.proto",
            "import \"definer.proto\";",
            "message M2 {",
            "  M1 user = 777;",
            "  int32 M1 = 2;",
            "}");
    PsiFile fileDef =
        createFile("definer.proto", "message M1" + CARET_TAG + " {", "  int32 M1 = 1;", "}");
    Collection<UsageInfo> usageInfos = findUsagesAtCaret(fileDef);
    assertSize(1, usageInfos);
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("user.proto", notNull(usageInfo.getFile()).getName());
    assertEquals(fileUser.getText().indexOf("M1 user = 777;"), usageInfo.getNavigationOffset());
    assertEquals(2, notNull(usageInfo.getRangeInElement()).getLength());
    assertSameElements(describeUsageTypeNames(usageInfos), PbUsageTypeProvider.fieldDeclaration().toString());
  }

  public void testNestedMessagePreventsToplevelUsage() {
    createFile(
        "user.proto",
        "import \"definer.proto\";",
        "message M2 {",
        "  message M1 {",
        "  }",
        "  M1 not_user = 777;",
        "  int32 M1 = 2;",
        "}");
    PsiFile fileDef =
        createFile("definer.proto", "message M1" + CARET_TAG + " {", "  int32 M1 = 1;", "}");
    Collection<UsageInfo> usageInfos = findUsagesAtCaret(fileDef);
    assertEmpty(usageInfos);
  }

  public void testEnumNonQualified() {
    PsiFile fileUser =
        createFile(
            "user.proto",
            "import \"definer.proto\";",
            "message M2 {",
            "  E1 user = 777;",
            "  int32 E1 = 2;",
            "}");
    PsiFile fileDef = createFile("definer.proto", "enum E1" + CARET_TAG + " {", "  E1 = 0;", "}");
    Collection<UsageInfo> usageInfos = findUsagesAtCaret(fileDef);
    assertSize(1, usageInfos);
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("user.proto", notNull(usageInfo.getFile()).getName());
    assertEquals(fileUser.getText().indexOf("E1 user = 777;"), usageInfo.getNavigationOffset());
    assertSameElements(describeUsageTypeNames(usageInfos), PbUsageTypeProvider.fieldDeclaration().toString());
  }

  public void testFullyQualified() {
    withPackage("foo.bar");
    PsiFile fileUser =
        createFile(
            "user.proto",
            "import \"definer.proto\";",
            "message M1 {",
            "  .com.baz.Foo.Bar Foo = 777;",
            "}");
    withPackage("com.baz");
    PsiFile fileDef =
        createFile(
            "definer.proto",
            "message Foo" + CARET_TAG + " {",
            "  enum Bar {X = 0;}",
            "}",
            "message B {",
            "  message Foo {",
            "     .com.baz.Foo Foo = 123;",
            "  }",
            "}",
            "service S {",
            "  rpc M(.com.baz.Foo) returns (B.Foo);",
            "}",
            "extend .com.baz.Foo {}");
    Collection<UsageInfo> usageInfos = findUsagesAtCaret(fileDef);
    assertSize(4, usageInfos);
    assertSameElements(
        describeUsages(usageInfos),
        "definer.proto:114",
        "definer.proto:164",
        "definer.proto:204",
        "user.proto:84");
    assertEquals(114, fileDef.getText().indexOf("Foo Foo = 123;"));
    assertEquals(164, fileDef.getText().indexOf("Foo) returns"));
    assertEquals(204, fileDef.getText().indexOf("Foo {}"));
    assertEquals(84, fileUser.getText().indexOf("Foo.Bar Foo = 777;"));
    assertSameElements(
        describeUsageTypeNames(usageInfos),
        PbUsageTypeProvider.fieldDeclaration().toString(),
        PbUsageTypeProvider.serviceType().toString(),
        PbUsageTypeProvider.extendDefinition().toString(),
        PbUsageTypeProvider.fieldDeclaration().toString());
  }

  public void testMessageNamedMessage() {
    withSyntax("proto2");
    PsiFile file =
        createFile(
            "def_and_use.proto",
            "message message" + CARET_TAG + " {",
            "  optional message field = 777;",
            "}");
    Collection<UsageInfo> usageInfos = findUsagesAtCaret(file);
    assertSize(1, usageInfos);
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("def_and_use.proto", notNull(usageInfo.getFile()).getName());
    assertEquals(file.getText().indexOf("message field = 777;"), usageInfo.getNavigationOffset());
  }

  public void testFieldAsCustomOption() {
    PsiFile fileUser =
        createFile(
            "user.proto",
            "import \"definer.proto\";",
            "message M1 {",
            "  option (moo) = true;",
            "  int32 f1 = 1;",
            "}",
            "message M2 {",
            "  bool moo = 1;",
            "}");
    PsiFile fileDef =
        createFile(
            "definer.proto",
            "extend proto2.MessageOptions {",
            "  bool moo" + CARET_TAG + " = 9001;",
            "}");
    Collection<UsageInfo> usageInfos = findUsagesAtCaret(fileDef);
    assertSize(1, usageInfos);
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("user.proto", notNull(usageInfo.getFile()).getName());
    assertEquals(fileUser.getText().indexOf("moo) = true;"), usageInfo.getNavigationOffset());
    assertSameElements(describeUsageTypeNames(usageInfos), PbUsageTypeProvider.optionExpression().toString());
  }

  public void testPackageName() {
    withPackage("com.foo.bar");
    PsiFile fileUser =
        createFile(
            "user.proto",
            "import \"define_enum.proto\";",
            "import \"define_message.proto\";",
            "message M1 {",
            "  com.abc.xyz.M1 f1 = 1;",
            "  com.abc.E1 f2 = 2;",
            "}");
    withPackage("com.abc");
    createFile("define_enum.proto", "enum E1 {", "  A = 0;", "}");
    withPackage("com.abc.xyz");
    PsiFile fileDefMessage = createFile("define_message.proto", "message M1 {", "}");
    Collection<UsageInfo> usageInfos = findUsagesAtText(fileDefMessage, "abc.xyz");
    assertSize(1, usageInfos);
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("user.proto", notNull(usageInfo.getFile()).getName());
    assertEquals(fileUser.getText().indexOf("abc.xyz.M1 f1 = 1;"), usageInfo.getNavigationOffset());
    assertSameElements(describeUsageTypeNames(usageInfos), PbUsageTypeProvider.fieldDeclaration().toString());
  }

  private PsiFile createFile(String filename, String... fileContents) {
    return myFixture.configureByText(
        filename,
        TestUtils.makeFileWithSyntaxAndPackage(syntaxForTest, packageForTest, fileContents));
  }

  private Collection<UsageInfo> findUsagesAtCaret(PsiFile fileWithCaret) {
    return findUsagesAtOffset(fileWithCaret, myFixture.getCaretOffset());
  }

  private Collection<UsageInfo> findUsagesAtText(PsiFile file, String searchString) {
    return findUsagesAtOffset(file, file.getText().indexOf(searchString));
  }

  private Collection<UsageInfo> findUsagesAtOffset(PsiFile file, int offset) {
    PbSymbol atCaret = PsiTreeUtil.getParentOfType(file.findElementAt(offset), PbSymbol.class);
    assert atCaret != null;
    return myFixture.findUsages(atCaret);
  }

  private static Collection<String> describeUsages(Collection<UsageInfo> usageInfos) {
    return usageInfos
        .stream()
        .map(
            usageInfo ->
                notNull(usageInfo.getFile()).getName() + ":" + usageInfo.getNavigationOffset())
        .collect(Collectors.toList());
  }

  private static Collection<String> describeUsageTypeNames(Collection<UsageInfo> usageInfos) {
    return usageInfos
        .stream()
        .map(
            usageInfo -> {
              UsageType usageType = USAGE_TYPE_PROVIDER.getUsageType(usageInfo.getElement());
              return usageType != null ? usageType.toString() : UsageType.UNCLASSIFIED.toString();
            })
        .collect(Collectors.toList());
  }
}
