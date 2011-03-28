package com.intellij.flex.uiDesigner.actions;

import com.intellij.flex.uiDesigner.FlexUIDesignerApplicationManager;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

public class RunDesignViewAction extends DumbAwareAction {
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

    final XmlFile psiFile = (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
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
    final boolean enabled = isEnabled(event.getDataContext()) &&!FlexUIDesignerApplicationManager.getInstance().isDocumentOpening();
    if (ActionPlaces.isPopupPlace(event.getPlace())) {
      event.getPresentation().setVisible(enabled);
    }
    else {
      event.getPresentation().setEnabled(enabled);
    }
  }

  private static boolean isEnabled(final DataContext dataContext) {
    final Project project = LangDataKeys.PROJECT.getData(dataContext);
    if (project == null || DumbService.isDumb(project)) {
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
    // TODO check MXML is valid
    if (psiFile == null || !JavaScriptSupportLoader.isFlexMxmFile(psiFile)) {
      return false;
    }
    final VirtualFile file = psiFile.getVirtualFile();
    if (file == null || !ProjectRootManager.getInstance(project).getFileIndex().isInSourceContent(file)) {
      return false;
    }

    JSClass jsClass = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)psiFile);
    return jsClass != null && JSInheritanceUtil.isParentClass(jsClass, ClassBackedElementDescriptor.UI_COMPONENT_BASE_INTERFACE);
  }
}
