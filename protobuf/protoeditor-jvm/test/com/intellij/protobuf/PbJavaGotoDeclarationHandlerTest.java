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
package com.intellij.protobuf;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.gencodeutils.GotoExpectationMarker;
import com.intellij.protobuf.gencodeutils.ReferenceGotoExpectation;
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationHandler;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.intellij.protobuf.TestUtils.notNull;

/**
 * Tests for {@link PbJavaGotoDeclarationHandler}.
 *
 * <p>The general form of the test is for each generated code variant:
 *
 * <ul>
 *   <li>Have a .proto file
 *   <li>Have the generated code from that .proto file, in the form of a library jar.
 *   <li>Have a "ProtoVersionXUser.java" that uses the generated code
 * </ul>
 * <p>
 * Each User.java file is annotated with {@link #CARET_MARKER}, and {@link GotoExpectationMarker}
 * annotations. We parse the User.java file to determine what to test (goto action from the caret
 * marker) and the expected outcome of the goto action for the next caret marker (expected target
 * .proto file + element). There must be at least one {@link #CARET_MARKER} after each {@link
 * GotoExpectationMarker}.
 */
public class PbJavaGotoDeclarationHandlerTest extends LightJavaCodeInsightFixtureTestCase {

  protected final Disposable testDisposable = new TestDisposable();

  // Marker that determines which caret positions to test.
  public static final String CARET_MARKER = "caretAfterThis";
  // How much to scoot past the CARET_MARKER. Usually it's a variable with a field,
  // or method, and we want to get to the field/method so scoot past the ".".
  // We might also have a CARET_MARKER in a /* */ comment, so scoot past the " */ "
  private static final int CARET_BUMP = 5;

  @Override
  protected void tearDown() throws Exception {
    try {
      Disposer.dispose(testDisposable);
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myFixture.setTestDataPath(getTestDataPath());
  }

  @Override
  public String getTestDataPath() {
    return PathManager.getHomePath() + "/contrib/protobuf/protoeditor-jvm/testData/";
  }

  private String getTestJarsDirectoryPath() {
    return getTestDataPath() + "jars/";
  }


  public void testProto2() {
    myFixture.copyFileToProject(new File(getTestDataPath(), "proto/Proto2.proto").getPath(), "protos/Proto2.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libProto2Lib-speed.jar", testDisposable);
    setupProto2BaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(myFixture, new File("java", "Proto2User.java"));

    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(60);
  }

  public void testProto2MultipleFiles() {
    myFixture.copyFileToProject(
      new File(getTestDataPath(), "proto/Proto2MultipleFiles.proto").getPath(), "protos/Proto2MultipleFiles.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libProto2MultipleFilesLib-speed.jar", testDisposable);
    setupProto2BaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(myFixture, new File("java", "Proto2MultipleFilesUser.java"));

    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(9);
  }

  public void testProto2OuterClass() {
    myFixture.copyFileToProject(
      new File(getTestDataPath(), "proto/Proto2OuterClass.proto").getPath(), "protos/Proto2OuterClass.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libProto2OuterClassLib-speed.jar", testDisposable);
    setupProto2BaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(myFixture, new File("java", "Proto2OuterClassUser.java"));

    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(4);
  }

  public void testProtoSyntax3() {
    myFixture.copyFileToProject(
      new File(getTestDataPath(), "proto/ProtoSyntax3.proto").getPath(), "protos/ProtoSyntax3.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libProtoSyntax3Lib-speed.jar", testDisposable);
    setupProto2BaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(myFixture, new File("java", "ProtoSyntax3User.java"));

    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(10);
  }

  public void testProto2Lite() {
    myFixture.copyFileToProject(
      new File(getTestDataPath(), "proto/Proto2Lite.proto").getPath(), "protos/Proto2Lite.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libProto2LiteLib-lite.jar", testDisposable);
    setupProto2LiteBaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(myFixture, new File("java", "Proto2LiteUser.java"));

    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(10);
  }

  public void testBadRecursion() {
    // While editing, it's possible to construct weird classes w/ inheritance recursion, etc.
    // Make sure that doesn't cause our goto handler to stack overflow.
    PsiClass badClass =
      myFixture.addClass(
        """
          public class Rec extends Rec2 {
            // EXPECT-NEXT: Rec2.java / Rec2
            public static /* caretAfterThis */ Rec2 identity(Rec x) { return x; }
          }
          """);
    myFixture.addClass("public class Rec2 extends Rec {}");
    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(badClass.getContainingFile().getVirtualFile()))
      .isEqualTo(1);
  }

  public void testClashingEnum() {
    myFixture.copyFileToProject(getTestDataPath() + "proto/clashing_enum.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libClashingEnum-speed.jar", testDisposable);
    setupProto2BaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(myFixture, new File("java", "ClashingEnumUser.java"));
    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(2);
  }

  public void testClashingMessage() {
    myFixture.copyFileToProject(getTestDataPath() + "proto/clashing_message.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libClashingMessage-speed.jar", testDisposable);
    setupProto2BaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(myFixture, new File("java", "ClashingMessageUser.java"));
    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(2);
  }

  public void testClashingNestedEnum() {
    myFixture.copyFileToProject(getTestDataPath() + "proto/clashing_nested_enum.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libClashingNestedEnum-speed.jar", testDisposable);
    setupProto2BaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(myFixture, new File("java", "ClashingNestedEnumUser.java"));
    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(2);
  }

  public void testClashingNestedMessage() {
    myFixture.copyFileToProject(getTestDataPath() + "proto/clashing_nested_message.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libClashingNestedMessage-speed.jar", testDisposable);
    setupProto2BaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(
        myFixture, new File("java", "ClashingNestedMessageUser.java"));
    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(2);
  }

  public void testNotClashingField() {
    myFixture.copyFileToProject(getTestDataPath() + "proto/not_clashing_field.proto");
    JavaTestData.addGenCodeJar(
      myFixture.getModule(), getTestJarsDirectoryPath(), "libNotClashingField-speed.jar", testDisposable);
    setupProto2BaseClass();
    VirtualFile javaFile =
      JavaTestData.copyJavaProtoUser(myFixture, new File("java", "NotClashingFieldUser.java"));
    assertWithMessage("Number of markers (" + GotoExpectationMarker.EXPECT_MARKER + ")")
      .that(checkExpectations(javaFile))
      .isEqualTo(1);
  }

  private void setupProto2BaseClass() {
    setupBaseClasses(
      "com.google.protobuf",
      "GeneratedMessage.java",
      "MessageOrBuilder.java",
      // oneof enums are lite enums, so we also need the lite base class
      "Internal.java",
      "ProtocolMessageEnum.java");
  }

  private void setupProto2LiteBaseClass() {
    setupBaseClasses(
      "com.google.protobuf",
      "GeneratedMessageLite.java",
      "MessageLiteOrBuilder.java",
      "Internal.java");
  }

  private void setupBaseClasses(String expectedPackage, String... filesToCopy) {
    String expectedPackageDir = expectedPackage.replace('.', File.separatorChar);
    Arrays.stream(filesToCopy)
      .forEach(
        file ->
          myFixture.copyFileToProject(
            FileUtil.join("java", "stubs", file), FileUtil.join(expectedPackageDir, file)));
  }

  /**
   * Parses the javaFile for {@link GotoExpectationMarker} annotations, and {@link #CARET_MARKER}.
   * Performs a "goto" action on the element highlighted by a caret marker, and checks that the
   * target matches the expectation. Returns the number of checks performed, so that we know that
   * the checks aren't accidentally skipped (grep the file yourself to sanity check)
   */
  private int checkExpectations(VirtualFile javaFile) {
    myFixture.configureFromExistingVirtualFile(javaFile);
    PsiFile psiFile = getFile();
    List<GotoExpectationMarker> expectations = GotoExpectationMarker.parseExpectations(psiFile);

    Project project = getProject();
    Editor editor = getEditor();
    String text = psiFile.getText();

    for (GotoExpectationMarker expectation : expectations) {
      String substring = text.substring(expectation.startIndex, expectation.endIndex);
      int caretOffset = substring.indexOf(CARET_MARKER);
      assertWithMessage(
        String.format(
          "Caret to check is in %s within range %s", substring, expectation.rangeString()))
        .that(caretOffset)
        .isNotEqualTo(-1);
      final int bumpedCaret =
        expectation.startIndex + caretOffset + CARET_MARKER.length() + CARET_BUMP;
      ApplicationManager.getApplication()
        .runReadAction(
          () -> {
            PsiElement srcElement = notNull(psiFile.findElementAt(bumpedCaret));
            PsiReference refElement =
              PsiTreeUtil.getParentOfType(srcElement, PsiQualifiedReferenceElement.class);
            assertThat(refElement).isNotNull();
            ReferenceGotoExpectation referenceGotoExpectation =
              ReferenceGotoExpectation.create(refElement.getCanonicalText(), expectation);
            PsiElement[] elements =
              GotoDeclarationAction.findAllTargetElements(project, editor, bumpedCaret);
            referenceGotoExpectation.assertCorrectTarget(elements);
          });
    }
    return expectations.size();
  }
}
