package org.angularjs.editor;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;

/**
 * @author Dennis.Ushakov
 */
public class AngularBracesInterpolationTypedHandler extends TypedHandlerDelegate {
  @Override
  public Result beforeCharTyped(char c, Project project, Editor editor, PsiFile file, FileType fileType) {
    if (file.getViewProvider() instanceof MultiplePsiFilesPerDocumentFileViewProvider ||
        DumbService.isDumb(project)) return Result.CONTINUE;

    if (!CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) return Result.DEFAULT;

    // we should use AngularJSBracesUtil here
    if (file.getFileType() == HtmlFileType.INSTANCE) {
      final Document document = editor.getDocument();
      if (c == '{') {
        if (!AngularJSBracesUtil.DEFAULT_START.equals(AngularJSBracesUtil.getInjectionStart(project)) ||
            !AngularJSBracesUtil.DEFAULT_END.equals(AngularJSBracesUtil.getInjectionEnd(project))) return Result.CONTINUE;
        JSCodeStyleSettings jsSettings = JSCodeStyleSettings.getSettings(file);
        boolean addWhiteSpaceBetweenBraces = jsSettings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS;
        int offset = editor.getCaretModel().getOffset();
        String chars = document.getText();
        if (offset > 0 && (chars.charAt(offset - 1)) == '{') {
          if (offset < 2 || (chars.charAt(offset - 2)) != '{') {
            if (alreadyHasEnding(chars, offset)) {
              return Result.CONTINUE;
            }
            else {
              String interpolation = addWhiteSpaceBetweenBraces ? "{  }" : "{}";

              if (offset == chars.length() || (offset < chars.length() && chars.charAt(offset) != '}')) {
                interpolation += "}";
              }

              EditorModificationUtil.insertStringAtCaret(editor, interpolation, true, addWhiteSpaceBetweenBraces ? 2 : 1);
              return Result.STOP;
            }
          }
        }
      }
      if (c == '}') {
        if (!AngularJSBracesUtil.DEFAULT_END.equals(AngularJSBracesUtil.getInjectionEnd(project))) return Result.CONTINUE;

        final int offset = editor.getCaretModel().getOffset();

        final char charAt;
        if (offset < document.getTextLength()) {
          charAt = document.getCharsSequence().charAt(offset);
          if (charAt == '}') {
            editor.getCaretModel().moveCaretRelatively(1, 0, false, false, true);
            return Result.STOP;
          }
        }
        else if (offset > 0) {
          charAt = document.getCharsSequence().charAt(offset - 1);
          if (charAt != '}') {
            EditorModificationUtil.insertStringAtCaret(editor, "}}", true, 2);
            return Result.STOP;
          }
        }
      }
    }

    return Result.CONTINUE;
  }

  private static boolean alreadyHasEnding(String chars, int offset) {
    int i = offset;
    while (i < chars.length() && (chars.charAt(i) != '{' && chars.charAt(i) != '}' && chars.charAt(i) != '\n')) {
      i++;
    }
    return i + 1 < chars.length() && chars.charAt(i) == '}' && chars.charAt(i + 1) == '}';
  }
}
