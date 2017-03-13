package org.intellij.plugins.markdown.editor;

import com.intellij.openapi.editor.impl.AbstractRtlTest;

public class MarkdownRtlTest extends AbstractRtlTest {
  public void testBasicCase() throws Exception {
    checkBidiRunBoundaries("R R", "md");
  }
}
