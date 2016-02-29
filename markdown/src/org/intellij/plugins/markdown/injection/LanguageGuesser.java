package org.intellij.plugins.markdown.injection;

import com.intellij.lang.Language;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
      final HashMap<String, Language> result = new HashMap<String, Language>();
      for (Language language : Language.getRegisteredLanguages()) {
        result.put(language.getID().toLowerCase(Locale.US), language);
      }

      result.put("js", result.get("javascript"));
      return result;
    }
  };

  @Nullable
  public Language guessLanguage(@NotNull String languageName) {
    final Language languageFromMap = langIdToLanguage.getValue().get(languageName.toLowerCase(Locale.US));
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
