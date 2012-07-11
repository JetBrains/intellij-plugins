package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.AbstractModificationTracker;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public class GherkinPsiManager extends AbstractModificationTracker implements ProjectComponent {
  private PsiModificationTrackerImpl myModificationTracker;
  protected Project myProject;

  public GherkinPsiManager(@NotNull final Project project, @NotNull final PsiManagerImpl psiManager) {
    super(psiManager);
    myProject = project;
  }

  public static GherkinPsiManager getInstance(@NotNull final Project project) {
    return project.getComponent(GherkinPsiManager.class);
  }

  public void projectOpened() {
    // Do nothing
  }

  public void projectClosed() {
    // Do nothing
  }

  @NotNull
  public String getComponentName() {
    return "GherkinPsiManager";
  }

  public void initComponent() {
    initTracker();
  }

  public void disposeComponent() {
    // Do nothing
  }


  @Override
  protected boolean isInsideCodeBlock(PsiElement element) {
    if (element instanceof PsiFileSystemItem) {
      return false;
    }

    if (element == null || element.getParent() == null) return true;

    while(true) {
      if (element instanceof GherkinFile
          || element instanceof GherkinStep
          || element instanceof GherkinFeature
          || element instanceof GherkinStepsHolder) {
        return false;
      }
      if (element instanceof PsiFile || element instanceof PsiDirectory || element == null) {
        return true;
      }
      PsiElement parent = element.getParent();
      if (parent instanceof GherkinTable
          || parent instanceof GherkinExamplesBlock
          || parent instanceof GherkinTableRow) {
        return true;
      }
      element = parent;
    }
  }

}
