// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.plugins.jade.lexer.IndentUtil;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import com.jetbrains.plugins.jade.psi.impl.JadePipedTextImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class JadeCopyPasteProcessor implements CopyPastePreProcessor {

  @Override
  public @Nullable String preprocessOnCopy(final PsiFile file, final int[] startOffsets, final int[] endOffsets, final String text) {
    return null;
  }

  @Override
  public @NotNull String preprocessOnPaste(final Project project,
                                           final PsiFile file,
                                           final Editor editor,
                                           final String text,
                                           final RawText rawText) {
    if (!(file instanceof JadeFileImpl)) {
      return text;
    }

    CommonCodeStyleSettings.IndentOptions indentOptions =
      CodeStyle.getSettings(file).getCommonSettings(JadeLanguage.INSTANCE).getIndentOptions();
    if (indentOptions == null) {
      indentOptions = CodeStyle.getSettings(file).getIndentOptions();
    }


    final String normalizedText;
    // If there's a whitespace in the beginning of the first line, assume that first line has been copied as a whole,
    // and all the indents are correct. Now let's subtract the minimal indent from all the indents and try to add indent needed
    // to paste the first line right at the cursor
    // If the first and some other line doesn't start with a whitespace, assume the original text has 0 indent
    // and we need just to add an indent needed by current cursor position
    if (Character.isWhitespace(text.charAt(0)) || someLineExceptFirstOneDoesntStartWithWhiteSpace(text)) {
      normalizedText = processTextWithLeadingWhitespace(text, indentOptions);
    }
    else {
      normalizedText = text;
    }

    final CaretModel caretModel = editor.getCaretModel();
    final SelectionModel selectionModel = editor.getSelectionModel();
    final Document document = editor.getDocument();
    final int caretOffset = selectionModel.getSelectionStart() != selectionModel.getSelectionEnd() ?
                            selectionModel.getSelectionStart() : caretModel.getOffset();

    final PsiElement containingElement = file.findElementAt(Math.max(0, caretOffset - 1));
    if (containingElement == null) {
      return normalizedText;
    }

    final String firstLineIndent = getIndentBefore(document, caretOffset);

    final IElementType containingElementType = containingElement.getNode().getElementType();
    if (JadeTokenTypes.TEXT_SET.contains(containingElementType) ||
        PsiTreeUtil.getParentOfType(containingElement, JadePipedTextImpl.class) != null) {
      return appendToLines(normalizedText, firstLineIndent + " | ");
    }

    return appendToLines(normalizedText, firstLineIndent);
  }

  private static @NotNull String appendToLines(@NotNull String text, @NotNull String toAppend) {
    return StringUtil.join(StringUtil.split(text, "\n", true, false), "\n" + toAppend);
  }

  private static @NotNull String getIndentBefore(@NotNull Document document, int offset) {
    final CharSequence s = document.getCharsSequence();

    final int lastEol = StringUtil.lastIndexOf(s, '\n', 0, offset);
    StringBuilder result = new StringBuilder();
    for (int i = lastEol + 1; i < offset && Character.isWhitespace(s.charAt(i)); ++i) {
      result.append(s.charAt(i));
    }
    return result.toString();
  }

  public @NotNull String processTextWithLeadingWhitespace(final @NotNull String text,
                                                          final @NotNull CommonCodeStyleSettings.IndentOptions indentOptions) {
    List<String> lines = StringUtil.split(text, "\n", true, false);
    int minIndent = Integer.MAX_VALUE;
    for (String line : lines) {
      if (!line.isEmpty()) {
        minIndent = Math.min(minIndent, IndentUtil.calcIndent(line, 0, indentOptions.TAB_SIZE));
      }
    }

    StringBuilder result = new StringBuilder();
    boolean initialLine = true;
    for (String line : lines) {
      if (!initialLine) {
        result.append("\n");
      } else {
        initialLine = false;
      }
      final int curIndent = IndentUtil.calcIndent(line, 0, indentOptions.TAB_SIZE);
      result.append(StringUtil.repeat(" ", Math.max(0, curIndent - minIndent))).append(StringUtil.trimLeading(line));
    }

    return result.toString();
  }

  private static boolean someLineExceptFirstOneDoesntStartWithWhiteSpace(String text) {
    List<String> lines = StringUtil.split(text, "\n");
    for (int i = 1; i < lines.size(); i++) {
      String line = lines.get(i);
      if (!Character.isWhitespace(line.charAt(0))) {
        return true;
      }
    }
    return false;
  }
}
