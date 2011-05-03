package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.Consumer;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LibraryManager extends EntityListManager<VirtualFile, OriginalLibrary> {
  public static LibraryManager getInstance() {
    return ServiceManager.getService(LibraryManager.class);
  }

  public boolean isRegistered(@NotNull OriginalLibrary library) {
    return list.contains(library);
  }

  public int add(@NotNull OriginalLibrary library) {
    return list.add(library);
  }

  public boolean isSdkRegistered(Sdk sdk, Module module) {
    ProjectInfo info = Client.getInstance().getRegisteredProjects().getNullableInfo(module.getProject());
    return info != null && info.getSdk() == sdk;
  }

  public void initLibrarySets(@NotNull final Module module, final File appDir) throws IOException, InitException {
    initLibrarySets(module, appDir, true);
  }

  public void initLibrarySets(@NotNull final Module module, final File appDir, boolean collectLocalStyleHolders)
    throws InitException, IOException {
    final ProblemsHolder problemsHolder = new ProblemsHolder();
    final Project project = module.getProject();
    final LibraryCollector libraryCollector = new LibraryCollector(this);
    final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(16384);
    stringWriter.startChange();

    final Client client;
    try {
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        @Override
        public void run() {
          libraryCollector.collect(module, new LibraryStyleInfoCollector(project, module, stringWriter, problemsHolder));
        }
      });

      client = Client.getInstance();
      if (stringWriter.hasChanges()) {
        client.updateStringRegistry(stringWriter);
      }
      else {
        stringWriter.finishChange();
      }
    }
    catch (Throwable e) {
      stringWriter.rollbackChange();
      throw new InitException(e, "error.collect.libraries");
    }

    final LibrarySet projectLibrarySet;
    final LibrarySet librarySet;
    final InfoList<Project, ProjectInfo> registeredProjects = client.getRegisteredProjects();
    final String projectLocationHash = project.getLocationHash();
    if (registeredProjects.contains(project)) {
      projectLibrarySet = registeredProjects.getInfo(project).getLibrarySet();
      // todo merge existing libraries and new. create new custom external library set for myModule,
      // if we have different version of the artifact
    }
    else {
      projectLibrarySet = createLibrarySet(projectLocationHash + "_fdk", null, libraryCollector.sdkLibraries,
                                           libraryCollector.getFlexSdkVersion(), new SwcDependenciesSorter(appDir, module), true);
      registeredProjects.add(new ProjectInfo(project, projectLibrarySet, libraryCollector.getFlexSdk()));
      client.openProject(project);
      client.registerLibrarySet(projectLibrarySet);
    }

    if (libraryCollector.externalLibraries.isEmpty()) {
      librarySet = projectLibrarySet;
    }
    else {
      librarySet = createLibrarySet(projectLocationHash, projectLibrarySet, libraryCollector.externalLibraries,
                                    libraryCollector.getFlexSdkVersion(), new SwcDependenciesSorter(appDir, module), false);
      client.registerLibrarySet(librarySet);
    }

    ModuleInfo moduleInfo = new ModuleInfo(module);
    if (collectLocalStyleHolders) {
      stringWriter.startChange();
      try {
        ModuleInfoUtil.collectLocalStyleHolders(moduleInfo, libraryCollector.getFlexSdkVersion(), stringWriter, problemsHolder);
        client.registerModule(project, moduleInfo, new String[]{librarySet.getId()}, stringWriter);
      }
      catch (Throwable e) {
        stringWriter.rollbackChange();
        throw new InitException(e, "error.collect.local.style.holders");
      }
    }
    else {
      client.registerModule(project, moduleInfo, new String[]{librarySet.getId()}, stringWriter);
    }

    if (!problemsHolder.isEmpty()) {
      DocumentProblemManager.getInstance().report(module.getProject(), problemsHolder);
    }
  }

  private LibrarySet createLibrarySet(String id, @Nullable LibrarySet parent, List<OriginalLibrary> libraries, String flexSdkVersion,
                                      SwcDependenciesSorter swcDependenciesSorter, final boolean isFromSdk)
    throws InitException {
    try {
      return new LibrarySet(id, parent, ApplicationDomainCreationPolicy.ONE,
                            swcDependenciesSorter.sort(libraries, id, flexSdkVersion, isFromSdk));
    }
    catch (Throwable e) {
      throw new InitException(e, "error.sort.libraries");
    }
  }

  @NotNull
  public OriginalLibrary createOriginalLibrary(@NotNull final VirtualFile virtualFile, @NotNull final VirtualFile jarFile,
                                               @NotNull final Consumer<OriginalLibrary> initializer) {
    if (list.contains(jarFile)) {
      return list.getInfo(jarFile);
    }
    else {
      final String path = virtualFile.getPath();
      OriginalLibrary library =
        new OriginalLibrary(virtualFile.getNameWithoutExtension() + "." + Integer.toHexString(path.hashCode()), jarFile);
      initializer.consume(library);
      return library;
    }
  }

  @Nullable
  public PropertiesFile getResourceBundleFile(String locale, String bundleName, ProjectInfo projectInfo) {
    for (Library library : projectInfo.getLibrarySet().getLibraries()) {
      if (library instanceof OriginalLibrary) {
        OriginalLibrary originalLibrary = (OriginalLibrary)library;
        if (originalLibrary.hasResourceBundles()) {
          final THashSet<String> bundles = originalLibrary.resourceBundles.get(locale);
          if (bundles.contains(bundleName)) {
            //noinspection ConstantConditions
            VirtualFile virtualFile = originalLibrary.getFile().findChild("locale").findChild(locale)
              .findChild(bundleName + CatalogXmlBuilder.PROPERTIES_EXTENSION);
            //noinspection ConstantConditions
            return (PropertiesFile)PsiDocumentManager.getInstance(projectInfo.getElement())
              .getPsiFile(FileDocumentManager.getInstance().getDocument(virtualFile));
          }
        }
      }
    }

    return null;
  }
}