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
package com.intellij.protobuf.ide.actions;

import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.ide.DataManager;
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.PbTextFileType;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

/** Tests for {@link InsertSchemaDirectiveAction}. */
@TestApplication
public class InsertSchemaDirectiveActionTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp() {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    TemplateManagerImpl.setTemplateTesting(myFixture.getTestRootDisposable());
  }

  @Test
  public void testActionIsVisibleForTextFormat() {
    AnAction action = getAction();
    AnActionEvent event = getTextEvent(action, "# Comment\n", PbTextFileType.INSTANCE);
    EdtTestUtil.runInEdtAndWait(() -> action.update(event));
    assertThat(event.getPresentation().isEnabledAndVisible()).isTrue();
  }

  @Test
  public void testActionIsNotVisibleForOthers() {
    AnAction action = getAction();
    AnActionEvent event = getTextEvent(action, "// Comment\n", PbFileType.INSTANCE);
    EdtTestUtil.runInEdtAndWait(() -> action.update(event));
    assertThat(event.getPresentation().isEnabledAndVisible()).isFalse();
  }

  @Test
  public void testInsertEmptyFile() {
    AnAction action = getAction();
    AnActionEvent event = getTextEvent(action, "");
    performAction(action, event);
    assertThat(getFileText())
        .matches(file("# proto-file: ", "# proto-message: ", ""));
  }

  @Test
  public void testInsertBeforeNoCommentsEntries() {
    AnAction action = getAction();
    AnActionEvent event = getTextEvent(action, file("foo: bar"));
    performAction(action, event);
    assertThat(getFileText())
        .matches(
            file( "# proto-file: ", "# proto-message: ", "", "foo: bar"));
  }

  @Test
  public void testInsertBeforeExistingBlock() {
    AnAction action = getAction();
    AnActionEvent event =
        getTextEvent(action, file("# First", "# Second", "# Third", "", "# New block", "foo: bar"));
    performAction(action, event);
    assertThat(getFileText())
        .matches(
            file(
                "# proto-file: ",
                "# proto-message: ",
                "#",
                "# First",
                "# Second",
                "# Third",
                "",
                "# New block",
                "foo: bar"));
  }

  @Test
  public void testUseExistingFileValue() {
    AnAction action = getAction();
    AnActionEvent event =
        getTextEvent(
            action,
            file(
                "# First",
                "# proto-file: foo/bar.proto",
                "# Second",
                "# Third",
                "",
                "# New block",
                "foo: bar"));
    performAction(action, event);
    assertThat(getFileText())
        .matches(
            file(
                "# proto-file: foo/bar.proto",
                "# proto-message: ",
                "#",
                "# First",
                "# Second",
                "# Third",
                "",
                "# New block",
                "foo: bar"));
  }

  @Test
  public void testUseExistingMessageValue() {
    AnAction action = getAction();
    AnActionEvent event =
        getTextEvent(
            action,
            file(
                "# First",
                "# proto-message: Foo",
                "# Second",
                "# Third",
                "",
                "# New block",
                "foo: bar"));
    performAction(action, event);
    assertThat(getFileText())
        .matches(
            file(
                "# proto-file: ",
                "# proto-message: Foo",
                "#",
                "# First",
                "# Second",
                "# Third",
                "",
                "# New block",
                "foo: bar"));
  }

  @Test
  public void testUseBothExistingValues() {
    AnAction action = getAction();
    AnActionEvent event =
        getTextEvent(
            action,
            file(
                "# First",
                "# proto-message: Foo",
                "# Second",
                "# proto-file: foo/bar.proto",
                "# Third",
                "",
                "# New block",
                "foo: bar"));
    performAction(action, event);
    assertThat(getFileText())
        .isEqualTo(
            file(
                "# proto-file: foo/bar.proto",
                "# proto-message: Foo",
                "#",
                "# First",
                "# Second",
                "# Third",
                "",
                "# New block",
                "foo: bar"));
  }

  @Test
  public void testInsertImportsAtEnd() {
    AnAction action = getAction();
    AnActionEvent event =
        getTextEvent(
            action,
            file(
                "# proto-import: foo/biz.proto",
                "# First",
                "# proto-message: Foo",
                "# Second",
                "# proto-file: foo/bar.proto",
                "# Third",
                "# proto-import: foo/baz.proto",
                "# Fourth",
                "",
                "# New block",
                "foo: bar"));
    performAction(action, event);
    assertThat(getFileText())
        .isEqualTo(
            file(
                "# proto-file: foo/bar.proto",
                "# proto-message: Foo",
                "# proto-import: foo/baz.proto",
                "# proto-import: foo/biz.proto",
                "#",
                "# First",
                "# Second",
                "# Third",
                "# Fourth",
                "",
                "# New block",
                "foo: bar"));
  }

  private String file(String... lines) {
    return String.join("\n", lines) + "\n";
  }

  private String getFileText() {
    return ReadAction.computeBlocking(() -> myFixture.getFile().getText());
  }

  private static void performAction(AnAction action, AnActionEvent event) {
    EdtTestUtil.runInEdtAndWait(() -> action.actionPerformed(event));
  }

  private AnActionEvent getTextEvent(AnAction action, String text) {
    return getTextEvent(action, text, PbTextFileType.INSTANCE);
  }

  private AnActionEvent getTextEvent(AnAction action, String text, FileType fileType) {
    myFixture.configureByText(fileType, text);
    return getEditorEvent(action);
  }

  private AnActionEvent getEditorEvent(AnAction action) {
    return EdtTestUtil.runInEdtAndGet(() -> AnActionEvent.createFromAnAction(
        action, null, "", DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent())));
  }

  private AnAction getAction() {
    AnAction action = ActionManager.getInstance().getAction(InsertSchemaDirectiveAction.ACTION_ID);
    assertThat(action).isNotNull();
    return action;
  }
}
