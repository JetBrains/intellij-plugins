package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbLanguageSubstitutor extends LanguageSubstitutor {
  @Nullable
  @Override
  public Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
    if (HbConfig.shouldOpenHtmlAsHandlebars(project) &&
        file.getName().endsWith(".html")) {
      return HbLanguage.INSTANCE;
    }

    return null;
  }
}
