package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlComment;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;

public class MxmlEnterHandler extends EnterHandlerDelegateAdapter {
  public Result preprocessEnter(@NotNull PsiFile file,
                                @NotNull Editor editor,
                                @NotNull Ref<Integer> caretOffset,
                                @NotNull Ref<Integer> caretAdvance,
                                @NotNull DataContext dataContext,
                                EditorActionHandler originalHandler) {
    int offset = caretOffset.get().intValue();
    
    if (file instanceof JSFile) {
      PsiElement context = file.getContext();
      if (context instanceof XmlComment) {
        file = context.getContainingFile();
        editor = ((EditorWindow)editor).getDelegate();
        offset = editor.getCaretModel().getOffset();
      }
    }
    
    if (!JavaScriptSupportLoader.isFlexMxmFile(file)) return Result.Continue;
    
    if (CodeInsightSettings.getInstance().INSERT_BRACE_ON_ENTER && isAfterUnmatchedMxmlComment(editor, file, offset)) {
      String indent = "";
      CharSequence buffer = editor.getDocument().getCharsSequence();
      int lineStart = CharArrayUtil.shiftBackwardUntil(buffer, offset - 1, "\n") + 1;
      int current = lineStart;
      while(current < offset && Character.isWhitespace(buffer.charAt(current))) ++ current;
      if (current > lineStart) {
        indent = buffer.subSequence(lineStart, current).toString();
      }
      editor.getDocument().insertString(offset, "\n" + indent + "-->");
      originalHandler.execute(editor, dataContext);
      
      return Result.Stop;
    }
    return Result.Continue;
  }

  private static boolean isAfterUnmatchedMxmlComment(Editor editor, PsiFile file, int offset) {
    CharSequence chars = editor.getDocument().getCharsSequence();

    if (!(offset >= 5 && chars.charAt(offset - 1) == '-' && 
          chars.charAt(offset - 2) == '-' && 
          chars.charAt(offset - 3) == '-' && 
          chars.charAt(offset - 4) == '!' && 
          chars.charAt(offset - 5) == '<')) {
      return false;
    }

    final PsiElement at = file.findElementAt(offset);
    String parentText;
    String marker = "<!---";
    if (at != null && (parentText = at.getParent().getText()).endsWith("-->")) {
      return parentText.indexOf(marker, marker.length()) != -1;
    }
    return true;
  }
}
