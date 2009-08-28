package com.intellij.tapestry.intellij;

import com.intellij.ProjectTopics;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.psi.*;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

/**
 * Registers all event listeners into IntelliJ IDEA API.
 */
public class TapestryListenersSupportLoader implements ProjectComponent {

  private final Project myProject;

  public TapestryListenersSupportLoader(Project project) {
    myProject = project;
  }

  /**
   * {@inheritDoc}
   */
  public void initComponent() {
  }

  /**
   * {@inheritDoc}
   */
  public void disposeComponent() {
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public String getComponentName() {
    return TapestryListenersSupportLoader.class.getName();
  }

  public void projectOpened() {

    // Listener for file deletion
    PsiManager.getInstance(myProject).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
      public void childRemoved(PsiTreeChangeEvent event) {
        Module module = getModuleFromEvent(event);

        if (!TapestryUtils.isTapestryModule(module)) {
          return;
        }
        TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

        PsiElement child = event.getChild();
        if (child instanceof PsiJavaFile && IdeaUtils.findPublicClass(((PsiJavaFile)child).getClasses()) != null) {
          tapestryProject.getEventsManager().classDeleted(IdeaUtils.findPublicClass(((PsiJavaFile)child).getClasses()).getQualifiedName());
        }

        if (child instanceof PsiFile) {
          tapestryProject.getEventsManager().fileDeleted(((PsiFile)child).getVirtualFile().getPath());
        }

        if (child instanceof PsiDirectory) {
          tapestryProject.getEventsManager().fileDeleted(((PsiDirectory)child).getVirtualFile().getPath());
        }

        if (event.getFile() != null) {
          tapestryProject.getEventsManager().fileContentsChanged(new IntellijResource(event.getFile()));
        }
      }

      public void childAdded(PsiTreeChangeEvent event) {
        Module module = getModuleFromEvent(event);
        if (module == null) {
          return;
        }

        TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

        if (!TapestryUtils.isTapestryModule(module)) {
          return;
        }

        if (event.getFile() != null) {
          tapestryProject.getEventsManager().fileContentsChanged(new IntellijResource(event.getFile()));
        }

        if (!(event.getChild() instanceof PsiFile)) {
          return;
        }

        PsiFile psiFile = (PsiFile)event.getChild();

        if (psiFile instanceof PsiJavaFile) {
          tapestryProject.getEventsManager().classCreated(null);

          return;
        }

        tapestryProject.getEventsManager().fileCreated(psiFile.getVirtualFile().getPath());
      }
    });

    MessageBusConnection connection = myProject.getMessageBus().connect(myProject);
    connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      public void beforeRootsChange(ModuleRootEvent event) {
      }

      public void rootsChanged(ModuleRootEvent event) {
        for (Module module : ModuleManager.getInstance((Project)event.getSource()).getModules()) {
          if (!TapestryUtils.isTapestryModule(module)) {
            return;
          }

          TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);
          tapestryProject.getEventsManager().modelChanged();
        }
      }
    });
  }

  public void projectClosed() {
  }

  private static Module getModuleFromEvent(PsiTreeChangeEvent event) {
    PsiElement parent = event.getParent();
    return parent == null ? null : ModuleUtil.findModuleForPsiElement(parent);
  }
}
