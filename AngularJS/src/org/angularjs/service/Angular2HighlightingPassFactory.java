package org.angularjs.service;

import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.lang.javascript.service.JSLanguageServiceBase;
import com.intellij.lang.javascript.service.JSLanguageServiceProvider;
import com.intellij.lang.javascript.service.highlighting.JSLanguageServiceHighlightingPassFactory;
import com.intellij.openapi.project.Project;
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

    myProvider = providerOptional.isPresent() ? (Angular2LanguageServiceProvider)providerOptional.get() : null;
  }

  @Nullable
  @Override
  protected JSLanguageServiceBase getService() {
    return myProvider != null ? myProvider.getService() : null;
  }
}
