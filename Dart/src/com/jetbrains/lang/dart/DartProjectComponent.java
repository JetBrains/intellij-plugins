package com.jetbrains.lang.dart;

import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PairConsumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartProjectComponent extends AbstractProjectComponent {

  private static final String DARTIUM_CHECKED_MODE_INITIALLY_ENABLED_KEY = "DARTIUM_CHECKED_MODE_INITIALLY_ENABLED";

  @NotNull private final PsiManager myPsiManager;

  protected DartProjectComponent(@NotNull final Project project, @NotNull PsiManager psiManager) {
    super(project);
    myPsiManager = psiManager;
  }

  @Override
  public void initComponent() {
    new DartPsiTreeChangePreprocessor(myPsiManager);
  }

  public void projectOpened() {
    StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        removeJSBreakpointsInDartFiles(myProject);

        final boolean dartSdkWasEnabledInOldModel = hasJSLibraryMappingToOldDartSdkGlobalLib(myProject);
        deleteDartSdkGlobalLibConfiguredInOldIde();

        final String dartSdkGlobalLibName = importKnowledgeAboutOldDartSdkAndReturnGlobalLibName(myProject);

        initiallyEnableDartiumCheckedModeIfNeeded();
        DartiumUtil.removeUnsupportedAsyncFlag();

        final Collection<VirtualFile> pubspecYamlFiles =
          FilenameIndex.getVirtualFilesByName(myProject, PUBSPEC_YAML, GlobalSearchScope.projectScope(myProject));

        for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
          final Module module = ModuleUtilCore.findModuleForFile(pubspecYamlFile, myProject);
          if (module != null && FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, module.getModuleContentScope())) {
            excludeBuildAndPackagesFolders(module, pubspecYamlFile);

            if (dartSdkGlobalLibName != null &&
                dartSdkWasEnabledInOldModel &&
                ModuleType.get(module) instanceof WebModuleTypeBase &&
                !DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, dartSdkGlobalLibName)) {
              ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                  DartSdkGlobalLibUtil.configureDependencyOnGlobalLib(module, dartSdkGlobalLibName);
                }
              });
            }
          }
        }
      }
    });
  }

  private static void removeJSBreakpointsInDartFiles(final Project project) {
    final XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
    final Collection<XBreakpoint<?>> toRemove = new ArrayList<XBreakpoint<?>>();

    for (XBreakpoint<?> breakpoint : breakpointManager.getAllBreakpoints()) {
      final XSourcePosition position = breakpoint.getSourcePosition();
      if (position != null &&
          position.getFile().getFileType() == DartFileType.INSTANCE &&
          !(breakpoint.getType() instanceof DartLineBreakpointType)) {
        toRemove.add(breakpoint);
      }
    }

    if (!toRemove.isEmpty()) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          for (XBreakpoint<?> breakpoint : toRemove) {
            breakpointManager.removeBreakpoint(breakpoint);
          }
        }
      });
    }
  }

  @Nullable
  private static String importKnowledgeAboutOldDartSdkAndReturnGlobalLibName(final @NotNull Project project) {
    final String oldDartSdkPath = PropertiesComponent.getInstance().getValue("dart_sdk_path");
    PropertiesComponent.getInstance().unsetValue("dart_sdk_path");

    final DartSdk sdk = DartSdk.getDartSdk(project);

    if (sdk != null) {
      return sdk.getGlobalLibName();
    }
    else if (DartSdkUtil.isDartSdkHome(oldDartSdkPath)) {
      if (DartiumUtil.getDartiumBrowser() == null) {
        // configure even if getDartiumPathForSdk() returns null
        final WebBrowser browser = DartiumUtil.ensureDartiumBrowserConfigured(DartiumUtil.getDartiumPathForSdk(oldDartSdkPath));
        final BrowserSpecificSettings browserSpecificSettings = browser.getSpecificSettings();
        if (browserSpecificSettings instanceof ChromeSettings) {
          DartiumUtil.setCheckedMode(browserSpecificSettings.getEnvironmentVariables(), true);
        }
      }

      return ApplicationManager.getApplication().runWriteAction(new Computable<String>() {
        public String compute() {
          return DartSdkGlobalLibUtil.createDartSdkGlobalLib(project, oldDartSdkPath);
        }
      });
    }

    return null;
  }

  private static void initiallyEnableDartiumCheckedModeIfNeeded() {
    if (PropertiesComponent.getInstance().getBoolean(DARTIUM_CHECKED_MODE_INITIALLY_ENABLED_KEY, false)) {
      return;
    }
    PropertiesComponent.getInstance().setValue(DARTIUM_CHECKED_MODE_INITIALLY_ENABLED_KEY, "true");

    final WebBrowser dartium = DartiumUtil.getDartiumBrowser();
    final BrowserSpecificSettings browserSpecificSettings = dartium == null ? null : dartium.getSpecificSettings();
    if (browserSpecificSettings instanceof ChromeSettings) {
      DartiumUtil.setCheckedMode(browserSpecificSettings.getEnvironmentVariables(), true);
    }
  }

  private static void deleteDartSdkGlobalLibConfiguredInOldIde() {
    final LibraryEx library = (LibraryEx)ApplicationLibraryTable.getApplicationTable().getLibraryByName("Dart SDK");
    final PersistentLibraryKind<?> kind = library == null ? null : library.getKind();
    if (library != null && kind != null && "javaScript".equals(kind.getKindId())) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          ApplicationLibraryTable.getApplicationTable().removeLibrary(library);
        }
      });
    }
  }

  private static boolean hasJSLibraryMappingToOldDartSdkGlobalLib(final @NotNull Project project) {
/*
    Mapping to old 'Dart SDK' global lib is removed when ScriptingLibraryManager is loaded if 'Dart SDK' lib does not exist. That's why we use hacky way.
    One more bonus is that it works even if JavaScript plugin is disabled and in IntelliJ IDEA Community Edition

    <?xml version="1.0" encoding="UTF-8"?>
    <project version="4">
      <component name="JavaScriptLibraryMappings">
        <file url="PROJECT" libraries="{Dart SDK}" />
        <excludedPredefinedLibrary name="HTML5 / EcmaScript 5" />
      </component>
    </project>
*/
    final File jsLibraryMappingsFile = new File(project.getBasePath() + "/.idea/jsLibraryMappings.xml");
    if (jsLibraryMappingsFile.isFile()) {
      try {
        final Element rootElement = JDOMUtil.load(jsLibraryMappingsFile);
        for (final Element componentElement : rootElement.getChildren("component")) {
          if ("JavaScriptLibraryMappings".equals(componentElement.getAttributeValue("name"))) {
            for (final Element fileElement : componentElement.getChildren("file")) {
              if ("PROJECT".equals(fileElement.getAttributeValue("url")) &&
                  "{Dart SDK}".equals(fileElement.getAttributeValue("libraries"))) {
                return true;
              }
            }
          }
        }
      }
      catch (Throwable ignore) {/* unlucky */}
    }

    return false;
  }

  public static void excludeBuildAndPackagesFolders(final @NotNull Module module, final @NotNull VirtualFile pubspecYamlFile) {
    final VirtualFile root = pubspecYamlFile.getParent();
    final VirtualFile contentRoot =
      root == null ? null : ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(root);
    if (contentRoot == null) return;

    // http://pub.dartlang.org/doc/glossary.html#entrypoint-directory
    // Entrypoint directory: A directory inside your package that is allowed to contain Dart entrypoints.
    // Pub will ensure all of these directories get a “packages” directory, which is needed for “package:” imports to work.
    // Pub has a whitelist of these directories: benchmark, bin, example, test, tool, and web.
    // Any subdirectories of those (except bin) may also contain entrypoints.
    //
    // the same can be seen in the pub tool source code: [repo root]/sdk/lib/_internal/pub/lib/src/entrypoint.dart

    final Collection<String> oldExcludedUrls =
      ContainerUtil.filter(ModuleRootManager.getInstance(module).getExcludeRootUrls(), new Condition<String>() {
        final String rootUrl = root.getUrl();

        public boolean value(final String url) {
          if (!url.equals(rootUrl + "/.pub") &&
              !url.equals(rootUrl + "/build") &&
              !url.startsWith(rootUrl + "/packages/") &&
              !url.startsWith(rootUrl + "/bin/") &&
              !url.startsWith(rootUrl + "/benchmark/") &&
              !url.startsWith(rootUrl + "/example/") &&
              !url.startsWith(rootUrl + "/test/") &&
              !url.startsWith(rootUrl + "/tool/") &&
              !url.startsWith(rootUrl + "/web/")) {
            return false;
          }

          if (url.equals(rootUrl + "/.pub")) return true;
          if (url.equals(rootUrl + "/build")) return true;
          if (url.endsWith("/packages")) return true;

          // excluded subfolder of 'packages' folder
          if (url.startsWith(root + "/packages/")) return true;

          return false;
        }
      });

    final THashSet<String> newExcludedUrls = collectFoldersToExclude(module, pubspecYamlFile);

    if (oldExcludedUrls.size() != newExcludedUrls.size() || !newExcludedUrls.containsAll(oldExcludedUrls)) {
      ModuleRootModificationUtil.updateExcludedFolders(module, contentRoot, oldExcludedUrls, newExcludedUrls);
    }
  }

  private static THashSet<String> collectFoldersToExclude(final Module module, final VirtualFile pubspecYamlFile) {
    final THashSet<String> newExcludedPackagesUrls = new THashSet<String>();
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(module.getProject()).getFileIndex();
    final VirtualFile root = pubspecYamlFile.getParent();

    // java.io.File is used here because exclusion is done before FS refresh (in order not to trigger indexing of files that are going to be excluded)
    final File pubFolder = new File(root.getPath() + "/.pub");
    if (pubFolder.isDirectory() || ApplicationManager.getApplication().isUnitTestMode() && root.findChild(".pub") != null) {
      newExcludedPackagesUrls.add(root.getUrl() + "/.pub");
    }

    final File buildFolder = new File(root.getPath() + "/build");
    if (buildFolder.isDirectory() || ApplicationManager.getApplication().isUnitTestMode() && root.findChild("build") != null) {
      newExcludedPackagesUrls.add(root.getUrl() + "/build");
    }

    final VirtualFile binFolder = root.findChild("bin");
    if (binFolder != null && binFolder.isDirectory() && fileIndex.isInContent(binFolder)) {
      newExcludedPackagesUrls.add(binFolder.getUrl() + "/packages");
    }

    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("benchmark"), fileIndex);
    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("example"), fileIndex);
    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("test"), fileIndex);
    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("tool"), fileIndex);
    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("web"), fileIndex);

    // Folders like packages/PathPackage and packages/ThisProject (where ThisProject is the name specified in pubspec.yaml) are symlinks to local 'lib' folders. Exclude it in order not to have duplicates. Resolve goes to local 'lib' folder.
    // Empty nodes like 'ThisProject (ThisProject/lib)' are added to Project Structure by DartTreeStructureProvider
    final DartUrlResolver resolver = DartUrlResolver.getInstance(module.getProject(), pubspecYamlFile);
    resolver.processLivePackages(new PairConsumer<String, VirtualFile>() {
      public void consume(final String packageName, final VirtualFile packageDir) {
        newExcludedPackagesUrls.add(root.getUrl() + "/packages/" + packageName);
      }
    });

    return newExcludedPackagesUrls;
  }

  private static void appendPackagesFolders(final @NotNull Collection<String> excludedPackagesUrls,
                                            final @Nullable VirtualFile folder,
                                            final @NotNull ProjectFileIndex fileIndex) {
    if (folder == null) return;

    VfsUtilCore.visitChildrenRecursively(folder, new VirtualFileVisitor() {
      @NotNull
      public Result visitFileEx(@NotNull final VirtualFile file) {
        if (!fileIndex.isInContent(file)) {
          return SKIP_CHILDREN;
        }

        if (file.isDirectory()) {
          if ("packages".equals(file.getName())) {
            return SKIP_CHILDREN;
          }
          // do not exclude 'packages' folder near another pubspec.yaml file
          else if (file.findChild(PUBSPEC_YAML) == null) {
            excludedPackagesUrls.add(file.getUrl() + "/packages");
          }
        }

        return CONTINUE;
      }
    });
  }
}
