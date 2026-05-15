package com.intellij.lang.javascript.linter.jshint.version;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ide.RegionUrlMapper;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.templates.github.DownloadUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Sergey Simonchik
 */
public final class JSHintVersionUtil {

  private static final Logger LOG = Logger.getInstance(JSHintVersionUtil.class);
  private static final String VERSIONS_JSON_FILENAME = "versions.json";
  private static final String BASE_URL = "https://download.jetbrains.com/idea/jshint";
  public static final String BUNDLED_VERSION = "2.13.6";
  private static final int DOWNLOAD_ATTEMPT_COUNT = 3;
  private static volatile ImmutableList<JSHintVersionDescriptor> OUR_CACHED_VERSION_DESCRIPTORS;

  private JSHintVersionUtil() {}

  public static @NotNull String guessUrl(@NotNull String version) {
    return createUrl(getJSHintSourceFileBaseName(version));
  }

  public static void downloadVersions(@Nullable ProgressIndicator indicator) throws IOException {
    String url = createUrl(VERSIONS_JSON_FILENAME);
    url = RegionUrlMapper.tryMapUrlBlocking(url);
    downloadFile(indicator, VERSIONS_JSON_FILENAME, url, DOWNLOAD_ATTEMPT_COUNT, input -> {
      if (input != null) {
        try {
          List<JSHintVersionDescriptor> versions = parseVersion(input);
          return !versions.isEmpty();
        }
        catch (Exception e) {
          return false;
        }
      }
      return false;
    });
    OUR_CACHED_VERSION_DESCRIPTORS = null;
  }

  public static @NotNull File downloadSourceVersionOnce(@Nullable ProgressIndicator indicator,
                                                        @NotNull JSHintVersionDescriptor versionDescriptor) throws IOException {
    return downloadSourceVersion(indicator, versionDescriptor, 1);
  }

  public static @NotNull File downloadSourceVersion(@Nullable ProgressIndicator indicator,
                                                    @NotNull JSHintVersionDescriptor versionDescriptor,
                                                    int attempts) throws IOException {
    String fileBaseName = getJSHintSourceFileBaseName(versionDescriptor.getVersion());
    String url = RegionUrlMapper.tryMapUrlBlocking(versionDescriptor.getUrl());
    return downloadFile(indicator, fileBaseName, url, attempts, input -> input != null && input.contains("JSHINT"));
  }

  /**
   * Returns cached versions parsed from locally downloaded versions.json.
   * If local versions.json is unavailable, parses bundled versions.json.
   * It's safe to call this method from any thread.
   *
   * @return version list that includes bundled version
   */
  public static @NotNull ImmutableList<JSHintVersionDescriptor> getVersions() {
    ImmutableList<JSHintVersionDescriptor> descriptors = OUR_CACHED_VERSION_DESCRIPTORS;
    if (descriptors != null) {
      return descriptors;
    }
    descriptors = doGetVersions();
    OUR_CACHED_VERSION_DESCRIPTORS = descriptors;
    return descriptors;
  }

  private static @NotNull ImmutableList<JSHintVersionDescriptor> doGetVersions() {
    File versionJsonFile = getVersionsJsonFile();
    List<JSHintVersionDescriptor> descriptors = null;
    if (versionJsonFile.isFile()) {
      try {
        String content = Files.asCharSource(versionJsonFile, StandardCharsets.UTF_8).read();
        descriptors = parseVersion(content);
      }
      catch (Exception e) {
        FileUtil.delete(versionJsonFile);
        LOG.warn("Can not parse '" + versionJsonFile.getAbsolutePath() + "'!", e);
      }
    }
    if (descriptors == null) {
      try {
        String content = readFromClassPath(VERSIONS_JSON_FILENAME);
        descriptors = parseVersion(content);
      }
      catch (Exception e) {
        throw new RuntimeException("Can not parse bundled " + VERSIONS_JSON_FILENAME + "!", e);
      }
    }
    descriptors = addGuessableVersionDescriptorFirstIfMissing(descriptors, BUNDLED_VERSION);
    return ImmutableList.copyOf(descriptors);
  }

  public static @NotNull ImmutableList<JSHintVersionDescriptor> addGuessableVersionDescriptorFirstIfMissing(
    @NotNull List<? extends JSHintVersionDescriptor> descriptors,
    @NotNull String version
  ) {
    JSHintVersionDescriptor descriptor = find(descriptors, version);
    if (descriptor != null) {
      return ImmutableList.copyOf(descriptors);
    }
    List<JSHintVersionDescriptor> list = new ArrayList<>();
    list.add(new JSHintVersionDescriptor(version, guessUrl(version)));
    list.addAll(descriptors);
    return ImmutableList.copyOf(list);
  }

  public static @Nullable JSHintVersionDescriptor find(@NotNull List<? extends JSHintVersionDescriptor> descriptors,
                                                       @NotNull String version) {
    for (JSHintVersionDescriptor versionDescriptor : descriptors) {
      if (versionDescriptor.getVersion().equals(version)) {
        return versionDescriptor;
      }
    }
    return null;
  }

  private static @NotNull List<JSHintVersionDescriptor> parseVersion(@NotNull String versionsFileContent) {
    if (versionsFileContent.trim().isEmpty()) {
      throw new RuntimeException("Can not parse version list from empty content!");
    }
    final JsonElement jsonElement;
    try {
      jsonElement = JsonParser.parseString(versionsFileContent);
    } catch (Exception e) {
      throw new RuntimeException("Can not parse JSON from version list. Malformed JSON was received, content: '" + versionsFileContent, e);
    }
    try {
      return toVersionList(jsonElement);
    } catch (RuntimeException e) {
      throw new RuntimeException("Wrong JSON was received, content: '" + versionsFileContent, e);
    }
  }

  private static @NotNull List<JSHintVersionDescriptor> toVersionList(@NotNull JsonElement jsonElement) {
    if (!jsonElement.isJsonArray()) {
      throw new RuntimeException("jsonElement is expected be an instance of " + JsonArray.class.getName());
    }
    JsonArray array = jsonElement.getAsJsonArray();
    List<JSHintVersionDescriptor> versions = new ArrayList<>();
    for (JsonElement element : array) {
      if (!element.isJsonObject()) {
        throw new RuntimeException("Unexpected child element " + element.getClass().getName());
      }
      JsonObject obj = element.getAsJsonObject();
      JsonElement nameElement = obj.get("version");
      String name = null;
      if (nameElement != null) {
        name = nameElement.getAsString();
      }
      String url = null;
      JsonElement urlElement = obj.get("url");
      if (urlElement != null) {
        url = urlElement.getAsString();
      }
      if (name != null && url != null) {
        versions.add(new JSHintVersionDescriptor(name, url));
      }
    }
    return versions;
  }

  private static @NotNull String createUrl(@NotNull String fileBaseName) {
    return StringUtil.trimEnd(BASE_URL, "/") + "/" + fileBaseName;
  }

  public static @NotNull String getJSHintSourceFileBaseName(@NotNull String version) {
    return "jshint-" + version + ".js";
  }

  private static @NotNull File downloadFile(@Nullable ProgressIndicator indicator,
                                            @NotNull String fileBaseName,
                                            @NotNull String url,
                                            final int attempts,
                                            @Nullable Predicate<? super String> contentChecker) throws IOException {
    File dir = getJSHintDirOrCreateIfNeeded();
    File outputFile = new File(dir, fileBaseName);
    File tempFile = FileUtil.createTempFile(dir, fileBaseName, ".tmp");
    for (int i = 1; i <= attempts; i++) {
      String message = (attempts > 1 ? "[Attempt#" + i + "] " : "") + "Downloading " + url;
      long startTimeNano = System.nanoTime();
      try {
        boolean success = DownloadUtil.downloadAtomically(indicator, url, outputFile, tempFile, contentChecker);
        if (!success) {
          throw new IOException("Content check failed.");
        }
        LOG.info(message + " succeed in " + formatTakenTime(startTimeNano) + " and saved to " + outputFile);
        return outputFile;
      } catch (IOException e) {
        LOG.warn(message + " failed in " + formatTakenTime(startTimeNano));
        if (i == attempts) {
          throw e;
        }
      }
    }
    return outputFile;
  }

  private static String formatTakenTime(long startTimeNano) {
    return String.format("%.1f ms", (System.nanoTime() - startTimeNano) / 1000000.0);
  }

  private static @NotNull File getJSHintDir() {
    File javascriptDir = new File(PathManager.getSystemPath(), "javascript");
    File jshintDir = new File(javascriptDir, "jshint");
    try {
      return jshintDir.getCanonicalFile();
    } catch (IOException e) {
      return jshintDir;
    }
  }

  private static @NotNull File getJSHintDirOrCreateIfNeeded() throws IOException {
    File dir = getJSHintDir();
    if (!FileUtil.createDirectory(dir)) {
      throw new IOException("Can't create " + dir.getAbsolutePath());
    }
    return dir;
  }

  private static File getVersionsJsonFile() {
    File jshintDir = getJSHintDir();
    return new File(jshintDir, VERSIONS_JSON_FILENAME);
  }

  static boolean isBundledVersion(@NotNull String version) {
    return version.equals(BUNDLED_VERSION);
  }

  public static boolean isSourceLocallyAvailable(@NotNull String version) {
    if (isBundledVersion(version)) {
      return true;
    }
    String fileBaseName = getJSHintSourceFileBaseName(version);
    File dir = getJSHintDir();
    File outputFile = new File(dir, fileBaseName);
    return outputFile.isFile();
  }

  private static @NotNull String readFromClassPath(@NotNull String fileBaseName) throws IOException {
    InputStream inputStream = JSHintVersionUtil.class.getResourceAsStream(fileBaseName);
    if (inputStream == null) {
      throw new RuntimeException("Resource " + fileBaseName + " is not found!");
    }
    Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    return FileUtil.loadTextAndClose(reader);
  }

  public static void downloadSourceContent(final @NotNull String version) throws IOException {
    List<JSHintVersionDescriptor> descriptors = getVersions();
    JSHintVersionDescriptor descriptor = find(descriptors, version);
    if (descriptor == null) {
      downloadVersions(null);
      descriptors = getVersions();
      descriptor = find(descriptors, version);
    }
    if (descriptor != null) {
      downloadSourceVersion(null, descriptor, DOWNLOAD_ATTEMPT_COUNT);
    }
  }

  /**
   * Should be called from EDT.
   *
   */
  public static void downloadSourceContentUnderProgress(@NotNull Project project,
                                                        final @NotNull String version,
                                                        final @NotNull Runnable callback) {
    Task.Backgroundable task = new Task.Backgroundable(project, JSHintBundle.message("jshint.progress.title.updating.jshint", version), false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          downloadSourceContent(version);
        }
        catch (Exception e) {
          LOG.warn("Can't fetch JSHint " + version, e);
        }
        finally {
          callback.run();
        }
      }
    };
    task.queue();
  }

  public static @Nullable String loadSourceContentFromLocalDrive(@NotNull String version) throws IOException {
    String fileBaseName = getJSHintSourceFileBaseName(version);
    if (isBundledVersion(version)) {
      LOG.info("JSHint " + version + " is loaded from classpath");
      return readFromClassPath(fileBaseName);
    }
    File dir = getJSHintDir();
    File outputFile = new File(dir, fileBaseName);
    if (outputFile.isFile()) {
      LOG.info("JSHint " + version + " is loaded from " + outputFile.getAbsolutePath());
      return FileUtil.loadFile(outputFile, StandardCharsets.UTF_8);
    }
    return null;
  }

}
