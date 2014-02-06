package com.jetbrains.lang.dart;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class DartProjectComponent extends AbstractProjectComponent {

  protected DartProjectComponent(final Project project) {
    super(project);
  }

  public void projectOpened() {
    StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        final boolean dartSdkWasEnabledInOldModel = false; //hasJSLibraryMappingToOldDartSdkGlobalLib(myProject);
        //deleteDartSdkGlobalLibConfiguredInOldIde();

        final String dartSdkGlobalLibName = null; //findOrCreateNewDartSdkGlobalLib();

        final Collection<VirtualFile> pubspecYamlFiles =
          FilenameIndex.getVirtualFilesByName(myProject, "pubspec.yaml", GlobalSearchScope.projectScope(myProject));

        for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
          final Module module = ModuleUtilCore.findModuleForFile(pubspecYamlFile, myProject);
          if (module != null && FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, module.getModuleContentScope())) {
            excludePackagesFolders(module, pubspecYamlFile);

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

  @Nullable
  private static String findOrCreateNewDartSdkGlobalLib() {
    final String oldDartSdkPath = PropertiesComponent.getInstance().getValue("dart_sdk_path");
    PropertiesComponent.getInstance().unsetValue("dart_sdk_path");

    final DartSdk sdk = DartSdk.getGlobalDartSdk();

    if (sdk != null) {
      return sdk.getGlobalLibName();
    }
    else if (DartSdkUtil.isDartSdkHome(oldDartSdkPath)) {
      return ApplicationManager.getApplication().runWriteAction(new Computable<String>() {
        public String compute() {
          return DartSdkGlobalLibUtil.createDartSdkGlobalLib(oldDartSdkPath);
        }
      });
    }

    return null;
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
        final Document document = JDOMUtil.loadDocument(jsLibraryMappingsFile);
        final Element rootElement = document.getRootElement();
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

  public static void excludePackagesFolders(final Module module, final VirtualFile pubspecYamlFile) {
    final VirtualFile root = pubspecYamlFile.getParent();

    root.refresh(false, true);

    // http://pub.dartlang.org/doc/glossary.html#entrypoint-directory
    // Entrypoint directory: A directory inside your package that is allowed to contain Dart entrypoints.
    // Pub will ensure all of these directories get a “packages” directory, which is needed for “package:” imports to work.
    // Pub has a whitelist of these directories: benchmark, bin, example, test, tool, and web.
    // Any subdirectories of those (except bin) may also contain entrypoints.
    //
    // the same can be seen in the pub tool source code: [repo root]/sdk/lib/_internal/pub/lib/src/entrypoint.dart

    final Collection<VirtualFile> foldersToExclude = new ArrayList<VirtualFile>();
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(module.getProject()).getFileIndex();

    final VirtualFile packagesFolder = VfsUtilCore.findRelativeFile("bin/packages", root);
    if (packagesFolder != null && packagesFolder.isDirectory()) {
      if (fileIndex.isInContent(packagesFolder)) {
        foldersToExclude.add(packagesFolder);
      }
    }

    appendPackagesFolders(foldersToExclude, root.findChild("benchmark"), fileIndex);
    appendPackagesFolders(foldersToExclude, root.findChild("example"), fileIndex);
    appendPackagesFolders(foldersToExclude, root.findChild("test"), fileIndex);
    appendPackagesFolders(foldersToExclude, root.findChild("tool"), fileIndex);
    appendPackagesFolders(foldersToExclude, root.findChild("web"), fileIndex);

    if (!foldersToExclude.isEmpty()) {
      excludeFoldersInWriteAction(module, foldersToExclude);
    }
  }

  private static void appendPackagesFolders(final Collection<VirtualFile> foldersToExclude,
                                            final @Nullable VirtualFile folder,
                                            final ProjectFileIndex fileIndex) {
    if (folder == null) return;

    VfsUtilCore.visitChildrenRecursively(folder, new VirtualFileVisitor() {
      @NotNull
      public Result visitFileEx(@NotNull final VirtualFile file) {
        if (file.isDirectory() && "packages".equals(file.getName())) {
          if (fileIndex.isInContent(file)) {
            foldersToExclude.add(file);
          }
          return SKIP_CHILDREN;
        }
        else {
          return CONTINUE;
        }
      }
    });
  }

  private static void excludeFoldersInWriteAction(final Module module, final Collection<VirtualFile> foldersToExclude) {
    final VirtualFile firstItem = ContainerUtil.getFirstItem(foldersToExclude);
    if (firstItem == null) return;

    final VirtualFile contentRoot = ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(firstItem);
    if (contentRoot == null) return;

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
        try {
          for (final ContentEntry contentEntry : modifiableModel.getContentEntries()) {
            if (contentEntry.getFile() == contentRoot) {
              for (VirtualFile packagesFolder : foldersToExclude) {
                contentEntry.addExcludeFolder(packagesFolder);
              }
              break;
            }
          }
          modifiableModel.commit();
        }
        catch (Exception e) {
          modifiableModel.dispose();
        }
      }
    });
  }
}
