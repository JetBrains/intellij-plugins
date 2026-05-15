package com.intellij.lang.javascript.linter.jshint.config;

import com.google.common.base.Splitter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.jshint.JSHintIgnoreInfo;
import com.intellij.lang.javascript.linter.jshint.JSHintOptionsState;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public final class JSHintConfigFileUtil {

  public static final String EXTENDS_KEY = "extends";
  private static final String AUTO_SCAN_CONFIG_NAME = ".jshintrc";
  private static final String PACKAGE_JSON_JSHINT_CONFIG = "jshintConfig";
  private static final Logger LOG = Logger.getInstance(JSHintConfigFileUtil.class);

  private JSHintConfigFileUtil() {}

  public static boolean isJSHintConfigFile(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();
    return file != null && isJSHintConfigFile(file);
  }

  public static boolean isJSHintConfigFile(@NotNull PsiFile file) {
    VirtualFile vFile = file.getViewProvider().getVirtualFile();
    return FileTypeRegistry.getInstance().isFileOfType(vFile, JSHintConfigFileType.INSTANCE);
  }

  public static boolean isWarningKey(@NotNull String key) {
    return key.startsWith("+W") || key.startsWith("-W");
  }


  public static boolean isIgnored(@NotNull Project project, @NotNull VirtualFile file) {
    VirtualFile fileDir = file.getParent();
    if (fileDir == null) {
      return true;
    }
    VirtualFile jshintIgnore = JSProjectUtil.findFileUpToContentRoot(project, fileDir, ".jshintignore");
    if (jshintIgnore == null) {
      return false;
    }
    try {
      JSHintIgnoreInfo ignoreInfo = parseIgnores(jshintIgnore);
      return ignoreInfo.isIgnore(file);
    }
    catch (IOException e) {
      LOG.warn("Cannot read " + jshintIgnore, e);
      return false;
    }
  }

  private static @NotNull JSHintIgnoreInfo parseIgnores(@NotNull VirtualFile jshintIgnore) throws IOException {
    String text = JSLinterConfigFileUtil.loadActualText(jshintIgnore);
    Iterable<String> lines = Splitter.on("\n").split(text);
    List<String> list = new ArrayList<>();
    for (String line : lines) {
      line = line.trim();
      if (!line.isEmpty()) {
        list.add(line);
      }
    }
    return new JSHintIgnoreInfo(jshintIgnore, list);
  }

  public static @Nullable JSHintConfigLookupResult lookupConfig(@NotNull Project project, @NotNull VirtualFile file) {
    VirtualFile fileDir = file.getParent();
    if (fileDir == null) {
      return null;
    }
    VirtualFile packageJson = JSProjectUtil.findFileUpToContentRoot(project, fileDir, PackageJsonUtil.FILE_NAME);
    if (packageJson != null) {
      try {
        JSHintOptionsState optionsState = parsePackageJson(packageJson);
        if (optionsState != null) {
          return JSHintConfigLookupResult.createSuccessfulResult(packageJson, optionsState);
        }
      }
      catch (IOException e) {
        return JSHintConfigLookupResult.createErrorResult(
          packageJson, JSHintBundle.message("jshint.config.error.failed.to.read.property", PACKAGE_JSON_JSHINT_CONFIG));
      }
    }
    VirtualFile jshintConfig = JSLinterConfigFileUtil.findFileUpToFileSystemRoot(fileDir, AUTO_SCAN_CONFIG_NAME);
    if (jshintConfig != null) {
      return JSHintConfigParser.parse(jshintConfig);
    }
    return null;
  }

  public static @Nullable JSHintConfigLookupResult loadConfigByPath(@NotNull String configFilePath) {
    LocalFileSystem fileSystem = LocalFileSystem.getInstance();
    String systemIndependentPath = FileUtil.toSystemIndependentName(configFilePath);
    VirtualFile file = fileSystem.findFileByPath(systemIndependentPath);
    if (file == null || !file.isValid()) {
      file = fileSystem.refreshAndFindFileByPath(systemIndependentPath);
    }
    if (file != null && file.isValid() && !file.isDirectory()) {
      return JSHintConfigParser.parse(file);
    }
    return null;
  }

  private static @Nullable JSHintOptionsState parsePackageJson(@NotNull VirtualFile packageJson) throws IOException {
    String text = JSLinterConfigFileUtil.loadActualText(packageJson);
    JsonReader reader = new JsonReader(new StringReader(text));
    // allow comments in package.json
    reader.setLenient(true);
    JsonToken topToken = reader.peek();
    if (topToken != JsonToken.BEGIN_OBJECT) {
      throw new IOException("Unexpected json element " + topToken);
    }
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals(PACKAGE_JSON_JSHINT_CONFIG)) {
        return parseOptionsState(reader);
      }
      else {
        reader.skipValue();
      }
    }
    reader.endObject();
    reader.close();
    return null;
  }

  public static boolean isExtendsKey(@NotNull PsiElement element) {
    return EXTENDS_KEY.equals(StringUtil.unquoteString(element.getText()));
  }

  static @NotNull JSHintOptionsState parseOptionsState(@NotNull JsonReader reader) throws IOException {
    JsonToken topToken = reader.peek();
    if (topToken != JsonToken.BEGIN_OBJECT) {
      throw new IOException("Unexpected json element " + topToken);
    }
    reader.beginObject();
    JSHintOptionsState.Builder builder = new JSHintOptionsState.Builder();
    while (reader.hasNext()) {
      String name = reader.nextName();
      Object value = JsonUtil.nextAny(reader);
      builder.put(name, value);
    }
    reader.endObject();
    return builder.build();
  }
}
