// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.json.psi.JsonFile;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.JSLinterConfigLangSubstitutor;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static com.intellij.prettierjs.PrettierConfig.createFromMap;

public final class PrettierUtil {

  public static final Icon ICON = null;
  public static final String PACKAGE_NAME = "prettier";
  public static final String CONFIG_SECTION_NAME = PACKAGE_NAME;
  public static final String RC_FILE_NAME = ".prettierrc";
  public static final String CONFIG_FILE_NAME = "prettier.config";
  static final String IGNORE_FILE_NAME = ".prettierignore";

  /**
   * <a href="https://github.com/prettier/prettier/blob/main/docs/configuration.md">github.com/prettier/prettier/blob/main/docs/configuration.md</a>
   */
  private static final List<String> CONFIG_FILE_NAMES = List.of(
    ".prettierrc",
    ".prettierrc.json", ".prettierrc.yml", ".prettierrc.yaml", ".prettierrc.json5",
    ".prettierrc.js", CONFIG_FILE_NAME + ".js",
    ".prettierrc.mjs", CONFIG_FILE_NAME + ".mjs",
    ".prettierrc.cjs", CONFIG_FILE_NAME + ".cjs",
    ".prettierrc.toml"
  );

  private static final List<String> CONFIG_FILE_NAMES_WITH_PACKAGE_JSON =
    ContainerUtil.append(CONFIG_FILE_NAMES, PackageJsonUtil.FILE_NAME);

  public static final SemVer MIN_VERSION = new SemVer("1.13.0", 1, 13, 0);
  private static final Logger LOG = Logger.getInstance(PrettierUtil.class);

  private static final class Holder {
    static final Gson OUR_GSON_SERIALIZER = new GsonBuilder().create();
  }

  private PrettierUtil() {
  }

  public static boolean isConfigFile(@NotNull PsiElement element) {
    PsiFile file = element instanceof PsiFile ? ((PsiFile)element) : null;
    if (file == null || file.isDirectory() || !file.isValid()) {
      return false;
    }
    return isConfigFile(file.getVirtualFile());
  }

  public static boolean isConfigFileOrPackageJson(@Nullable VirtualFile virtualFile) {
    return PackageJsonUtil.isPackageJsonFile(virtualFile) || isConfigFile(virtualFile);
  }

  @Contract("null -> false")
  public static boolean isJSConfigFile(@Nullable VirtualFile virtualFile) {
    return isConfigFile(virtualFile) && ArrayUtil.contains(StringUtil.toLowerCase(virtualFile.getExtension()), "js", "mjs", "cjs");
  }

  @Contract("null -> false")
  public static boolean isNonJSConfigFile(@Nullable VirtualFile virtualFile) {
    return isConfigFile(virtualFile) && !ArrayUtil.contains(StringUtil.toLowerCase(virtualFile.getExtension()), "js", "mjs", "cjs");
  }

  @Contract("null -> false")
  public static boolean isConfigFile(@Nullable VirtualFile virtualFile) {
    return virtualFile != null && CONFIG_FILE_NAMES.contains(virtualFile.getName());
  }

  @NotNull
  public static Collection<VirtualFile> lookupPossibleConfigFiles(@NotNull List<VirtualFile> from, @NotNull Project project) {
    HashSet<VirtualFile> results = new HashSet<>();
    VirtualFile baseDir = project.getBaseDir();
    if (baseDir == null) {
      return results;
    }
    for (VirtualFile file : from) {
      addPossibleConfigsForFile(file, results, baseDir);
    }
    return results;
  }

  public static @Nullable PackageJsonData findPackageJsonWithPrettierUpTree(@NotNull Project project, @NotNull VirtualFile file) {
    return PackageJsonUtil.processUpPackageJsonFilesAndFindFirst(project, file, packageJson -> {
      var data = PackageJsonData.getOrCreate(packageJson);
      return data.isDependencyOfAnyType(PACKAGE_NAME) ? data : null;
    });
  }

  private static void addPossibleConfigsForFile(@NotNull VirtualFile from, @NotNull Set<VirtualFile> result, @NotNull VirtualFile baseDir) {
    VirtualFile current = from.getParent();
    while (current != null && current.isValid() && current.isDirectory()) {
      for (String name : CONFIG_FILE_NAMES_WITH_PACKAGE_JSON) {
        VirtualFile file = current.findChild(name);
        if (file != null && file.isValid() && !file.isDirectory()) {
          result.add(file);
        }
      }
      if (current.equals(baseDir)) {
        return;
      }
      current = current.getParent();
    }
  }

  @Nullable
  public static VirtualFile findSingleConfigInContentRoots(@NotNull Project project) {
    return JSLinterConfigFileUtil.findDistinctConfigInContentRoots(project, CONFIG_FILE_NAMES_WITH_PACKAGE_JSON, file -> {
      if (PackageJsonUtil.isPackageJsonFile(file)) {
        PackageJsonData data = PackageJsonData.getOrCreate(file);
        return data.getTopLevelProperties().contains(CONFIG_SECTION_NAME);
      }
      return true;
    });
  }

  @Nullable
  public static VirtualFile findSingleConfigInDirectory(@NotNull VirtualFile dir) {
    if (!dir.isDirectory()) {
      return null;
    }
    List<VirtualFile> configs = ContainerUtil.mapNotNull(CONFIG_FILE_NAMES, name -> dir.findChild(name));
    return configs.size() == 1 ? configs.get(0) : null;
  }

  /**
   * returns config parsed from config file or package.json
   * returns null if package.json does not contain a dependency
   */
  @Nullable
  public static PrettierConfig parseConfig(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    return ReadAction.compute(() -> {
      if (!isConfigFileOrPackageJson(virtualFile)) {
        return null;
      }
      final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
      if (psiFile == null) {
        return null;
      }
      return CachedValuesManager.getCachedValue(psiFile, () -> CachedValueProvider.Result.create(parseConfigInternal(psiFile), psiFile));
    });
  }

  private static @Nullable PrettierConfig parseConfigInternal(@NotNull PsiFile file) {
    try {
      if (PackageJsonUtil.isPackageJsonFile(file)) {
        PackageJsonData packageJsonData = PackageJsonData.getOrCreate(file.getVirtualFile());
        if (!packageJsonData.isDependencyOfAnyType(PACKAGE_NAME)) {
          return null;
        }
        Object prettierProperty = ObjectUtils.coalesce(Holder.OUR_GSON_SERIALIZER.<Map<String, Object>>fromJson(file.getText(), Map.class),
                                                       Collections.emptyMap()).get(PACKAGE_NAME);
        //noinspection unchecked
        return prettierProperty instanceof Map ? createFromMap(((Map)prettierProperty)) : null;
      }
      if (file instanceof JsonFile) {
        return parseConfigFromJsonText(file.getText());
      }
      if (JSLinterConfigLangSubstitutor.YamlLanguageHolder.INSTANCE.equals(file.getLanguage())) {
        return createFromMap(new Yaml().load(file.getText()));
      }
    }
    catch (Exception e) {
      LOG.info(String.format("Could not read config data from file [%s]", file.getVirtualFile().getPath()), e);
    }
    return null;
  }

  @Nullable
  public static PrettierConfig parseConfigFromJsonText(String text) {
    try (JsonReader reader = new JsonReader(new StringReader(text))) {
      if (reader.peek() == JsonToken.STRING) {
        return null;
      }
      return createFromMap(Holder.OUR_GSON_SERIALIZER.fromJson(reader, Map.class));
    }
    catch (IOException e) {
      LOG.info("Could not parse config from text", e);
      return null;
    }
  }
}
