package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.TypedHandler;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartPsiCompositeElement;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class DartTypeHandler extends TypedHandlerDelegate {
  private boolean myAfterTypeOrComponentName = false;
  private boolean myAfterDollar = false;
  static final TokenSet INVALID_INSIDE_REFERENCE = TokenSet.create(DartTokenTypes.SEMICOLON, DartTokenTypes.LBRACE, DartTokenTypes.RBRACE);
  
  @Override
  public Result beforeCharTyped(char c,
                                Project project,
                                Editor editor,
                                PsiFile file,
                                FileType fileType) {
    int offset = editor.getCaretModel().getOffset();
    if (c == '<') {
      TypedHandler.commitDocumentIfCurrentCaretIsNotTheFirstOne(editor, project);
      myAfterTypeOrComponentName = checkAfterTypeOrComponentName(file, offset);
    }
    else if (c == '>') {
      if (handleDartGT(editor, DartTokenTypes.LT, DartTokenTypes.GT, INVALID_INSIDE_REFERENCE)) {
        return Result.STOP;
      }
    }
    else if (c == '{') {
      TypedHandler.commitDocumentIfCurrentCaretIsNotTheFirstOne(editor, project);
      myAfterDollar = checkAfterDollarInString(file, offset);
    }
      else if (c == '}') {
      if (editor.getDocument().getText().charAt(editor.getCaretModel().getOffset()) == '}') {
        EditorModificationUtil.moveCaretRelatively(editor, 1);
        return Result.STOP;
      }
    }

    return super.beforeCharTyped(c, project, editor, file, fileType);
  }

  private static boolean checkAfterTypeOrComponentName(PsiFile file, int offset) {
    PsiElement at = file.findElementAt(offset - 1);
    PsiElement toCheck = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(at, false);
    return PsiTreeUtil.getParentOfType(toCheck, DartType.class, DartComponentName.class) != null;
  }

  private static boolean checkAfterDollarInString(PsiFile file, int offset) {
    PsiElement at = file.findElementAt(offset - 1);
    final String text = at != null ? at.getText() : "";
    return StringUtil.endsWithChar(text, '$') && isDartContext(at);
  }

  private static boolean isDartContext(PsiElement at) {
    return PsiTreeUtil.getParentOfType(at, DartPsiCompositeElement.class) != null;
  }

  @Override
  public Result charTyped(char c, Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    String textToInsert = null;
    if (c == '<' && myAfterTypeOrComponentName) {
      myAfterTypeOrComponentName = false;
      textToInsert = ">";
    }
    else if (c == '{' && myAfterDollar) {
      myAfterDollar = false;
      textToInsert = "}";
    }
    if (textToInsert != null) {
      EditorModificationUtil.insertStringAtCaret(editor, textToInsert, false, 0);
      return Result.STOP;
    }
    return super.charTyped(c, project, editor, file);
  }

  //need custom handler, since brace matcher cannot be used
  public static boolean handleDartGT(final Editor editor,
                                     final IElementType lt,
                                     final IElementType gt,
                                     final TokenSet invalidInsideReference) {
    if (!CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) return false;

    int offset = editor.getCaretModel().getOffset();

    if (offset == editor.getDocument().getTextLength()) return false;

    HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(offset);
    if (iterator.getTokenType() != gt) return false;
    while (!iterator.atEnd() && !invalidInsideReference.contains(iterator.getTokenType())) {
      iterator.advance();
    }

    if (!iterator.atEnd() && invalidInsideReference.contains(iterator.getTokenType())) iterator.retreat();

    int balance = 0;
    while (!iterator.atEnd() && balance >= 0) {
      final IElementType tokenType = iterator.getTokenType();
      if (tokenType == lt) {
        balance--;
      }
      else if (tokenType == gt) {
        balance++;
      }
      else if (invalidInsideReference.contains(tokenType)) {
        break;
      }

      iterator.retreat();
    }

    if (balance == 0) {
      EditorModificationUtil.moveCaretRelatively(editor, 1);
      return true;
    }

    return false;
  }
}
