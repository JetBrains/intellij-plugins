package com.jetbrains.plugins.jade.watcher;

import com.intellij.ide.macro.FileDirMacro;
import com.intellij.ide.macro.FileNameMacro;
import com.intellij.ide.macro.FileNameWithoutExtension;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.watcher.config.BackgroundTaskConsumer;
import com.intellij.plugins.watcher.model.TaskOptions;
import com.intellij.psi.PsiFile;
import com.jetbrains.plugins.jade.JadeBundle;
import com.jetbrains.plugins.jade.JadeToPugTransitionHelper;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import org.jetbrains.annotations.NotNull;

final class JadeOrPugTaskConsumer extends BackgroundTaskConsumer {
  @Override
  public boolean isAvailable(@NotNull PsiFile file) {
    return file instanceof JadeFileImpl;
  }

  @Override
  public String getConsumeMessage() {
    return JadeBundle.message("pug.task-consumer.message");
  }

  @Override
  public @NotNull TaskOptions getOptionsTemplate() {
    TaskOptions options = new TaskOptions();
    options.setName("Pug/Jade");
    options.setDescription(JadeBundle.message("pug.task-consumer.task.description"));
    options.setFileExtension(StringUtil.join(JadeToPugTransitionHelper.ALL_EXTENSIONS, FileTypeConsumer.EXTENSION_DELIMITER));

    options.setArguments("$" + new FileNameMacro().getName() + "$");
    options.setWorkingDir("$" + new FileDirMacro().getName() + "$");
    options.setOutput("$" + new FileNameWithoutExtension().getName() + "$.html");
    options.setProgram("pug");

    return options;
  }
}
