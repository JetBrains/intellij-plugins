package org.angular2.service;


import com.intellij.lang.javascript.service.JSLanguageService;
import com.intellij.lang.javascript.service.highlighting.JSLanguageServiceFilter;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.angular2.settings.AngularSettings;
import org.jetbrains.annotations.NotNull;

import static org.angular2.service.Angular2LanguageService.isEnabledAngularService;

public class Angular2LanguageServiceFilter implements JSLanguageServiceFilter {
  @Override
  public boolean isAvailable(Project project, @NotNull JSLanguageService service, @NotNull VirtualFile file) {
    if (!(service instanceof TypeScriptServerServiceImpl)) {
      return true;
    }

    if (!AngularSettings.get(project).isUseService()) {
      return !(service instanceof Angular2LanguageService);
    }

    //we don't know which service we need
    if (DumbService.isDumb(project)) {
      return false;
    }

    return service instanceof Angular2LanguageService == isEnabledAngularService(project, file);
  }
}
