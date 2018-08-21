package org.angular2.service;


import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.javascript.service.JSLanguageServiceProvider;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings;
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class Angular2LanguageServiceProvider implements JSLanguageServiceProvider {
  private final AtomicNotNullLazyValue<Angular2LanguageService> myLanguageService;

  public Angular2LanguageServiceProvider(Project project) {
    myLanguageService =
      AtomicNotNullLazyValue.createValue(() -> {
        Angular2LanguageService service = new Angular2LanguageService(project, TypeScriptCompilerSettings.getSettings(project));
        Disposer.register(project, service);

        return service;
      });
  }

  @NotNull
  @Override
  public Angular2LanguageService getService() {
    return myLanguageService.getValue();
  }

  @Override
  public boolean isCandidate(@NotNull VirtualFile file) {
    FileType type = file.getFileType();
    return TypeScriptLanguageServiceProvider.isJavaScriptOrTypeScriptFileType(type) || type == HtmlFileType.INSTANCE;
  }
}
