package org.angularjs.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.javascript.JSInjectionBracesUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.angularjs.html.Angular2HTMLLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularBracesInterpolationTypedHandler extends TypedHandlerDelegate {
  private final JSInjectionBracesUtil.InterpolationBracesCompleter myBracesCompleter;

  public AngularBracesInterpolationTypedHandler() {
    myBracesCompleter = new JSInjectionBracesUtil.InterpolationBracesCompleter(AngularJSInjector.BRACES_FACTORY);
  }

  @NotNull
  @Override
  public Result beforeCharTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file, @NotNull FileType fileType) {
    final Language language = file.getLanguage();
    if (HTMLLanguage.INSTANCE.equals(language) || Angular2HTMLLanguage.INSTANCE.equals(language)) {
      return myBracesCompleter.beforeCharTyped(c, project, editor, file);
    }
    return Result.CONTINUE;
  }
}