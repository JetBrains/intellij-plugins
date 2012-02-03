package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.LogMessageUtil;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.GlobalSearchScope;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

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

  //flexmojosFlexSdkRootPath = sdkHomePath.substring(0, sdkHomePath.indexOf("flex"));
  @SuppressWarnings("UnusedDeclaration")
  private String flexmojosFlexSdkRootPath;

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

  private boolean isGlobalLibrary(String name, VirtualFile jarFile) {
    final boolean isAirglobal = name.equals("airglobal.swc");
    final boolean isGlobal = isAirglobal || name.equals("playerglobal.swc");
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

  private Sdk collectSdkLibraries(final FlexIdeBuildConfiguration bc) {
    final Sdk sdk = FlexUtils.createFlexSdkWrapper(bc);
    assert sdk != null;
    for (VirtualFile jarFile : sdk.getRootProvider().getFiles(OrderRootType.CLASSES)) {
      String swcPath = VirtualFileManager.extractPath(StringUtil.trimEnd(jarFile.getUrl(), JarFileSystem.JAR_SEPARATOR));
      if (BCUtils.getSdkEntryLinkageType(swcPath, bc) != null) {
        VirtualFile file = getRealFileIfValidSwc(jarFile);
        if (file != null && !isAutomationOrUselessLibrary(file.getName())) {
          addLibrary(jarFile, true);
        }
      }
    }
    
    return sdk;
  }

  /**
   * We don't use BuildConfigurationEntry as source of libraries. If reference to component declared in such build configuration is resolved, so, we register such bc's module
   */
  public void collect(Module module) {
    final FlexIdeBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
    final Sdk sdk = collectSdkLibraries(bc);

    flexSdkVersion = sdk.getVersionString();
    assert flexSdkVersion != null && flexSdkVersion.length() >= 3;
    flexSdkVersion = flexSdkVersion.substring(0, 3);

    globalCatalogForTests(bc);

    final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
    for (DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof ModuleLibraryEntry) {
        LibraryOrderEntry orderEntry = FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, moduleRootManager);
        if (orderEntry != null) {
          collectFromLibraryOrderEnrty(orderEntry.getRootFiles(OrderRootType.CLASSES));
        }
      }
      else if (entry instanceof SharedLibraryEntry) {
        com.intellij.openapi.roots.libraries.Library library = FlexProjectRootsUtil.findOrderEntry(module.getProject(), 
          (SharedLibraryEntry)entry);
        if (library != null) {
          collectFromLibraryOrderEnrty(library.getFiles(OrderRootType.CLASSES));
        }
      }
    }

    // well, we don't implement real search for themes — detects only by bc type
    // IDEA-71055
    if (bc.getNature().isMobilePlatform()) {
      //PsiElement clazz = JSResolveUtil.findClassByQName("MobileThemeClasses", module.getModuleWithDependenciesAndLibrariesScope(false));
      //if (clazz != null && clazz instanceof JSClass) {
      //  JSClass clazz1 = (JSClass)clazz;
      //  PsiFile containingFile = clazz1.getContainingFile();
      //}
      
      VirtualFile file = sdk.getHomeDirectory();
      if (file != null) {
        file = file.findFileByRelativePath("frameworks/themes/Mobile/mobile.swc");
        if (file != null && uniqueGuard.add(file)) {
          final VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(file);
          if (jarFile != null) {
            addLibrary(jarFile, true);
          }
        }
      }
    }
  }

  private void globalCatalogForTests(FlexIdeBuildConfiguration bc) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      assert globalLibrary == null;
      globalLibrary = LibraryUtil.getTestGlobalLibrary(bc.getTargetPlatform() == TargetPlatform.Web);
    }
  }

  private boolean isFlexSdkLibrary(VirtualFile file, VirtualFile jarFile) {
    if (flexmojosFlexSdkRootPath != null) {
      return file.getPath().startsWith(flexmojosFlexSdkRootPath);
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

  private void collectFromLibraryOrderEnrty(VirtualFile[] files) {
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