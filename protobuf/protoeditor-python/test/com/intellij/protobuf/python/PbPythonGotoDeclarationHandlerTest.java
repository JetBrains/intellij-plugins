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
package com.intellij.protobuf.python;

import com.google.common.collect.Iterables;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.gencodeutils.GotoExpectationMarker;
import com.intellij.protobuf.gencodeutils.ReferenceGotoExpectation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.python.psi.PyReferenceExpression;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.intellij.protobuf.TestUtils.notNull;

/**
 * Tests for {@link PbPythonGotoDeclarationHandler}.
 *
 * <p>The general form of the test is for each generated code variant:
 *
 * <ul>
 *   <li>Have a .proto file
 *   <li>Have the generated code from that .proto file, in the form of a foo_pb{1,2}.py file
 *   <li>Have a "proto_versionX_user.py" that uses the generated code
 * </ul>
 *
 * Each proto_x_user.py file is annotated with <caret>, and {@link GotoExpectationMarker}
 * annotations. We parse the proto_x_user.py file to determine what to test (goto action from the
 * caret marker) and the expected outcome of the goto action for the next caret marker (expected
 * target .proto file + element). There must be at least one caret marker after each {@link
 * GotoExpectationMarker}.
 */
public class PbPythonGotoDeclarationHandlerTest extends PbCodeInsightFixtureTestCase {

  // TEST_PYTHON_PATH should match the import statements in the resources/users/*.py.test files.
  private static final String TEST_PYTHON_PATH = "python";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TestUtils.addTestFileResolveProvider(getProject(), getTestRootDisposable());
  }

  @Override
  public String getTestDataPath() {
    return PathManager.getHomePath() + "/contrib/protobuf/protoeditor-python/testData/";
  }

  public void testApiV2User() {
    setupGenCodePackage();
    copyProtoAndGenCode("proto/proto2.proto", "python/proto2_pb2.py");
    VirtualFile pythonFile =
        myFixture.copyFileToProject("users/proto2_user.py.test", "proto2_user.py");
    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
        .that(checkExpectations(pythonFile))
        .isEqualTo(15);
  }

  public void testProtoSyntax3User() {
    setupGenCodePackage();
    copyProtoAndGenCode("proto/proto3.proto", "python/proto3_pb2.py");
    VirtualFile pythonFile =
        myFixture.copyFileToProject("users/proto3_user.py.test", "proto3_user.py");
    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
        .that(checkExpectations(pythonFile))
        .isEqualTo(4);
  }

  // Place __init__.py files in the TEST_PYTHON_PATH so that it is considered a python package
  // by the default python import resolver.
  private void setupGenCodePackage() {
    String[] components = TEST_PYTHON_PATH.split("/");
    List<String> componentsList = Arrays.asList(components);
    myFixture.addFileToProject("__init__.py", "");
    for (int i = 1; i <= components.length; ++i) {
      String joined = String.join("/", Iterables.limit(componentsList, i));
      myFixture.addFileToProject(new File(joined, "__init__.py").getPath(), "");
    }
  }

  private void copyProtoAndGenCode(String protoFile, String generatedPy) {
    myFixture.copyFileToProject(protoFile, protoFile);
    myFixture.copyFileToProject(generatedPy, new File(generatedPy).getPath());
  }

  /**
   * Parses the file for {@link GotoExpectationMarker} annotations, and caret markers. Performs a
   * "goto" action on the element highlighted by a caret marker, and checks that the target matches
   * the expectation. Returns the number of checks performed, so that we know that the checks are
   * not accidentally skipped (grep the file yourself to sanity check)
   */
  private int checkExpectations(VirtualFile file) {
    myFixture.configureFromExistingVirtualFile(file);
    PsiFile psiFile = getFile();
    int numExpectations = 0;
    List<GotoExpectationMarker> expectations = GotoExpectationMarker.parseExpectations(psiFile);

    Project project = getProject();
    Editor editor = getEditor();
    String text = psiFile.getText();

    CaretModel caretModel = editor.getCaretModel();
    List<Caret> carets = caretModel.getAllCarets();
    for (GotoExpectationMarker expectation : expectations) {
      String substring = text.substring(expectation.startIndex, expectation.endIndex);
      Caret caret = findCaretInRange(carets, expectation.startIndex, expectation.endIndex);
      assertWithMessage(
              String.format(
                  "Caret to check is in %s within range %s", substring, expectation.rangeString()))
          .that(caret)
          .isNotNull();
      ApplicationManager.getApplication()
          .runReadAction(
              () -> {
                PsiElement srcElement = notNull(psiFile.findElementAt(caret.getOffset()));
                PyReferenceExpression refExpression =
                    PsiTreeUtil.getParentOfType(srcElement, PyReferenceExpression.class);
                assertThat(refExpression).isNotNull();
                ReferenceGotoExpectation referenceGotoExpectation =
                    ReferenceGotoExpectation.create(refExpression.getText(), expectation);
                PsiElement[] elements =
                    GotoDeclarationAction.findAllTargetElements(project, editor, caret.getOffset());
                referenceGotoExpectation.assertCorrectTarget(elements);
              });
      numExpectations++;
    }
    return numExpectations;
  }

  private static Caret findCaretInRange(List<Caret> carets, int start, int end) {
    return ContainerUtil.find(carets, c -> start <= c.getOffset() && c.getOffset() < end);
  }
}
