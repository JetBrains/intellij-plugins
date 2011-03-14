package com.intellij.flex.uiDesigner.actions;

import com.intellij.flex.uiDesigner.FlexUIDesignerApplicationManager;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

public class RunDesignViewAction extends AnAction {
  @Override
  public void actionPerformed(final AnActionEvent event) {
    final DataContext dataContext = event.getDataContext();
    final Project project = LangDataKeys.PROJECT.getData(dataContext);
    assert project != null;

    Editor editor = LangDataKeys.EDITOR.getData(dataContext);
    if (editor == null) {
      editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      assert editor != null;
    }

    final XmlFile psiFile = (XmlFile) PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    assert psiFile != null;
    final VirtualFile file = psiFile.getVirtualFile();
    assert file != null;
    final Module module = ModuleUtil.findModuleForFile(file, project);
    assert module != null;

    FlexUIDesignerApplicationManager.getInstance().openDocument(project, module, psiFile, isDebug());
  }

  protected boolean isDebug() {
    return false;
  }

  public void update(final AnActionEvent event) {
    event.getPresentation().setEnabled(isEnabled(event.getDataContext()));
  }

  private boolean isEnabled(final DataContext dataContext) {
    final Project project = LangDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      return false;
    }

    Editor editor = LangDataKeys.EDITOR.getData(dataContext);
    if (editor == null) {
      editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      if (editor == null) {
        return false;
      }
    }

    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    // todo icon enabled only for valid MXML — psiFile.isValid is not enough
    if (!(psiFile instanceof XmlFile) || !psiFile.isValid()) {
      return false;
    }
    else {
      final VirtualFile file = psiFile.getVirtualFile();
      if (file == null || !file.getName().endsWith(JavaScriptSupportLoader.MXML_FILE_EXTENSION_DOT) || !ProjectRootManager.getInstance(project).getFileIndex().isInSourceContent(file)) {
        return false;
      }

      JSClass jsClass = (JSClass) JSResolveUtil.unwrapProxy(XmlBackedJSClassImpl.getXmlBackedClass((XmlFile) psiFile));
      return jsClass != null && JSResolveUtil.isAssignableType("mx.core.IUIComponent", jsClass.getQualifiedName(), jsClass);
    }
  }
}
