package org.angularjs.service;

import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.lang.javascript.service.JSLanguageService;
import com.intellij.lang.javascript.service.JSLanguageServiceProvider;
import com.intellij.lang.javascript.service.highlighting.JSLanguageServiceHighlightingPassFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;


public class Angular2HighlightingPassFactory extends JSLanguageServiceHighlightingPassFactory {

  @Nullable
  private final Angular2LanguageServiceProvider myProvider;

  public Angular2HighlightingPassFactory(Project project,
                                         @NotNull TextEditorHighlightingPassRegistrar highlightingPassRegistrar) {
    super(project, highlightingPassRegistrar);

    Optional<JSLanguageServiceProvider> providerOptional = Arrays.stream(JSLanguageServiceProvider.getProviders(project))
      .filter(el -> el instanceof Angular2LanguageServiceProvider)
      .findAny();

    myProvider = (Angular2LanguageServiceProvider)providerOptional.orElse(null);
  }

  @Nullable
  @Override
  protected JSLanguageService getService(@NotNull PsiFile file) {
    return myProvider != null ? myProvider.getService() : null;
  }

  @Override
  protected boolean isAcceptablePsiFile(@NotNull PsiFile file) {
    //fast check
    if (!(super.isAcceptablePsiFile(file) || file instanceof HtmlFileImpl)) {
      return false;
    }

    VirtualFile virtualFile = file.getVirtualFile();

    return virtualFile != null && virtualFile.isInLocalFileSystem();
  }
}
