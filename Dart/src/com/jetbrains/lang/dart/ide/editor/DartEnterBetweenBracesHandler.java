package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.enter.EnterBetweenBracesHandler;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartLanguage;
import org.jetbrains.annotations.NotNull;

public class DartEnterBetweenBracesHandler extends EnterBetweenBracesHandler {
  @Override
  public EnterHandlerDelegate.Result preprocessEnter(@NotNull PsiFile file,
                                                     @NotNull Editor editor,
                                                     @NotNull Ref<Integer> caretOffsetRef,
                                                     @NotNull Ref<Integer> caretAdvance,
                                                     @NotNull DataContext dataContext,
                                                     EditorActionHandler originalHandler) {
    if (!file.getLanguage().is(DartLanguage.INSTANCE)) {
      return EnterHandlerDelegate.Result.Continue;
    }
    return super.preprocessEnter(file, editor, caretOffsetRef, caretAdvance, dataContext, originalHandler);
  }

  @Override
  protected boolean isBracePair(char c1, char c2) {
    return super.isBracePair(c1, c2) || (c1 == '[' && c2 == ']');
  }
}
