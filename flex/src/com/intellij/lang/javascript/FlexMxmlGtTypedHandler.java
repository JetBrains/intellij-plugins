// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript;

import com.intellij.codeInsight.editorActions.XmlGtTypedHandler;
import com.intellij.lang.javascript.flex.MxmlLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementDescriptorWithCDataContent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FlexMxmlGtTypedHandler extends XmlGtTypedHandler {
  @Override
  public @NotNull Result beforeCharTyped(char c,
                                         @NotNull Project project,
                                         @NotNull Editor editor,
                                         @NotNull PsiFile editedFile,
                                         @NotNull FileType fileType) {
    if (editedFile.getLanguage() != MxmlLanguage.INSTANCE) return Result.CONTINUE;

    return super.beforeCharTyped(c, project, editor, editedFile, fileType);
  }

  @Override
  protected @NotNull Result insertTagContent(@NotNull Project project,
                                             XmlTag tag,
                                             String name,
                                             PsiFile file,
                                             @NotNull Editor editor) {
    Collection<TextRange> cdataReformatRanges = null;
    final XmlElementDescriptor descriptor = tag.getDescriptor();

    if (descriptor instanceof XmlElementDescriptorWithCDataContent) {
      final XmlElementDescriptorWithCDataContent cDataContainer = (XmlElementDescriptorWithCDataContent)descriptor;

      cdataReformatRanges = new SmartList<>();
      if (cDataContainer.requiresCdataBracesInContext(tag)) {
        @NonNls final String cDataStart = "><![CDATA[";
        final String inserted = cDataStart + "\n]]>";
        EditorModificationUtil.insertStringAtCaret(editor, inserted, false, cDataStart.length());
        int caretOffset = editor.getCaretModel().getOffset();
        if (caretOffset >= cDataStart.length()) {
          cdataReformatRanges.add(TextRange.from(caretOffset - cDataStart.length(), inserted.length() + 1));
        }
      }
    }

    if (cdataReformatRanges != null && !cdataReformatRanges.isEmpty()) {
      PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
      try {
        CodeStyleManager.getInstance(project).reformatText(file, cdataReformatRanges);
      }
      catch (IncorrectOperationException e) {
        Logger.getInstance(FlexMxmlGtTypedHandler.class).error(e);
      }
    }
    return cdataReformatRanges != null && !cdataReformatRanges.isEmpty() ? Result.STOP : Result.CONTINUE;
  }
}
