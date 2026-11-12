// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoItem;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
public class JadeTodoTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  @Test
  public void testTodos() {
    CodeInsightTestFixture myFixture = fixture.get();
    final String fileContent =
      """
          // TODO find this todo
          div
          //
            TODO find this todo too
          div
          //- Fixme this to do also to be found
          div
          //-
            Fixme and this one, too
          | TODO this is not todo
          div TODO this is not, too\
        """;
    myFixture.configureByText("todotest.jade", fileContent);

    TodoItem[] items =
      ReadAction.computeBlocking(() -> PsiTodoSearchHelper.getInstance(myFixture.getProject()).findTodoItems(myFixture.getFile()));
    assertEquals(4, items.length);
  }
}
