package com.jetbrains.lang.dart.ide.settings;

import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.webcore.ScriptingFrameworkDescriptor;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.module.DartModuleTypeBase;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartSdkUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * @author: Fedor.Korotkov
 */
public class DartSettings {
  private static final Key<Pair<Long, Map<String, String>>> LIBRARIES_TIME_AND_MAP_KEY = Key.create("dart.internal.libraries");
  private final String sdkPath;

  public DartSettings(String path) {
    sdkPath = path;
  }

  public String getSdkPath() {
    return sdkPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DartSettings settings = (DartSettings)o;

    if (sdkPath != null ? !sdkPath.equals(settings.sdkPath) : settings.sdkPath != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return sdkPath != null ? sdkPath.hashCode() : 0;
  }

  @Nullable
  public VirtualFile getCompiler() {
    return VirtualFileManager.getInstance().findFileByUrl(getCompilerPath());
  }

  public String getCompilerPath() {
    return VfsUtilCore.pathToUrl(sdkPath) + "/bin/dart";
  }

  @Nullable
  public VirtualFile getAnalyzer() {
    return VirtualFileManager.getInstance().findFileByUrl(getAnalyzerPath());
  }

  public String getAnalyzerPath() {
    return VfsUtilCore.pathToUrl(sdkPath) + "/bin/" + (SystemInfo.isWindows ? "dartanalyzer.bat" : "dartanalyzer");
  }

  @Nullable
  public VirtualFile getDart2JS() {
    return VirtualFileManager.getInstance().findFileByUrl(getDart2JSPath());
  }

  public String getDart2JSPath() {
    return VfsUtilCore.pathToUrl(sdkPath) + "/bin/" + (SystemInfo.isWindows ? "dart2js.bat" : "dart2js");
  }

  @Nullable
  public VirtualFile getPub() {
    return VirtualFileManager.getInstance().findFileByUrl(getPubPath());
  }

  public String getPubPath() {
    return VfsUtilCore.pathToUrl(sdkPath) + "/bin/" + (SystemInfo.isWindows ? "pub.bat" : "pub");
  }

  @Nullable
  public VirtualFile findSdkLibrary(PsiElement context, String libName) {
    VirtualFile libRoot = getLib();
    String relativeLibPath = getLibrariesMap(context).get(libName);
    if (relativeLibPath == null) {
      return null;
    }
    return VfsUtil.findRelativeFile(libRoot, relativeLibPath.split("/"));
  }

  public Collection<String> getLibraries(PsiElement context) {
    return getLibrariesMap(context).keySet();
  }

  @Nullable
  public VirtualFile getLib() {
    return VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(getLibPath()));
  }

  public String getLibPath() {
    return getSdkPath() + "/lib/";
  }

  @NotNull
  Map<String, String> getLibrariesMap(@Nullable PsiElement context) {
    VirtualFile configFile = getConfigFile();
    if (configFile == null || context == null) {
      return Collections.emptyMap();
    }

    Pair<Long, Map<String, String>> data = configFile.getUserData(LIBRARIES_TIME_AND_MAP_KEY);
    final Long cachedTimestamp = data == null ? null : data.first;
    long modificationCount = configFile.getModificationCount();
    if (cachedTimestamp == null || !cachedTimestamp.equals(modificationCount)) {
      PsiFile psiFile = context.getManager().findFile(configFile);
      data = Pair.create(modificationCount, computeData(psiFile));
      configFile.putUserData(LIBRARIES_TIME_AND_MAP_KEY, data);
    }
    return data.getSecond();
  }

  public static Map<String, String> computeData(@Nullable PsiFile file) {
    if (file == null) {
      return Collections.emptyMap();
    }
    final Map<String, String> result = new THashMap<String, String>();
    file.acceptChildren(new DartRecursiveVisitor() {
      @Override
      public void visitConstConstructorExpression(@NotNull DartConstConstructorExpression constructorExpression) {
        Pair<String, String> libInfo = extractLibraryInfo(constructorExpression);
        if (libInfo != null) {
          result.put(libInfo.getFirst(), libInfo.getSecond());
        }
      }
    });
    return result;
  }

  @Nullable
  private static Pair<String, String> extractLibraryInfo(DartConstConstructorExpression constructorExpression) {
    PsiElement parent = constructorExpression.getParent();
    DartStringLiteralExpression literalExpression = PsiTreeUtil.getChildOfType(parent, DartStringLiteralExpression.class);
    if (literalExpression == null) {
      return null;
    }
    String libName = StringUtil.unquoteString(literalExpression.getText());

    DartArguments arguments = constructorExpression.getArguments();
    DartArgumentList argumentList = arguments != null ? arguments.getArgumentList() : null;
    List<DartExpression> expressionList = argumentList != null ? argumentList.getExpressionList() : null;

    String libPath = expressionList != null && !expressionList.isEmpty() ? expressionList.iterator().next().getText() : null;
    return Pair.create(libName, StringUtil.unquoteString(StringUtil.notNullize(libPath)));
  }

  @Nullable
  public VirtualFile getConfigFile() {
    String path = VfsUtilCore.pathToUrl(sdkPath) + "/lib/_internal/libraries.dart";
    return VirtualFileManager.getInstance().findFileByUrl(path);
  }

  @Nullable
  public static DartSettings getSettingsForModule(@Nullable Module module) {
    // todo: check file path
    DartSettings settings = null;
    if (shouldTakeWebSettings(module) || ApplicationManager.getApplication().isUnitTestMode()) {
      settings = DartSettingsUtil.getSettings();
    }
    else if (module != null && ModuleType.get(module) instanceof DartModuleTypeBase) {
      final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
      final Sdk sdk = moduleRootManager.getSdk();
      settings = new DartSettings(StringUtil.notNullize(sdk == null ? null : sdk.getHomePath()));
    }
    return settings;
  }

  public static boolean shouldTakeWebSettings(@Nullable Module module) {
    return module == null || ModuleType.get(module) instanceof WebModuleTypeBase;
  }

  public static ScriptingLibraryModel setUpDartLibrary(final Project myProject, final String sdkPath) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<ScriptingLibraryModel>() {
      @Override
      public ScriptingLibraryModel compute() {
        JSLibraryManager libraryManager = ServiceManager.getService(myProject, JSLibraryManager.class);
        final File rootDir = new File(FileUtil.toSystemDependentName(sdkPath));
        final List<File> dartFiles = findDartFiles(rootDir);
        final List<VirtualFile> vFiles = new ArrayList<VirtualFile>();
        for (File file : dartFiles) {
          VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
          if (vf == null) {
            vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
          }
          if (vf != null) {
            vFiles.add(vf);
          }
        }
        ScriptingLibraryModel libraryModel = libraryManager.getLibraryByName(DartBundle.message("dart.sdk.name"));
        if (libraryModel != null) {
          libraryManager.removeLibrary(libraryModel);
        }
        libraryModel = libraryManager.createLibrary(
          DartBundle.message("dart.sdk.name"),
          VfsUtilCore.toVirtualFileArray(vFiles),
          VirtualFile.EMPTY_ARRAY,
          ArrayUtil.EMPTY_STRING_ARRAY,
          ScriptingLibraryModel.LibraryLevel.GLOBAL,
          false
        );
        libraryModel.setFrameworkDescriptor(new ScriptingFrameworkDescriptor(
          DartBundle.message("dart.sdk.name"),
          DartSdkUtil.getSdkVersion(FileUtil.toSystemDependentName(sdkPath)))
        );
        final JSLibraryMappings mappings = ServiceManager.getService(myProject, JSLibraryMappings.class);
        mappings.associateWithProject(DartBundle.message("dart.sdk.name"));
        libraryManager.commitChanges();
        return libraryModel;
      }
    });
  }

  private static List<File> findDartFiles(@NotNull File rootDir) {
    final File libRoot = new File(rootDir, "lib");
    if (!libRoot.exists()) {
      return Collections.emptyList();
    }
    final List<File> result = new ArrayList<File>();
    final Processor<File> fileProcessor = new Processor<File>() {
      @Override
      public boolean process(File file) {
        if (file.isFile() && file.getName().endsWith("" + DartFileType.DEFAULT_EXTENSION)) {
          result.add(file);
        }
        return true;
      }
    };
    for (File child : libRoot.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return !"html".equals(file.getName()) && !"_internal".equals(file.getName());
      }
    })) {
      FileUtil.processFilesRecursively(child, fileProcessor);
    }

    File htmlDartium = new File(new File(libRoot, "html"), "dartium");
    if (htmlDartium.exists()) {
      FileUtil.processFilesRecursively(htmlDartium, fileProcessor);
    }

    return result;
  }
}
