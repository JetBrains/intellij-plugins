package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.uiDesigner.LogMessageUtil;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.GlobalSearchScope;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class LibraryCollector {
  private static final String DOT_SWC = ".swc";

  final List<Library> externalLibraries = new ArrayList<Library>();
  final List<Library> sdkLibraries = new ArrayList<Library>();
  private VirtualFile globalLibrary;

  final LibraryStyleInfoCollector initializer;
  private final Module module;

  private String flexmojosSdkHomePath;

  // AS-200
  private final Set<VirtualFile> uniqueGuard = new THashSet<VirtualFile>();

  private final LibraryManager libraryManager;

  private String flexSdkVersion;

  public LibraryCollector(LibraryManager libraryManager, LibraryStyleInfoCollector initializer, Module module) {
    this.libraryManager = libraryManager;
    this.initializer = initializer;
    this.module = module;
  }

  public String getFlexSdkVersion() {
    return flexSdkVersion;
  }

  @NotNull
  public VirtualFile getGlobalLibrary() {
    return globalLibrary;
  }

  private static boolean isAutomationOrUselessLibrary(String name) {
    return name.startsWith("qtp") || name.startsWith("automation")
           || name.equals("flex.swc") /* flex.swc is only aggregation library */
           || name.equals("servicemonitor.swc")  /* aircore contains all classes */
           || name.equals("utilities.swc")  /* flex sdk 4.1 */
           || name.equals("core.swc") /* hero (4.5) aggregation library */
           || name.equals("applicationupdater.swc") /* applicationupdater_ui contains all classes */
           || name.equals("flash-integration.swc") || name.equals("authoringsupport.swc");
  }

  private boolean isGlobalLibrary(String name, VirtualFile jarFile, String prefix) {
    if (flexmojosSdkHomePath == null) {
      return name.equals(prefix + DOT_SWC);
    }
    else {
      return jarFile.getPath().startsWith(flexmojosSdkHomePath) && name.startsWith(prefix);
    }
  }

  private boolean isGlobalLibrary(String name, VirtualFile jarFile) {
    final boolean isAirglobal = isGlobalLibrary(name, jarFile, "airglobal");
    final boolean isGlobal = isAirglobal || isGlobalLibrary(name, jarFile, "playerglobal");
    // flexmojos project may has playerglobal and airglobal simultaneous
    if (isGlobal && (globalLibrary == null || isAirglobal)) {
      globalLibrary = Library.getCatalogFile(jarFile);
    }
    return isGlobal;
  }

  @Nullable
  private VirtualFile getRealFileIfValidSwc(final VirtualFile jarFile) {
    if (jarFile.getFileSystem() instanceof JarFileSystem) {
      VirtualFile file = JarFileSystem.getInstance().getVirtualFileForJar(jarFile);
      if (file != null && !file.isDirectory() && file.getName().endsWith(DOT_SWC) && !isGlobalLibrary(file.getName(), jarFile) &&
          isSwfAndCatalogExists(jarFile) && uniqueGuard.add(file)) {
        return file;
      }
    }

    return null;
  }

  private void collectSdkLibraries(final FlexBuildConfiguration bc, Sdk sdk) {
    for (VirtualFile jarFile : sdk.getRootProvider().getFiles(OrderRootType.CLASSES)) {
      String swcPath = VirtualFileManager.extractPath(StringUtil.trimEnd(jarFile.getUrl(), JarFileSystem.JAR_SEPARATOR));
      if (BCUtils.getSdkEntryLinkageType(swcPath, bc) != null) {
        VirtualFile file = getRealFileIfValidSwc(jarFile);
        if (file != null && !isAutomationOrUselessLibrary(file.getName())) {
          addLibrary(jarFile, true);
        }
      }
    }
  }

  /**
   * We don't use BuildConfigurationEntry as source of libraries. If reference to component declared in such build configuration is resolved, so, we register such bc's module
   */
  public void collect(Module module) {
    final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
    final Sdk sdk = bc.getSdk();
    assert sdk != null;

    final SdkType sdkType;
    try {
      sdkType = (SdkType)sdk.getClass().getMethod("getSdkType").invoke(sdk);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    final boolean isFlexmojosSdk = sdkType instanceof FlexmojosSdkType;
    if (!isFlexmojosSdk) {
      collectSdkLibraries(bc, sdk);
    }
    else {
      final String sdkHomePath = sdk.getHomePath();
      LogMessageUtil.LOG.assertTrue(sdkHomePath != null && sdkHomePath.contains("flex"), sdkHomePath + " must be path to maven repo and contains 'flex'");
      assert sdkHomePath != null;
      flexmojosSdkHomePath = sdkHomePath.substring(0, sdkHomePath.indexOf("flex"));
    }

    flexSdkVersion = sdk.getVersionString();
    assert flexSdkVersion != null && flexSdkVersion.length() >= 3;
    flexSdkVersion = flexSdkVersion.substring(0, 3);

    globalCatalogForTests(bc);

    final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
    for (DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof ModuleLibraryEntry) {
        LibraryOrderEntry orderEntry = FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, moduleRootManager);
        if (orderEntry != null) {
          collectFromLibraryOrderEntry(orderEntry.getRootFiles(OrderRootType.CLASSES));
        }
      }
      else if (entry instanceof SharedLibraryEntry) {
        com.intellij.openapi.roots.libraries.Library library = FlexProjectRootsUtil.findOrderEntry(module.getProject(), 
          (SharedLibraryEntry)entry);
        if (library != null) {
          collectFromLibraryOrderEntry(library.getFiles(OrderRootType.CLASSES));
        }
      }
    }

    // IDEA-71055
    // todo css-based themes
    final List<String> themes = CompilerOptionInfo.getThemes(module, bc);
    for (String theme : themes) {
      if (theme.endsWith(DOT_SWC)) {
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(theme);
        if (file != null && uniqueGuard.add(file)) {
          final VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(file);
          if (jarFile != null) {
            addLibrary(jarFile, true);
          }
        }
      }
    }
  }

  private void globalCatalogForTests(FlexBuildConfiguration bc) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      assert globalLibrary == null;
      //noinspection TestOnlyProblems
      globalLibrary = LibraryUtil.getTestGlobalLibrary(bc.getTargetPlatform() == TargetPlatform.Web);
    }
  }

  private boolean isFlexSdkLibrary(VirtualFile file, VirtualFile jarFile) {
    if (flexmojosSdkHomePath != null) {
      return file.getPath().startsWith(flexmojosSdkHomePath);
    }

    final String name = file.getName();
    for (Pair<String, String> pair : FlexDefinitionMapProcessor.FLEX_LIBS_PATTERNS) {
      if (name.startsWith(pair.first)) {
        return libraryContains(pair.second, jarFile);
      }
    }
    
    if (name.equals("textLayout.swc")) {
      return libraryContains("flashx.textLayout.EditClasses", jarFile);
    }
    else if (name.equals("osmf.swc")) {
      return libraryContains("org.osmf.utils.Version", jarFile);
    }
    // todo check and add
    else if (name.startsWith("miglayout-")) {
      return true;
    }

    return false;
  }

  private boolean libraryContains(String className, VirtualFile jarFile) {
    return JSResolveUtil.findClassByQName(className, GlobalSearchScope.fileScope(module.getProject(), Library.getSwfFile(jarFile))) != null;
  }

  private void collectFromLibraryOrderEntry(VirtualFile[] files) {
    for (VirtualFile jarFile : files) {
      VirtualFile file = getRealFileIfValidSwc(jarFile);
      if (file != null && !isAutomationOrUselessLibrary(file.getName())) {
        addLibrary(jarFile, isFlexSdkLibrary(file, jarFile));
      }
    }
  }

  private void addLibrary(VirtualFile jarFile, boolean isFromFlexSdk) {
    (isFromFlexSdk ? sdkLibraries : externalLibraries).add(libraryManager.createOriginalLibrary(jarFile, initializer));
  }

  // IDEA-74117
  private static boolean isSwfAndCatalogExists(VirtualFile jarFile) {
    if (Library.getSwfFile(jarFile) == null || Library.getCatalogFile(jarFile) == null) {
      LogMessageUtil.LOG.warn("SWC is corrupted (library.swf or catalog.xml doesn't exists): " + jarFile.getPath());
      return false;
    }

    return true;
  }
}