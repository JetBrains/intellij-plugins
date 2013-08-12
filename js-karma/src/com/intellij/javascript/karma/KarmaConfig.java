package com.intellij.javascript.karma;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaConfig {

  private static final Logger LOG = Logger.getInstance(KarmaConfig.class);
  private static final String BASE_PATH = "basePath";
  private static final String HOST_NAME = "hostname";
  private static final String URL_ROOT = "urlRoot";

  private final String myBasePath;
  private final String myHostname;
  private final String myUrlRoot;

  public KarmaConfig(@Nullable String basePath, @NotNull String hostname, @NotNull String urlRoot) {
    myBasePath = basePath;
    myHostname = hostname;
    myUrlRoot = urlRoot;
  }

  @Nullable
  public String getBasePath() {
    return myBasePath;
  }

  @NotNull
  public String getHostname() {
    return myHostname;
  }

  @NotNull
  public String getUrlRoot() {
    return myUrlRoot;
  }

  @Nullable
  public static KarmaConfig parseFromJson(@NotNull JsonElement jsonElement) {
    if (jsonElement.isJsonObject()) {
      JsonObject rootObject = jsonElement.getAsJsonObject();
      String basePath = getAsString(rootObject.get(BASE_PATH));
      String hostname = getAsString(rootObject.get(HOST_NAME));
      String urlRoot = getAsString(rootObject.get(URL_ROOT));
      if (hostname == null) {
        LOG.warn("Can not parse Karma config.hostname from " + jsonElement.toString());
        hostname = "localhost";
      }
      hostname = hostname.toLowerCase();
      if (urlRoot == null) {
        LOG.warn("Can not parse Karma config.urlRoot from " + jsonElement.toString());
        urlRoot = "/";
      }
      if (!urlRoot.startsWith("/")) {
        urlRoot = "/" + urlRoot;
      }
      if (urlRoot.length() > 1 && urlRoot.endsWith("/")) {
        urlRoot = urlRoot.substring(0, urlRoot.length() - 1);
      }
      return new KarmaConfig(basePath, hostname, urlRoot);
    }
    return null;
  }

  @Nullable
  private static String getAsString(@Nullable JsonElement element) {
    if (element != null && element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isString()) {
        return primitive.getAsString();
      }
    }
    return null;
  }

}
