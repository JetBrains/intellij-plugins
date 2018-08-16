package com.intellij.tapestry.intellij.actions.navigation;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.intellij.toolwindow.TapestryToolWindow;
import com.intellij.tapestry.intellij.toolwindow.TapestryToolWindowFactory;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Allows navigation from a tag to it's corresponding documentation.
 */
public class TagDocumentationNavigation extends AnAction {
  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    e.getPresentation().setEnabled(getTapestryComponent(e) != null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {

    Project project = (Project)event.getDataContext().getData(DataKeys.PROJECT.getName());
    if (project == null) return;
    Module module = (Module)event.getDataContext().getData(DataKeys.MODULE.getName());

    Component component = getTapestryComponent(event);
    if (component == null) return;

    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TapestryToolWindowFactory.TAPESTRY_TOOLWINDOW_ID);
    TapestryToolWindow metatoolWindow = TapestryToolWindowFactory.getToolWindow(project);

    if (!metatoolWindow.getMainPanel().isDisplayable() && toolWindow != null) {
      toolWindow.show(null);
    }

    metatoolWindow.update(module, component, Arrays.asList(component.getElementClass()));
  }

  @Nullable
  private static Component getTapestryComponent(AnActionEvent event) {
    Editor editor = (Editor)event.getDataContext().getData(DataKeys.EDITOR.getName());
    PsiFile psiFile = ((PsiFile)event.getDataContext().getData(DataKeys.PSI_FILE.getName()));

    if (editor == null || psiFile == null) return null;

    int caretOffset = editor.getCaretModel().getOffset();
    XmlTag tag = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), XmlTag.class);

    if (TapestryUtils.getComponentIdentifier(tag) == null) return null;

    return TapestryUtils.getTypeOfTag(tag);
  }
}
