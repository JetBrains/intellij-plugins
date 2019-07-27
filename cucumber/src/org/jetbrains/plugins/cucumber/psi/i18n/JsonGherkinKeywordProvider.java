// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi.i18n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.intellij.openapi.module.ModuleUtilCore.findModuleForPsiElement;

public class JsonGherkinKeywordProvider implements GherkinKeywordProvider {
  private static final Logger LOG = Logger.getInstance(JsonGherkinKeywordProvider.class.getName());

  private final GherkinKeywordList myEmptyKeywordList = new GherkinKeywordList();
  private final Map<String, GherkinKeywordList> myLanguageKeywords = new HashMap<>();
  private final Set<String> myAllStepKeywords = new HashSet<>();

  private static GherkinKeywordProvider myKeywordProvider;
  private static GherkinKeywordProvider myGherkin6KeywordProvider;

  public static GherkinKeywordProvider getKeywordProvider() {
    if (myKeywordProvider == null) {
      myKeywordProvider = createKeywordProviderFromJson("i18n_old.json");
    }
    return myKeywordProvider;
  }

  public static GherkinKeywordProvider getKeywordProvider(boolean gherkin6) {
    if (!gherkin6) {
      return getKeywordProvider();
    }
    if (myGherkin6KeywordProvider == null) {
      myGherkin6KeywordProvider = createKeywordProviderFromJson("i18n.json");
    }
    return myGherkin6KeywordProvider;
  }
  
  public static GherkinKeywordProvider getKeywordProvider(@NotNull PsiElement context) {
    Module module = findModuleForPsiElement(context);
    boolean gherkin6Enabled = module != null && CucumberStepsIndex.getInstance(context.getProject()).isGherkin6Supported(module);
    return getKeywordProvider(gherkin6Enabled);
  }

  private static GherkinKeywordProvider createKeywordProviderFromJson(@NotNull String jsonFileName) {
    GherkinKeywordProvider result = null;
    ClassLoader classLoader = JsonGherkinKeywordProvider.class.getClassLoader();
    if (classLoader != null) {
      InputStream gherkinKeywordStream = ObjectUtils.notNull(classLoader.getResourceAsStream(jsonFileName));
      result = new JsonGherkinKeywordProvider(gherkinKeywordStream);
    }

    return result != null ? result : new PlainGherkinKeywordProvider(); 
  }

  public JsonGherkinKeywordProvider(@NotNull InputStream inputStream) {
    Map<String, Map<String, Object>> fromJson;
    try {
      final Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      try {
        fromJson = new Gson().fromJson(in, new TypeToken<Map<String, HashMap<String, Object>>>() {}.getType());

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
      finally {
        in.close();
      }
    }
    catch (MalformedJsonException e) {
      // ignore
    }
    catch (IOException e) {
      LOG.error(e);
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

  @NotNull
  @Override
  public GherkinKeywordTable getKeywordsTable(@Nullable String language) {
    return getKeywordList(language).getKeywordsTable();
  }

  @NotNull
  private GherkinKeywordList getKeywordList(@Nullable final String language) {
    GherkinKeywordList keywordList = myLanguageKeywords.get(language);
    if (keywordList == null) {
      keywordList = myEmptyKeywordList;
    }
    return keywordList;
  }
}
