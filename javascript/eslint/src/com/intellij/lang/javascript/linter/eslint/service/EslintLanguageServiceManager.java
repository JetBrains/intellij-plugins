package com.intellij.lang.javascript.linter.eslint.service;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.GlobPatternUtil;
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation;
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager;
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration;
import com.intellij.lang.javascript.linter.eslint.EslintState;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service(Service.Level.PROJECT)
public final class EslintLanguageServiceManager extends MultiRootJSLinterLanguageServiceManager<ESLintLanguageService> {
  private final @NotNull CoroutineScope myCoroutineScope;

  public EslintLanguageServiceManager(@NotNull Project project, @NotNull CoroutineScope sc) {
    super(project, EslintUtil.PACKAGE_NAME);
    myCoroutineScope = sc;
  }

  public static @NotNull EslintLanguageServiceManager getInstance(@NotNull Project project) {
    return project.getService(EslintLanguageServiceManager.class);
  }

  /**
   * The IDE will stop the ESLint service process if it is not used for the specified period of time (in milliseconds)
   */
  public int getInactivityTimeoutMs() {
    return Registry.intValue("eslint.service.expiration.timeout.ms", (int)TimeUnit.MINUTES.toMillis(2));
  }

  public @NotNull CoroutineScope getCoroutineScope() {
    return myCoroutineScope;
  }

  public void applyFileLevelAnnotation(@NotNull PsiFile file, @Nullable JSLinterFileLevelAnnotation fileLevelAnnotation) {
    EslintState state = ReadAction.compute(() -> {
      return EslintConfiguration.getInstance(file.getProject()).getExtendedState().getState();
    });

    var isAnnotationChanged = useService(file.getVirtualFile(), state.getNodePackageRef(), service -> {
      if (service == null) {
        return false;
      }

      var previousAnnotation = service.getFileLevelAnnotation();
      service.setFileLevelAnnotation(fileLevelAnnotation);
      return !Comparing.equal(previousAnnotation, fileLevelAnnotation);
    });

    if (isAnnotationChanged) {
      jsLinterStateChanged();
    }
  }

  @Override
  protected @NotNull ESLintLanguageService createServiceInstance(@NotNull NodePackage resolvedPackage,
                                                                 @NotNull VirtualFile workingDirectory) {
    return new ESLintLanguageService(myProject, resolvedPackage, workingDirectory);
  }

  @Override
  protected boolean hasLinterSpecificConfiguration(@NotNull VirtualFile packageJson) {
    for (VirtualFile child : packageJson.getParent().getChildren()) {
      if (child.isDirectory()) continue;
      if (EslintUtil.isFlatOrLegacyConfigFile(child)) {
        return true;
      }
    }

    Set<String> properties = PackageJsonData.getOrCreate(packageJson).getTopLevelProperties();
    if (properties.contains(EslintUtil.CONFIG_SECTION_NAME)) {
      return true;
    }

    return false;
  }

  @Override
  protected @NotNull VirtualFile getWorkingDirectory(@NotNull NodePackage resolvedPackage,
                                                     @NotNull Project project,
                                                     @NotNull VirtualFile file) {
    VirtualFile wd = getConfiguredWorkingDirectoryForFile(project, file);
    if (wd != null) return wd;

    SemVer eslintVersion = resolvedPackage.getVersion(project);

    String customConfigFilePath = EslintConfiguration.getInstance(project).getExtendedState().getState().getCustomConfigFilePath();
    boolean usingCustomConfig = !customConfigFilePath.isEmpty();
    boolean customFlatConfigFileSpecified = usingCustomConfig &&
                                            !EslintUtil.isCustomLegacyConfigFileName(PathUtil.getFileName(customConfigFilePath));

    if (usingCustomConfig && EslintUtil.isUseFlatConfigMode(eslintVersion, customFlatConfigFileSpecified)) {
      VirtualFile customConfigFile = LocalFileSystem.getInstance().findFileByPath(customConfigFilePath);
      if (customConfigFile != null) {
        VirtualFile dir = customConfigFile.getParent();
        if (VfsUtilCore.isAncestor(dir, file, false)) {
          return dir;
        }
      }
      return super.getWorkingDirectory(resolvedPackage, project, file);
    }

    Ref<VirtualFile> flatConfigFile = new Ref<>();
    Ref<VirtualFile> workingDirIfNotFlatConfig = new Ref<>();
    JSProjectUtil.processDirectoriesUpToContentRoot(project, file, dir -> {
      for (VirtualFile child : dir.getChildren()) {
        if (child.isDirectory()) continue;

        if (EslintUtil.isFlatConfigFileName(child.getName()) && flatConfigFile.isNull()) {
          flatConfigFile.set(child);
        }

        if (child.getName().equals(EslintUtil.DEFAULT_IGNORE_FILENAME) || EslintUtil.isLegacyConfigFileName(child.getName())) {
          if (workingDirIfNotFlatConfig.isNull()) {
            workingDirIfNotFlatConfig.set(dir);
          }
        }
      }

      VirtualFile packageJson = dir.findChild(PackageJsonUtil.FILE_NAME);
      if (packageJson != null) {
        Set<String> properties = PackageJsonData.getOrCreate(packageJson).getTopLevelProperties();
        if (properties.contains(EslintUtil.ESLINTIGNORE_PACKAGE_SECTION_NAME) || properties.contains(EslintUtil.CONFIG_SECTION_NAME)) {
          if (workingDirIfNotFlatConfig.isNull()) {
            workingDirIfNotFlatConfig.set(dir);
          }
        }
      }
      return true;
    });

    if (!usingCustomConfig && EslintUtil.isUseFlatConfigMode(eslintVersion, !flatConfigFile.isNull())) {
      return !flatConfigFile.isNull() ? flatConfigFile.get().getParent()
                                      : super.getWorkingDirectory(resolvedPackage, project, file);
    }

    return !workingDirIfNotFlatConfig.isNull() ? workingDirIfNotFlatConfig.get()
                                               : super.getWorkingDirectory(resolvedPackage, project, file);
  }

  private static @Nullable VirtualFile getConfiguredWorkingDirectoryForFile(@NotNull Project project, @NotNull VirtualFile file) {
    String patternsString = EslintConfiguration.getInstance(project).getExtendedState().getState().getWorkDirPatterns();
    if (patternsString.isEmpty()) return null;

    List<String> patterns = ContainerUtil.mapNotNull(StringUtil.split(patternsString, ";"),
                                                     s -> StringUtil.nullize(StringUtil.trimStart(Strings.trimEnd(s.trim(), '/'), "./")));
    VirtualFile dir = file.getParent();
    ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);

    while (fileIndex.isInContent(dir)) {
      for (String pattern : patterns) {
        if (pattern.equals(".") && dir.getPath().equals(project.getBasePath())) return dir;
        if (pattern.equals(dir.getPath())) return dir;
        if (GlobPatternUtil.isFileMatchingGlobPattern(project, pattern, dir)) return dir;
      }

      dir = dir.getParent();
    }

    return null;
  }
}
