// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.editor;


import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoItem;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
public class HbTodoIndexTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    fixture.get().setTestDataPath(HbTestUtils.BASE_TEST_DATA_PATH + "/todo/");
  }

  @Test
  public void testNoTodo() {
    checkTodoCount(0);
  }

  @Test
  public void testFileWithTwoTodo() {
    checkTodoCount(2);
  }

  @Test
  public void testHtmlTodoOnly() {
    checkTodoCount(1);
  }

  private void checkTodoCount(int expectedTodoCount) {
    CodeInsightTestFixture myFixture = fixture.get();
    PsiFile file = myFixture.configureByFile(myTestName + ".hbs");
    TodoItem[] items = ReadAction.computeBlocking(() -> PsiTodoSearchHelper.getInstance(myFixture.getProject()).findTodoItems(file));
    assertEquals(expectedTodoCount, items.length);
  }
}
