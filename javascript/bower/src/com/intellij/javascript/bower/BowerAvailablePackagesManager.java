package com.intellij.javascript.bower;

import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.reference.SoftReference;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public final class BowerAvailablePackagesManager {

  private static final Logger LOG = Logger.getInstance(BowerAvailablePackagesManager.class);
  private static final BowerAvailablePackagesManager INSTANCE = new BowerAvailablePackagesManager();

  private volatile WeakReference<Result> myLastResultRef;
  private final Object myLoadRunLock = new Object();
  private LoadRun myCurrentLoadRun;

  public static @NotNull BowerAvailablePackagesManager getInstance() {
    return INSTANCE;
  }

  public @NotNull List<String> getOrLoadAvailablePackages(@NotNull BowerSettings settings, boolean forceReload) throws IOException {
    if (!forceReload) {
      Result lastResult = SoftReference.dereference(myLastResultRef);
      if (lastResult != null && lastResult.mySettings.equals(settings)) {
        return lastResult.myPackages;
      }
    }
    try {
      LoadRun loadRun = getOrCreateLoadRun(settings);
      return loadRun.getFuture().get();
    }
    catch (Exception e) {
      LOG.info("Failed to list all bower packages", e);
      throw new IOException("Failed to list all bower packages", e);
    }
  }

  private @NotNull LoadRun getOrCreateLoadRun(final @NotNull BowerSettings settings) throws ExecutionException {
    LoadRun loadRun;
    FutureTask<List<String>> future;
    synchronized (myLoadRunLock) {
      loadRun = myCurrentLoadRun;
      if (loadRun != null && !loadRun.getSettings().equals(settings)) {
        loadRun.terminate();
        loadRun = null;
      }
      if (loadRun != null) {
        return loadRun;
      }
      final BowerCommandRun commandRun = BowerCommandLineUtil.startBowerCommand(settings, "search", "--json");
      future = new FutureTask<>(() -> {
        try {
          ProcessOutput output = commandRun.captureOutput(null, TimeUnit.MINUTES.toMillis(10));
          List<String> packages = parseAllPackages(output.getStdout());
          myLastResultRef = new WeakReference<>(new Result(settings, packages));
          return packages;
        }
        finally {
          LOG.info("Done 'bower search --json' command");
          synchronized (myLoadRunLock) {
            myCurrentLoadRun = null;
          }
        }
      });
      loadRun = new LoadRun(settings, commandRun, future);
      myCurrentLoadRun = loadRun;
    }
    future.run();
    return loadRun;
  }

  private static @NotNull List<String> parseAllPackages(@NotNull String jsonContent) throws IOException {
    JsonReader jsonReader = new JsonReader(new StringReader(jsonContent));
    jsonReader.setLenient(false);
    JsonToken topLevelToken = jsonReader.peek();
    if (topLevelToken != JsonToken.BEGIN_ARRAY) {
      LOG.warn("[parse all bower packages] Top-level element should be object, but " + topLevelToken + " found.");
      return Collections.emptyList();
    }
    jsonReader.beginArray();
    List<String> packages = new ArrayList<>();
    while (jsonReader.hasNext()) {
      JsonToken childToken = jsonReader.peek();
      if (childToken == JsonToken.BEGIN_OBJECT) {
        String packageName = JsonUtil.getChildAsString(jsonReader, "name");
        if (StringUtil.isNotEmpty(packageName)) {
          packages.add(packageName);
        }
      }
      else {
        jsonReader.skipValue();
      }
    }
    jsonReader.endArray();
    Collections.sort(packages);
    return ImmutableList.copyOf(packages);
  }

  private static final class Result {
    private final BowerSettings mySettings;
    private final List<String> myPackages;

    private Result(@NotNull BowerSettings settings, @NotNull List<String> packages) {
      mySettings = settings;
      myPackages = packages;
    }
  }

  private static class LoadRun {
    private final BowerSettings mySettings;
    private final BowerCommandRun myCommandRun;
    private final Future<List<String>> myFuture;

    LoadRun(@NotNull BowerSettings settings, @NotNull BowerCommandRun commandRun, @NotNull Future<List<String>> future) {
      mySettings = settings;
      myCommandRun = commandRun;
      myFuture = future;
    }

    public @NotNull BowerSettings getSettings() {
      return mySettings;
    }

    public @NotNull Future<List<String>> getFuture() {
      return myFuture;
    }

    public void terminate() {
      myCommandRun.terminate();
    }
  }
}
