// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.editor;


import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoItem;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import org.jetbrains.annotations.NotNull;

public class HbTodoIndexTest extends LightPlatformCodeInsightTestCase {
  @Override
  @NotNull
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH + "/todo/";
  }

  public void testNoTodo() {
    checkTodoCount(0);
  }

  public void testFileWithTwoTodo() {
    checkTodoCount(2);
  }

  public void testHtmlTodoOnly() {
    checkTodoCount(1);
  }

  private void checkTodoCount(int expectedTodoCount) {
    configureByFile(getTestName(true) + ".hbs");
    TodoItem[] items = PsiTodoSearchHelper.getInstance(getProject()).findTodoItems(getFile());
    assertEquals(expectedTodoCount, items.length);
  }
}
