package org.intellij.plugins.postcss.editor;

import com.intellij.editor.TodoItemsTestCase;
import org.intellij.plugins.postcss.PostCssFileType;

public class PostCssMultiLineTodoTest extends TodoItemsTestCase {
  @Override
  protected String getFileExtension() {
    return PostCssFileType.DEFAULT_EXTENSION;
  }

  @Override
  protected boolean supportsCStyleSingleLineComments() {
    return true;
  }

  @Override
  protected boolean supportsCStyleMultiLineComments() {
    return true;
  }
}
