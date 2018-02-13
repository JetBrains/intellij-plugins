package com.intellij.prettierjs;

import com.google.gson.JsonObject;
import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.json.psi.JsonFile;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSLinterConfigLangSubstitutor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrettierUtil {

  public static final Icon ICON = null;
  public static final String PACKAGE_NAME = "prettier";
  public static final List<String> CONFIG_FILE_EXTENSIONS = ContainerUtil.list(".yaml", ".yml", ".json", ".js");
  public static final String RC_FILE_NAME = ".prettierrc";
  public static final String JS_CONFIG_FILE_NAME = "prettier.config.js";

  public static final List<String> CONFIG_FILE_NAMES = Stream.concat(CONFIG_FILE_EXTENSIONS.stream().map(ext -> RC_FILE_NAME + ext),
                                                                     Stream.of(JS_CONFIG_FILE_NAME, RC_FILE_NAME))
                                                             .collect(Collectors.toList());
  private static final Logger LOG = Logger.getInstance(PrettierUtil.class);
  public static final String BRACKET_SPACING = "bracketSpacing";
  public static final String PRINT_WIDTH = "printWidth";
  public static final String SEMI = "semi";
  public static final String SINGLE_QUOTE = "singleQuote";
  public static final String TAB_WIDTH = "tabWidth";
  public static final String TRAILING_COMMA = "trailingComma";
  public static final String USE_TABS = "useTabs";
  private static final String JSX_BRACKET_SAME_LINE = "jsxBracketSameLine";

  private PrettierUtil() {
  }

  public static boolean isEnabled() {
    return ApplicationManager.getApplication().isUnitTestMode() || Registry.is("prettierjs.enabled");
  }

  public static boolean isConfigFile(@NotNull PsiElement element) {
    PsiFile file = element instanceof PsiFile ? ((PsiFile)element) : null;
    if (file == null || file.isDirectory() || !file.isValid()) {
      return false;
    }
    return isConfigFile(file.getVirtualFile());
  }

  public static boolean isConfigFileOrPackageJson(@Nullable VirtualFile virtualFile) {
    return virtualFile != null && (PackageJsonUtil.FILE_NAME.equals(virtualFile.getName()) || isConfigFile(virtualFile));
  }

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
  public static VirtualFile findConfigInContentRoots(@NotNull Project project) {
    List<VirtualFile> configs = ContainerUtil.newSmartList();
    for (VirtualFile dir : ProjectRootManager.getInstance(project).getContentRoots()) {
      configs.addAll(CONFIG_FILE_NAMES.stream().map(el -> dir.findChild(el)).filter(el -> el != null).collect(Collectors.toList()));
    }

    return configs.size() == 1 ? ContainerUtil.getFirstItem(configs) : null;
  }

  @Nullable
  private static VirtualFile lookupConfigFile(@NotNull VirtualFile fileToProcess) {
    VirtualFile dir = fileToProcess.getParent();
    while (dir != null) {
      for (String name : CONFIG_FILE_NAMES) {
        VirtualFile file = dir.findChild(name);
        if (file != null && file.isValid() && !file.isDirectory()) {
          return file;
        }
      }
      dir = dir.getParent();
    }
    return null;
  }

  /**
   * returns configuration from nearest config or package.json for file to reformat
   * returns null if prettier should not be used for this file
   * (or self for config or package.json)
   */
  @Nullable
  public static Pair<Config, VirtualFile> lookupConfiguration(@NotNull PsiFile file) {
    return ReadAction.compute(() -> CachedValuesManager.getCachedValue(file, () -> {
      Pair<Config, VirtualFile> result = lookupConfigurationWithoutCache(file.getProject(), file.getVirtualFile());
      return new CachedValueProvider.Result<>(result, file);
    }));
  }

  @Nullable
  private static Pair<Config, VirtualFile> lookupConfigurationWithoutCache(@NotNull Project project,
                                                                           @NotNull VirtualFile virtualFile) {
    if (isConfigFileOrPackageJson(virtualFile)) {
      return Pair.create(parseConfig(project, virtualFile), virtualFile);
    }
    VirtualFile packageJson = PackageJsonUtil.findUpPackageJson(virtualFile);
    if (packageJson != null) {
      if (!PackageJsonUtil.getOrCreateData(packageJson).isDependencyOfAnyType(PACKAGE_NAME)) {
        return null;
      }
      VirtualFile configFile = lookupConfigFile(virtualFile);
      if (configFile != null) {
        return Pair.create(parseConfig(project, configFile), configFile);
      }
      return Pair.create(parseConfig(project, packageJson), packageJson);
    }
    return null;
  }


  /**
   * returns config parsed from config file or package.json
   * returns null if package.json does not contain a dependency
   */
  @Nullable
  public static Config parseConfig(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    return ReadAction.compute(() -> {
      if (!isConfigFileOrPackageJson(virtualFile)) {
        return null;
      }
      final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
      if (psiFile == null) {
        return null;
      }

      return CachedValuesManager
        .getCachedValue(psiFile, () -> CachedValueProvider.Result.create(parseConfigInternal(virtualFile, psiFile), psiFile));
    });
  }

  @Nullable
  private static Config parseConfigInternal(@NotNull VirtualFile virtualFile, @NotNull PsiFile file) {
    try {
      if (PackageJsonUtil.FILE_NAME.equals(file.getName())) {
        PackageJsonData packageJsonData = PackageJsonUtil.getOrCreateData(virtualFile);
        if (!packageJsonData.isDependencyOfAnyType(PACKAGE_NAME)) {
          return null;
        }
        JsonObject object = JsonUtil.tryParseJsonObject(file.getText());
        return parseConfigFromJsonObject(JsonUtil.getChildAsObject(object, PACKAGE_NAME));
      }
      if (file instanceof JsonFile) {
        JsonObject object = JsonUtil.tryParseJsonObject(file.getText());
        return parseConfigFromJsonObject(object);
      }
      if (JSLinterConfigLangSubstitutor.YamlLanguageHolder.INSTANCE.equals(file.getLanguage())) {
        return parseYamlConfig(file);
      }
    }
    catch (Exception e) {
      LOG.info(String.format("Could not read config data from file [%s]", file.getVirtualFile().getPath()), e);
    }
    return null;
  }

  @Nullable
  private static Config parseYamlConfig(@NotNull PsiFile file) {
    Map<String, Object> map;

    try {
      //noinspection unchecked
      map = (Map<String, Object>)new Yaml().load(file.getText());
    }
    catch (Exception e) {
      LOG.info(String.format("Could not read config data from file [%s]", file.getVirtualFile().getPath()), e);
      return null;
    }
    if (map == null) {
      return Config.DEFAULT;
    }
    return new Config(
      getBooleanValue(map, JSX_BRACKET_SAME_LINE),
      getBooleanValue(map, BRACKET_SPACING),
      getIntValue(map, PRINT_WIDTH),
      getBooleanValue(map, SEMI),
      getBooleanValue(map, SINGLE_QUOTE),
      getIntValue(map, TAB_WIDTH),
      parseTrailingCommaValue(ObjectUtils.tryCast(map.get(TRAILING_COMMA), String.class)),
      getBooleanValue(map, USE_TABS)
    );
  }

  private static Boolean getBooleanValue(@NotNull Map<String, Object> map, String key) {
    Boolean value = ObjectUtils.tryCast(map.get(key), Boolean.class);
    return value == null ? null : value.booleanValue();
  }

  private static Integer getIntValue(@NotNull Map<String, Object> map, String key) {
    Integer value = ObjectUtils.tryCast(map.get(key), Integer.class);
    return value == null ? null : value.intValue();
  }

  @NotNull
  private static Config parseConfigFromJsonObject(@Nullable JsonObject obj) {
    if (obj == null) {
      return PrettierUtil.Config.DEFAULT;
    }
    return new Config(
      JsonUtil.getChildAsBooleanObj(obj, JSX_BRACKET_SAME_LINE),
      JsonUtil.getChildAsBooleanObj(obj, BRACKET_SPACING),
      JsonUtil.getChildAsIntegerObj(obj, PRINT_WIDTH),
      JsonUtil.getChildAsBooleanObj(obj, SEMI),
      JsonUtil.getChildAsBooleanObj(obj, SINGLE_QUOTE),
      JsonUtil.getChildAsIntegerObj(obj, TAB_WIDTH),
      parseTrailingCommaValue(JsonUtil.getChildAsString(obj, TRAILING_COMMA)),
      JsonUtil.getChildAsBooleanObj(obj, USE_TABS)
    );
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
  
  public static class Config {
    public static final Config DEFAULT = new Config();
    public final boolean jsxBracketSameLine;
    public final boolean bracketSpacing;
    public final int printWidth;
    public final boolean semi;
    public final boolean singleQuote;
    public final int tabWidth;
    public final TrailingCommaOption trailingComma;
    public final boolean useTabs;

    private Config() {
      this(null, null, null, null, null, null, null, null);
    }

    public Config(@Nullable Boolean jsxBracketSameLine,
                  @Nullable Boolean bracketSpacing,
                  @Nullable Integer printWidth,
                  @Nullable Boolean semi,
                  @Nullable Boolean singleQuote,
                  @Nullable Integer tabWidth,
                  @Nullable TrailingCommaOption trailingComma,
                  @Nullable Boolean useTabs) {
      this.jsxBracketSameLine = ObjectUtils.coalesce(jsxBracketSameLine, false);
      this.bracketSpacing = ObjectUtils.coalesce(bracketSpacing, true);
      this.printWidth = ObjectUtils.coalesce(printWidth, 80);
      this.semi = ObjectUtils.coalesce(semi, true);
      this.singleQuote = ObjectUtils.coalesce(singleQuote, false);
      this.tabWidth = ObjectUtils.coalesce(tabWidth, 2);
      this.trailingComma = ObjectUtils.coalesce(trailingComma, TrailingCommaOption.none);
      this.useTabs = ObjectUtils.coalesce(useTabs, false);
    }

    @Override
    public String toString() {
      return "Config{" +
             "jsxBracketSameLine=" + jsxBracketSameLine +
             ", bracketSpacing=" + bracketSpacing +
             ", printWidth=" + printWidth +
             ", semi=" + semi +
             ", singleQuote=" + singleQuote +
             ", tabWidth=" + tabWidth +
             ", trailingComma='" + trailingComma + '\'' +
             ", useTabs=" + useTabs +
             '}';
    }
  }
}
