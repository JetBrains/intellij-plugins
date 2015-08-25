package com.jetbrains.lang.dart.ide.editor;

import com.intellij.openapi.editor.DefaultLineWrapPositionStrategy;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartLineWrapPositionStrategy extends DefaultLineWrapPositionStrategy {

  public DartLineWrapPositionStrategy() {
    super();
    addRule(new Rule('\\', WrapCondition.BEFORE)); // Wrap before escape sequences in strings
  }

  @Override
  public int calculateWrapPosition(@NotNull Document document,
                                   @Nullable Project project,
                                   int startOffset,
                                   int endOffset,
                                   int maxPreferredOffset,
                                   boolean allowToBeyondMaxPreferredOffset,
                                   boolean virtual) {
    int pos =
      super.calculateWrapPosition(document, project, startOffset, endOffset, maxPreferredOffset, allowToBeyondMaxPreferredOffset, virtual);
    if (pos < 0) {
      return pos;
    }
    char ch = document.getCharsSequence().charAt(pos);
    if (ch == '\'' || ch == '"') {
      return maxPreferredOffset;
    }
    return pos;
  }

  @Override
  protected boolean canUseOffset(@NotNull Document document, int offset, boolean virtual) {
    CharSequence chars = document.getCharsSequence();
    char charAtOffset = chars.charAt(offset);

    if (charAtOffset == '.') {
      // Do not split the cascade token, but allow wrapping in front of it.
      if (offset > 0 && chars.charAt(offset - 1) == '.') {
        return false;
      }
    }
    return true;
  }
}
