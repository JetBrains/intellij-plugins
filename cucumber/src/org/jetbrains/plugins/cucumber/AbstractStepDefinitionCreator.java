package org.jetbrains.plugins.cucumber;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.StringBuilderSpinAllocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStepDefinitionCreator implements StepDefinitionCreator {
  @NotNull
  public String getStepDefinitionFilePath(@NotNull final PsiFile psiFile) {
    final VirtualFile file = psiFile.getVirtualFile();
    assert file != null;
    VirtualFile parent = file.getParent();
    // if file is direct child of step definitions dir
    if (parent != null && CucumberUtil.STEP_DEFINITIONS_DIR_NAME.equals(parent.getName())) {
      return file.getName();
    }

    // in subfolder
    final List<String> dirsReversed = new ArrayList<>();
    while (parent != null) {
      final String name = parent.getName();
      if (CucumberUtil.STEP_DEFINITIONS_DIR_NAME.equals(name)) {
        break;
      }
      dirsReversed.add(name);
      parent = parent.getParent();
    }
    final StringBuilder buf = StringBuilderSpinAllocator.alloc();
    try {
      for (int i = dirsReversed.size() - 1; i >= 0; i--) {
        buf.append(dirsReversed.get(i)).append(File.separatorChar);
      }
      buf.append(file.getName());
      return buf.toString();
    }
    finally {
      StringBuilderSpinAllocator.dispose(buf);
    }
  }

  @NotNull
  @Override
  public PsiDirectory getDefaultStepDefinitionFolder(@NotNull GherkinStep step) {
    PsiFile featureFile = step.getContainingFile();
    final PsiDirectory dir = findStepDefinitionDirectory(featureFile);
    if (dir == null) {
      final PsiDirectory featureParentDir = featureFile.getParent();
      assert featureParentDir != null;

      final Ref<PsiDirectory> dirRef = new Ref<>();
      new WriteCommandAction.Simple(step.getProject(),
                                    CucumberBundle.message("cucumber.quick.fix.create.step.command.name.add")) {
        @Override
        protected void run() throws Throwable {
          // create steps_definitions directory
          dirRef.set(featureParentDir.createSubdirectory(CucumberUtil.STEP_DEFINITIONS_DIR_NAME));
        }
      }.execute();

      return dirRef.get();
    }
    return dir;
  }

  @Nullable
  private static PsiDirectory findStepDefinitionDirectory(@NotNull final PsiFile featureFile) {
    final PsiDirectory psiFeatureDir = featureFile.getContainingDirectory();
    assert psiFeatureDir != null;

    VirtualFile featureDir = psiFeatureDir.getVirtualFile();
    VirtualFile contentRoot = ProjectRootManager.getInstance(featureFile.getProject()).getFileIndex().getContentRootForFile(featureDir);
    while (featureDir != null &&
           !Comparing.equal(featureDir, contentRoot) &&
           featureDir.findChild(CucumberUtil.STEP_DEFINITIONS_DIR_NAME) == null) {
      featureDir = featureDir.getParent();
    }
    if (featureDir != null) {
      VirtualFile stepsDir = featureDir.findChild(CucumberUtil.STEP_DEFINITIONS_DIR_NAME);
      if (stepsDir != null) {
        return featureFile.getManager().findDirectory(stepsDir);
      }
    }
    return null;
  }

  protected void closeActiveTemplateBuilders(PsiFile file) {
    final Project project = file.getProject();
    final VirtualFile vFile = ObjectUtils.assertNotNull(file.getVirtualFile());
    final OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vFile);
    FileEditorManager.getInstance(project).getAllEditors(vFile);
    FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
    final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

    assert editor != null;
    final TemplateManager templateManager = TemplateManager.getInstance(file.getProject());
    final TemplateState templateState = TemplateManagerImpl.getTemplateState(editor);
    final Template template = templateManager.getActiveTemplate(editor);
    if (templateState != null && template != null) {
      templateState.gotoEnd(false);
    }
  }
}
