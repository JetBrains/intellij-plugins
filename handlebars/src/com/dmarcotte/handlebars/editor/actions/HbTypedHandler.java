// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.*;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Handler for custom plugin actions on chars typed by the user.  See {@link HbEnterHandler} for custom actions
 * on Enter.
 */
public class HbTypedHandler extends TypedHandlerDelegate {
  private static final Logger LOG = Logger.getInstance(HbTypedHandler.class);

  public static final String OPEN_BRACE = "{";
  public static final String CLOSE_BRACES = "}}";

  @Override
  public @NotNull Result beforeCharTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, @NotNull FileType fileType) {
    int offset = editor.getCaretModel().getOffset();

    if (offset == 0 || offset > editor.getDocument().getTextLength()) {
      return Result.CONTINUE;
    }

    String previousChar = editor.getDocument().getText(new TextRange(offset - 1, offset));

    if (file.getLanguage() instanceof HbLanguage) {
      PsiDocumentManager.getInstance(project).commitAllDocuments();

      // we suppress the built-in "}" auto-complete when we see "{{"
      if (c == '{' && previousChar.equals("{")) {
        // since the "}" autocomplete is built in to IDEA, we need to hack around it a bit by
        // intercepting it before it is inserted, doing the work of inserting for the user
        // by inserting the '{' the user just typed...
        editor.getDocument().insertString(offset, Character.toString(c));
        // ... and position their caret after it as they'd expect...
        editor.getCaretModel().moveToOffset(offset + 1);

        // ... then finally telling subsequent responses to this charTyped to do nothing
        return Result.STOP;
      }
    }

    return Result.CONTINUE;
  }

  @Override
  public @NotNull Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    FileViewProvider provider = file.getViewProvider();

    if (!provider.getBaseLanguage().isKindOf(HbLanguage.INSTANCE)) {
      return Result.CONTINUE;
    }

    if (offset < 2 || offset > editor.getDocument().getTextLength()) {
      return Result.CONTINUE;
    }

    String previousChar = editor.getDocument().getText(new TextRange(offset - 2, offset - 1));
    boolean closeBraceCompleted = false;

    if (file.getLanguage() instanceof HbLanguage) {
      if (HbConfig.isAutocompleteMustachesEnabled() && c == '}' && !previousChar.equals("}")) {
        // we may be able to complete the second brace
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        PsiElement elementAt = provider.findElementAt(offset - 1, provider.getBaseLanguage());
        ASTNode node = elementAt != null ? elementAt.getNode() : null;
        if (node != null && node.getElementType() == HbTokenTypes.INVALID) {
          // we should be looking at the beginning of a close brace.  Find its matching open brace and auto-complete based on its type
          PsiElement mustache = PsiTreeUtil.findFirstParent(elementAt, psiElement -> psiElement instanceof HbMustache);

          if (mustache != null) {
            String braceCompleter;

            if (mustache.getFirstChild().getNode().getElementType() == HbTokenTypes.OPEN_UNESCAPED) {
              // add "}}" to complete the CLOSE_UNESCAPED
              braceCompleter = "}}";
            } else {
              // add "}" to complete the CLOSE
              braceCompleter = "}";
            }

            editor.getDocument().insertString(offset, braceCompleter);
            offset += braceCompleter.length();
            editor.getCaretModel().moveToOffset(offset);
            closeBraceCompleted = true;
          }
        }
      }
    }

    // if we just completed a close brace or the user just typed one, we may have some business to attend to
    if (closeBraceCompleted || (c == '}' && previousChar.equals("}"))) {
      autoInsertCloseTag(project, offset, editor, provider);
      adjustMustacheFormatting(project, offset, editor, file, provider);
    } else if (c == '/' && previousChar.equals("{")) {
      finishClosingTag(offset, editor, provider);
    }

    return Result.CONTINUE;
  }

  private static void finishClosingTag(int offset, Editor editor, FileViewProvider provider) {
    PsiElement elementAtCaret = provider.findElementAt(offset - 1, HbLanguage.class);
    if (elementAtCaret != null) {
      HbBlockWrapper block = PsiTreeUtil.getParentOfType(elementAtCaret, HbBlockWrapper.class);
      if (block != null) {
        final HbOpenBlockMustache open = PsiTreeUtil.findChildOfType(block, HbOpenBlockMustache.class);
        final HbCloseBlockMustache close = PsiTreeUtil.findChildOfType(block, HbCloseBlockMustache.class);
        if (open != null && close == null) {
          final HbMustacheName mustacheName = PsiTreeUtil.findChildOfType(open, HbMustacheName.class);
          if (mustacheName != null) {
            if (offset > 3) {
              final String prePreviousChar = editor.getDocument().getText(new TextRange(offset - 3, offset - 2));
              if (prePreviousChar.equals("{")) {
                editor.getDocument().insertString(offset, mustacheName.getText() + CLOSE_BRACES);
                editor.getCaretModel().moveToOffset(offset + mustacheName.getText().length() + CLOSE_BRACES.length());
              } else {
                editor.getDocument().replaceString(offset - 1, offset, OPEN_BRACE + '/' + mustacheName.getText() + CLOSE_BRACES);
                editor.getCaretModel().moveToOffset(offset + mustacheName.getText().length() + CLOSE_BRACES.length() + 1);
              }
            } else {
              LOG.warn("Unexpected offset inside HbBlockWrapper element");
            }
          }
        }
      }
    }
  }

  /**
   * When appropriate, auto-inserts Handlebars close tags.  i.e.  When "{{#tagId}}" or "{{^tagId}} is typed,
   * {{/tagId}} is automatically inserted
   */
  private static void autoInsertCloseTag(Project project, int offset, Editor editor, FileViewProvider provider) {
    if (!HbConfig.isAutoGenerateCloseTagEnabled()) {
      return;
    }

    PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

    PsiElement elementAtCaret = provider.findElementAt(offset - 1, HbLanguage.class);

    if (elementAtCaret == null || elementAtCaret.getNode().getElementType() != HbTokenTypes.CLOSE) {
      return;
    }

    HbOpenBlockMustache openTag = HbPsiUtil.findParentOpenTagElement(elementAtCaret);

    if (openTag != null && openTag.getChildren().length > 1) {
      HbMustacheName mustacheName = PsiTreeUtil.findChildOfType(openTag, HbMustacheName.class);

      if (mustacheName != null) {
        // insert the corresponding close tag
        editor.getDocument().insertString(offset, "{{/" + mustacheName.getText() + "}}");
      }
    }
  }

  /**
   * When appropriate, adjusts the formatting for some 'staches, particularily close 'staches
   * and simple inverses ("{{^}}" and "{{else}}")
   */
  private static void adjustMustacheFormatting(Project project, int offset, Editor editor, PsiFile file, FileViewProvider provider) {
    if (!HbConfig.isFormattingEnabled()) {
      // formatting disabled; nothing to do
      return;
    }

    PsiElement elementAtCaret = provider.findElementAt(offset - 1, HbLanguage.class);
    PsiElement closeOrSimpleInverseParent = PsiTreeUtil.findFirstParent(elementAtCaret, true, element -> (element instanceof HbSimpleInverse
                                                                                                          ||
                                                                                                          element instanceof HbCloseBlockMustache));

    // run the formatter if the user just completed typing a SIMPLE_INVERSE or a CLOSE_BLOCK_STACHE
    if (closeOrSimpleInverseParent != null) {
      // grab the current caret position (AutoIndentLinesHandler is about to mess with it)
      PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
      CaretModel caretModel = editor.getCaretModel();
      CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
      codeStyleManager.adjustLineIndent(file, editor.getDocument().getLineStartOffset(caretModel.getLogicalPosition().line));
    }
  }
}
