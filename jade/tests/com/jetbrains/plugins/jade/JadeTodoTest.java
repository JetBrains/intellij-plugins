// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoItem;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class JadeTodoTest extends BasePlatformTestCase {

  public void testTodos() {
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

    TodoItem[] items = PsiTodoSearchHelper.getInstance(getProject()).findTodoItems(myFixture.getFile());
    assertEquals(4, items.length);
  }
}
