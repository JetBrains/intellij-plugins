package com.jetbrains.flask.project;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class FlaskProjectGenerator implements DirectoryProjectGenerator<FlaskProjectSettings> {
  @Nls
  @Override
  public String getName() {
    return "Flask project";
  }

  @Override
  public FlaskProjectSettings showGenerationSettings(VirtualFile baseDir) throws ProcessCanceledException {
    return new FlaskProjectSettings();
  }

  @Override
  public void generateProject(Project project, VirtualFile baseDir, FlaskProjectSettings settings, Module module) {
  }

  @NotNull
  @Override
  public ValidationResult validate(@NotNull String baseDirPath) {
    return ValidationResult.OK;
  }
}
