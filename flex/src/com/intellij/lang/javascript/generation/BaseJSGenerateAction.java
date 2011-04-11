package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 19, 2008
 *         Time: 7:04:37 PM
 */
abstract class BaseJSGenerateAction extends AnAction {

  public void actionPerformed(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    assert project != null;
    final Pair<Editor, PsiFile> editorAndPsiFile = getEditorAndPsiFile(e);
    getGenerateHandler().invoke(project, editorAndPsiFile.first, editorAndPsiFile.second);
  }

  private static Pair<Editor, PsiFile> getEditorAndPsiFile(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return Pair.create(null, null);
    Editor editor = e.getData(PlatformDataKeys.EDITOR);
    PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
    
    final VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    if (file != null && JavaScriptSupportLoader.isFlexMxmFile(file)) {
      editor = BaseCodeInsightAction.getInjectedEditor(project, editor);
      psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
    }

    return Pair.create(editor, psiFile);
  }
  
  protected abstract BaseJSGenerateHandler getGenerateHandler();

  @Override
  public void update(final AnActionEvent e) {
    final Pair<Editor, PsiFile> editorAndPsiFile = getEditorAndPsiFile(e);
    final VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    final Editor editor = editorAndPsiFile.first;
    final PsiFile psiFile = editorAndPsiFile.second;


    JSClass jsClass = null;
    if (file != null && psiFile != null && editor != null) {
      if (JavaScriptSupportLoader.isFlexMxmFile(file) || (file.getFileType() == ActionScriptFileType.INSTANCE)) {
        jsClass = BaseJSGenerateHandler.findClass(psiFile, editor);
      }
    }

    final boolean status = jsClass != null && !jsClass.isInterface() && isApplicableForJsClass(jsClass, psiFile, editor);

    e.getPresentation().setEnabled(status);
    e.getPresentation().setVisible(status);
  }

  protected boolean isApplicableForJsClass(final @NotNull JSClass jsClass, final PsiFile psiFile, final @NotNull Editor editor) {
    return true;
  }
}
