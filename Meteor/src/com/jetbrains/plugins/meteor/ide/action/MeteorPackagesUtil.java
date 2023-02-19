package com.jetbrains.plugins.meteor.ide.action;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.MeteorProjectStartupActivity;
import com.jetbrains.plugins.meteor.ide.action.MeteorImportPackagesAsExternalLib.CodeType;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.LocalFileFinder;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;


public final class MeteorPackagesUtil {
  public static final Logger LOG = Logger.getInstance(MeteorPackagesUtil.class);
  public static final String PACKAGES_FOLDER = "packages";
  public static final String VERSIONS_FILE_NAME = "versions";

  public static final String SETTING_SCOPE_IMPORT = "meteor.settings.import.codes.2";
  public static final String SETTING_METEOR_GLOBAL = "meteor.settings.global.path";

  private static final Set<String> IGNORED_PACKAGES = Set.of("meteor", "mongo", "minimongo", "templating");
  public static final Set<String> EXCLUDED_FOLDERS =
    Set.of("node_modules", "test", "example", "tests", "examples", "tmp");


  public static final String[] EXTENSIONS = JSFileReferencesUtil.IMPLICIT_EXTENSIONS;

  @Nullable
  public static VirtualFile getVersionPackage(@Nullable VirtualFile packagesFolder, @NotNull PackageWrapper wrapper) {
    if (packagesFolder == null) return null;
    if (IGNORED_PACKAGES.contains(wrapper.getName())) return null;

    VirtualFile rootForNamedPackage = packagesFolder.findChild(wrapper.getName());

    if (rootForNamedPackage == null) {
      rootForNamedPackage = packagesFolder.findChild(wrapper.getName());
    }

    //can be local project package so just skipped
    if (rootForNamedPackage == null) return null;

    VirtualFile versionedPackage = rootForNamedPackage.findChild(wrapper.getVersion());
    if (versionedPackage == null) {
      versionedPackage = ArrayUtil.getFirstElement(rootForNamedPackage.getChildren());
    }

    return versionedPackage;
  }

  @Nullable
  public static VirtualFile getDotMeteorVirtualFile(@NotNull Project project, @Nullable PsiFile baseFile) {
    return getDotMeteorVirtualFile(project, baseFile, false);
  }

  @Nullable
  public static VirtualFile getDotMeteorVirtualFile(@NotNull Project project, @Nullable PsiFile baseFile, boolean stored) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    if (baseFile == null) {
      MeteorFacade instance = MeteorFacade.getInstance();
      final Collection<VirtualFile> folders = stored ? instance.getStoredMeteorFolders(project) : instance.getMeteorFolders(project);

      return folders.size() == 1 ? ContainerUtil.getFirstItem(folders) : null;
    }

    final VirtualFile parent = baseFile.getVirtualFile().getParent();
    if (parent == null) return null;

    if (!MeteorProjectStartupActivity.METEOR_FOLDER.equals(parent.getName())) return null;

    return parent;
  }

  @Nullable
  static VirtualFile getVersionsFile(@NotNull Project project, @Nullable VirtualFile dotMeteorVirtualFile) {
    return getMeteorDirectoryFileByName(project, dotMeteorVirtualFile, VERSIONS_FILE_NAME);
  }

  @Nullable
  public static VirtualFile getMeteorDirectoryFileByName(@NotNull Project project,
                                                         @Nullable VirtualFile dotMeteorVirtualFile,
                                                         @NotNull String fileName) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    if (dotMeteorVirtualFile != null) {
      return dotMeteorVirtualFile.findChild(fileName);
    }
    else {
      Collection<VirtualFile> versions = FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.projectScope(project));
      if (versions.size() != 1) {
        return null;
      }
      return ContainerUtil.getFirstItem(versions);
    }
  }

  public static String getPathToGlobalMeteorRoot(Project project) {
    final String settings = PropertiesComponent.getInstance(project).getValue(SETTING_METEOR_GLOBAL);

    return StringUtil.isEmpty(settings) ? getDefaultPathToGlobalMeteorRoot() : settings;
  }

  public static void setPathToGlobalMeteorRoot(@NotNull Project project, @NotNull String value) {
    PropertiesComponent.getInstance(project).setValue(SETTING_METEOR_GLOBAL, value, getDefaultPathToGlobalMeteorRoot());
  }

  @NotNull
  public static Collection<CodeType> getCodes(Project project) {
    final String settings = PropertiesComponent.getInstance(project).getValue(SETTING_SCOPE_IMPORT);
    return StringUtil.isEmpty(settings) ? getDefaultCodes() : parse(settings);
  }

  @NotNull
  private static EnumSet<CodeType> getDefaultCodes() {
    return EnumSet.of(CodeType.CLIENT);
  }

  public static void setCodes(@NotNull Project project, Collection<CodeType> codes) {
    PropertiesComponent.getInstance(project).setValue(SETTING_SCOPE_IMPORT, codesToString(codes), codesToString(getDefaultCodes()));
  }

  private static Collection<CodeType> parse(String value) {
    EnumSet<CodeType> types = EnumSet.noneOf(CodeType.class);
    final String[] split = value.split(";");
    for (String s : split) {
      CodeType e = CodeType.valueOf(s);
      if (e == CodeType.EMPTY) {
        continue;
      }
      types.add(e);
    }

    return types;
  }

  private static String codesToString(@NotNull Collection<CodeType> codes) {
    if (codes.isEmpty()) return CodeType.EMPTY.name();

    return StringUtil.join(codes, type -> type.name(), ";");
  }

  @NotNull
  public static String getDefaultPathToGlobalMeteorRoot() {
    if (!SystemInfo.isWindows) {
      return SystemProperties.getUserHome() + "/" + MeteorProjectStartupActivity.METEOR_FOLDER;
    }

    String meteorExecutable = MeteorSettings.getInstance().getExecutablePath();

    if (StringUtil.isEmpty(meteorExecutable)) {
      return "";
    }

    VirtualFile file = LocalFileFinder.findFile(meteorExecutable);
    if (file == null) {
      return "";
    }

    return file.getParent().getPath();
  }

  public static class PackageWrapper {
    private final String myVersion;
    private final String myName;

    private final String myOriginalName;

    public PackageWrapper(String @NotNull [] strings) {
      assert strings.length == 2;
      myOriginalName = strings[0];
      myName = strings[0].replace(':', '_');
      myVersion = strings[1];
    }

    @NotNull
    public String getVersion() {
      return myVersion;
    }

    @NotNull
    public String getOriginalName() {
      return myOriginalName;
    }

    @NotNull
    public String getName() {
      return myName;
    }
  }
}
