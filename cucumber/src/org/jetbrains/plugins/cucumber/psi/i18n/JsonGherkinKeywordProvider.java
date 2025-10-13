// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.i18n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.steps.CucumberStepHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.intellij.openapi.module.ModuleUtilCore.findModuleForPsiElement;

/// Provides Gherkin keywords from the upstream [`gherkin-languages.json`](https://github.com/cucumber/gherkin/blob/main/gherkin-languages.json) file,
/// with one modification from us: "Значения" (see RUBY-29359).
///
/// Our `gherkin-languages.json` file is vendored and must be manually updated to keep track of upstream changes.
///
/// There is also the `step-keywords.json` file which is generated from `gherkin-languages.json`.
/// It must also be updated manually whenever `gherkin-languages.json` changes.
/// See [CucumberStepIndex][org.jetbrains.plugins.cucumber.CucumberStepIndex] to learn more.
@NotNullByDefault
public final class JsonGherkinKeywordProvider implements GherkinKeywordProvider {

  private static final class Lazy {
    // leads to init of gher
    static final GherkinKeywordList myEmptyKeywordList = new GherkinKeywordList();
  }

  private final Map<String, GherkinKeywordList> myLanguageKeywords = new HashMap<>();
  private final Set<String> myAllStepKeywords = new HashSet<>();

  private static @Nullable GherkinKeywordProvider myKeywordProvider;
  private static @Nullable GherkinKeywordProvider myGherkin6KeywordProvider;

  public static GherkinKeywordProvider getKeywordProvider() {
    if (myKeywordProvider == null) {
      myKeywordProvider = createKeywordProviderFromJson("gherkin-languages-old.json");
    }
    return myKeywordProvider;
  }

  public static GherkinKeywordProvider getKeywordProvider(boolean gherkin6) {
    if (!gherkin6) {
      return getKeywordProvider();
    }
    if (myGherkin6KeywordProvider == null) {
      myGherkin6KeywordProvider = createKeywordProviderFromJson("gherkin-languages.json");
    }
    return myGherkin6KeywordProvider;
  }

  public static GherkinKeywordProvider getKeywordProvider(PsiElement context) {
    Module module = findModuleForPsiElement(context);
    boolean gherkin6Enabled = module != null && CucumberStepHelper.isGherkin6Supported(module);
    return getKeywordProvider(gherkin6Enabled);
  }

  private static GherkinKeywordProvider createKeywordProviderFromJson(String jsonFileName) {
    GherkinKeywordProvider result = null;
    ClassLoader classLoader = JsonGherkinKeywordProvider.class.getClassLoader();
    if (classLoader != null) {
      InputStream gherkinKeywordStream = Objects.requireNonNull(classLoader.getResourceAsStream(jsonFileName));
      result = new JsonGherkinKeywordProvider(gherkinKeywordStream);
    }

    return result != null ? result : new PlainGherkinKeywordProvider();
  }

  public JsonGherkinKeywordProvider(InputStream inputStream) {
    Map<String, Map<String, Object>> fromJson;
    try (Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      fromJson = new Gson().fromJson(in, new TypeToken<Map<String, HashMap<String, Object>>>() {
      }.getType());

      for (Map.Entry<String, Map<String, Object>> entry : fromJson.entrySet()) {
        Map<String, Object> translation = entry.getValue();
        final GherkinKeywordList keywordList = new GherkinKeywordList(translation);
        myLanguageKeywords.put(entry.getKey(), keywordList);
        for (String keyword : keywordList.getAllKeywords()) {
          if (keywordList.getTokenType(keyword) == GherkinTokenTypes.STEP_KEYWORD) {
            myAllStepKeywords.add(keyword);
          }
        }
      }
    }
    catch (MalformedJsonException e) {
      // ignore
    }
    catch (IOException e) {
      Logger.getInstance(JsonGherkinKeywordProvider.class.getName()).error(e);
    }
  }

  @Override
  public Collection<String> getAllKeywords(String language) {
    return getKeywordList(language).getAllKeywords();
  }

  @Override
  public IElementType getTokenType(String language, String keyword) {
    return getKeywordList(language).getTokenType(keyword);
  }

  @Override
  public String getBaseKeyword(String language, String keyword) {
    return getKeywordList(language).getBaseKeyword(keyword);
  }

  @Override
  public boolean isSpaceRequiredAfterKeyword(String language, String keyword) {
    return getKeywordList(language).isSpaceAfterKeyword(keyword);
  }

  @Override
  public boolean isStepKeyword(String keyword) {
    return myAllStepKeywords.contains(keyword);
  }

  @Override
  public GherkinKeywordTable getKeywordsTable(@Nullable String language) {
    return getKeywordList(language).getKeywordsTable();
  }

  private GherkinKeywordList getKeywordList(@Nullable String language) {
    GherkinKeywordList keywordList = myLanguageKeywords.get(language);
    if (keywordList == null) {
      keywordList = Lazy.myEmptyKeywordList;
    }
    return keywordList;
  }
}
