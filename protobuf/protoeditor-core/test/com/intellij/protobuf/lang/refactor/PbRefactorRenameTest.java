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
package com.intellij.protobuf.lang.refactor;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

import java.io.IOException;

import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;

/**
 * Tests for the refactor rename action (handled by {@link
 * com.intellij.psi.PsiReferenceBase#handleElementRename(String)} and {@link
 * com.intellij.psi.PsiNamedElement#setName(String)}).
 */
public class PbRefactorRenameTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.addTestFileResolveProvider(getProject(), getTestRootDisposable());
    TestUtils.registerTestdataFileExtension();
  }

  public void testRenameMessage() throws IOException {
    PsiFile fileUser =
        createFile(
            "user.proto",
            "import \"definer.proto\";",
            "message M2 {",
            "  M1.M2.M3 user = 777;",
            "  int32 M2 = 2;",
            "}");
    PsiFile fileDef =
        createFile(
            "definer.proto",
            "message M1 {",
            "  message M2" + CARET_TAG + " {",
            "    message M3 {}",
            "    int32 M2 = 1;",
            "    M2 other_user = 999;",
            "  }",
            "}");
    renameElementAtCaret("MXYZ");
    assertFileContents(
        fileUser,
        "import \"definer.proto\";",
        "message M2 {",
        "  M1.MXYZ.M3 user = 777;",
        "  int32 M2 = 2;",
        "}");
    assertFileContents(
        fileDef,
        "message M1 {",
        "  message MXYZ {",
        "    message M3 {}",
        "    int32 M2 = 1;",
        "    MXYZ other_user = 999;",
        "  }",
        "}");
  }

  public void testRenameGroup() throws IOException {
    withSyntax("proto2");
    PsiFile fileUser =
        createFile(
            "user.proto",
            "import \"definer.proto\";",
            "message M2 {",
            "  optional M1.Agroup user = 777;",
            "}");
    PsiFile fileDef =
        createFile(
            "definer.proto",
            "message M1 {",
            "  optional group Agroup" + CARET_TAG + " = 1 {",
            "    int32 x = 1;",
            "    int32 y = 2;",
            "  }",
            "}");
    renameElementAtCaret("Ggroup");
    assertFileContents(
        fileUser,
        "import \"definer.proto\";",
        "message M2 {",
        "  optional M1.Ggroup user = 777;",
        "}");
    assertFileContents(
        fileDef,
        "message M1 {",
        "  optional group Ggroup = 1 {",
        "    int32 x = 1;",
        "    int32 y = 2;",
        "  }",
        "}");
  }

  public void testRenameFieldOption() throws IOException {
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
    renameElementAtCaret("mootwo");
    assertFileContents(
        fileUser,
        "import \"definer.proto\";",
        "message M1 {",
        "  option (mootwo) = true;",
        "  int32 f1 = 1;",
        "}",
        "message M2 {",
        "  bool moo = 1;",
        "}");
    assertFileContents(fileDef, "extend proto2.MessageOptions {", "  bool mootwo = 9001;", "}");
  }

  public void testRenameFieldReferencedInAggregate() throws IOException {
    PsiFile fileUser =
        createFile(
            "user.proto",
            "import \"definer.proto\";",
            "message M1 {",
            "  option (msg_opt) = { moo: true };",
            "  bool moo = 1;",
            "}");
    PsiFile fileDef =
        createFile(
            "definer.proto",
            "message M2 {",
            "  bool moo" + CARET_TAG + " = 1;",
            "}",
            "extend proto2.MessageOptions {",
            "  M2 msg_opt = 9001;",
            "}");
    renameElementAtCaret("mootwo");
    assertFileContents(
        fileUser,
        "import \"definer.proto\";",
        "message M1 {",
        "  option (msg_opt) = { mootwo: true };",
        "  bool moo = 1;",
        "}");
    assertFileContents(
        fileDef,
        "message M2 {",
        "  bool mootwo = 1;",
        "}",
        "extend proto2.MessageOptions {",
        "  M2 msg_opt = 9001;",
        "}");
  }

  public void testRenamePackage() throws IOException {
    // The "com.foo" part of the package name "com.foo.bar" in one file is separate from
    // the "com.foo" that is part of "com.foo.bee" in another file. Thus, we really only
    // rename the exact package of one file.
    withPackage("com.foo.bar");
    PsiFile file1 =
        createFile(
            "file1.proto",
            "import \"file2.proto\";",
            "message M2 {",
            "  int32 a = 1;",
            "  .com.foo.bee.M1.M2 b = 2;",
            "}");
    withPackage("com.foo.bee");
    PsiFile file2 =
        createFile(
            "file2.proto",
            "message M1 {",
            "  message M2 {",
            "    bool b = 1;",
            "    .com.fo" + CARET_TAG + "o.bee.M1 y = 2;",
            "  }",
            "}");
    renameElementAtCaret("baz");
    withPackage("com.foo.bar");
    assertFileContents(
        file1,
        "import \"file2.proto\";",
        "message M2 {",
        "  int32 a = 1;",
        "  .com.baz.bee.M1.M2 b = 2;",
        "}");
    withPackage("com.baz.bee");
    assertFileContents(
        file2,
        "message M1 {",
        "  message M2 {",
        "    bool b = 1;",
        "    .com.baz.bee.M1 y = 2;",
        "  }",
        "}");
  }

  public void testRenameEnumValue() throws IOException {
    PsiFile fileUser =
        createFile(
            "user.proto",
            "import \"definer.proto\";",
            "message M1 {",
            "  Pet x = 1 [default = CAT];",
            "  Pet2 y = 2 [default = CAT];",
            "}");
    PsiFile fileDef =
        createFile(
            "definer.proto",
            "enum Pet {",
            "  HAMSTER = 0;",
            "  CAT" + CARET_TAG + " = 1;",
            "}",
            "enum Pet2 {",
            "  CAT = 0;",
            "  DOG = 1;",
            "  GOAT = 2;",
            "}");
    renameElementAtCaret("FURBALL");
    assertFileContents(
        fileUser,
        "import \"definer.proto\";",
        "message M1 {",
        "  Pet x = 1 [default = FURBALL];",
        "  Pet2 y = 2 [default = CAT];",
        "}");
    assertFileContents(
        fileDef,
        "enum Pet {",
        "  HAMSTER = 0;",
        "  FURBALL = 1;",
        "}",
        "enum Pet2 {",
        "  CAT = 0;",
        "  DOG = 1;",
        "  GOAT = 2;",
        "}");
  }

  public void testRenameFile_inSubdir_shorter() throws IOException {
    createFile("definer.proto", "");
    PsiFile fileInSubdirDef = createFile("subdir/definer.proto", "");
    PsiFile fileUser =
        createFile("user.proto", "import \"definer.proto\";", "import \"subdir/definer.proto\";");
    myFixture.renameElement(fileInSubdirDef, "s.proto");
    assertFileContents(fileUser, "import \"definer.proto\";", "import \"subdir/s.proto\";");
  }

  public void testRenameFile_inSubdir_longer() throws IOException {
    createFile("definer.proto", "");
    PsiFile fileInSubdirDef = createFile("subdir/definer.proto", "");
    PsiFile fileUser =
        createFile("user.proto", "import \"definer.proto\";", "import \"subdir/definer.proto\";");
    myFixture.renameElement(fileInSubdirDef, "loooooonger.proto");
    assertFileContents(
        fileUser, "import \"definer.proto\";", "import \"subdir/loooooonger.proto\";");
  }

  public void testRenameFile_partsNoSlash() throws IOException {
    PsiFile fileDef = createFile("definer.proto", "");
    // While testing multiple string parts, rename only detects references starting with text
    // occurrence (the filename itself must be whole). So, only try adding empty string parts.
    PsiFile fileUser = createFile("user.proto", "import \"\" \"definer.proto\";");
    myFixture.renameElement(fileDef, "s.proto");
    assertFileContents(fileUser, "import \"s.proto\";");
  }

  public void testRenameFile_partsSlashAtEnd() throws IOException {
    PsiFile fileDef = createFile("parent/subdir/definer.proto", "");
    PsiFile fileUser = createFile("user.proto", "import \"parent/sub\" \"dir/definer.proto\";");
    myFixture.renameElement(fileDef, "s.proto");
    assertFileContents(fileUser, "import \"parent/sub\" \"dir/s.proto\";");
  }

  public void testRenameFile_partsSlashBeforeEnd() throws IOException {
    PsiFile fileDef = createFile("parent/subdir/definer.proto", "");
    PsiFile fileUser =
        createFile("user.proto", "import \"parent/sub\" \"dir/\" \"\" \"definer.proto\";");
    myFixture.renameElement(fileDef, "s.proto");
    assertFileContents(fileUser, "import \"parent/sub\" \"dir/\"  \"s.proto\";");
  }

  private void renameElementAtCaret(String newName) {
    myFixture.renameElementAtCaret(newName);
  }

  private String syntaxForTest = "proto3";
  private String packageForTest = "plugin.test";

  private void withSyntax(String syntaxForTest) {
    this.syntaxForTest = syntaxForTest;
  }

  private void withPackage(String packageForTest) {
    this.packageForTest = packageForTest;
  }

  private PsiFile createFile(String filename, String... fileContents) throws IOException {
    VirtualFile virtualFile =
        myFixture
            .getTempDirFixture()
            .createFile(
                filename,
                TestUtils.makeFileWithSyntaxAndPackage(
                    syntaxForTest, packageForTest, fileContents));
    myFixture.configureFromExistingVirtualFile(virtualFile);
    return myFixture.getFile();
  }

  private void assertFileContents(PsiFile file, String... fileContents) {
    assertEquals(
        file.getText(),
        TestUtils.makeFileWithSyntaxAndPackage(syntaxForTest, packageForTest, fileContents));
  }
}
