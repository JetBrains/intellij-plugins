// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.HttpRequests;
import com.intellij.webcore.packaging.RepoPackage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public final class PhoneGapPluginsList {
  public static final String PLUGINS_URL = "http://registry.cordova.io/-/all";

  public static volatile Map<String, PhoneGapRepoPackage> CACHED_REPO;

  private static final Logger LOGGER = Logger.getInstance(PhoneGapPluginsList.class);

  private static boolean isExcludedProperty(String name) {
    return "_updated".equals(name);
  }

  private static final Lock lock = new ReentrantLock();

  public static final class PhoneGapRepoPackage extends RepoPackage {
    private final String myDesc;

    public PhoneGapRepoPackage(String name, JsonObject jsonObject) {
      super(name, PLUGINS_URL, getVersionLatest(jsonObject.getAsJsonObject()));
      myDesc = getDescr(jsonObject);
    }

    private static String getDescr(JsonObject jsonObject) {
      JsonElement descriptionElement = jsonObject.get("description");
      return descriptionElement == null ? "" : descriptionElement.getAsString();
    }

    private static String getVersionLatest(JsonObject jsonObject) {
      JsonElement element = jsonObject.get("dist-tags");
      if (element == null || !element.isJsonObject()) {
        return null;
      }
      JsonObject asObject = element.getAsJsonObject();
      JsonElement latest = asObject.get("latest");
      return latest == null ? null : latest.getAsString();
    }

    public @NlsSafe String getDesc() {
      return myDesc;
    }
  }

  public static PhoneGapRepoPackage getPackage(String name) {
    return mapCached().get(name);
  }

  public static List<RepoPackage> listCached() {
    return new ArrayList<>(mapCached().values());
  }

  public static Map<String, PhoneGapRepoPackage> mapCached() {
    Map<String, PhoneGapRepoPackage> value = CACHED_REPO;
    if (value == null) {
      lock.lock();
      try {
        value = CACHED_REPO;
        if (value == null) {
          value = listNoCache();
          CACHED_REPO = value;
        }
      }
      finally {
        lock.unlock();
      }
    }
    return value;
  }

  private static Map<String, PhoneGapRepoPackage> listNoCache() {
    try {
      return HttpRequests.request(PLUGINS_URL).connect(new HttpRequests.RequestProcessor<>() {
        @Override
        public Map<String, PhoneGapRepoPackage> process(@NotNull HttpRequests.Request request) throws IOException {
          Map<String, PhoneGapRepoPackage> result = new HashMap<>();
          for (Map.Entry<String, JsonElement> entry : new JsonParser().parse(request.getReader()).getAsJsonObject().entrySet()) {
            if (!isExcludedProperty(entry.getKey())) {
              result.put(entry.getKey(), new PhoneGapRepoPackage(entry.getKey(), entry.getValue().getAsJsonObject()));
            }
          }
          return result;
        }
      });
    }
    catch (IOException e) {
      //throw new RuntimeException(e.getMessage(), e);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(e.getMessage(), e);
      }
      return new HashMap<>();
    }
  }

  public static void resetCache() {
    CACHED_REPO = null;
  }

  public static List<RepoPackage> wrapRepo(List<String> names) {
    return ContainerUtil.map(names, s -> new RepoPackage(s, s));
  }
}
