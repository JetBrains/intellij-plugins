package com.dmarcotte.handlebars.editor;


import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoItem;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;

public class HbTodoIndexTest extends LightPlatformCodeInsightTestCase {

  @SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
  public HbTodoIndexTest() {
    PlatformTestCase.initPlatformLangPrefix();
  }

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


  private void checkTodoCount(int expectedTodoCount) {
    configureByFile(getTestName(true) + ".hbs");
    TodoItem[] items = PsiTodoSearchHelper.SERVICE.getInstance(getProject()).findTodoItems(getFile());
    assertEquals(expectedTodoCount, items.length);
  }
}
