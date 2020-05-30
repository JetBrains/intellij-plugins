// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JSInjectionBracesUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlElement;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes;
import org.jetbrains.annotations.NotNull;

public class Angular2BracesInterpolationTypedHandler extends TypedHandlerDelegate {
  private final JSInjectionBracesUtil.InterpolationBracesCompleter myBracesCompleter;

  public Angular2BracesInterpolationTypedHandler() {
    myBracesCompleter = new JSInjectionBracesUtil.InterpolationBracesCompleter(Angular2Injector.Holder.BRACES_FACTORY) {
      @Override
      protected boolean checkTypingContext(@NotNull Editor editor, @NotNull PsiFile file) {
        PsiElement atCaret = getContextElement(editor, file);
        return atCaret == null
               || atCaret instanceof XmlElement
               || atCaret.getNode().getElementType() == Angular2HtmlTokenTypes.INTERPOLATION_END;
      }
    };
  }

  @Override
  public @NotNull Result beforeCharTyped(char c,
                                         @NotNull Project project,
                                         @NotNull Editor editor,
                                         @NotNull PsiFile file,
                                         @NotNull FileType fileType) {
    final Language language = file.getLanguage();
    if (language.isKindOf(Angular2HtmlLanguage.INSTANCE)) {
      return myBracesCompleter.beforeCharTyped(c, project, editor, file);
    }
    return Result.CONTINUE;
  }
}
