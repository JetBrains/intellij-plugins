package com.intellij.tapestry.intellij.actions.navigation;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.intellij.toolwindow.TapestryToolWindow;
import com.intellij.tapestry.intellij.toolwindow.TapestryToolWindowFactory;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * Allows navigation from a tag to it's corresponding documentation.
 */
public class TagDocumentationNavigation extends AnAction {
  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(getTapestryComponent(e) != null);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {

    Project project = event.getData(CommonDataKeys.PROJECT);
    if (project == null) return;
    Module module = event.getData(PlatformCoreDataKeys.MODULE);

    TapestryComponent component = getTapestryComponent(event);
    if (component == null) return;

    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TapestryToolWindowFactory.TAPESTRY_TOOLWINDOW_ID);
    TapestryToolWindow metatoolWindow = TapestryToolWindowFactory.getToolWindow(project);

    if (!metatoolWindow.getMainPanel().isDisplayable() && toolWindow != null) {
      toolWindow.show(null);
    }

    metatoolWindow.update(module, component, Collections.singletonList(component.getElementClass()));
  }

  @Nullable
  private static TapestryComponent getTapestryComponent(AnActionEvent event) {
    Editor editor = event.getData(CommonDataKeys.EDITOR);
    PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

    if (editor == null || psiFile == null) return null;

    int caretOffset = editor.getCaretModel().getOffset();
    XmlTag tag = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), XmlTag.class);

    if (TapestryUtils.getComponentIdentifier(tag) == null) return null;

    return TapestryUtils.getTypeOfTag(tag);
  }
}