package org.jetbrains.plugins.cucumber.java.psi;

import com.google.common.base.Functions;
import gherkin.I18n;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class GherkinJsonLoader implements GherkinLoader {

  private static void closeQuietly(InputStreamReader stream) {
    if (stream != null) {
      try {
        stream.close();
      }
      catch (IOException e) {
        //ignore it
      }
    }
  }

  public static GherkinLoader loadLanguagesFromGherkinLibrary() {
    GherkinJsonLoader loader = new GherkinJsonLoader("/gherkin/i18n.json");
    loader.load();
    return loader;
  }

  private final String myResourcePath;
  private Map<String, Map<String, String>> rawData = Collections.emptyMap();

  public GherkinJsonLoader(String resourcePath) {
    myResourcePath = resourcePath;
  }

  @Override
  public Set<String> supportedLanguages() {
    return rawData.keySet();
  }

  @Override
  public Map<String, String> getRawInputFromI18nJsonFileFor(String isoCode) {
    Map<String, String> emptyMap = Collections.emptyMap();
    return Functions.forMap(rawData, emptyMap).apply(isoCode);
  }

  public void load() {
    InputStreamReader json = null;
    try {
      json = new InputStreamReader(I18n.class.getResourceAsStream(myResourcePath), "UTF-8");
      this.rawData = new Gson().fromJson(json, new TypeToken<Map<String, Map<String, String>>>() {
      }.getType());
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    finally {
      closeQuietly(json);
    }

  }
}
