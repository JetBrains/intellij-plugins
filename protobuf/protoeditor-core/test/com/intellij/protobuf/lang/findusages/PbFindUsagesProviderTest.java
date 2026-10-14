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

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.psi.PbSymbol;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.util.containers.ContainerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.protobuf.TestUtils.notNull;
import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests for {@link PbFindUsagesProvider}. */
@TestApplication
public class PbFindUsagesProviderTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp(@TestDisposable Disposable disposable) {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    TestUtils.addTestFileResolveProvider(myFixture.getProject(), disposable);
    EdtTestUtil.runInEdtAndWait(TestUtils::registerTestdataFileExtension);
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

  @Test
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
    assertEquals(1, usageInfos.size());
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("user.proto", fileName(usageInfo));
    assertEquals(text(fileUser).indexOf("M1 user = 777;"), navigationOffset(usageInfo));
    assertEquals(2, ReadAction.computeBlocking(() -> notNull(usageInfo.getRangeInElement()).getLength()));
    assertThat(describeUsageTypeNames(usageInfos)).containsExactly(PbUsageTypeProvider.fieldDeclaration().toString());
  }

  @Test
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
    assertThat(usageInfos).isEmpty();
  }

  @Test
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
    assertEquals(1, usageInfos.size());
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("user.proto", fileName(usageInfo));
    assertEquals(text(fileUser).indexOf("E1 user = 777;"), navigationOffset(usageInfo));
    assertThat(describeUsageTypeNames(usageInfos)).containsExactly(PbUsageTypeProvider.fieldDeclaration().toString());
  }

  @Test
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
    assertEquals(4, usageInfos.size());
    assertThat(describeUsages(usageInfos)).containsExactly("definer.proto:114",
        "definer.proto:164",
        "definer.proto:204",
        "user.proto:84");
    assertEquals(114, text(fileDef).indexOf("Foo Foo = 123;"));
    assertEquals(164, text(fileDef).indexOf("Foo) returns"));
    assertEquals(204, text(fileDef).indexOf("Foo {}"));
    assertEquals(84, text(fileUser).indexOf("Foo.Bar Foo = 777;"));
    assertThat(describeUsageTypeNames(usageInfos)).containsExactly(PbUsageTypeProvider.fieldDeclaration().toString(),
      PbUsageTypeProvider.serviceMethod().toString(),
      PbUsageTypeProvider.extendDefinition().toString(),
      PbUsageTypeProvider.fieldDeclaration().toString());
  }

  @Test
  public void testMessageNamedMessage() {
    withSyntax("proto2");
    PsiFile file =
        createFile(
            "def_and_use.proto",
            "message message" + CARET_TAG + " {",
            "  optional message field = 777;",
            "}");
    Collection<UsageInfo> usageInfos = findUsagesAtCaret(file);
    assertEquals(1, usageInfos.size());
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("def_and_use.proto", fileName(usageInfo));
    assertEquals(text(file).indexOf("message field = 777;"), navigationOffset(usageInfo));
  }

  @Test
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
    assertEquals(1, usageInfos.size());
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("user.proto", fileName(usageInfo));
    assertEquals(text(fileUser).indexOf("moo) = true;"), navigationOffset(usageInfo));
    assertThat(describeUsageTypeNames(usageInfos)).containsExactly(PbUsageTypeProvider.optionExpression().toString());
  }

  @Test
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
    assertEquals(1, usageInfos.size());
    UsageInfo usageInfo = notNull(ContainerUtil.getFirstItem(usageInfos));
    assertEquals("user.proto", fileName(usageInfo));
    assertEquals(text(fileUser).indexOf("abc.xyz.M1 f1 = 1;"), navigationOffset(usageInfo));
    assertThat(describeUsageTypeNames(usageInfos)).containsExactly(PbUsageTypeProvider.fieldDeclaration().toString());
  }

  private PsiFile createFile(String filename, String... fileContents) {
    PsiFile file = myFixture.addFileToProject(
        filename,
        TestUtils.makeFileWithSyntaxAndPackage(syntaxForTest, packageForTest, fileContents));
    myFixture.configureFromExistingVirtualFile(file.getVirtualFile());
    return myFixture.getFile();
  }

  private static String text(PsiFile file) {
    return ReadAction.computeBlocking(() -> file.getText());
  }

  private static String fileName(UsageInfo usageInfo) {
    return ReadAction.computeBlocking(() -> notNull(usageInfo.getFile()).getName());
  }

  private static int navigationOffset(UsageInfo usageInfo) {
    return ReadAction.computeBlocking(() -> usageInfo.getNavigationOffset());
  }

  private Collection<UsageInfo> findUsagesAtCaret(PsiFile fileWithCaret) {
    return findUsagesAtOffset(fileWithCaret, ReadAction.computeBlocking(() -> myFixture.getCaretOffset()));
  }

  private Collection<UsageInfo> findUsagesAtText(PsiFile file, String searchString) {
    return findUsagesAtOffset(file, text(file).indexOf(searchString));
  }

  private Collection<UsageInfo> findUsagesAtOffset(PsiFile file, int offset) {
    PbSymbol atCaret = ReadAction.computeBlocking(() -> PsiTreeUtil.getParentOfType(file.findElementAt(offset), PbSymbol.class));
    assert atCaret != null;
    return myFixture.findUsages(atCaret);
  }

  private static Collection<String> describeUsages(Collection<UsageInfo> usageInfos) {
    return ReadAction.computeBlocking(() -> usageInfos
        .stream()
        .map(
            usageInfo ->
                notNull(usageInfo.getFile()).getName() + ":" + usageInfo.getNavigationOffset())
        .collect(Collectors.toList()));
  }

  private static Collection<String> describeUsageTypeNames(Collection<UsageInfo> usageInfos) {
    return ReadAction.computeBlocking(() -> usageInfos
        .stream()
        .map(
            usageInfo -> {
              UsageType usageType = new PbUsageTypeProvider().getUsageType(usageInfo.getElement());
              return usageType != null ? usageType.toString() : UsageType.UNCLASSIFIED.toString();
            })
        .collect(Collectors.toList()));
  }
}
