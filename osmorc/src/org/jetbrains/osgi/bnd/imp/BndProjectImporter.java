// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.imp;

import aQute.bnd.build.Container;
import aQute.bnd.build.Project;
import aQute.bnd.build.Workspace;
import aQute.bnd.header.Attrs;
import aQute.bnd.service.Refreshable;
import aQute.bnd.service.RepositoryPlugin;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfiguration;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ExportableOrderEntry;
import com.intellij.openapi.roots.JdkOrderEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleJdkOrderEntry;
import com.intellij.openapi.roots.ModuleOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ModuleSourceOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.ModifiableModelCommitter;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.util.ObjectUtils;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.compiler.JpsJavaCompilerOptions;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.jetbrains.osgi.jps.model.OutputPathType;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacetType;
import org.osmorc.i18n.OsmorcBundle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.osmorc.i18n.OsmorcBundle.message;

@SuppressWarnings({"IO_FILE_USAGE", "UsagesOfObsoleteApi"})
public final class BndProjectImporter {
  public static final String CNF_DIR = Workspace.CNFDIR;
  public static final String BUILD_FILE = Workspace.BUILDFILE;
  public static final String BND_FILE = Project.BNDFILE;
  public static final String BND_LIB_PREFIX = "bnd:";

  private static final Logger LOG = Logger.getInstance(BndProjectImporter.class);

  private static final Key<Workspace> BND_WORKSPACE_KEY = Key.create("bnd.workspace.key");

  private static final String JAVAC_SOURCE = "javac.source";
  private static final String JAVAC_TARGET = "javac.target";
  private static final String SRC_ROOT = "OSGI-OPT/src";
  private static final String JDK_DEPENDENCY = "ee.j2se";

  private static final Comparator<OrderEntry> ORDER_ENTRY_COMPARATOR = new Comparator<>() {
    @Override
    public int compare(OrderEntry o1, OrderEntry o2) {
      return weight(o1) - weight(o2);
    }

    private static int weight(OrderEntry e) {
      return e instanceof JdkOrderEntry ? 2 :
             e instanceof ModuleSourceOrderEntry ? 0 :
             1;
    }
  };

  private static boolean isUnitTestMode() {
    return ApplicationManager.getApplication().isUnitTestMode();
  }

  private final com.intellij.openapi.project.Project myProject;
  private final Workspace myWorkspace;
  private final Collection<? extends Project> myProjects;
  private final Map<String, String> mySourcesMap = CollectionFactory.createFilePathMap();

  public BndProjectImporter(
    @NotNull com.intellij.openapi.project.Project project,
    @NotNull Workspace workspace,
    @NotNull Collection<? extends Project> toImport
  ) {
    myProject = project;
    myWorkspace = workspace;
    myProjects = toImport;
  }

  public @NotNull Module createRootModule(@NotNull ModifiableModuleModel model) {
    String rootDir = myProject.getBasePath();
    assert rootDir != null : myProject;
    String imlPath = rootDir + File.separator + myProject.getName() + ModuleFileType.DOT_DEFAULT_EXTENSION;
    Module module = model.newModule(imlPath, JavaModuleType.getModuleType().getId());
    ModuleRootModificationUtil.addContentRoot(module, rootDir);
    ModuleRootModificationUtil.setSdkInherited(module);
    return module;
  }

  public void setupProject() {
    LanguageLevel sourceLevel = LanguageLevel.parse(myWorkspace.getProperty(JAVAC_SOURCE));
    if (sourceLevel != null) {
      LanguageLevelProjectExtension langLevelExt = LanguageLevelProjectExtension.getInstance(myProject);
      WriteAction.run(() -> langLevelExt.setLanguageLevel(sourceLevel));
    }

    String targetLevel = myWorkspace.getProperty(JAVAC_TARGET);
    CompilerConfiguration targetLevelExt = CompilerConfiguration.getInstance(myProject);
    WriteAction.run(() -> targetLevelExt.setProjectBytecodeTarget(targetLevel));

    // compilation options (see Project#getCommonJavac())
    JpsJavaCompilerOptions javacOptions = JavacConfiguration.getOptions(myProject, JavacConfiguration.class);
    javacOptions.DEBUGGING_INFO = booleanProperty(myWorkspace.getProperty("javac.debug", "true"));
    javacOptions.DEPRECATION = booleanProperty(myWorkspace.getProperty("java.deprecation"));
    javacOptions.ADDITIONAL_OPTIONS_STRING = myWorkspace.getProperty("java.options", "");
  }

  public void resolve(boolean refresh) {
    if (!isUnitTestMode()) {
      new Task.Backgroundable(myProject, message("bnd.import.resolve.task"), true) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          if (resolve(indicator)) {
            ApplicationManager.getApplication().invokeLater(() -> {
              createProjectStructure();
              if (refresh) {
                VirtualFileManager.getInstance().asyncRefresh();
              }
            }, BndProjectImporter.this.myProject.getDisposed());
          }
        }
      }.queue();
    }
    else {
      resolve(null);
      createProjectStructure();
    }
  }

  private boolean resolve(@Nullable ProgressIndicator indicator) {
    int progress = 0;
    for (Project project : myProjects) {
      LOG.info("resolving: " + project.getBase());

      if (indicator != null) {
        indicator.checkCanceled();
        indicator.setText(project.getName());
      }

      try {
        project.prepare();
      }
      catch (Exception e) {
        checkErrors(project, e);
        return false;
      }

      checkWarnings(project, project.getErrors(), true);
      checkWarnings(project, project.getWarnings(), false);

      findSources(project);

      if (indicator != null) {
        indicator.setFraction((double)(++progress) / myProjects.size());
      }
    }

    return true;
  }

  private void findSources(Project project) {
    try {
      findSources(project.getBootclasspath());
      findSources(project.getBuildpath());
      findSources(project.getTestpath());
    }
    catch (Exception ignored) { }
  }

  private void findSources(Collection<Container> classpath) {
    for (Container dependency : classpath) {
      Container.TYPE type = dependency.getType();
      if (type == Container.TYPE.REPO || type == Container.TYPE.EXTERNAL) {
        File file = dependency.getFile();
        if (file.isFile() && FileUtilRt.extensionEquals(file.getName(), "jar")) {
          String path = file.getPath();
          if (!mySourcesMap.containsKey(path)) {
            try (ZipFile zipFile = new ZipFile(file)) {
              ZipEntry srcRoot = zipFile.getEntry(SRC_ROOT);
              if (srcRoot != null) {
                mySourcesMap.put(path, SRC_ROOT);
              }
            }
            catch (IOException e) {
              mySourcesMap.put(path, null);
            }
          }
        }
      }
    }
  }

  private void createProjectStructure() {
    if (myProject.isDisposed()) {
      return;
    }

    ApplicationManager.getApplication().runWriteAction(() -> {
      LanguageLevel projectLevel = LanguageLevelProjectExtension.getInstance(myProject).getLanguageLevel();
      Map<Project, ModifiableRootModel> rootModels = new HashMap<>();
      ModifiableModuleModel moduleModel = ModuleManager.getInstance(myProject).getModifiableModel();
      LibraryTable.ModifiableModel libraryModel = LibraryTablesRegistrar.getInstance().getLibraryTable(myProject).getModifiableModel();
      try {
        for (Project project : myProjects) {
          try {
            rootModels.put(project, createModule(moduleModel, project, projectLevel));
          }
          catch (Exception e) {
            LOG.error(e);  // should not happen, since project.prepare() is already called
          }
        }
        for (Project project : myProjects) {
          try {
            setDependencies(moduleModel, libraryModel, rootModels.get(project), project);
          }
          catch (Exception e) {
            LOG.error(e);  // should not happen, since project.prepare() is already called
          }
        }
      }
      finally {
        libraryModel.commit();
        ModifiableModelCommitter.multiCommit(rootModels.values(), moduleModel);
      }
    });
  }

  private ModifiableRootModel createModule(ModifiableModuleModel moduleModel, Project project, LanguageLevel projectLevel) throws Exception {
    String name = project.getName();
    Module module = moduleModel.findModuleByName(name);
    if (module == null) {
      String path = project.getBase().getPath() + File.separator + name + ModuleFileType.DOT_DEFAULT_EXTENSION;
      module = moduleModel.newModule(path, JavaModuleType.getModuleType().getId());
    }

    ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
    for (ContentEntry entry : rootModel.getContentEntries()) {
      rootModel.removeContentEntry(entry);
    }
    for (OrderEntry entry : rootModel.getOrderEntries()) {
      if (!(entry instanceof ModuleJdkOrderEntry || entry instanceof ModuleSourceOrderEntry)) {
        rootModel.removeOrderEntry(entry);
      }
    }
    rootModel.inheritSdk();

    ContentEntry contentEntry = rootModel.addContentEntry(url(project.getBase()));
    for (File src : project.getSourcePath()) {
      contentEntry.addSourceFolder(url(src), false);
    }
    File testSrc = project.getTestSrc();
    if (testSrc != null) {
      contentEntry.addSourceFolder(url(testSrc), true);
    }
    contentEntry.addExcludeFolder(url(project.getTarget()));

    LanguageLevel sourceLevel = LanguageLevel.parse(project.getProperty(JAVAC_SOURCE));
    if (sourceLevel == projectLevel) sourceLevel = null;
    rootModel.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(sourceLevel);

    CompilerModuleExtension compilerExt = rootModel.getModuleExtension(CompilerModuleExtension.class);
    compilerExt.inheritCompilerOutputPath(false);
    compilerExt.setExcludeOutput(true);
    compilerExt.setCompilerOutputPath(url(project.getSrcOutput()));
    compilerExt.setCompilerOutputPathForTests(url(project.getTestOutput()));

    String targetLevel = project.getProperty(JAVAC_TARGET);
    CompilerConfiguration.getInstance(myProject).setBytecodeTargetLevel(module, targetLevel);

    OsmorcFacet facet = OsmorcFacet.getInstance(module);

    if (project.isNoBundles() && facet != null) {
      FacetUtil.deleteFacet(facet);
      facet = null;
    }
    else if (!project.isNoBundles() && facet == null) {
      facet = FacetUtil.addFacet(module, OsmorcFacetType.getInstance());
    }

    if (facet != null) {
      OsmorcFacetConfiguration facetConfig = facet.getConfiguration();

      facetConfig.setManifestGenerationMode(ManifestGenerationMode.Bnd);
      facetConfig.setBndFileLocation(FileUtil.getRelativePath(path(project.getBase()), path(project.getPropertiesFile()), '/'));

      Map.Entry<String, Attrs> bsn = project.getBundleSymbolicName();
      File bundle = project.getOutputFile(bsn != null ? bsn.getKey() : name, project.getBundleVersion());
      facetConfig.setJarFileLocation(path(bundle), OutputPathType.SpecificOutputPath);

      facetConfig.setDoNotSynchronizeWithMaven(true);
    }

    return rootModel;
  }

  private void setDependencies(ModifiableModuleModel moduleModel,
                               LibraryTable.ModifiableModel libraryModel,
                               ModifiableRootModel rootModel,
                               Project project) throws Exception {
    List<String> warnings = new ArrayList<>();

    Collection<Container> boot = project.getBootclasspath();
    Set<Container> bootSet = Collections.emptySet();
    if (!boot.isEmpty()) {
      setDependencies(moduleModel, libraryModel, rootModel, project, boot, false, bootSet, warnings);
      bootSet = new HashSet<>(boot);

      OrderEntry[] entries = rootModel.getOrderEntries();
      if (entries.length > 2) {
        Arrays.sort(entries, ORDER_ENTRY_COMPARATOR);
        rootModel.rearrangeOrderEntries(entries);
      }
    }

    setDependencies(moduleModel, libraryModel, rootModel, project, project.getBuildpath(), false, bootSet, warnings);
    setDependencies(moduleModel, libraryModel, rootModel, project, project.getTestpath(), true, bootSet, warnings);

    checkWarnings(project, warnings, false);
  }

  private void setDependencies(ModifiableModuleModel moduleModel,
                               LibraryTable.ModifiableModel libraryModel,
                               ModifiableRootModel rootModel,
                               Project project,
                               Collection<Container> classpath,
                               boolean tests,
                               Set<Container> excluded,
                               List<String> warnings) {
    DependencyScope scope = tests ? DependencyScope.TEST : DependencyScope.COMPILE;
    for (Container dependency : classpath) {
      if (excluded.contains(dependency)) {
        continue;  // skip boot path dependency
      }
      if (dependency.getType() == Container.TYPE.PROJECT && project == dependency.getProject()) {
        continue;  // skip self-reference
      }
      try {
        addEntry(moduleModel, libraryModel, rootModel, dependency, scope);
      }
      catch (IllegalArgumentException e) {
        warnings.add(e.getMessage());
      }
    }
  }

  private void addEntry(ModifiableModuleModel moduleModel,
                        LibraryTable.ModifiableModel libraryModel,
                        ModifiableRootModel rootModel,
                        Container dependency,
                        DependencyScope scope) throws IllegalArgumentException {
    File file = dependency.getFile();
    String bsn = dependency.getBundleSymbolicName();
    String version = dependency.getVersion();

    String path = file.getPath();
    if (path.contains(": ")) {
      throw new IllegalArgumentException("Cannot resolve " + bsn + ":" + version + ": " + path);
    }

    if (JDK_DEPENDENCY.equals(bsn)) {
      String name = BND_LIB_PREFIX + bsn + ":" + version;
      if (FileUtil.isAncestor(myWorkspace.getBase(), file, true)) {
        name += "-" + myProject.getName();
      }
      ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
      Sdk jdk = jdkTable.findJdk(name);
      if (jdk == null) {
        jdk = jdkTable.createSdk(name, JavaSdk.getInstance());
        SdkModificator jdkModel = jdk.getSdkModificator();
        jdkModel.setHomePath(file.getParent());
        jdkModel.setVersionString(version);
        VirtualFile root = VirtualFileManager.getInstance().findFileByUrl(url(file));
        assert root != null : file + " " + file.exists();
        jdkModel.addRoot(root, OrderRootType.CLASSES);
        VirtualFile srcRoot = VirtualFileManager.getInstance().findFileByUrl(url(file) + SRC_ROOT);
        if (srcRoot != null) jdkModel.addRoot(srcRoot, OrderRootType.SOURCES);
        jdkModel.commitChanges();
        jdkTable.addJdk(jdk);
      }
      rootModel.setSdk(jdk);
      return;
    }

    ExportableOrderEntry entry;

    switch (dependency.getType()) {
      case PROJECT -> {
        String name = dependency.getProject().getName();
        Module module = moduleModel.findModuleByName(name);
        if (module == null) {
          throw new IllegalArgumentException("Unknown module '" + name + "'");
        }
        entry = (ModuleOrderEntry)ContainerUtil.find(
          rootModel.getOrderEntries(), e -> e instanceof ModuleOrderEntry && ((ModuleOrderEntry)e).getModuleName().equals(name));
        if (entry == null) {
          entry = rootModel.addModuleOrderEntry(module);
        }
      }
      case REPO -> {
        String name = BND_LIB_PREFIX + bsn + ":" + version;
        Library library = libraryModel.getLibraryByName(name);
        if (library == null) {
          library = libraryModel.createLibrary(name);
        }

        Library.ModifiableModel model = library.getModifiableModel();
        for (String url : model.getUrls(OrderRootType.CLASSES)) model.removeRoot(url, OrderRootType.CLASSES);
        for (String url : model.getUrls(OrderRootType.SOURCES)) model.removeRoot(url, OrderRootType.SOURCES);
        model.addRoot(url(file), OrderRootType.CLASSES);
        String srcRoot = mySourcesMap.get(path);
        if (srcRoot != null) {
          model.addRoot(url(file) + srcRoot, OrderRootType.SOURCES);
        }
        model.commit();

        entry = rootModel.addLibraryEntry(library);
      }
      case EXTERNAL -> {
        Library library = rootModel.getModuleLibraryTable().createLibrary(file.getName());
        Library.ModifiableModel model = library.getModifiableModel();
        model.addRoot(url(file), OrderRootType.CLASSES);
        String srcRoot = mySourcesMap.get(path);
        if (srcRoot != null) {
          model.addRoot(url(file) + srcRoot, OrderRootType.SOURCES);
        }
        model.commit();
        entry = rootModel.findLibraryOrderEntry(library);
        assert entry != null : library;
      }
      default -> throw new IllegalArgumentException("Unknown dependency '" + dependency + "' of type " + dependency.getType());
    }

    entry.setScope(scope);
  }

  private void checkErrors(Project project, Exception e) {
    if (!isUnitTestMode()) {
      LOG.warn(e);
      String text = message("bnd.import.resolve.error", project.getName(), e.getMessage());
      OsmorcBundle.bnd(message("bnd.import.error.title"), text, NotificationType.ERROR).notify(myProject);
    }
    else {
      throw new AssertionError(e);
    }
  }

  private void checkWarnings(Project project, List<String> warnings, boolean error) {
    if (warnings != null && !warnings.isEmpty()) {
      if (!isUnitTestMode()) {
        LOG.warn(warnings.toString());
        String text = message("bnd.import.warn.text", project.getName(), "<br>" + StringUtil.join(warnings, "<br>"));
        NotificationType type = error ? NotificationType.ERROR : NotificationType.WARNING;
        OsmorcBundle.bnd(message("bnd.import.warn.title"), text, type).notify(myProject);
      }
      else {
        throw new AssertionError(warnings.toString());
      }
    }
  }

  private static boolean booleanProperty(String value) {
    return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
  }

  private static String path(File file) {
    return FileUtil.toSystemIndependentName(file.getPath());
  }

  private static String url(File file) {
    return VfsUtil.getUrlForLibraryRoot(file);
  }

  public static @NotNull Collection<Project> getWorkspaceProjects(@NotNull Workspace workspace) {
    return ContainerUtil.filter(workspace.getAllProjects(), Conditions.notNull());
  }

  /**
   * Caches a workspace for methods below.
   */
  public static @Nullable Workspace findWorkspace(@NotNull com.intellij.openapi.project.Project project) {
    String basePath = project.getBasePath();
    if (basePath != null && Files.exists(Paths.get(basePath, CNF_DIR))) {
      try {
        Workspace ws = Workspace.getWorkspace(new File(basePath), CNF_DIR);
        BND_WORKSPACE_KEY.set(project, ws);
        return ws;
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }

    return null;
  }

  public static @Nullable Workspace getWorkspace(@Nullable com.intellij.openapi.project.Project project) {
    return project == null || project.isDefault() ? null : BND_WORKSPACE_KEY.get(project);
  }

  public static void reimportWorkspace(@NotNull com.intellij.openapi.project.Project project) {
    if (!isUnitTestMode()) {
      new Task.Backgroundable(project, message("bnd.reimport.task"), true) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          doReimportWorkspace(project, indicator);
        }
      }.queue();
    }
    else {
      doReimportWorkspace(project, null);
    }
  }

  private static void doReimportWorkspace(com.intellij.openapi.project.Project project, ProgressIndicator indicator) {
    Workspace workspace = getWorkspace(project);
    assert workspace != null : project;

    Collection<Project> projects;
    try {
      workspace.clear();
      workspace.forceRefresh();

      refreshRepositories(workspace, indicator);

      projects = getWorkspaceProjects(workspace);
      for (Project p : projects) {
        if (indicator != null) indicator.checkCanceled();
        p.clear();
        p.forceRefresh();
      }
    }
    catch (ProcessCanceledException e) { throw e; }
    catch (Exception e) {
      LOG.error("ws=" + workspace.getBase(), e);
      return;
    }

    Runnable task = () -> {
      BndProjectImporter importer = new BndProjectImporter(project, workspace, projects);
      importer.setupProject();
      importer.resolve(true);
    };
    if (!isUnitTestMode()) {
      ApplicationManager.getApplication().invokeLater(task, project.getDisposed());
    }
    else {
      task.run();
    }
  }

  public static void reimportProjects(@NotNull com.intellij.openapi.project.Project project, @NotNull Collection<String> projectDirs) {
    if (!isUnitTestMode()) {
      new Task.Backgroundable(project, message("bnd.reimport.task"), true) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          doReimportProjects(project, projectDirs, indicator);
        }
      }.queue();
    }
    else {
      doReimportProjects(project, projectDirs, null);
    }
  }

  private static void doReimportProjects(
    com.intellij.openapi.project.Project project,
    Collection<String> projectDirs,
    ProgressIndicator indicator
  ) {
    Workspace workspace = getWorkspace(project);
    assert workspace != null : project;

    Collection<Project> projects;
    try {
      refreshRepositories(workspace, indicator);

      projects = new ArrayList<>(projectDirs.size());
      for (String dir : projectDirs) {
        if (indicator != null) indicator.checkCanceled();
        Project p = workspace.getProject(PathUtil.getFileName(dir));
        if (p != null) {
          p.clear();
          p.forceRefresh();
          projects.add(p);
        }
      }
    }
    catch (ProcessCanceledException e) { throw e; }
    catch (Exception e) {
      LOG.error("ws=" + workspace.getBase() + " pr=" + projectDirs, e);
      return;
    }

    Runnable task = () -> new BndProjectImporter(project, workspace, projects).resolve(true);
    if (!isUnitTestMode()) {
      ApplicationManager.getApplication().invokeLater(task, project.getDisposed());
    }
    else {
      task.run();
    }
  }

  private static void refreshRepositories(Workspace workspace, ProgressIndicator indicator) {
    List<RepositoryPlugin> plugins = workspace.getPlugins(RepositoryPlugin.class);
    for (RepositoryPlugin plugin : plugins) {
      if (indicator != null) indicator.checkCanceled();
      if (plugin instanceof Refreshable) {
        try {
          ((Refreshable)plugin).refresh();
        }
        catch (Exception e) {
          LOG.warn(ObjectUtils.notNull(e.getMessage(), "NPE") + ", plugin=" + plugin);
          LOG.debug(e);
        }
      }
    }
  }
}
