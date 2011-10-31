package com.intellij.flex.uiDesigner.libraries;

import com.intellij.ProjectTopics;
import com.intellij.diagnostic.errordialog.Attachment;
import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.LibrarySorter.SortResult;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.ExceptionUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("MethodMayBeStatic")
public class LibraryManager extends EntityListManager<VirtualFile, Library> {
  private static final String ABC_FILTER_VERSION = "16";
  private static final String ABC_FILTER_VERSION_VALUE_NAME = "fud_abcFilterVersion";
  private static final char NAME_POSTFIX = '@';

  private File appDir;

  public static LibraryManager getInstance() {
    return ServiceManager.getService(LibraryManager.class);
  }

  public void setAppDir(@NotNull File appDir) {
    this.appDir = appDir;
  }

  public boolean isRegistered(@NotNull Library library) {
    return list.contains(library);
  }

  public int add(@NotNull Library library) {
    return list.add(library);
  }

  public boolean isSdkRegistered(Sdk sdk, Module module) {
    ProjectInfo info = Client.getInstance().getRegisteredProjects().getNullableInfo(module.getProject());
    return info != null && info.getSdk() == sdk;
  }

  public void garbageCollection(ProgressIndicator indicator) {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    if (ABC_FILTER_VERSION.equals(propertiesComponent.getValue(ABC_FILTER_VERSION_VALUE_NAME))) {
      return;
    }

    indicator.setText(FlexUIDesignerBundle.message("delete.old.libraries"));

    for (String path : appDir.list()) {
      if (path.endsWith(LibrarySorter.SWF_EXTENSION) && path.indexOf(NAME_POSTFIX) != -1) {
        //noinspection ResultOfMethodCallIgnored
        new File(appDir, path).delete();
      }
    }

    propertiesComponent.setValue(ABC_FILTER_VERSION_VALUE_NAME, ABC_FILTER_VERSION);
  }

  public XmlFile[] initLibrarySets(@NotNull final Module module) throws InitException {
    final ProblemsHolder problemsHolder = new ProblemsHolder();
    XmlFile[] unregisteredDocumentReferences = initLibrarySets(module, true, problemsHolder, null);
    if (!problemsHolder.isEmpty()) {
      DocumentProblemManager.getInstance().report(module.getProject(), problemsHolder);
    }

    return unregisteredDocumentReferences;
  }

  public XmlFile[] initLibrarySets(@NotNull final Module module, @NotNull ProblemsHolder problemsHolder) throws InitException {
    return initLibrarySets(module, true, problemsHolder, null);
  }

  // librarySet for test only
  public XmlFile[] initLibrarySets(@NotNull final Module module, boolean collectLocalStyleHolders, ProblemsHolder problemsHolder,
                                   @Nullable LibrarySet librarySet)
      throws InitException {
    final Project project = module.getProject();
    final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(16384);
    stringWriter.startChange();
    final LibraryCollector libraryCollector = new LibraryCollector(this, new LibraryStyleInfoCollector(project, module, stringWriter,
                                                                                                       problemsHolder), project);

    final Client client;
    try {
      final AccessToken token = ReadAction.start();
      try {
        libraryCollector.collect(module);
      }
      finally {
        token.finish();
      }

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

    final InfoList<Project, ProjectInfo> registeredProjects = client.getRegisteredProjects();
    final String projectLocationHash = project.getLocationHash();
    ProjectInfo info = registeredProjects.getNullableInfo(project);
    final boolean isNewProject = info == null;

    final AssetCounterInfo assetCounterInfo;
    if (isNewProject) {
      assetCounterInfo = new AssetCounterInfo(libraryCollector.sdkLibraries);
      if (librarySet == null) {
        librarySet = createLibrarySet(projectLocationHash + "_fdk", null, libraryCollector.sdkLibraries,
                                      libraryCollector.getFlexSdkVersion(), module, true);
        client.registerLibrarySet(librarySet);
      }

      info = new ProjectInfo(project, librarySet, libraryCollector.getFlexSdk());
      registeredProjects.add(info);
      client.openProject(project);
    }
    else {
      // different flex sdk version for module
      if (libraryCollector.sdkLibraries != null) {
        librarySet = createLibrarySet(Integer.toHexString(module.getName().hashCode()) + "_fdk", null, libraryCollector.sdkLibraries,
                                      libraryCollector.getFlexSdkVersion(), module, true);
        assetCounterInfo = new AssetCounterInfo(libraryCollector.sdkLibraries);
        client.registerLibrarySet(librarySet);
      }
      else {
        assetCounterInfo = info.assetCounterInfo;
      }
    }

    if (libraryCollector.externalLibraries.isEmpty()) {
      if (!isNewProject && librarySet == null) {
        librarySet = info.getLibrarySet();
      }
    }
    else if (isNewProject) {
      librarySet = createLibrarySet(projectLocationHash, librarySet, libraryCollector.externalLibraries,
                                    libraryCollector.getFlexSdkVersion(), module, false);
      assetCounterInfo.demanded.append(libraryCollector.externalLibraries);
      client.registerLibrarySet(librarySet);
      info.setLibrarySet(librarySet);
    }
    else {
      // todo merge existing libraries and new. create new custom external library set for myModule,
      // if we have different version of the artifact
      throw new UnsupportedOperationException("merge existing libraries and new");
    }

    final ModuleInfo moduleInfo = ModuleInfoUtil.createInfo(module, assetCounterInfo);
    final List<XmlFile> unregisteredDocumentReferences = new ArrayList<XmlFile>();
    if (collectLocalStyleHolders) {
      // client.registerModule finalize it
      stringWriter.startChange();
      try {
        ModuleInfoUtil.collectLocalStyleHolders(moduleInfo, libraryCollector.getFlexSdkVersion(), stringWriter, problemsHolder,
                                                unregisteredDocumentReferences, assetCounterInfo.demanded);
      }
      catch (Throwable e) {
        stringWriter.rollbackChange();
        throw new InitException(e, "error.collect.local.style.holders");
      }
    }

    client.registerModule(project, moduleInfo, new String[]{librarySet.getId()}, stringWriter);
    client.fillAssetClassPoolIfNeed(module);

    module.getMessageBus().connect(moduleInfo).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      @Override
      public void beforeRootsChange(ModuleRootEvent event) {
      }

      @Override
      public void rootsChanged(ModuleRootEvent event) {
        new Notification(FlexUIDesignerBundle.message("plugin.name"), FlexUIDesignerBundle.message("plugin.name"),
            "Please reopen your project to update on library changes.",
            NotificationType.WARNING).notify(project);
      }
    });

    return unregisteredDocumentReferences.isEmpty() ? null : unregisteredDocumentReferences.toArray(new XmlFile[unregisteredDocumentReferences.size()]);
  }

  @NotNull
  private LibrarySet createLibrarySet(String id, @Nullable LibrarySet parent, List<Library> libraries, String flexSdkVersion,
      Module module, final boolean isFromSdk)
    throws InitException {
    try {
      final SortResult result = new LibrarySorter(appDir, module).sort(libraries, id, flexSdkVersion, isFromSdk);
      return new LibrarySet(id, parent, ApplicationDomainCreationPolicy.ONE, result.items, result.resourceBundleOnlyitems,
                            result.embedItems);
    }
    catch (Throwable e) {
      String technicalMessage = "Flex SDK " + flexSdkVersion;
      final Attachment[] attachments = new Attachment[libraries.size()];
      try {
        for (int i = 0, librariesSize = libraries.size(); i < librariesSize; i++) {
          attachments[i] = new Attachment(libraries.get(i).getCatalogFile());
        }
      }
      catch (Throwable innerE) {
        technicalMessage += " Cannot collect library catalog files due to " + ExceptionUtil.getThrowableText(innerE);
      }
      
      throw new InitException(e, "error.sort.libraries", attachments, technicalMessage);
    }
  }

  // created library will be register later, in Client.registerLibrarySet, so, we expect that createOriginalLibrary never called with duplicated virtualFile, i.e.
  // sdkLibraries doesn't contain duplicated virtualFiles and externalLibraries too (http://youtrack.jetbrains.net/issue/AS-200)
  Library createOriginalLibrary(@NotNull final VirtualFile virtualFile, @NotNull final VirtualFile jarFile,
                                @NotNull final Consumer<Library> initializer) {
    final Library info = list.getNullableInfo(jarFile);
    if (info != null) {
      return info;
    }

    // todo for flexmojos we must use artifactId (because file name contains version and classifier)
    final String nameWithoutExtension = virtualFile.getNameWithoutExtension();
    Library library = new Library(nameWithoutExtension,
                                  nameWithoutExtension + NAME_POSTFIX + Integer.toHexString(virtualFile.getPath().hashCode()), jarFile);
    initializer.consume(library);
    return library;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Nullable
  public PropertiesFile getResourceBundleFile(String locale, String bundleName, ProjectInfo projectInfo) {
    LibrarySet librarySet = projectInfo.getLibrarySet();
    PropertiesFile propertiesFile;
    do {
      for (LibrarySetItem item : librarySet.getItems()) {
        Library library = item.library;
        if (library.hasResourceBundles() && (propertiesFile = getResourceBundleFile(locale, bundleName, library, projectInfo)) != null) {
          return propertiesFile;
        }
      }

      for (LibrarySetItem item : librarySet.getResourceBundleOnlyItems()) {
        if ((propertiesFile = getResourceBundleFile(locale, bundleName, item.library, projectInfo)) != null) {
          return propertiesFile;
        }
      }
    }
    while ((librarySet = librarySet.getParent()) != null);

    return null;
  }

  private static PropertiesFile getResourceBundleFile(String locale, String bundleName, Library library, ProjectInfo projectInfo) {
    final THashSet<String> bundles = library.resourceBundles.get(locale);
    if (!bundles.contains(bundleName)) {
      return null;
    }

    //noinspection ConstantConditions
    VirtualFile virtualFile = library.getFile().findChild("locale").findChild(locale)
        .findChild(bundleName + CatalogXmlBuilder.PROPERTIES_EXTENSION);
    //noinspection ConstantConditions
    return (PropertiesFile)PsiDocumentManager.getInstance(projectInfo.getElement())
        .getPsiFile(FileDocumentManager.getInstance().getDocument(virtualFile));
  }
}