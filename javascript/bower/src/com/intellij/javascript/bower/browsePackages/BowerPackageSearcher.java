package com.intellij.javascript.bower.browsePackages;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.intellij.javascript.bower.BowerPackageBasicInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.reference.SoftReference;
import com.intellij.util.Consumer;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.io.URLUtil;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BowerPackageSearcher {
  private static final Logger LOG = Logger.getInstance(BowerPackageSearcher.class);
  private static final String FILE_NAME = "popular-bower-packages.json";
  static final String NAME_PROP = "name";
  static final String DESCRIPTION_PROP = "description";
  private static final BowerPackageSearcher INSTANCE = new BowerPackageSearcher();
  private volatile WeakReference<List<BowerPackageBasicInfo>> myInfosRef;

  public static @NotNull BowerPackageSearcher getInstance() {
    return INSTANCE;
  }

  @SuppressWarnings("SameParameterValue")
  public List<BowerPackageBasicInfo> findPopularPackagesByPrefix(@NotNull String packageNamePrefix,
                                                                 int limit,
                                                                 @NotNull Condition<? super BowerPackageBasicInfo> filter) {
    List<BowerPackageBasicInfo> infos = SoftReference.dereference(myInfosRef);
    if (infos == null) {
      try {
        long startTime = System.nanoTime();
        infos = loadPopularPackages();
        LOG.info("Popular bower package list (" + infos.size() + ") loaded in " + TimeoutUtil.getDurationMillis(startTime) + " ms");
      }
      catch (IOException e) {
        LOG.warn("Cannot parse popular bower package list from " + FILE_NAME, e);
        infos = Collections.emptyList();
      }
      myInfosRef = new WeakReference<>(infos);
    }
    List<BowerPackageBasicInfo> result = new ArrayList<>();
    for (BowerPackageBasicInfo info : infos) {
      if (info.getName().startsWith(packageNamePrefix) && filter.value(info)) {
        result.add(info);
        if (result.size() >= limit) {
          break;
        }
      }
    }
    return result;
  }

  @SuppressWarnings("SameParameterValue")
  public void findPackagesByPrefix(@Nullable ProgressIndicator indicator,
                                   @NotNull String packageNamePrefix,
                                   int limit,
                                   @NotNull Condition<? super BowerPackageBasicInfo> filter,
                                   @NotNull Consumer<? super BowerPackageBasicInfo> consumer) throws IOException {
    List<BowerPackageBasicInfo> result;
    if (ApplicationManager.getApplication().isReadAccessAllowed()) {
      try {
        result = ApplicationUtil.runWithCheckCanceled(() -> {
          //noinspection CodeBlock2Expr
          return doFindPackagesByPrefix(indicator, packageNamePrefix, limit);
        }, EmptyProgressIndicator.notNullize(indicator));
      }
      catch (ProcessCanceledException e) {
        throw e;
      }
      catch (Exception e) {
        throw new IOException(e);
      }
    }
    else {
      result = doFindPackagesByPrefix(indicator, packageNamePrefix, limit);
    }
    for (BowerPackageBasicInfo info : result) {
      if (filter.value(info)) {
        consumer.consume(info);
      }
    }
  }

  static @NotNull List<BowerPackageBasicInfo> doFindPackagesByPrefix(@Nullable ProgressIndicator indicator,
                                                                     @NotNull String packageNamePrefix,
                                                                     int limit) throws IOException {
    String url = "https://libraries.io/api/bower-search?q=" + URLUtil.encodeURIComponent(packageNamePrefix)
                 + "&per_page=" + limit;
    try {
      long startNano = System.nanoTime();
      String content = HttpRequests.request(url).readString(indicator);
      List<BowerPackageBasicInfo> infos = parse(content);
      LOG.info("Found " + infos.size() + " packages matching '" + packageNamePrefix + "*' in " + TimeoutUtil.getDurationMillis(startNano) + "ms (" + url + ")");
      return infos;
    }
    catch (IOException e) {
      throw new IOException("Failed to fetch packages for '" + packageNamePrefix + "'", e);
    }
  }

  private static @NotNull List<BowerPackageBasicInfo> loadPopularPackages() throws IOException {
    InputStream stream = BowerPackageSearcher.class.getResourceAsStream(FILE_NAME);
    if (stream != null) {
      try {
        //noinspection IOResourceOpenedButNotSafelyClosed
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        return parse(new String(FileUtil.adaptiveLoadText(reader)));
      }
      finally {
        try {
          stream.close();
        }
        catch (IOException e) {
          LOG.error("Failed to close stream", e);
        }
      }
    }
    else {
      throw new IOException("Cannot find " + FILE_NAME);
    }
  }

  private static @NotNull List<BowerPackageBasicInfo> parse(@NotNull String text) throws IOException {
    JsonReader reader = new JsonReader(new StringReader(text));
    if (reader.peek() != JsonToken.BEGIN_ARRAY) {
      reader.skipValue();
      return Collections.emptyList();
    }
    reader.beginArray();
    List<BowerPackageBasicInfo> result = new ArrayList<>();
    while (reader.hasNext()) {
      BowerPackageBasicInfo info = readPackage(reader);
      if (info != null) {
        result.add(info);
      }
    }
    reader.endArray();
    return result;
  }

  private static @Nullable BowerPackageBasicInfo readPackage(@NotNull JsonReader reader) throws IOException {
    if (reader.peek() != JsonToken.BEGIN_OBJECT) {
      reader.skipValue();
      return null;
    }
    reader.beginObject();
    String name = null, description = null;
    while (reader.hasNext()) {
      String key = reader.nextName();
      if (NAME_PROP.equals(key)) {
        name = JsonUtil.nextStringOrSkip(reader);
      }
      else if (DESCRIPTION_PROP.equals(key)) {
        description = JsonUtil.nextStringOrSkip(reader);
      }
      else {
        reader.skipValue();
      }
    }
    reader.endObject();
    return name != null ? new BowerPackageBasicInfo(name, description) : null;
  }
}
