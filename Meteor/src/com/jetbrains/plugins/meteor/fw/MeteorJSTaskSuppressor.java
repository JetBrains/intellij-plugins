package com.jetbrains.plugins.meteor.fw;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.plugins.watcher.config.BackgroundTaskSuppressor;
import com.intellij.plugins.watcher.model.TaskOptions;
import com.intellij.psi.PsiFile;
import com.jetbrains.plugins.meteor.MeteorFacade;
import org.jetbrains.annotations.NotNull;

final class MeteorJSTaskSuppressor extends BackgroundTaskSuppressor {
  @Override
  public boolean suppressed(@NotNull PsiFile file, @NotNull TaskOptions options) {
    if (!(file instanceof JSFile)) {
      return false;
    }

    final DialectOptionHolder dialect = DialectDetector.dialectOfElement(file);
    if (dialect != DialectOptionHolder.JS_WITH_JSX) {
      return false;
    }

    if (!MeteorFacade.getInstance().isMeteorProject(file.getProject())) {
      return false;
    }

    return "Babel".equals(options.getName());
  }
}
