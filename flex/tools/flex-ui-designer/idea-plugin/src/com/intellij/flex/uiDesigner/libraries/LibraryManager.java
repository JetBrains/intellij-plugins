package com.intellij.flex.uiDesigner.libraries;

import com.intellij.ProjectTopics;
import com.intellij.diagnostic.errordialog.Attachment;
import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.css.CssWriter;
import com.intellij.flex.uiDesigner.io.IdPool;
import com.intellij.flex.uiDesigner.io.InfoMap;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet.ContainsCondition;
import com.intellij.flex.uiDesigner.libraries.LibrarySorter.SortResult;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.util.Condition;
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
import java.io.IOException;
import java.util.*;

@SuppressWarnings("MethodMayBeStatic")
public class LibraryManager {
  private static final String SWF_EXTENSION = ".swf";
  static final String PROPERTIES_EXTENSION = ".properties";

  //private static final String ABC_FILTER_VERSION = "16";
  //private static final String ABC_FILTER_VERSION_VALUE_NAME = "fud_abcFilterVersion";
  private static final char NAME_POSTFIX = '@';

  private File appDir;

  private final InfoMap<VirtualFile, Library> libraries = new InfoMap<VirtualFile, Library>();

  private final Map<String, LibrarySet> librarySets = new THashMap<String, LibrarySet>();
  private final IdPool librarySetIdPool = new IdPool();
  private final Map<VirtualFile, Set<CharSequence>> globalDefinitionsMap = new THashMap<VirtualFile, Set<CharSequence>>();

  public static LibraryManager getInstance() {
    return DesignerApplicationManager.getService(LibraryManager.class);
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
    FlexLibrarySet flexLibrarySet = getOrCreateFlexLibrarySet(libraryCollector);
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
        final int id = librarySetIdPool.allocate();
        final SortResult sortResult = sortLibraries(new LibrarySorter(), id, libraryCollector.externalLibraries, libraryCollector.getFlexSdkVersion(),
                                                    flexLibrarySet.contains);
        librarySet = new LibrarySet(id, flexLibrarySet, sortResult.items, sortResult.resourceBundleOnlyItems);
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

  private FlexLibrarySet getOrCreateFlexLibrarySet(LibraryCollector libraryCollector) throws InitException {
    final String key = createKey(libraryCollector.sdkLibraries);
    FlexLibrarySet flexLibrarySet = (FlexLibrarySet)librarySets.get(key);
    if (flexLibrarySet == null) {
      final Set<CharSequence> globalDefinitions = getGlobalDefinitions(libraryCollector.getGlobalLibrary());
      final int id = librarySetIdPool.allocate();
      Condition<String> globalContains = new Condition<String>() {
        @Override
        public boolean value(String name) {
          return globalDefinitions.contains(name);
        }
      };
      final SortResult sortResult = sortLibraries(
        new LibrarySorter(new FlexDefinitionProcessor(), new FlexDefinitionMapProcessor(libraryCollector.getFlexSdkVersion(), globalContains)), id, libraryCollector.sdkLibraries,
        libraryCollector.getFlexSdkVersion(),
        globalContains);

      flexLibrarySet = new FlexLibrarySet(id, null, sortResult.items, sortResult.resourceBundleOnlyItems, new ContainsCondition(globalDefinitions, sortResult.definitionMap));
      Client.getInstance().registerLibrarySet(flexLibrarySet);
      librarySets.put(key, flexLibrarySet);
    }

    return flexLibrarySet;
  }

  private Set<CharSequence> getGlobalDefinitions(VirtualFile file) throws InitException {
    Set<CharSequence> globalDefinitions = globalDefinitionsMap.get(file);
    if (globalDefinitions == null) {
      try {
        globalDefinitions = LibrarySorter.getDefinitions(file);
      }
      catch (IOException e) {
        throw new InitException(e, "error.sort.libraries");
      }
    }
    
    globalDefinitionsMap.put(file, globalDefinitions);
    return globalDefinitions;
  }

  private String createKey(List<Library> sdkLibraries) {
    final StringBuilder stringBuilder = StringBuilderSpinAllocator.alloc();
    try {
      for (Library sdkLibrary : sdkLibraries) {
        stringBuilder.append(sdkLibrary.getFile().getPath()).append(':');
      }

      return stringBuilder.toString();
    }
    finally {
      StringBuilderSpinAllocator.dispose(stringBuilder);
    }
  }

  @NotNull
  private SortResult sortLibraries(LibrarySorter librarySorter, int librarySetId, List<Library> libraries, String flexSdkVersion,
                                   Condition<String> isExternal)
    throws InitException {
    try {
      return librarySorter.sort(libraries, new File(appDir, librarySetId + SWF_EXTENSION), isExternal);
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
        for (Library library : librarySet.getLibraries()) {
          if (library.hasResourceBundles() && (propertiesFile = getResourceBundleFile(locale, bundleName, library, project)) != null) {
            return propertiesFile;
          }
        }

        for (Library library : librarySet.getResourceLibrariesOnly()) {
          if ((propertiesFile = getResourceBundleFile(locale, bundleName, library, project)) != null) {
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
    VirtualFile file = library.getFile().findChild("locale").findChild(locale).findChild(bundleName + PROPERTIES_EXTENSION);
    //noinspection ConstantConditions
    return (PropertiesFile)PsiDocumentManager.getInstance(project).getPsiFile(FileDocumentManager.getInstance().getDocument(file));
  }
}