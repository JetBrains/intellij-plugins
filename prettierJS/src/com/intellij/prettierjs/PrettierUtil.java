// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.idea.AppMode;
import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.javascript.nodejs.execution.NodeTargetRun;
import com.intellij.json.psi.JsonFile;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonCommonUtil;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.JSLinterConfigLangSubstitutor;
import com.intellij.lang.javascript.psi.util.JSPluginPathManager;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.BaseProjectDirectories;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.ui.LightweightHint;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import icons.JavaScriptLanguageIcons;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkListener;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.intellij.prettierjs.PrettierConfig.createFromMap;

public final class PrettierUtil {
  public static final Icon ICON = JavaScriptLanguageIcons.FileTypes.Prettier;
  public static final String PACKAGE_NAME = "prettier";
  public static final String CONFIG_SECTION_NAME = PACKAGE_NAME;
  public static final String RC_FILE_NAME = ".prettierrc";
  public static final String CONFIG_FILE_NAME = "prettier.config";
  public static final String EDITOR_CONFIG_FILE_NAME = ".editorconfig";
  static final String IGNORE_FILE_NAME = ".prettierignore";

  /**
   * <a href="https://github.com/prettier/prettier/blob/main/docs/configuration.md">github.com/prettier/prettier/blob/main/docs/configuration.md</a>
   */
  private static final List<String> CONFIG_FILE_NAMES = List.of(
    RC_FILE_NAME,
    RC_FILE_NAME + ".json", RC_FILE_NAME + ".yml",
    RC_FILE_NAME + ".yaml", RC_FILE_NAME + ".json5",
    RC_FILE_NAME + ".js", CONFIG_FILE_NAME + ".js",
    RC_FILE_NAME + ".mjs", CONFIG_FILE_NAME + ".mjs",
    RC_FILE_NAME + ".cjs", CONFIG_FILE_NAME + ".cjs", RC_FILE_NAME + ".ts", CONFIG_FILE_NAME + ".ts", RC_FILE_NAME + ".mts",
    CONFIG_FILE_NAME + ".mts", RC_FILE_NAME + ".cts", CONFIG_FILE_NAME + ".cts",
    RC_FILE_NAME + ".toml"
  );

  private static final List<String> CONFIG_FILE_NAMES_WITH_PACKAGE_JSON =
    ContainerUtil.append(CONFIG_FILE_NAMES, PackageJsonUtil.FILE_NAME);

  public static final SemVer MIN_VERSION = new SemVer("1.13.0", 1, 13, 0);
  public static final SemVer NODE_MIN_VERSION_FOR_STRIP_TYPES_FLAG = new SemVer("22.6.0", 22, 6, 0);
  public static final SemVer NODE_MAX_VERSION_FOR_STRIP_TYPES_FLAG = new SemVer("23.6.0", 23, 6, 0);
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
    return PackageJsonCommonUtil.isPackageJsonFile(virtualFile) || isConfigFile(virtualFile);
  }

  @Contract("null -> false")
  public static boolean isJSConfigFile(@Nullable VirtualFile virtualFile) {
    return isConfigFile(virtualFile) && ArrayUtil.contains(StringUtil.toLowerCase(virtualFile.getExtension()), "js", "mjs", "cjs");
  }

  @Contract("null -> false")
  public static boolean isNonJSConfigFile(@Nullable VirtualFile virtualFile) {
    return isConfigFile(virtualFile) &&
           !ArrayUtil.contains(StringUtil.toLowerCase(virtualFile.getExtension()), "js", "mjs", "cjs", "ts", "mts", "cts");
  }

  @Contract("null -> false")
  public static boolean isConfigFile(@Nullable VirtualFile virtualFile) {
    return virtualFile != null && CONFIG_FILE_NAMES.contains(virtualFile.getName());
  }

  public static @NotNull Collection<VirtualFile> lookupPossibleConfigFiles(@NotNull List<VirtualFile> from, @NotNull Project project) {
    HashSet<VirtualFile> results = new HashSet<>();
    Set<VirtualFile> baseDirs = BaseProjectDirectories.getBaseDirectories(project);
    if (baseDirs.isEmpty()) {
      return results;
    }
    for (VirtualFile file : from) {
      addPossibleConfigsForFile(file, results, baseDirs);
    }
    return results;
  }

  private static void addPossibleConfigsForFile(@NotNull VirtualFile from,
                                                @NotNull Set<VirtualFile> result,
                                                @NotNull Set<VirtualFile> baseDirs) {
    VirtualFile current = from.getParent();
    while (current != null && current.isValid() && current.isDirectory()) {
      for (String name : CONFIG_FILE_NAMES_WITH_PACKAGE_JSON) {
        VirtualFile file = current.findChild(name);
        if (file != null && file.isValid() && !file.isDirectory()) {
          result.add(file);
        }
      }
      if (baseDirs.contains(current)) {
        return;
      }
      current = current.getParent();
    }
  }

  public static @Nullable VirtualFile findSingleConfigInContentRoots(@NotNull Project project) {
    return JSLinterConfigFileUtil.findDistinctConfigInContentRoots(project, CONFIG_FILE_NAMES_WITH_PACKAGE_JSON, file -> {
      if (PackageJsonCommonUtil.isPackageJsonFile(file)) {
        PackageJsonData data = PackageJsonData.getOrCreate(file);
        return data.getTopLevelProperties().contains(CONFIG_SECTION_NAME);
      }
      return true;
    });
  }

  public static @Nullable VirtualFile findSingleConfigInDirectory(@NotNull VirtualFile dir) {
    if (!dir.isDirectory()) {
      return null;
    }
    List<VirtualFile> configs = ContainerUtil.mapNotNull(CONFIG_FILE_NAMES, name -> dir.findChild(name));
    return configs.size() == 1 ? configs.getFirst() : null;
  }

  public static @Nullable VirtualFile findFileConfig(@NotNull Project project, @NotNull VirtualFile from) {
    Ref<VirtualFile> result = Ref.create();
    JSProjectUtil.processDirectoriesUpToContentRoot(project, from, directory -> {
      VirtualFile config = findChildConfigFile(directory);
      if (config != null) {
        result.set(config);
        return false;
      }
      return true;
    });

    return result.get();
  }

  public static @Nullable VirtualFile findChildConfigFile(@Nullable VirtualFile dir) {
    if (dir != null && dir.isValid()) {
      for (String name : CONFIG_FILE_NAMES_WITH_PACKAGE_JSON) {
        VirtualFile file = dir.findChild(name);
        if (file != null && file.isValid() && !file.isDirectory()) {
          if (PackageJsonCommonUtil.isPackageJsonFile(file)) {
            PackageJsonData data = PackageJsonData.getOrCreate(file);
            if (data.getTopLevelProperties().contains(CONFIG_SECTION_NAME)) {
              return file;
            }
          }
          else {
            return file;
          }
        }
      }
    }
    return null;
  }

  /**
   * returns config parsed from config file or package.json
   * returns null if package.json does not contain a dependency
   */
  public static @Nullable PrettierConfig parseConfig(@NotNull Project project, @NotNull VirtualFile virtualFile) {
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
      if (PackageJsonCommonUtil.isPackageJsonFile(file)) {
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

  public static @Nullable PrettierConfig parseConfigFromJsonText(String text) {
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

  public static @Nullable VirtualFile findIgnoreFile(@NotNull Project project, @NotNull VirtualFile source) {
    var configuration = PrettierConfiguration.getInstance(project);

    if (configuration.isDisabled()) {
      return null;
    }

    var ignorePath = configuration.getCustomIgnorePath();

    if (configuration.isAutomatic() || ignorePath.isBlank()) {
      return findAutoIgnoreFile(project, source);
    }

    return LocalFileSystem.getInstance().findFileByPath(ignorePath);
  }

  private static @Nullable VirtualFile findAutoIgnoreFile(@NotNull Project project, @NotNull VirtualFile source) {
    var fileDir = source.getParent();
    if (fileDir == null) {
      return null;
    }

    return JSProjectUtil.findFileUpToContentRoot(project, fileDir, IGNORE_FILE_NAME);
  }


  public static void showHintLater(@NotNull Editor editor,
                                   @NotNull @Nls String text,
                                   boolean isError,
                                   @Nullable HyperlinkListener hyperlinkListener) {
    ApplicationManager.getApplication().invokeLater(() -> {
      final JComponent component = isError ? HintUtil.createErrorLabel(text, hyperlinkListener, null)
                                           : HintUtil.createInformationLabel(text, hyperlinkListener, null, null);
      final LightweightHint hint = new LightweightHint(component);
      HintManagerImpl.getInstanceImpl()
        .showEditorHint(hint, editor, HintManager.UNDER, HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE |
                                                         HintManager.HIDE_BY_SCROLLING, 0, false);
    }, ModalityState.nonModal(), o -> editor.isDisposed() || !editor.getComponent().isShowing());
  }

  public static void addExperimentalStripTypesIfNeeded(@NotNull NodeTargetRun targetRun, @NotNull String serviceName) {
    var interpreter = targetRun.getInterpreter();
    SemVer nodeVersion;
    try {
      nodeVersion = interpreter.provideCachedVersionOrFetch().blockingGet(1500, TimeUnit.MILLISECONDS);
    }
    catch (Exception e) {
      nodeVersion = null;
    }
    if (nodeVersion != null &&
        nodeVersion.compareTo(NODE_MIN_VERSION_FOR_STRIP_TYPES_FLAG) >= 0 &&
        nodeVersion.compareTo(NODE_MAX_VERSION_FOR_STRIP_TYPES_FLAG) < 0) {
      JSLanguageServiceUtil.addNodeProcessArguments(targetRun.getCommandLineBuilder(), serviceName, "--experimental-strip-types");
    }
  }

  static Path getPrettierLanguageServicePath() throws IOException {
    return JSPluginPathManager.getPluginResource(
      PrettierUtil.class,
      "prettierLanguageService",
      AppMode.isRunningFromDevBuild() ? "prettierJS" : "prettierJS/gen"
    );
  }
}
