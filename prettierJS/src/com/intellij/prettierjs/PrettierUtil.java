// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.util.LineSeparator;
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

public final class PrettierUtil {

  public static final Icon ICON = null;
  public static final String PACKAGE_NAME = "prettier";
  public static final List<String> CONFIG_FILE_EXTENSIONS = ContainerUtil.immutableList(".yaml", ".yml", ".json", ".js", ".toml");
  public static final String RC_FILE_NAME = ".prettierrc";
  private static final String IGNORE_FILE_NAME = ".prettierignore";
  public static final String JS_CONFIG_FILE_NAME = "prettier.config.js";
  public static final Key<ParameterizedCachedValue<PrettierConfig, PsiFile>> CACHE_KEY = new Key<>(PrettierUtil.class.getName() + ".config");

  public static final List<String> CONFIG_FILE_NAMES =
    ContainerUtil.append(
      ContainerUtil.map(CONFIG_FILE_EXTENSIONS, ext -> RC_FILE_NAME + ext),
      JS_CONFIG_FILE_NAME, RC_FILE_NAME
    );

  public static final List<String> CONFIG_FILE_NAMES_WITH_PACKAGE_JSON =
    ContainerUtil.append(CONFIG_FILE_NAMES, PackageJsonUtil.FILE_NAME);

  public static final SemVer MIN_VERSION = new SemVer("1.13.0", 1, 13, 0);
  private static final Logger LOG = Logger.getInstance(PrettierUtil.class);

  private static final String BRACKET_SPACING = "bracketSpacing";
  private static final String PRINT_WIDTH = "printWidth";
  private static final String SEMI = "semi";
  private static final String SINGLE_QUOTE = "singleQuote";
  private static final String TAB_WIDTH = "tabWidth";
  private static final String TRAILING_COMMA = "trailingComma";
  private static final String USE_TABS = "useTabs";
  private static final String END_OF_LINE = "endOfLine";
  private static final String JSX_BRACKET_SAME_LINE = "jsxBracketSameLine";
  private static final String VUE_INDENT_SCRIPT_AND_STYLE = "vueIndentScriptAndStyle";

  private static final Gson OUR_GSON_SERIALIZER = new GsonBuilder().create();
  private static final String CONFIG_SECTION_NAME = PACKAGE_NAME;

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
    return virtualFile != null && "js".equals(virtualFile.getExtension()) && isConfigFile(virtualFile);
  }

  @Contract("null -> false")
  public static boolean isNonJSConfigFile(@Nullable VirtualFile virtualFile) {
    return virtualFile != null && !"js".equals(virtualFile.getExtension()) && isConfigFile(virtualFile);
  }

  @Contract("null -> false")
  public static boolean isConfigFile(@Nullable VirtualFile virtualFile) {
    if (virtualFile == null) {
      return false;
    }
    CharSequence name = virtualFile.getNameSequence();
    if (StringUtil.equals(name, JS_CONFIG_FILE_NAME)
        || StringUtil.equals(name, RC_FILE_NAME)) {
      return true;
    }

    if (StringUtil.startsWith(name, RC_FILE_NAME)) {
      for (String ext : CONFIG_FILE_EXTENSIONS) {
        if (name.length() == RC_FILE_NAME.length() + ext.length() && StringUtil.endsWith(name, ext)) {
          return true;
        }
      }
    }
    return false;
  }

  @Nullable
  public static VirtualFile findIgnoreFile(@NotNull VirtualFile source, @NotNull Project project) {
    VirtualFile packageJson = PackageJsonUtil.findUpPackageJson(source);
    VirtualFile rootDir = packageJson != null ? packageJson.getParent() : project.getBaseDir();
    return rootDir == null ? null : rootDir.findChild(IGNORE_FILE_NAME);
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
        return PackageJsonUtil.getOrCreateData(file).getTopLevelProperties().contains(CONFIG_SECTION_NAME);
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
      return CachedValuesManager.getManager(project).getParameterizedCachedValue(psiFile, CACHE_KEY, param ->
        CachedValueProvider.Result.create(parseConfigInternal(param.getVirtualFile(), param), param), false, psiFile);
    });
  }

  @Nullable
  private static PrettierConfig parseConfigInternal(@NotNull VirtualFile virtualFile, @NotNull PsiFile file) {
    try {
      if (PackageJsonUtil.isPackageJsonFile(file)) {
        PackageJsonData packageJsonData = PackageJsonUtil.getOrCreateData(virtualFile);
        if (!packageJsonData.isDependencyOfAnyType(PACKAGE_NAME)) {
          return null;
        }
        Object prettierProperty = ObjectUtils.coalesce(OUR_GSON_SERIALIZER.<Map<String, Object>>fromJson(file.getText(), Map.class),
                                                       Collections.emptyMap()).get(PACKAGE_NAME);
        //noinspection unchecked
        return prettierProperty instanceof Map ? parseConfigFromMap(((Map)prettierProperty)) : null;
      }
      if (file instanceof JsonFile) {
        return parseConfigFromJsonText(file.getText());
      }
      if (JSLinterConfigLangSubstitutor.YamlLanguageHolder.INSTANCE.equals(file.getLanguage())) {
        return parseConfigFromMap(new Yaml().load(file.getText()));
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
      return parseConfigFromMap(OUR_GSON_SERIALIZER.fromJson(reader, Map.class));
    }
    catch (IOException e) {
      LOG.info("Could not parse config from text", e);
      return null;
    }
  }

  @NotNull
  private static PrettierConfig parseConfigFromMap(@Nullable Map<String, Object> map) {
    if (map == null) {
      return PrettierConfig.DEFAULT;
    }
    return new PrettierConfig(
      getBooleanValue(map, JSX_BRACKET_SAME_LINE),
      getBooleanValue(map, BRACKET_SPACING),
      getIntValue(map, PRINT_WIDTH),
      getBooleanValue(map, SEMI),
      getBooleanValue(map, SINGLE_QUOTE),
      getIntValue(map, TAB_WIDTH),
      parseTrailingCommaValue(ObjectUtils.tryCast(map.get(TRAILING_COMMA), String.class)),
      getBooleanValue(map, USE_TABS),
      parseLineSeparatorValue(ObjectUtils.tryCast(map.get(END_OF_LINE), String.class)),
      getBooleanValue(map, VUE_INDENT_SCRIPT_AND_STYLE)
    );
  }

  private static Boolean getBooleanValue(@NotNull Map<String, Object> map, String key) {
    Boolean value = ObjectUtils.tryCast(map.get(key), Boolean.class);
    return value == null ? null : value.booleanValue();
  }

  private static Integer getIntValue(@NotNull Map<String, Object> map, String key) {
    Number value = ObjectUtils.tryCast(map.get(key), Number.class);
    return value == null ? null : value.intValue();
  }

  @Nullable
  public static String parseLineSeparatorValue(@Nullable String string) {
    LineSeparator separator = parseLineSeparator(string);
    return separator != null ? separator.getSeparatorString() : null;
  }

  @Nullable
  public static LineSeparator parseLineSeparator(@Nullable String string) {
    if (string == null) {
      return null;
    }
    return StringUtil.parseEnum(StringUtil.toUpperCase(string), null, LineSeparator.class);
  }

  @Nullable
  private static TrailingCommaOption parseTrailingCommaValue(@Nullable String string) {
    return string == null ? null : StringUtil.parseEnum(string, null, TrailingCommaOption.class);
  }

  public enum TrailingCommaOption {
    none,
    all,
    es5
  }
}
