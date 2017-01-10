package org.intellij.plugins.markdown.injection;

import com.intellij.lang.Language;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public enum LanguageGuesser {
  INSTANCE;

  private NotNullLazyValue<List<EmbeddedTokenTypesProvider>> embeddedTokenTypeProviders =
    new NotNullLazyValue<List<EmbeddedTokenTypesProvider>>() {
      @NotNull
      @Override
      protected List<EmbeddedTokenTypesProvider> compute() {
        return Arrays.asList(Extensions.getExtensions(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME));
      }
    };

  private NotNullLazyValue<Map<String, Language>> langIdToLanguage = new NotNullLazyValue<Map<String, Language>>() {
    @NotNull
    @Override
    protected Map<String, Language> compute() {
      final HashMap<String, Language> result = new HashMap<>();
      for (Language language : Language.getRegisteredLanguages()) {
        if (language.getID().isEmpty()) {
          continue;
        }

        result.put(language.getID().toLowerCase(Locale.US), language);
      }

      final Language javascriptLanguage = result.get("javascript");
      if (javascriptLanguage != null) {
        result.put("js", javascriptLanguage);
      }
      return result;
    }
  };

  @NotNull
  public Map<String, Language> getLangToLanguageMap() {
    return Collections.unmodifiableMap(langIdToLanguage.getValue());
  }

  @Nullable
  public Language guessLanguage(@NotNull String languageName) {
    // Add support for rmarkdown chunks (see http://rmarkdown.rstudio.com/authoring_rcodechunks.html)
    languageName = languageName.startsWith("{r ") ? "r" : languageName;

    final Language languageFromMap = langIdToLanguage.getValue().get(languageName);
    if (languageFromMap != null) {
      return languageFromMap;
    }

    for (EmbeddedTokenTypesProvider provider : embeddedTokenTypeProviders.getValue()) {
      if (provider.getName().equalsIgnoreCase(languageName)) {
        return provider.getElementType().getLanguage();
      }
    }
    return null;
  }
}
