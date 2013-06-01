package com.jetbrains.lang.dart.ide.watcher;

import com.intellij.ide.macro.FileDirPathFromParentMacro;
import com.intellij.ide.macro.FileNameMacro;
import com.intellij.ide.macro.FilePathMacro;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.plugins.watcher.config.BackgroundTaskConsumer;
import com.intellij.plugins.watcher.model.TaskOptions;
import com.intellij.psi.PsiBundle;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public class DartWebUITaskConsumer extends BackgroundTaskConsumer {
  private static final String BUILD_FILE_NAME = "build.dart";

  @Override
  public boolean isAvailable(PsiFile file) {
    return BUILD_FILE_NAME.equalsIgnoreCase(file.getName()) &&
           file instanceof DartFile &&
           DartSettingsUtil.getSettings().getCompiler() != null;
  }

  @NotNull
  @Override
  public TaskOptions getOptionsTemplate() {
    TaskOptions options = new TaskOptions();
    options.setName("dwc");
    final VirtualFile dartExecutable = DartSettingsUtil.getSettings().getCompiler();
    if (dartExecutable != null) {
      options.setProgram(dartExecutable.getPath());
    }
    options.setDescription("Executes " + BUILD_FILE_NAME + " on html file changes");
    options.setFileExtension("html");
    options.setScopeName(PsiBundle.message("psi.search.scope.project"));

    options.setArguments("--out=$" + new FilePathMacro().getName() + "$.js $" + new FilePathMacro().getName() + "$");
    if (dartExecutable != null) {
      options.setWorkingDir(dartExecutable.getParent().getPath());
    }

    options.setOutput("$" + new FileNameMacro().getName() + "$.js:$" +
                      new FileNameMacro().getName() + "$.js.map:$" +
                      new FileNameMacro().getName() + "$.js.deps");
    options.setTrackOnlyRoot(true);
    options.setImmediateSync(false);

    return options;
  }

  @Override
  public void additionalConfiguration(@NotNull Project project, @Nullable PsiFile file, @NotNull TaskOptions options) {
    super.additionalConfiguration(project, file, options);

    VirtualFile buildFile = file != null ? file.getVirtualFile() : null;
    if (buildFile == null) {
      return;
    }

    VirtualFile workingDir = buildFile.getParent();
    options.setWorkingDir(workingDir.getPath());

    StringBuilder arguments = new StringBuilder();

    arguments.append("--package-root=packages/ packages/web_ui/dwc.dart");
    arguments.append(" --out out/$");
    arguments.append(new FileDirPathFromParentMacro().getName());
    arguments.append("(").append(workingDir.getName()).append(")$");

    arguments.append(" $").append(new FileDirPathFromParentMacro().getName());
    arguments.append("(").append(workingDir.getName()).append(")$");
    arguments.append('$').append(new FileNameMacro().getName()).append('$');

    options.setArguments(arguments.toString());

    StringBuilder output = new StringBuilder();

    output.append("out/$");
    output.append(new FileDirPathFromParentMacro().getName());
    output.append("(").append(workingDir.getName()).append(")$");

    options.setOutput(output.toString());
  }
}
