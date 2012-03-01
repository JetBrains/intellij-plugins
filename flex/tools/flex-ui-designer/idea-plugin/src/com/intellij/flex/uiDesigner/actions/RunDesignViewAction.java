package com.intellij.flex.uiDesigner.actions;

import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.flex.uiDesigner.DesignerApplicationManager;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nullable;

public class RunDesignViewAction extends DumbAwareAction {
  private final String usageTriggerFeature;

  public RunDesignViewAction() {
    this("FlashUIDesigner.toolbar");
  }

  public RunDesignViewAction(String usageTriggerFeature) {
    this.usageTriggerFeature = usageTriggerFeature;
  }

  @Override
  public void actionPerformed(final AnActionEvent event) {
    final DataContext dataContext = event.getDataContext();
    XmlFile psiFile = (XmlFile)getPsiFile(PlatformDataKeys.PROJECT.getData(dataContext), dataContext, ActionPlaces.isPopupPlace(event.getPlace()));
    assert psiFile != null;

    if (!DebugPathManager.IS_DEV) {
      UsageTrigger.trigger(usageTriggerFeature);
    }

    DesignerApplicationManager.getInstance().openDocument(psiFile,
                                                          isDebug() || (DebugPathManager.IS_DEV && event.getInputEvent().isControlDown()));
  }

  protected boolean isDebug() {
    return false;
  }

  public void update(final AnActionEvent event) {
    final boolean popupPlace = ActionPlaces.isPopupPlace(event.getPlace());
    final boolean enabled = isEnabled(event.getDataContext(), popupPlace) && !DesignerApplicationManager.getInstance().isInitialRendering();
    if (popupPlace) {
      event.getPresentation().setVisible(enabled);
    }
    else {
      event.getPresentation().setEnabled(enabled);
    }
  }

  @Nullable
  private static PsiFile getPsiFile(Project project, DataContext dataContext, boolean popupPlace) {
    if (popupPlace) {
      final VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
      if (virtualFile != null) {
        return PsiManager.getInstance(project).findFile(virtualFile);
      }
    }
    else {
      Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
      if (editor == null) {
        editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      }
      if (editor != null) {
        return PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      }
    }

    return null;
  }

  private static boolean isEnabled(final DataContext dataContext, boolean popupPlace) {
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    return project != null &&
           !DumbService.isDumb(project) &&
           DesignerApplicationManager.isApplicable(project, getPsiFile(project, dataContext, popupPlace));
  }
}
