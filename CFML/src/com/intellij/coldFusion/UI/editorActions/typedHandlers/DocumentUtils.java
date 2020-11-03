// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.intellij.coldFusion.UI.editorActions.typedHandlers;

import com.intellij.openapi.editor.Document;

/**
 * @author anonymous
 */
public final class DocumentUtils {
  static char getCharAt(Document document, int offset) {
    if (offset >= document.getTextLength() || offset < 0) {
      return 0;
    }
    return document.getCharsSequence().charAt(offset);
  }
}
