package com.intellij.tapestry.intellij.actions.navigation;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.util.ComponentUtils;
import com.intellij.tapestry.intellij.TapestryProjectSupportLoader;
import com.intellij.tapestry.intellij.core.resource.xml.IntellijXmlTag;
import com.intellij.tapestry.intellij.toolwindow.TapestryToolWindow;
import com.intellij.tapestry.intellij.util.TapestryUtils;

import java.util.Arrays;

/**
 * Allows navigation from a tag to it's corresponding documentation.
 */
public class TagDocumentationNavigation extends AnAction {

  /**
   * {@inheritDoc}
   */
  public void actionPerformed(AnActionEvent event) {
    TapestryToolWindow metatoolWindow;

    Project project = (Project)event.getDataContext().getData(DataKeys.PROJECT.getName());
    if (project == null) return;
    Module module = (Module)event.getDataContext().getData(DataKeys.MODULE.getName());

    Editor editor = (Editor)event.getDataContext().getData(DataKeys.EDITOR.getName());
    PsiFile psiFile = ((PsiFile)event.getDataContext().getData(DataKeys.PSI_FILE.getName()));

    if (editor == null || psiFile == null) return;

    int caretOffset = editor.getCaretModel().getOffset();
    PsiElement element = psiFile.findElementAt(caretOffset);

    XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);

    if (tag == null || !ComponentUtils.isComponentTag(new IntellijXmlTag(tag))) return;

    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TapestryProjectSupportLoader.TAPESTRY_TOOLWINDOW_ID);
    metatoolWindow = project.getComponent(TapestryProjectSupportLoader.class).getTapestryToolWindow();

    Component component = TapestryUtils.getComponentFromTag(module, tag);
    if (component == null) return;

    if (!metatoolWindow.getMainPanel().isDisplayable() && toolWindow != null) {
      toolWindow.show(null);
    }

    metatoolWindow.update(module, component, Arrays.asList(component.getElementClass()));
    project.getComponent(TapestryProjectSupportLoader.class).enableToolWindow();
  }
}
