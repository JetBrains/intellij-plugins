package org.intellij.plugins.postcss.editor;

import com.intellij.editor.TodoItemsTestUtil;
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssFileType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.moduleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

@TestApplication
public class PostCssMultiLineTodoTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<Path> pathFixture = tempPathFixture();
  @SuppressWarnings("unused")
  private final TestFixture<Module> moduleFixture = moduleFixture(projectFixture, pathFixture, true);
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightFixture(projectFixture, pathFixture);

  @Test
  public void testSuccessiveLineComments() {
    TodoItemsTestUtil.doTestSuccessiveLineComments(codeInsightFixture.get(), PostCssFileType.DEFAULT_EXTENSION);
  }

  @Test
  public void testSuccessiveLineCommentsAfterEditing() {
    TodoItemsTestUtil.doTestSuccessiveLineCommentsAfterEditing(codeInsightFixture.get(), PostCssFileType.DEFAULT_EXTENSION);
  }

  @Test
  public void testNewTodoAfterUnsavedEditing() {
    TodoItemsTestUtil.doTestNewTodoAfterUnsavedEditing(codeInsightFixture.get(), PostCssFileType.DEFAULT_EXTENSION);
  }

  @Test
  public void testAllLinesLoseHighlightingWithFirstLine() {
    TodoItemsTestUtil.doTestAllLinesLoseHighlightingWithFirstLine(codeInsightFixture.get(), PostCssFileType.DEFAULT_EXTENSION);
  }

  @Test
  public void testContinuationIsNotOverlappedWithFollowingTodo() {
    TodoItemsTestUtil.doTestContinuationIsNotOverlappedWithFollowingTodo(codeInsightFixture.get(), PostCssFileType.DEFAULT_EXTENSION);
  }

  @Test
  public void testContinuationInBlockCommentWithStars() {
    TodoItemsTestUtil.doTestContinuationInBlockCommentWithStars(codeInsightFixture.get(), PostCssFileType.DEFAULT_EXTENSION);
  }
}
