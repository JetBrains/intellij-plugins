package org.angularjs.service;


import com.intellij.lang.javascript.service.JSLanguageService;
import com.intellij.lang.javascript.service.highlighting.JSLanguageServiceFilter;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static org.angularjs.service.Angular2LanguageService.isEnabledAngularService;

public class Angular2LanguageServiceFilter implements JSLanguageServiceFilter {
  @Override
  public boolean isAvailable(Project project, @NotNull JSLanguageService service, @NotNull VirtualFile file) {
    return service instanceof Angular2LanguageService ||
           !(service instanceof TypeScriptServerServiceImpl) ||
           !isEnabledAngularService(project);
  }
}
