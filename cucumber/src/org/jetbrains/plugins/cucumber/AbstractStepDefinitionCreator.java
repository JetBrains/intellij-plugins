// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractStepDefinitionCreator implements StepDefinitionCreator {
  @Override
  public @NotNull String getStepDefinitionFilePath(final @NotNull PsiFile psiFile) {
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
    final StringBuilder buf = new StringBuilder();
    for (int i = dirsReversed.size() - 1; i >= 0; i--) {
      buf.append(dirsReversed.get(i)).append(File.separatorChar);
    }
    buf.append(file.getName());
    return buf.toString();
  }

  @Override
  public @NotNull String getDefaultStepDefinitionFolderPath(@NotNull GherkinStep step) {
    PsiFile featureFile = step.getContainingFile();
    final PsiDirectory dir = findStepDefinitionDirectory(featureFile);
    if (dir != null) {
      return dir.getVirtualFile().getPath();
    }
    return FileUtil.join(featureFile.getContainingDirectory().getVirtualFile().getPath(), CucumberUtil.STEP_DEFINITIONS_DIR_NAME);
  }

  private static @Nullable PsiDirectory findStepDefinitionDirectory(final @NotNull PsiFile featureFile) {
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
    final VirtualFile vFile = Objects.requireNonNull(file.getVirtualFile());
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
