package com.jetbrains.plugins.meteor.spacebars.lang;

import com.dmarcotte.handlebars.file.HbLanguageSubstitutor;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.plugins.meteor.MeteorFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SpacebarsLanguageSubstitutor extends HbLanguageSubstitutor {
  public static final String METEOR_ANGULAR_EXTENSION = ".ng.html";

  @Override
  public @Nullable Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
    Language language = super.getLanguage(file, project);
    if (null != language && MeteorFacade.getInstance().isMeteorProject(project)) {
      return file.getName().endsWith(METEOR_ANGULAR_EXTENSION) ? HTMLLanguage.INSTANCE : SpacebarsLanguageDialect.INSTANCE;
    }

    return language;
  }
}
