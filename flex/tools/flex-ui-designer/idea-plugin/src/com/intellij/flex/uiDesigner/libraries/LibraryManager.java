package com.intellij.flex.uiDesigner.libraries;

import com.intellij.ProjectTopics;
import com.intellij.diagnostic.errordialog.Attachment;
import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.css.CssWriter;
import com.intellij.flex.uiDesigner.io.IdPool;
import com.intellij.flex.uiDesigner.io.InfoMap;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.LibrarySorter.SortResult;
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
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.StringBuilderSpinAllocator;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MethodMayBeStatic")
public class LibraryManager {
  private static final String SWF_EXTENSION = ".swf";

  //private static final String ABC_FILTER_VERSION = "16";
  //private static final String ABC_FILTER_VERSION_VALUE_NAME = "fud_abcFilterVersion";
  private static final char NAME_POSTFIX = '@';

  private File appDir;

  private final InfoMap<VirtualFile, Library> libraries = new InfoMap<VirtualFile, Library>();
  //private final List<VirtualFile, LibrarySet> librarySets = new InfoMap<VirtualFile, LibrarySet>();

  private final Map<String, LibrarySet> librarySets = new THashMap<String, LibrarySet>();

  private final IdPool librarySetIdPool = new IdPool();

  public static LibraryManager getInstance() {
    return ServiceManager.getService(LibraryManager.class);
  }

  public void reset() {
    libraries.clear();
  }

  public void setAppDir(@NotNull File appDir) {
    this.appDir = appDir;
  }

  public boolean isRegistered(@NotNull Library library) {
    return libraries.contains(library);
  }

  public int add(@NotNull Library library) {
    return libraries.add(library);
  }

  public void garbageCollection(@SuppressWarnings("UnusedParameters") ProgressIndicator indicator) {
    //PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    //if (ABC_FILTER_VERSION.equals(propertiesComponent.getValue(ABC_FILTER_VERSION_VALUE_NAME))) {
    //  return;
    //}
    //
    //indicator.setText(FlexUIDesignerBundle.message("delete.old.libraries"));
    //
    //for (String path : appDir.list()) {
    //  if (path.endsWith(SWF_EXTENSION) && path.indexOf(NAME_POSTFIX) != -1) {
    //    //noinspection ResultOfMethodCallIgnored
    //    new File(appDir, path).delete();
    //  }
    //}
    //
    //propertiesComponent.setValue(ABC_FILTER_VERSION_VALUE_NAME, ABC_FILTER_VERSION);
  }

  public XmlFile[] initLibrarySets(@NotNull final Module module) throws InitException {
    final ProblemsHolder problemsHolder = new ProblemsHolder();
    XmlFile[] unregisteredDocumentReferences = initLibrarySets(module, true, problemsHolder);
    if (!problemsHolder.isEmpty()) {
      DocumentProblemManager.getInstance().report(module.getProject(), problemsHolder);
    }

    return unregisteredDocumentReferences;
  }

  public XmlFile[] initLibrarySets(@NotNull final Module module, boolean collectLocalStyleHolders, ProblemsHolder problemsHolder)
      throws InitException {
    final Project project = module.getProject();
    final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(16384);
    stringWriter.startChange();
    final AssetCounter assetCounter = new AssetCounter();
    final LibraryCollector libraryCollector = new LibraryCollector(this, new LibraryStyleInfoCollector(new CssWriter(stringWriter, problemsHolder, assetCounter), module, stringWriter), project);

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

    assert !libraryCollector.sdkLibraries.isEmpty();
    FlexLibrarySet flexLibrarySet = getOrCreateFlexLibrarySet(module, libraryCollector);
    final InfoMap<Project, ProjectInfo> registeredProjects = client.getRegisteredProjects();
    ProjectInfo info = registeredProjects.getNullableInfo(project);
    final boolean isNewProject = info == null;
    if (isNewProject) {
      info = new ProjectInfo(project);
      registeredProjects.add(info);
      client.openProject(project);
    }

    LibrarySet librarySet;
    if (libraryCollector.externalLibraries.isEmpty()) {
      librarySet = null;
    }
    else {
      final String key = createKey(libraryCollector.externalLibraries);
      librarySet = librarySets.get(key);
      if (librarySet == null) {
        createAndRegisterLibrarySet(flexLibrarySet, libraryCollector.sdkLibraries, libraryCollector.getFlexSdkVersion(), module, false);
        librarySets.put(key, flexLibrarySet);
      }
    }

    final ModuleInfo moduleInfo = new ModuleInfo(module, flexLibrarySet,
                                                 Collections.singletonList(librarySet == null ? flexLibrarySet : librarySet),
                                                 ModuleInfoUtil.isApp(module));
    final List<XmlFile> unregisteredDocumentReferences = new ArrayList<XmlFile>();
    if (collectLocalStyleHolders) {
      // client.registerModule finalize it
      stringWriter.startChange();
      try {
        ModuleInfoUtil.collectLocalStyleHolders(moduleInfo, libraryCollector.getFlexSdkVersion(), stringWriter, problemsHolder,
                                                unregisteredDocumentReferences, assetCounter);
      }
      catch (Throwable e) {
        stringWriter.rollbackChange();
        throw new InitException(e, "error.collect.local.style.holders");
      }
    }

    client.registerModule(project, moduleInfo, stringWriter);

    flexLibrarySet.assetCounterInfo.demanded.append(assetCounter);
    client.fillAssetClassPoolIfNeed(flexLibrarySet);

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

  private FlexLibrarySet getOrCreateFlexLibrarySet(Module module, LibraryCollector libraryCollector) throws InitException {
    final String key = createKey(libraryCollector.sdkLibraries);
    FlexLibrarySet flexLibrarySet = (FlexLibrarySet)librarySets.get(key);
    if (flexLibrarySet == null) {
      flexLibrarySet = (FlexLibrarySet)createAndRegisterLibrarySet(null, libraryCollector.sdkLibraries,
                                                                   libraryCollector.getFlexSdkVersion(), module, true);
      librarySets.put(key, flexLibrarySet);
    }
    return flexLibrarySet;
  }

  private String createKey(List<Library> sdkLibraries) {
    final StringBuilder stringBuilder = StringBuilderSpinAllocator.alloc();
    try {
      for (Library sdkLibrary : sdkLibraries) {
        stringBuilder.append(sdkLibrary.getFile().getPath());
      }

      return stringBuilder.toString();
    }
    finally {
      StringBuilderSpinAllocator.dispose(stringBuilder);
    }
  }

  @NotNull
  private LibrarySet createAndRegisterLibrarySet(@Nullable LibrarySet parent, List<Library> libraries, String flexSdkVersion, Module module,
                                                 final boolean isFromSdk)
    throws InitException {
    try {
      final int id = librarySetIdPool.allocate();
      final SortResult result = new LibrarySorter(module).sort(libraries, flexSdkVersion, isFromSdk, new File(appDir, id + SWF_EXTENSION));
      final LibrarySet librarySet = new LibrarySet(id, parent, ApplicationDomainCreationPolicy.ONE, result.items, result.resourceBundleOnlyitems);
      Client.getInstance().registerLibrarySet(librarySet);
      return librarySet;
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
  Library createOriginalLibrary(@NotNull final VirtualFile virtualFile, @NotNull final VirtualFile jarFile, final String artifactId,
                                @NotNull final Consumer<Library> initializer) {
    final Library info = libraries.getNullableInfo(jarFile);
    if (info != null) {
      return info;
    }

    Library library = new Library(artifactId, artifactId + NAME_POSTFIX + Integer.toHexString(virtualFile.getPath().hashCode()), jarFile);
    initializer.consume(library);
    return library;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Nullable
  public PropertiesFile getResourceBundleFile(String locale, String bundleName, ModuleInfo moduleInfo) {
    for (LibrarySet librarySet : moduleInfo.getLibrarySets()) {
      do {
        PropertiesFile propertiesFile;
        final Project project = moduleInfo.getElement().getProject();
        for (LibrarySetItem item : librarySet.getItems()) {
          Library library = item.library;
          if (library.hasResourceBundles() && (propertiesFile = getResourceBundleFile(locale, bundleName, library, project)) != null) {
            return propertiesFile;
          }
        }

        for (LibrarySetItem item : librarySet.getResourceBundleOnlyItems()) {
          if ((propertiesFile = getResourceBundleFile(locale, bundleName, item.library, project)) != null) {
            return propertiesFile;
          }
        }
      }
      while ((librarySet = librarySet.getParent()) != null);
    }

    return null;
  }

  private static PropertiesFile getResourceBundleFile(String locale, String bundleName, Library library, Project project) {
    final THashSet<String> bundles = library.resourceBundles.get(locale);
    if (!bundles.contains(bundleName)) {
      return null;
    }

    //noinspection ConstantConditions
    VirtualFile virtualFile = library.getFile().findChild("locale").findChild(locale)
        .findChild(bundleName + CatalogXmlBuilder.PROPERTIES_EXTENSION);
    //noinspection ConstantConditions
    return (PropertiesFile)PsiDocumentManager.getInstance(project).getPsiFile(FileDocumentManager.getInstance().getDocument(virtualFile));
  }
}