package org.intellij.plugins.postcss;

import com.intellij.lang.Language;
import com.intellij.lang.css.CssDialectMappings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssLanguageSubstitutor extends LanguageSubstitutor {
  @Override
  public @Nullable Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
    String dialectName = CssDialectMappings.getInstance(project).getMapping(file);
    return PostCssDialect.DIALECT_NAME.equals(dialectName) ? PostCssLanguage.INSTANCE : null;
  }
}