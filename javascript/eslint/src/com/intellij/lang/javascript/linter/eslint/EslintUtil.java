package com.intellij.lang.javascript.linter.eslint;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.json.JsonUtil;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonCommonUtil;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.javascript.linter.GlobPatternUtil;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

public final class EslintUtil {

  public static final String DEFAULT_IGNORE_FILENAME = ".eslintignore";
  public static final String CONFIG_SECTION_NAME = "eslintConfig";
  public static final String ESLINTIGNORE_PACKAGE_SECTION_NAME = "eslintIgnore";
  public static final String PACKAGE_NAME = "eslint";

  public static final String DEFAULT_CONFIG_PREFIX = ".eslintrc";
  private static final String[] CONFIG_EXTENSIONS = {"", ".js", ".cjs", ".yaml", ".yml", ".json"};

  // https://github.com/eslint/eslint/blob/de408743b5c3fc25ebd7ef5fb11ab49ab4d06c36/lib/eslint/eslint.js#L97
  private static final String[] FLAT_CONFIG_NAMES_TS =
    {"eslint.config.ts", "eslint.config.cts", "eslint.config.mts"};
  private static final String[] FLAT_CONFIG_NAMES_NON_TS =
    {"eslint.config.js", "eslint.config.cjs", "eslint.config.mjs"};

  private static final String USE_FLAT_CONFIG_ENV_VAR = "ESLINT_USE_FLAT_CONFIG";

  private static final String[] FLAT_CONFIG_NAMES;
  private static final String[] FLAT_AND_LEGACY_CONFIG_NAMES;
  private static final String[] FLAT_AND_LEGACY_CONFIGS_AND_PACKAGE_JSON;

  static {
    FLAT_CONFIG_NAMES = ArrayUtil.mergeArrays(FLAT_CONFIG_NAMES_TS, FLAT_CONFIG_NAMES_NON_TS);
    FLAT_AND_LEGACY_CONFIG_NAMES = new String[CONFIG_EXTENSIONS.length + FLAT_CONFIG_NAMES.length];
    for (int i = 0; i < CONFIG_EXTENSIONS.length; i++) {
      FLAT_AND_LEGACY_CONFIG_NAMES[i] = DEFAULT_CONFIG_PREFIX + CONFIG_EXTENSIONS[i];
    }
    System.arraycopy(FLAT_CONFIG_NAMES, 0, FLAT_AND_LEGACY_CONFIG_NAMES, CONFIG_EXTENSIONS.length, FLAT_CONFIG_NAMES.length);

    FLAT_AND_LEGACY_CONFIGS_AND_PACKAGE_JSON = new String[FLAT_AND_LEGACY_CONFIG_NAMES.length + 1];
    System.arraycopy(FLAT_AND_LEGACY_CONFIG_NAMES, 0, FLAT_AND_LEGACY_CONFIGS_AND_PACKAGE_JSON, 0, FLAT_AND_LEGACY_CONFIG_NAMES.length);
    FLAT_AND_LEGACY_CONFIGS_AND_PACKAGE_JSON[FLAT_AND_LEGACY_CONFIGS_AND_PACKAGE_JSON.length - 1] = PackageJsonUtil.FILE_NAME;
  }

  private EslintUtil() { }

  public static long getTimeout() {
    var configuredTimeout = Registry.intValue("eslint.service.request.timeout.ms", -1);
    return configuredTimeout > 0 ? configuredTimeout : JSLanguageServiceUtil.getTimeout();
  }

  public static boolean isUseFlatConfigMode(@Nullable SemVer eslintVersion, boolean flatConfigFileExists) {
    if (eslintVersion == null) return false;

    int majorVersion = eslintVersion.getMajor();

    if (majorVersion < 8) return false;

    if (majorVersion == 8 || majorVersion == 9) {
      // flat config is opt-in in ESLint 8
      // flat config is by default in ESLint 9 but we want to have a fallback to legacy config
      String flatConfigEnvVar = System.getenv(USE_FLAT_CONFIG_ENV_VAR);
      // logic like in  https://github.com/eslint/eslint/blob/8f9759e2a94586357d85fac902e038fabdba79a7/lib/cli.js#L288-L300
      return "true".equals(flatConfigEnvVar) ||
             !"false".equals(flatConfigEnvVar) && flatConfigFileExists;
    }

    return true; // 10+
  }

  public static boolean isFlatConfigFileName(@NotNull String fileName) {
    return ArrayUtil.contains(fileName, FLAT_CONFIG_NAMES);
  }

  public static boolean isFlatConfigFileNameTs(@NotNull String fileName) {
    return ArrayUtil.contains(fileName, FLAT_CONFIG_NAMES_TS);
  }

  public static boolean isLegacyConfigFileName(@NotNull String fileName) {
    if (StringUtil.startsWith(fileName, DEFAULT_CONFIG_PREFIX)) {
      for (String ext : CONFIG_EXTENSIONS) {
        if (fileName.length() == DEFAULT_CONFIG_PREFIX.length() + ext.length() && StringUtil.endsWith(fileName, ext)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if the given file name corresponds to a custom legacy ESLint configuration file.
   * It is particularly useful for distinguishing custom paths that may not conform to flat configuration standards.
   * For example, a file named `eslint.config.fast.mjs` might use flat config mode,
   * but its name deviates from standard ESLint configuration file naming conventions.
   */
  public static boolean isCustomLegacyConfigFileName(@NotNull String fileName) {
    if (StringUtil.startsWith(fileName, DEFAULT_CONFIG_PREFIX)) {
      for (String ext : CONFIG_EXTENSIONS) {
        if (StringUtil.endsWith(fileName, ext)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isFlatOrLegacyConfigFile(@NotNull PsiElement element) {
    PsiFile psiFile = element.getContainingFile();
    psiFile = psiFile != null ? psiFile.getOriginalFile() : null;
    VirtualFile file = psiFile != null ? psiFile.getVirtualFile() : null;
    return file != null && isFlatOrLegacyConfigFile(file);
  }

  public static boolean isFlatOrLegacyConfigFile(@NotNull VirtualFile file) {
    if (!file.isValid() || file.isDirectory()) {
      return false;
    }

    return isFlatConfigFileName(file.getName()) || isLegacyConfigFileName(file.getName());
  }

  public static @NotNull List<VirtualFile> findAllConfigsWithPackageJsonUpFileSystem(@NotNull VirtualFile fileToLint) {
    return JSLinterConfigFileUtil.findAllFilesUpToFileSystemRoot(fileToLint, FLAT_AND_LEGACY_CONFIGS_AND_PACKAGE_JSON);
  }

  public static @NotNull List<VirtualFile> findAllFlatAndLegacyConfigFiles(@NotNull Project project) {
    return JSLinterConfigFileUtil.findAllConfigs(project, FLAT_AND_LEGACY_CONFIG_NAMES);
  }

  public static boolean hasConfigFiles(@NotNull Project project) {
    return JSLinterConfigFileUtil.hasConfigFiles(project, FLAT_AND_LEGACY_CONFIG_NAMES);
  }

  public static @Nullable JsonObject getConfigRootObject(@NotNull PsiFile configFile) {
    JsonObject eslintRootObject = configFile instanceof JsonFile ? JsonUtil.getTopLevelObject((JsonFile)configFile) : null;
    if (eslintRootObject != null && PackageJsonCommonUtil.isPackageJsonFile(configFile)) {
      eslintRootObject = JsonUtil.getPropertyValueOfType(eslintRootObject, CONFIG_SECTION_NAME, JsonObject.class);
    }
    return eslintRootObject;
  }

  public static @Nullable VirtualFile findDistinctConfigInContentRoots(@NotNull Project project) {
    return JSLinterConfigFileUtil.findDistinctConfigInContentRoots(
      project,
      Arrays.asList(FLAT_AND_LEGACY_CONFIGS_AND_PACKAGE_JSON),
      file -> {
        if (PackageJsonCommonUtil.isPackageJsonFile(file)) {
          return PackageJsonData.getOrCreate(file).getTopLevelProperties().contains(CONFIG_SECTION_NAME);
        }
        return true;
      });
  }

  public static @Nullable VirtualFile lookupIgnoreFile(@NotNull VirtualFile virtualFile, @NotNull VirtualFile stopAt) {
    return JSLinterConfigFileUtil.findFileUpToRoot(virtualFile, new String[]{DEFAULT_IGNORE_FILENAME}, stopAt);
  }

  /**
   * Used in <code>languageService/eslint/src/eslint-plugin.ts</code> to check presence of the required plugin or parser.
   * See <code>isFileKindAcceptedByConfig()</code> in that file. No checks are performed for <code>JavaScriptAndOther</code> kind.
   */
  public enum FileKind {
    TypeScript("ts"),
    Html("html"),
    Vue("vue"),
    JavaScriptAndOther("js_and_other");

    private final String myStringValue;

    FileKind(String stringValue) {
      myStringValue = stringValue;
    }

    public String getStringValue() {
      return myStringValue;
    }
  }

  public static boolean isPossiblyAcceptableFileType(@NotNull PsiFile file) {
    return getFileKind(file) != null;
  }

  public static @Nullable FileKind getFileKind(@NotNull PsiFile psiFile) {
    VirtualFile virtualFile = psiFile.getVirtualFile();
    if (virtualFile == null) {
      return null;
    }

    String additionalExtensions;
    try {
      additionalExtensions = Registry.stringValue("eslint.additional.file.extensions");
    }
    catch (MissingResourceException e) {
      additionalExtensions = null;
    }
    if (!StringUtil.isEmpty(additionalExtensions)) {
      String fileExtension = virtualFile.getExtension();
      for (String extension : additionalExtensions.split(",")) {
        extension = StringUtil.trimStart(StringUtil.trim(extension), ".");
        if (StringUtil.equalsIgnoreCase(extension, fileExtension)) {
          return FileKind.JavaScriptAndOther;
        }
      }
    }

    String filesPattern = EslintConfiguration.getInstance(psiFile.getProject()).getExtendedState().getState().getFilesPattern();
    ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(psiFile.getProject());
    if (!fileIndex.isInContent(virtualFile) || !GlobPatternUtil.isFileMatchingGlobPattern(psiFile.getProject(), filesPattern, virtualFile)) {
      return null;
    }

    FileType fileType = psiFile.getFileType();
    if (TypeScriptUtil.TYPESCRIPT_FILE_TYPES.contains(fileType)) {
      return FileKind.TypeScript;
    }
    if (JSLinterUtil.isPureHtmlFile(psiFile)) {
      return FileKind.Html;
    }

    if (JSLinterUtil.isVueFile(psiFile)) {
      if (FileTypeRegistry.getInstance().isFileOfType(virtualFile, HtmlFileType.INSTANCE)) {
        return FileKind.Html;
      }
      else {
        return FileKind.Vue;
      }
    }

    return FileKind.JavaScriptAndOther;
  }
}
