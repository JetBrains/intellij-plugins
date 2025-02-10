// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.javascript.nodejs.interpreter.NodeInterpreterUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.npm.InstallNodeLocalDependenciesAction;
import com.intellij.javascript.nodejs.npm.NpmManager;
import com.intellij.javascript.nodejs.settings.NodeSettingsConfigurable;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.json.psi.JsonFile;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.*;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ShowSettingsUtil;
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
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.LightweightHint;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
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
    RC_FILE_NAME + ".cjs", CONFIG_FILE_NAME + ".cjs",
    RC_FILE_NAME + ".toml"
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

  public static @NotNull Collection<VirtualFile> lookupPossibleConfigFiles(@NotNull List<VirtualFile> from, @NotNull Project project) {
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

  public static boolean isFormattingAllowedForFile(@NotNull Project project, @NotNull VirtualFile file) {
    var configuration = PrettierConfiguration.getInstance(project);

    if (!GlobPatternUtil.isFileMatchingGlobPattern(project, configuration.getFilesPattern(), file)) {
      return false;
    }

    if (!configuration.getFormatFilesOutsideDependencyScope()) {
      return findPackageJsonWithPrettierUpTree(project, file) != null;
    }

    return true;
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

  public static @Nullable VirtualFile findSingleConfigInContentRoots(@NotNull Project project) {
    return JSLinterConfigFileUtil.findDistinctConfigInContentRoots(project, CONFIG_FILE_NAMES_WITH_PACKAGE_JSON, file -> {
      if (PackageJsonUtil.isPackageJsonFile(file)) {
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
    return configs.size() == 1 ? configs.get(0) : null;
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
          if (PackageJsonUtil.isPackageJsonFile(file)) {
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

  public interface ErrorHandler {
    ErrorHandler DEFAULT = new DefaultErrorHandler();

    void showError(@NotNull Project project,
                   @Nullable Editor editor,
                   @NotNull @Nls String text,
                   @Nullable Runnable onLinkClick);

    default void showErrorWithDetails(@NotNull Project project,
                                      @Nullable Editor editor,
                                      @NotNull @Nls String text,
                                      @NotNull String details) {
      showError(project, editor, text, () -> showErrorDetails(project, details));
    }
  }

  public static final ErrorHandler NOOP_ERROR_HANDLER = new ErrorHandler() {
    @Override
    public void showError(@NotNull Project project, @Nullable Editor editor, @NotNull String text, @Nullable Runnable onLinkClick) {
      // No need to show any notification in case of 'Prettier on save' failure.
      // Most likely the file is simply not syntactically valid at the moment.
    }
  };

  private static class DefaultErrorHandler implements ErrorHandler {
    @Override
    public void showError(@NotNull Project project, @Nullable Editor editor, @NotNull @Nls String text, @Nullable Runnable onLinkClick) {
      if (editor != null) {
        HyperlinkListener listener = onLinkClick == null ? null : new HyperlinkAdapter() {
          @Override
          protected void hyperlinkActivated(@NotNull HyperlinkEvent e) {
            onLinkClick.run();
          }
        };
        showHintLater(editor, PrettierBundle.message("prettier.formatter.hint.0", text), true, listener);
      }
      else {
        Notification notification =
          JSLinterGuesser.NOTIFICATION_GROUP.createNotification(PrettierBundle.message("prettier.formatter.notification.title"), text,
                                                                NotificationType.ERROR);
        if (onLinkClick != null) {
          notification.setListener(new NotificationListener.Adapter() {
            @Override
            protected void hyperlinkActivated(@NotNull Notification notification1, @NotNull HyperlinkEvent e) {
              onLinkClick.run();
            }
          });
        }
        notification.notify(project);
      }
    }
  }

  private static void showErrorDetails(@NotNull Project project, @NotNull String text) {
    ProcessOutput output = new ProcessOutput();
    output.appendStderr(text);
    JsqtProcessOutputViewer
      .show(project, PrettierBundle.message("prettier.formatter.notification.title"), ICON, null, null, output);
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

  public static boolean checkNodeAndPackage(@NotNull PsiFile psiFile, @Nullable Editor editor, @NotNull ErrorHandler errorHandler) {
    Project project = psiFile.getProject();
    NodeJsInterpreterRef interpreterRef = NodeJsInterpreterRef.createProjectRef();
    NodePackage nodePackage = PrettierConfiguration.getInstance(project).getPackage(psiFile);

    NodeJsInterpreter nodeJsInterpreter;
    try {
      nodeJsInterpreter = NodeInterpreterUtil.getValidInterpreterOrThrow(interpreterRef.resolve(project));
    }
    catch (ExecutionException e1) {
      errorHandler.showError(project, editor, PrettierBundle.message("error.invalid.interpreter"),
                             () -> NodeSettingsConfigurable.showSettingsDialog(project));
      return false;
    }

    if (nodePackage.isEmptyPath()) {
      errorHandler.showError(project, editor, PrettierBundle.message("error.no.valid.package"),
                             () -> editSettings(project));
      return false;
    }
    if (!nodePackage.isValid(project, nodeJsInterpreter)) {
      String message = PrettierBundle.message("error.package.is.not.installed",
                                              NpmManager.getInstance(project).getNpmInstallPresentableText());
      errorHandler.showError(project, editor, message, () -> installPackage(project));
      return false;
    }
    SemVer nodePackageVersion = nodePackage.getVersion(project);
    if (nodePackageVersion != null && nodePackageVersion.compareTo(MIN_VERSION) < 0) {
      errorHandler.showError(project, editor,
                             PrettierBundle.message("error.unsupported.version", MIN_VERSION.getRawVersion()), null);
      return false;
    }

    return true;
  }

  private static void editSettings(@NotNull Project project) {
    ShowSettingsUtil.getInstance().editConfigurable(project, new PrettierConfigurable(project));
  }

  private static void installPackage(@NotNull Project project) {
    final VirtualFile packageJson = PackageJsonUtil.findChildPackageJsonFile(project.getBaseDir());
    if (packageJson != null) {
      InstallNodeLocalDependenciesAction.runAndShowConsole(project, packageJson);
    }
  }
}
