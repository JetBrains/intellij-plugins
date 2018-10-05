package com.google.jstestdriver.idea.server;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Consumer;
import com.intellij.util.io.HttpRequests;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class JstdServerUtils {
  private JstdServerUtils() {}

  public static void asyncFetchServerInfo(final String serverUrl, final Consumer<? super JstdServerFetchResult> consumer) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> consumer.consume(syncFetchServerInfo(serverUrl)));
  }

  @NotNull
  private static JstdServerFetchResult syncFetchServerInfo(final String serverUrl) {
    try {
      new URL(serverUrl);
    }
    catch (MalformedURLException e) {
      return JstdServerFetchResult.fromErrorMessage("Malformed url: " + serverUrl);
    }

    try {
      return HttpRequests.request(serverUrl.replaceAll("/$", "") + "/cmd?listBrowsers").connect(new HttpRequests.RequestProcessor<JstdServerFetchResult>() {
        @Override
        public JstdServerFetchResult process(@NotNull HttpRequests.Request request) throws IOException {
          final String badResponse = "Malformed server response received";
          JsonElement jsonElement;
          try {
            jsonElement = new JsonParser().parse(request.getReader());
          }
          catch (JsonSyntaxException e) {
            return JstdServerFetchResult.fromErrorMessage(badResponse);
          }

          try {
            return JstdServerFetchResult.fromServerInfo(new JstdServerInfo(serverUrl, parseBrowsers(jsonElement.getAsJsonArray())));
          }
          catch (Exception e) {
            return JstdServerFetchResult.fromErrorMessage(badResponse);
          }
        }
      });
    }
    catch (HttpRequests.HttpStatusException e) {
      return JstdServerFetchResult.fromErrorMessage("Incorrect server response status: " + e.getStatusCode());
    }
    catch (Exception e) {
      return JstdServerFetchResult.fromErrorMessage("Could not connect to " + serverUrl);
    }
  }

  @NotNull
  private static List<JstdBrowserInfo> parseBrowsers(JsonArray jsonArray) {
    List<JstdBrowserInfo> browserInfos = Lists.newArrayList();
    for (JsonElement child : jsonArray) {
      if (child.isJsonObject()) {
        JsonObject browserJsonObject = child.getAsJsonObject();
        String name = JsonUtil.getString(browserJsonObject, "name");
        String version = JsonUtil.getString(browserJsonObject, "version");
        if (name != null && version != null) {
          browserInfos.add(new JstdBrowserInfo(name, version));
        }
      }
    }
    return browserInfos;
  }
}
