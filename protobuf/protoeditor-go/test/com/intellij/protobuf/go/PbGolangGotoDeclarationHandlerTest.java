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
package com.intellij.protobuf.go;

import com.goide.GoTestUtil;
import com.goide.SdkAware;
import com.goide.junit5.GoCodeInsightContext;
import com.goide.junit5.layout.GoTestDataLayout;
import com.goide.psi.GoFile;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.gencodeutils.GotoExpectationMarker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static com.goide.junit5.GoCodeInsightFixtureKt.goCodeInsightFixture;
import static com.goide.junit5.GoProjectFixtureKt.goSharedProjectFixture;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.intellij.protobuf.TestUtils.notNull;

/**
 * Tests for {@link PbGolangGotoDeclarationHandler}.
 *
 * <p>The general form of the test is for each generated code variant:
 *
 * <ul>
 *   <li>Have a .proto file
 *   <li>Have the generated code from that .proto file, in the form of a .pb.go.
 *   <li>Have a "proto_versionX_user.go" that uses the generated code
 * </ul>
 *
 * Each user.go file is annotated with {@link #CARET_MARKER}, and {@link GotoExpectationMarker}
 * annotations. We parse the user.go file to determine what to test (goto action from the caret
 * marker) and the expected outcome of the goto action for the next caret marker (expected target
 * .proto file + element). There must be at least one {@link #CARET_MARKER} after each {@link
 * GotoExpectationMarker}.
 */
@TestApplication
@SdkAware(GoTestUtil.NO_SDK)
@GoTestDataLayout(doNotCopy = true)
@TestDataPath("$PROJECT_ROOT/contrib/protobuf/protoeditor-go/testData")
public class PbGolangGotoDeclarationHandlerTest {
  private static final TestFixture<Project> goProject = goSharedProjectFixture();
  private final TestFixture<GoCodeInsightContext> go =
    goCodeInsightFixture(goProject, PathManager.getHomePath() + "/contrib/protobuf/protoeditor-go/testData");

  // Marker that determines which caret positions to test.
  private static final String CARET_MARKER = "caretAfterThis";
  // CARET_MARKER will be in a /* */ comment, scoot past the " */ "
  private static final int CARET_BUMP = 4;

  @BeforeEach
  void setUp() {
    GoCodeInsightContext myFixture = go.get();
    TestUtils.addTestFileResolveProvider(myFixture.getProject(), myFixture.getTestRootDisposable());
  }

  @Test
  public void testProto2() {
    copyProtoAndGenCode("proto/proto2.proto", "go/proto2.pb.go", "proto2_go_proto");
    GoFile userFile = copyProtoUserFile("users/proto2_user.go", "proto2_user.go");

    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
        .that(checkExpectations(userFile))
        .isEqualTo(62);
  }

  @Test
  public void testProto2Gogo() {
    copyProtoAndGenCode("proto/proto2_gogo.proto", "go/proto2_gogo.pb.go", "proto2_gogo_go_proto");
    GoFile userFile = copyProtoUserFile("users/proto2_gogo_user.go", "proto2_gogo_user.go");

    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
            .that(checkExpectations(userFile))
            .isEqualTo(62);
  }

  @Test
  public void testProto3() {
    copyProtoAndGenCode("proto/proto3.proto", "go/proto3.pb.go", "proto3_go_proto");
    GoFile userFile = copyProtoUserFile("users/proto3_user.go", "proto3_user.go");

    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
        .that(checkExpectations(userFile))
        .isEqualTo(62);
  }

  @Test
  public void testProto3Gogo() {
    copyProtoAndGenCode("proto/proto3_gogo.proto", "go/proto3_gogo.pb.go", "proto3_gogo_go_proto");
    GoFile userFile = copyProtoUserFile("users/proto3_gogo_user.go", "proto3_gogo_user.go");

    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
            .that(checkExpectations(userFile))
            .isEqualTo(62);
  }

  private void copyProtoAndGenCode(String protoFile, String generatedGoFile, String goPackageName) {
    GoCodeInsightContext myFixture = go.get();
    // Proto file has to go to the regular place so the proto declaration resolving works.
    myFixture.copyFileToProject(protoFile, new File(protoFile).getPath());
    // Go file has to go into a packageName subdirectory so importPath resolving works.
    File packagePath = Paths.get(goPackageName).toFile();
    myFixture.copyFileToProject(generatedGoFile, new File(packagePath, generatedGoFile).getPath());
  }

  private GoFile copyProtoUserFile(String relativeSourcePath, String destPath) {
    GoCodeInsightContext myFixture = go.get();
    VirtualFile virtualFile = myFixture.copyFileToProject(relativeSourcePath, destPath);
    myFixture.configureFromExistingVirtualFile(virtualFile);
    PsiFile psiFile = myFixture.getFile();
    assertThat(psiFile).isInstanceOf(GoFile.class);
    return (GoFile) psiFile;
  }

  /**
   * Parses the userFile for {@link GotoExpectationMarker} annotations, and {@link #CARET_MARKER}.
   * Performs a "goto" action on the element highlighted by a caret marker, and checks that the
   * target matches the expectation. Returns the number of checks performed, so that we know that
   * the checks aren't accidentally skipped (grep the file yourself to sanity check)
   */
  private int checkExpectations(GoFile psiFile) {
    GoCodeInsightContext myFixture = go.get();
    Project project = myFixture.getProject();
    Editor editor = myFixture.getEditor();
    return ReadAction.computeBlocking(() -> {
      List<GotoExpectationMarker> expectations = GotoExpectationMarker.parseExpectations(psiFile);
      String text = psiFile.getText();
      for (GotoExpectationMarker expectation : expectations) {
        String subtestText = expectation.textRange.substring(text);
        int caretOffset = subtestText.indexOf(CARET_MARKER);
        assertWithMessage(
                String.format(
                    "Caret to check is in %s within range %s",
                    subtestText, expectation.textRange))
            .that(caretOffset)
            .isNotEqualTo(-1);
        final int bumpedCaret =
            expectation.textRange.getStartOffset() + caretOffset + CARET_MARKER.length() + CARET_BUMP;
        final int lineNumber = editor.getDocument().getLineNumber(bumpedCaret) + 1;

        PsiElement srcElement = notNull(psiFile.findElementAt(bumpedCaret));
        PsiElement[] elements =
            GotoDeclarationAction.findAllTargetElements(project, editor, bumpedCaret);
        expectation.checkGotoTargets(srcElement.getText(), elements, lineNumber);
      }
      return expectations.size();
    });
  }
}
