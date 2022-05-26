package jetbrains.plugins.yeoman.actions;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.plugins.yeoman.projectGenerator.ui.run.YeomanRunGeneratorDialog;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class YeomanRunGeneratorByNameAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    assert project != null;

    VirtualFile toProcess = getDirectoryForProcessing(e);
    if (toProcess == null || toProcess.getCanonicalPath() == null) {
      return;
    }

    final YeomanRunGeneratorDialog dialog = new YeomanRunGeneratorDialog(project, toProcess, null);
    dialog.showAndGet();

    Disposer.dispose(dialog.getForm());
  }

  private static VirtualFile getDirectoryForProcessing(@NotNull AnActionEvent e) {

    VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
    if (files == null) {
      return null;
    }
    else {
      Optional<VirtualFile> result = Arrays.stream(files).filter(VirtualFile::isDirectory).findAny();
      return result.orElse(null);
    }
  }
}
