// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.ide.todo.TodoConfiguration;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoItem;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.intellij.terraform.hcl.HCLFileType;

import java.util.Arrays;
import java.util.Comparator;

public class HCLTodoTest extends BasePlatformTestCase {
  public void testDetectedInSingleLineComments() {
    assertTrue(TodoConfiguration.getInstance().isMultiLine());

    PsiFile file = createLightFile(HCLFileType.INSTANCE, """

      // todo: single line

      # todo: single line

      // todo: first part
      #        second part
      """
    );
    PsiTodoSearchHelper helper = PsiTodoSearchHelper.getInstance(getProject());
    TodoItem[] items;

    items = helper.findTodoItemsLight(file);
    Arrays.sort(items, Comparator.comparingInt(o -> o.getTextRange().getStartOffset()));
    assertEquals(3, items.length);
    assertEmpty(items[0].getAdditionalTextRanges());
    assertEmpty(items[1].getAdditionalTextRanges());
    assertNotEmpty(items[2].getAdditionalTextRanges());
  }

  public void testDetectedInMultiLineComments() {
    assertTrue(TodoConfiguration.getInstance().isMultiLine());

    PsiFile file = createLightFile(HCLFileType.INSTANCE, """

      /* todo: single line */

      /*
      todo: single line
      */

      /* todo: first part
              second part*/
      """
    );
    PsiTodoSearchHelper helper = PsiTodoSearchHelper.getInstance(getProject());
    TodoItem[] items;

    items = helper.findTodoItemsLight(file);
    Arrays.sort(items, Comparator.comparingInt(o -> o.getTextRange().getStartOffset()));
    assertEquals(3, items.length);
    assertEmpty(items[0].getAdditionalTextRanges());
    assertEmpty(items[1].getAdditionalTextRanges());
    assertNotEmpty(items[2].getAdditionalTextRanges());
  }
}
