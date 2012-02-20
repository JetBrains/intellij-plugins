package com.intellij.flex.uiDesigner.libraries;

import com.intellij.ProjectTopics;
import com.intellij.diagnostic.errordialog.Attachment;
import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.abc.AbcTranscoder;
import com.intellij.flex.uiDesigner.io.InfoMap;
import com.intellij.flex.uiDesigner.io.RetainCondition;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet.ContainsCondition;
import com.intellij.flex.uiDesigner.libraries.LibrarySorter.SortResult;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.PersistentHashMap;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.*;

@SuppressWarnings("MethodMayBeStatic")
public class LibraryManager implements Disposable {
  private static final String SWF_EXTENSION = ".swf";
  static final String PROPERTIES_EXTENSION = ".properties";

  private static final String ABC_FILTER_VERSION = "17";
  private static final String ABC_FILTER_VERSION_VALUE_NAME = "fud_abcFilterVersion";

  private static final char NAME_PREFIX = '@';

  private final File appDir;

  private final InfoMap<VirtualFile, Library> libraries = new InfoMap<VirtualFile, Library>();

  private final THashMap<String, LibrarySet> librarySets = new THashMap<String, LibrarySet>();
  private final Map<VirtualFile, Set<CharSequence>> globalDefinitionsMap = new THashMap<VirtualFile, Set<CharSequence>>();

  private final PersistentHashMap<String, SortResult> persistentCache;
  private boolean cacheCleared;

  public LibraryManager() throws IOException {
    appDir = DesignerApplicationManager.APP_DIR;
    persistentCache = createCache();
  }

  private PersistentHashMap<String, SortResult> createCache() throws IOException {
    final File file = new File(appDir, "librarySets");

    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    if (!ABC_FILTER_VERSION.equals(propertiesComponent.getValue(ABC_FILTER_VERSION_VALUE_NAME))) {
      clearCache(file);
    }

    try {
      return new PersistentHashMap<String, SortResult>(file, new EnumeratorStringDescriptor(), new MyDataExternalizer());
    }
    catch (IOException e) {
      LogMessageUtil.LOG.info(e);
      clearCache(file);
      return new PersistentHashMap<String, SortResult>(file, new EnumeratorStringDescriptor(), new MyDataExternalizer());
    }
  }

  private void clearCache(File file) {
    cacheCleared = true;
    PersistentHashMap.deleteFilesStartingWith(file);
  }

  @Override
  public void dispose() {
    try {
      persistentCache.close();
    }
    catch (IOException e) {
      LogMessageUtil.LOG.info(e);
    }
  }

  private static class MyDataExternalizer implements DataExternalizer<SortResult> {
    @Override
    public void save(final DataOutput out, SortResult value) throws IOException {
      out.writeShort(value.libraries.size());
      for (Library library : value.libraries) {
        out.writeUTF(library.getFile().getPath());
      }

      if (value.definitionMap == null) {
        out.writeInt(0);
        return;
      }

      out.writeInt(value.definitionMap.size());
      value.definitionMap.forEachKey(new TObjectProcedure<CharSequence>() {
        @Override
        public boolean execute(CharSequence charSequence) {
          try {
            out.writeUTF(charSequence.toString());
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }

          return true;
        }
      });
    }

    @Override
    public SortResult read(DataInput in) throws IOException {
      int librariesSize = in.readShort();
      String[] libraryPathes = new String[librariesSize];
      while (librariesSize-- > 0) {
        libraryPathes[librariesSize] = in.readUTF();
      }

      int size = in.readInt();
      final THashMap<CharSequence, Definition> map;
      if (size != 0) {
        map = new THashMap<CharSequence, Definition>(size, AbcTranscoder.HASHING_STRATEGY);
        while (size-- > 0) {
          map.put(in.readUTF(), null);
        }
      }
      else {
        map = null;
      }

      return new SortResult(map, libraryPathes);
    }
  }

  public void unregister(final int[] ids) {
    librarySets.retainEntries(new RetainCondition<String, LibrarySet>(ids));
  }

  public static LibraryManager getInstance() {
    return DesignerApplicationManager.getService(LibraryManager.class);
  }

  public boolean isRegistered(@NotNull Library library) {
    return libraries.contains(library);
  }

  public int add(@NotNull Library library) {
    return libraries.add(library);
  }

  public void garbageCollection(ProgressIndicator indicator) {
    if (!cacheCleared) {
      return;
    }

    indicator.setText(FlashUIDesignerBundle.message("delete.old.libraries"));

    for (String path : appDir.list()) {
      if (path.charAt(0) == NAME_PREFIX) {
        //noinspection ResultOfMethodCallIgnored
        new File(appDir, path).delete();
      }
    }

    PropertiesComponent.getInstance().setValue(ABC_FILTER_VERSION_VALUE_NAME, ABC_FILTER_VERSION);
  }

  @NotNull
  public ProjectComponentReferenceCounter initLibrarySets(@NotNull final Module module, ProblemsHolder problemsHolder) throws InitException {
    return initLibrarySets(module, problemsHolder, true);
  }

  @NotNull
  public ProjectComponentReferenceCounter initLibrarySets(@NotNull final Module module,
                                                         ProblemsHolder problemsHolder,
                                                         boolean collectLocalStyleHolders) throws InitException {
    final Project project = module.getProject();
    final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter(16384);
    stringWriter.startChange();
    final AssetCounter assetCounter = new AssetCounter();
    final LibraryCollector libraryCollector = new LibraryCollector(this, new LibraryStyleInfoCollector(assetCounter, problemsHolder, module, stringWriter), module);
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
    final FlexLibrarySet flexLibrarySet = getOrCreateFlexLibrarySet(libraryCollector, assetCounter);
    final InfoMap<Project, ProjectInfo> registeredProjects = client.getRegisteredProjects();
    ProjectInfo info = registeredProjects.getNullableInfo(project);
    if (info == null) {
      info = new ProjectInfo(project);
      registeredProjects.add(info);
      client.openProject(project);
    }

    LibrarySet librarySet;
    if (libraryCollector.externalLibraries.isEmpty()) {
      librarySet = null;
    }
    else {
      final String key = createKey(libraryCollector.externalLibraries, false);
      librarySet = librarySets.get(key);
      if (librarySet == null) {
        final SortResult sortResult = sortLibraries(new LibrarySorter(), libraryCollector, flexLibrarySet.contains, key, false);
        librarySet = new LibrarySet(sortResult.id, flexLibrarySet, sortResult.libraries);
        registerLibrarySet(key, librarySet);
      }
    }

    final ModuleInfo moduleInfo = new ModuleInfo(module,
      Collections.singletonList(librarySet == null ? flexLibrarySet : librarySet), ModuleInfoUtil.isApp(module));
    final ProjectComponentReferenceCounter projectComponentReferenceCounter = new ProjectComponentReferenceCounter();
    if (collectLocalStyleHolders) {
      // client.registerModule finalize it
      stringWriter.startChange();
      try {
        ModuleInfoUtil.collectLocalStyleHolders(moduleInfo, libraryCollector.getFlexSdkVersion(), stringWriter, problemsHolder,
                                                projectComponentReferenceCounter, assetCounter);
      }
      catch (Throwable e) {
        stringWriter.rollbackChange();
        throw new InitException(e, "error.collect.local.style.holders");
      }
    }

    client.registerModule(project, moduleInfo, stringWriter);
    client.fillAssetClassPoolIfNeed(flexLibrarySet);

    module.getMessageBus().connect(moduleInfo).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      @Override
      public void beforeRootsChange(ModuleRootEvent event) {
      }

      @Override
      public void rootsChanged(ModuleRootEvent event) {
        new Notification(FlashUIDesignerBundle.message("plugin.name"), FlashUIDesignerBundle.message("plugin.name"),
          "Please reopen your project to update on library changes.",
          NotificationType.WARNING).notify(project);
      }
    });

    return projectComponentReferenceCounter;
  }

  private FlexLibrarySet getOrCreateFlexLibrarySet(LibraryCollector libraryCollector, AssetCounter assetCounter) throws InitException {
    final String key = createKey(libraryCollector.sdkLibraries, true);
    FlexLibrarySet flexLibrarySet = (FlexLibrarySet)librarySets.get(key);
    if (flexLibrarySet == null) {
      final Set<CharSequence> globalDefinitions = getGlobalDefinitions(libraryCollector.getGlobalLibrary());
      final Condition<String> globalContains = new Condition<String>() {
        @Override
        public boolean value(String name) {
          return globalDefinitions.contains(name);
        }
      };
      final SortResult sortResult = sortLibraries(new LibrarySorter(new FlexDefinitionProcessor(libraryCollector.getFlexSdkVersion()),
                                                                    new FlexDefinitionMapProcessor(libraryCollector.getFlexSdkVersion(),
                                                                                                   globalContains)), libraryCollector,
                                                  globalContains, key, true);
      flexLibrarySet =
        new FlexLibrarySet(sortResult, null, new ContainsCondition(globalDefinitions, sortResult.definitionMap), assetCounter);
      registerLibrarySet(key, flexLibrarySet);
    }

    return flexLibrarySet;
  }

  private void registerLibrarySet(String key, LibrarySet librarySet) {
    Client.getInstance().registerLibrarySet(librarySet);
    librarySets.put(key, librarySet);
  }

  private Set<CharSequence> getGlobalDefinitions(VirtualFile file) throws InitException {
    Set<CharSequence> globalDefinitions = globalDefinitionsMap.get(file);
    if (globalDefinitions == null) {
      try {
        globalDefinitions = LibraryUtil.getDefinitions(file);
      }
      catch (IOException e) {
        throw new InitException(e, "error.sort.libraries");
      }
    }
    
    globalDefinitionsMap.put(file, globalDefinitions);
    return globalDefinitions;
  }

  private String createKey(List<Library> libraries, boolean isSdk) {
    // we don't depend on library order
    final VirtualFile[] files = new VirtualFile[libraries.size()];
    for (int i = 0, librariesSize = libraries.size(); i < librariesSize; i++) {
      files[i] = libraries.get(i).getFile();
    }
    
    Arrays.sort(files, new Comparator<VirtualFile>() {
      @Override
      public int compare(VirtualFile o1, VirtualFile o2) {
        return StringUtil.compare(o1.getPath(), o2.getPath(), false);
      }
    });
    
    final StringBuilder stringBuilder = StringBuilderSpinAllocator.alloc();
    try {
      if (isSdk) {
        stringBuilder.append('_');
      }

      for (VirtualFile file : files) {
        stringBuilder.append(file.getTimeStamp()).append(file.getPath()).append(':');
      }

      return stringBuilder.toString();
    }
    finally {
      StringBuilderSpinAllocator.dispose(stringBuilder);
    }
  }

  @NotNull
  private SortResult sortLibraries(LibrarySorter sorter, LibraryCollector collector, Condition<String> isExternal, String key, boolean isSdk)
    throws InitException {
    final List<Library> libraries = isSdk ? collector.sdkLibraries : collector.externalLibraries;
    try {
      final int id = persistentCache.enumerate(key);
      SortResult result = persistentCache.get(key);
      if (result == null) {
        result = sorter.sort(libraries, new File(appDir, NAME_PREFIX + Integer.toString(id) + SWF_EXTENSION), isExternal, isSdk);
        persistentCache.put(key, result);
      }
      else {
        final String[] libraryPathes = result.libraryPathes;
        final List<Library> filteredLibraries = new ArrayList<Library>(libraryPathes.length);
        for (Library library : libraries) {
          if (ArrayUtil.indexOf(libraryPathes, library.getFile().getPath()) != -1) {
            filteredLibraries.add(library);
          }
        }

        result = new SortResult(result.definitionMap, filteredLibraries);
      }

      result.id = id;
      return result;
    }
    catch (ClosedByInterruptException e) {
      throw new InitException(e);
    }
    catch (Throwable e) {
      String technicalMessage = "Flex SDK " + collector.getFlexSdkVersion();
      final Attachment[] attachments = new Attachment[libraries.size()];
      try {
        for (int i = 0, librariesSize = libraries.size(); i < librariesSize; i++) {
          Library library = libraries.get(i);
          technicalMessage += " " + library.getFile().getPath();
          attachments[i] = new Attachment(library.getCatalogFile());
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
  Library createOriginalLibrary(@NotNull final VirtualFile jarFile, @NotNull final LibraryStyleInfoCollector processor) {
    Library info = libraries.getNullableInfo(jarFile);
    final boolean isNew = info == null;
    if (isNew) {
      info = new Library(jarFile);
    }
    processor.process(info, isNew);
    return info;
  }

  @SuppressWarnings("MethodMayBeStatic")
  @Nullable
  public PropertiesFile getResourceBundleFile(String locale, String bundleName, ModuleInfo moduleInfo) {
    final Project project = moduleInfo.getElement().getProject();
    for (LibrarySet librarySet : moduleInfo.getLibrarySets()) {
      do {
        PropertiesFile propertiesFile;
        for (Library library : librarySet.getLibraries()) {
          if (library.hasResourceBundles() && (propertiesFile = getResourceBundleFile(locale, bundleName, library, project)) != null) {
            return propertiesFile;
          }
        }
      }
      while ((librarySet = librarySet.getParent()) != null);
    }

    // AS-273
    final Sdk sdk = FlexUtils.getSdkForActiveBC(moduleInfo.getModule());
    VirtualFile dir = sdk == null ? null : sdk.getHomeDirectory();
    if (dir != null) {
      dir = dir.findFileByRelativePath("frameworks/projects");
    }

    if (dir != null) {
      for (String libName : new String[]{"framework", "spark", "mx", "airframework", "rpc", "advancedgrids", "charts", "textLayout"}) {
        VirtualFile file = dir.findFileByRelativePath(libName + "/bundles/" + locale + "/" + bundleName + PROPERTIES_EXTENSION);
        if (file != null) {
          return virtualFileToProperties(project, file);
        }
      }
    }

    return null;
  }

  @Nullable
  private static PropertiesFile getResourceBundleFile(String locale, String bundleName, Library library, Project project) {
    final THashSet<String> bundles = library.resourceBundles.get(locale);
    if (!bundles.contains(bundleName)) {
      return null;
    }

    //noinspection ConstantConditions
    VirtualFile file = library.getFile().findChild("locale").findChild(locale).findChild(bundleName + PROPERTIES_EXTENSION);
    //noinspection ConstantConditions
    return virtualFileToProperties(project, file);
  }

  private static PropertiesFile virtualFileToProperties(Project project, VirtualFile file) {
    Document document = FileDocumentManager.getInstance().getDocument(file);
    assert document != null;
    //noinspection ConstantConditions
    return (PropertiesFile)PsiDocumentManager.getInstance(project).getPsiFile(document);
  }
}