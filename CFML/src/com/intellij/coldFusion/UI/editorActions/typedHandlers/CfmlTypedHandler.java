/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.UI.editorActions.typedHandlers;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.coldFusion.UI.editorActions.matchers.CfmlBraceMatcher;
import com.intellij.coldFusion.UI.editorActions.utils.CfmlEditorUtil;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 13.10.2008
 */
public class CfmlTypedHandler extends TypedHandlerDelegate {
  static final boolean ourEnableDoublePoundInsertion = SystemProperties.getBooleanProperty("idea.cfml.insert.pair.pound", true);

  @NotNull
  @Override
  public Result checkAutoPopup(char charTyped, @NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
    PsiFile cfmlFile = file.getViewProvider().getPsi(CfmlLanguage.INSTANCE);

    if (isNotCfmlFile(cfmlFile, editor)) {
      return Result.CONTINUE;
    }

    if (charTyped == '/') {
      CfmlUtil.showCompletion(editor);
    }
    return Result.CONTINUE;
  }

  @NotNull
  @Override
  public Result beforeCharTyped(final char c, @NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file, @NotNull final FileType fileType) {
    PsiFile cfmlFile = file.getViewProvider().getPsi(CfmlLanguage.INSTANCE);

    if (isNotCfmlFile(cfmlFile, editor)) {
      return Result.CONTINUE;
    }
    int offset = editor.getCaretModel().getOffset();

    if (c == '{') {
      CfmlBraceMatcher braceMatcher = new CfmlBraceMatcher();
      HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(offset);
      if (!braceMatcher.isLBraceToken(iterator, editor.getDocument().getCharsSequence(), fileType)) {
        EditorModificationUtil.insertStringAtCaret(editor, "}", true, 0);
        // return Result.STOP;
      }
      return Result.CONTINUE;
    }
    if (c == '#') {
      if (ourEnableDoublePoundInsertion && CfmlEditorUtil.countSharpsBalance(editor) == 0) {
        char charAtOffset = DocumentUtils.getCharAt(editor.getDocument(), offset);
        if (charAtOffset == '#') {
          EditorModificationUtil.moveCaretRelatively(editor, 1);
          return Result.STOP;
        }
        EditorModificationUtil.insertStringAtCaret(editor, "#", true, 0);
      }
    }
    else if (c == '>') {
      if (((EditorEx)editor).getHighlighter().createIterator(editor.getCaretModel().getOffset()).getTokenType() == CfmlTokenTypes.COMMENT ||
          ((EditorEx)editor).getHighlighter().createIterator(editor.getCaretModel().getOffset()).getTokenType().getLanguage() !=
          CfmlLanguage.INSTANCE) {
        return Result.CONTINUE;
      }
      insertCloseTagIfNeeded(editor, cfmlFile, project);
      return Result.STOP;
    }
    return Result.CONTINUE;
  }

  public static boolean insertCloseTagIfNeeded(Editor editor, PsiFile file, Project project) {
    final Document document = editor.getDocument();
    final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);

    int offset = editor.getCaretModel().getOffset();
    documentManager.commitDocument(document);
    char charAtOffset = DocumentUtils.getCharAt(document, offset);

    if (charAtOffset != '>') {
      EditorModificationUtil.insertStringAtCaret(editor, ">", true, 0);
    }
    EditorModificationUtil.moveCaretRelatively(editor, 1);
    ++offset;
    if (DocumentUtils.getCharAt(document, offset - 2) == '/') {
      return false;
    }
    HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(offset - 2);

    while (!iterator.atEnd() && !iterator.getTokenType().equals(CfmlTokenTypes.CF_TAG_NAME)) {
      if (CfmlUtil.isControlToken(iterator.getTokenType())) {
        return false;
      }
      iterator.retreat();
    }
    if (!iterator.atEnd()) {
      iterator.retreat();
      if (!iterator.atEnd() && iterator.getTokenType().equals(CfmlTokenTypes.LSLASH_ANGLEBRACKET)) {
        return false;
      }
      iterator.advance();
    }
    if (iterator.atEnd()) {
      return false;
    }
    String tagName = document.getCharsSequence().subSequence(iterator.getStart(), iterator.getEnd()).toString();
    if (CfmlUtil.isSingleCfmlTag(tagName, project) || CfmlUtil.isUserDefined(tagName)) {
      return false;
    }
    PsiElement tagElement = file.findElementAt(iterator.getStart());
    while (tagElement != null && !(tagElement instanceof CfmlTag)) {
      tagElement = tagElement.getParent();
    }
    if (tagElement == null) {
      return false;
    }
    boolean doInsertion = false;
    if (tagElement.getLastChild() instanceof PsiErrorElement) {
      doInsertion = true;
    }
    else {
      iterator = ((EditorEx)editor).getHighlighter().createIterator(0);
      while (!iterator.atEnd() && iterator.getStart() < offset) {
        if (iterator.getTokenType() == CfmlTokenTypes.CF_TAG_NAME) {
          String currentTagName = document.getCharsSequence().subSequence(iterator.getStart(), iterator.getEnd()).toString();
          if (tagName.equals(currentTagName)) {
            PsiElement currentTagElement = file.findElementAt(iterator.getStart());
            currentTagElement = PsiTreeUtil.getParentOfType(currentTagElement, CfmlTag.class);
            if (currentTagElement.getLastChild() instanceof PsiErrorElement) {
              doInsertion = true;
              break;
            }
          }
        }
        iterator.advance();
      }
    }
    String tagNameFromPsi = ((CfmlTag)tagElement).getTagName(); // tag name in lowercase
    if (doInsertion && CfmlUtil.isEndTagRequired(tagNameFromPsi, project)) {
      if (!Comparing.equal(tagNameFromPsi, tagName, false)) {
        tagName = tagNameFromPsi; // use tagName because it has proper case
      }
      EditorModificationUtil.insertStringAtCaret(editor, "</" + tagName + ">", true, 0);
      return true;
    }
    return false;
  }


  @NotNull
  @Override
  public Result charTyped(final char c, @NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
    if (isNotCfmlFile(file, editor)) {
      return Result.CONTINUE;
    }
    return Result.CONTINUE;
  }

  static boolean isNotCfmlFile(final PsiFile file, final Editor editor) {
    return !(file instanceof CfmlFile)
           || editor.getCaretModel().getOffset() == 0;
  }
}
