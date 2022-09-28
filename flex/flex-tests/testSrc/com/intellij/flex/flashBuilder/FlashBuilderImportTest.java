// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.flashBuilder;

import com.intellij.flex.model.bc.ComponentSet;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.flashbuilder.*;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.ModifiableModelCommitter;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.testFramework.HeavyPlatformTestCase;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.zip.ZipOutputStream;

public class FlashBuilderImportTest extends HeavyPlatformTestCase {

  private static final String FB_PROJECT_DIR_NAME = "flash_builder_importer_test";

  protected VirtualFile myFlashBuilderProjectDir;

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
    WriteAction.run(() -> myFlashBuilderProjectDir = prepareFlashBuilderProjectDir());
  }

  private VirtualFile prepareFlashBuilderProjectDir() {
    final VirtualFile baseDir = getOrCreateProjectBaseDir();
    assert baseDir != null;
    final VirtualFile tempDataDir = baseDir.findChild(FB_PROJECT_DIR_NAME);
    if (tempDataDir != null) {
      tempDataDir.refresh(false, true);
      if (tempDataDir.exists()) {
        delete(tempDataDir);
      }
    }
    return createChildDirectory(baseDir, FB_PROJECT_DIR_NAME);
  }

  @Override
  protected void setUpModule() {
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      if (myModule != null) {
        final Sdk sdk = FlexUtils.getSdkForActiveBC(myModule);
        if (sdk != null) {
          ApplicationManager.getApplication().runWriteAction(() -> ProjectJdkTable.getInstance().removeJdk(sdk));
        }
      }

      if (myFlashBuilderProjectDir != null && myFlashBuilderProjectDir.exists()) {
        delete(myFlashBuilderProjectDir);

        myFlashBuilderProjectDir = null;
      }
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  private String getStandardDotProjectFileContent() {
    return "<projectDescription>\n" +
           "  <name>" + getTestName(true) + "</name>\n" +
           "</projectDescription>";
  }

  private FlexBuildConfiguration getBC() {
    return getBC(getTestName(true), 1);
  }

  private FlexBuildConfiguration getBC(final String name, int totalCount) {
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(myModule);
    assertEquals(totalCount, manager.getBuildConfigurations().length);
    final FlexBuildConfiguration bc = manager.findConfigurationByName(name);
    assertNotNull("Build configuration '" + name + "' not found", bc);
    return bc;
  }

  protected void importProject(final String dotActionScriptPropertiesFileContent) throws IOException, ConfigurationException {
    importProject(dotActionScriptPropertiesFileContent, null);
  }

  protected void importProject(final String dotActionScriptPropertiesFileContent, final @Nullable String flashBuilderWorkspacePath)
    throws IOException, ConfigurationException {
    importProject(dotActionScriptPropertiesFileContent, Collections.emptyList(), flashBuilderWorkspacePath);
  }

  protected void importProject(final String dotActionScriptPropertiesFileContent,
                               final Collection<String> otherProjectNames,
                               final @Nullable String flashBuilderWorkspacePath) throws IOException, ConfigurationException {
    importProject(getStandardDotProjectFileContent(), dotActionScriptPropertiesFileContent, otherProjectNames,
                  flashBuilderWorkspacePath);
  }

  protected void importProject(final String dotProjectFileContent,
                               final String dotActionScriptPropertiesFileContent,
                               final Collection<String> otherProjectNames,
                               final @Nullable String flashBuilderWorkspacePath) throws IOException, ConfigurationException {
    final VirtualFile dotProjectFile =
      addFileWithContent(FlashBuilderImporter.DOT_PROJECT, dotProjectFileContent, myFlashBuilderProjectDir);
    addFileWithContent(FlashBuilderImporter.DOT_ACTION_SCRIPT_PROPERTIES, dotActionScriptPropertiesFileContent, myFlashBuilderProjectDir);

    final FlashBuilderProject flashBuilderProject = FlashBuilderProjectLoadUtil.loadProject(dotProjectFile, false);
    final Collection<FlashBuilderProject> allFBProjects = new ArrayList<>();
    allFBProjects.add(flashBuilderProject);
    for (String name : otherProjectNames) {
      allFBProjects.add(FlashBuilderProjectLoadUtil.getDummyFBProject(name));
    }

    final String moduleFilePath =
      myFlashBuilderProjectDir.getPath() + "/" + flashBuilderProject.getName() + ModuleFileType.DOT_DEFAULT_EXTENSION;
    final ModifiableModuleModel moduleModel = ModuleManager.getInstance(myProject).getModifiableModel();
    myModule = moduleModel.newModule(moduleFilePath, FlexModuleType.getInstance().getId());

    final ModifiableRootModel rootModel = ModuleRootManager.getInstance(myModule).getModifiableModel();
    final FlashBuilderSdkFinder sdkFinder = new FlashBuilderSdkFinder(myProject, StringUtil.notNullize(flashBuilderWorkspacePath),
                                                                      Collections.singletonList(flashBuilderProject));
    final FlexProjectConfigurationEditor flexEditor =
      FlexProjectConfigurationEditor.createEditor(myProject, Collections.singletonMap(myModule, rootModel), null, null);
    new FlashBuilderModuleImporter(myProject, flexEditor, allFBProjects, sdkFinder).setupModule(rootModel, flashBuilderProject);
    flexEditor.commit();
    ApplicationManager.getApplication()
      .runWriteAction(() -> ModifiableModelCommitter.multiCommit(Collections.singletonList(rootModel), moduleModel));
  }

  protected static String getSomeAbsoluteFolderPath() {
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath("");
    assert file != null;
    return file.getPath();
  }

  protected void checkContentRoots(final String... contentRootUrls) {
    assertSameElements(ModuleRootManager.getInstance(myModule).getContentRootUrls(), contentRootUrls);
  }

  private void checkSourceRoots(final String... sourceRootUrls) {
    assertSameElements(ModuleRootManager.getInstance(myModule).getSourceRootUrls(), sourceRootUrls);
  }

  private void checkTestSourceRoots(final String... sourceRootUrls) {
    final Collection<String> roots = ContainerUtil.set(ModuleRootManager.getInstance(myModule).getSourceRootUrls(true));
    roots.removeAll(Arrays.asList(ModuleRootManager.getInstance(myModule).getSourceRootUrls(false)));
    assertSameElements(roots, Arrays.asList(sourceRootUrls));
  }

  private static void checkCompilerOutputUrl(final FlexBuildConfiguration bc, final String compilerOutputUrl) {
    assertEquals(compilerOutputUrl, VfsUtilCore.pathToUrl(bc.getOutputFolder()));
  }

  private void checkLibraries(final LibraryInfo[] libraryInfos) {
    final Collection<Library> libraries = new ArrayList<>();

    final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration();
    for (DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof SharedLibraryEntry) {
        fail("Unexpected " + ((SharedLibraryEntry)entry).getLibraryLevel() + "library: " + ((SharedLibraryEntry)entry).getLibraryName());
      }
      else if (entry instanceof ModuleLibraryEntry) {
        assertEquals(LinkageType.Merged, entry.getDependencyType().getLinkageType());
        libraries.add(findLibrary(((ModuleLibraryEntry)entry).getLibraryId()));
      }
    }

    assertEquals(libraryInfos.length, libraries.size());
    final Iterator<Library> libraryIterator = libraries.iterator();
    for (final LibraryInfo libraryInfo : libraryInfos) {
      final Library library = libraryIterator.next();
      assertNotNull(MessageFormat.format("Library ''{0}'' not found", libraryInfo.libraryName), library);
      if (libraryInfo.jarFolderPath != null) {
        assertTrue(
          MessageFormat.format("JAR directory ''{0}'' not set for library ''{1}''", libraryInfo.jarFolderPath, libraryInfo.libraryName),
          library.isJarDirectory(VfsUtilCore.pathToUrl(libraryInfo.jarFolderPath)));
      }
      if (libraryInfo.swcPath != null) {
        final String[] swcRoots = library.getRootProvider().getUrls(OrderRootType.CLASSES);
        assertEquals(1, swcRoots.length);
        assertEquals(VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, libraryInfo.swcPath) + JarFileSystem.JAR_SEPARATOR,
                     swcRoots[0]);
      }
    }
  }

  private Library findLibrary(final String libraryId) {
    for (OrderEntry orderEntry : ModuleRootManager.getInstance(myModule).getOrderEntries()) {
      if (orderEntry instanceof LibraryOrderEntry) {
        final Library library = ((LibraryOrderEntry)orderEntry).getLibrary();
        if (((LibraryEx)library).getKind() == FlexLibraryType.FLEX_LIBRARY) {
          if (libraryId.equals(FlexProjectRootsUtil.getLibraryId(library))) {
            return library;
          }
        }
      }
    }
    fail("Library with id=" + libraryId + " not found");
    return null;
  }

  protected static class LibraryInfo {
    final String libraryName;
    final String jarFolderPath;
    final String swcPath;

    public LibraryInfo(final String libraryName, final String jarFolderPath, final String swcPath) {
      this.libraryName = libraryName;
      this.jarFolderPath = jarFolderPath;
      this.swcPath = swcPath;
    }
  }

  public void testContentAndSourceRoots() throws Exception {
    final VirtualFile src3 = createChildDirectory(myFlashBuilderProjectDir, "src3");
    final VirtualFile flexUnitTestsDir = createChildDirectory(src3, "flexUnitTests");
    addFileWithContent("doesNotMatter.as", "", flexUnitTestsDir);
    addFileWithContent("BarTestSuite.as", "", src3);
    addFileWithContent("FooTest.as", "", src3);

    final String someAbsoluteFolderPath = getSomeAbsoluteFolderPath();
    final String dotProjectFileContent = "<projectDescription>\n" +
                                         "  <name>" + getTestName(true) + "</name>\n" +
                                         "  <linkedResources>" +
                                         "    <link>" +
                                         "      <name>LINKED_RESOURCE_1</name>" +
                                         "      <location>" + someAbsoluteFolderPath + "</location>" +
                                         "    </link>" +
                                         "  </linkedResources>" +
                                         "</projectDescription>";
    final String dotActionScriptPropertiesFileContent =
      "<actionScriptProperties>\n" +
      "  <compiler sourceFolderPath='src1'>\n" +
      "    <compilerSourcePath>\n" +
      "      <compilerSourcePathEntry path='subdir\\src2'/>\n" +
      "      <compilerSourcePathEntry path='LINKED_RESOURCE_1\\other1'/>\n" +
      "      <compilerSourcePathEntry path='" + myFlashBuilderProjectDir.getPath() + "\\src3" + "'/>\n" +
      "    </compilerSourcePath>\n" +
      "  </compiler>\n" +
      "</actionScriptProperties>";
    importProject(dotProjectFileContent,
                  dotActionScriptPropertiesFileContent,
                  Collections.emptyList(), null);
    final String contentRootUrl = myFlashBuilderProjectDir.getUrl();
    checkContentRoots(contentRootUrl, VfsUtilCore.pathToUrl(someAbsoluteFolderPath + "/other1"));
    checkSourceRoots(contentRootUrl + "/src1", contentRootUrl + "/subdir/src2", VfsUtilCore.pathToUrl(someAbsoluteFolderPath + "/other1"),
                     VfsUtilCore.pathToUrl(myFlashBuilderProjectDir.getPath() + "/src3"));
    checkTestSourceRoots(VfsUtilCore.pathToUrl(myFlashBuilderProjectDir.getPath() + "/src3"));
  }

  public void testRelativeOutputPath() throws Exception {
    importProject("""
                    <actionScriptProperties>
                      <compiler outputFolderPath='subdir/out'/>
                    </actionScriptProperties>""");
    checkCompilerOutputUrl(getBC(),
                           myFlashBuilderProjectDir.getUrl() + "/subdir/out");
  }

  public void testAbsoluteOutputPath() throws Exception {
    final String someAbsolutePath = getSomeAbsoluteFolderPath();
    importProject("<actionScriptProperties>\n" +
                  "  <compiler outputFolderLocation='" + someAbsolutePath + "' outputFolderPath='does not matter'/>\n" +
                  "</actionScriptProperties>");
    checkCompilerOutputUrl(getBC(), VfsUtilCore.pathToUrl(someAbsolutePath));
  }

  public void testWebFlexApp() throws Exception {
    addFileWithContent("FlexApp.mxml", "", myFlashBuilderProjectDir);
    addFileWithContent(FlashBuilderImporter.DOT_FLEX_PROPERTIES, "", myFlashBuilderProjectDir);
    importProject("""
                    <actionScriptProperties mainApplicationPath='FlexApp.mxml'>
                      <compiler additionalCompilerArguments='-locale en_US&#10;-other' outputFolderPath='bin-debug' targetPlayerVersion='0.0.0'/>
                    </actionScriptProperties>""");
    final FlexBuildConfiguration bc = getBC("FlexApp", 1);
    assertEquals(TargetPlatform.Web, bc.getTargetPlatform());
    assertFalse(bc.isPureAs());
    assertEquals(OutputType.Application, bc.getOutputType());
    assertEquals("FlexApp", bc.getMainClass());
    assertEquals("FlexApp.swf", bc.getOutputFileName());
    assertEquals(myFlashBuilderProjectDir.getPath() + "/bin-debug", bc.getOutputFolder());
    assertFalse(bc.isSkipCompile());

    assertEquals("", bc.getDependencies().getTargetPlayer());
    assertEquals(ComponentSet.SparkAndMx, bc.getDependencies().getComponentSet());
    assertEquals(LinkageType.Default, bc.getDependencies().getFrameworkLinkage());
    assertNull(bc.getSdk());
    assertEmpty(bc.getDependencies().getEntries());

    assertEquals(1, bc.getCompilerOptions().getAllOptions().size());
    assertEquals("", bc.getCompilerOptions().getAdditionalConfigFilePath());
    assertEquals("-other", bc.getCompilerOptions().getAdditionalOptions());
    assertEquals("en_US", bc.getCompilerOptions().getOption("compiler.locale"));

    // no source roots specified but main class exists => implicit source root is equal to content root
    final String contentRootUrl = myFlashBuilderProjectDir.getUrl();
    checkContentRoots(contentRootUrl);
    checkSourceRoots(contentRootUrl);
  }

  public void testWebFlexLib() throws Exception {
    final String dotFlexLibPropertiesContent = "<flexLibProperties>" +
                                               "  <includeResources>" +
                                               "    <resourceEntry destPath='foo/a.txt' sourcePath='foo/a.txt'/>" +
                                               "  </includeResources>" +
                                               "  <namespaceManifests>" +
                                               "    <namespaceManifestEntry manifest=\"path\\manifest.xml\" namespace=\"http://MyNamespace\"/>" +
                                               "  </namespaceManifests>" +
                                               "</flexLibProperties>";
    addFileWithContent(FlashBuilderImporter.DOT_FLEX_LIB_PROPERTIES, dotFlexLibPropertiesContent, myFlashBuilderProjectDir);
    final VirtualFile dir = VfsUtil.createDirectories(myFlashBuilderProjectDir.getPath() + "/src/foo");
    addFileWithContent("a.txt", "", dir);

    importProject("""
                    <actionScriptProperties mainApplicationPath='does not matter'>
                      <compiler outputFolderPath='bin' targetPlayerVersion='9.0.124' sourceFolderPath="src"/>
                    </actionScriptProperties>""");
    final String fbProjectName = getTestName(true);
    final FlexBuildConfiguration bc = getBC();
    assertEquals(TargetPlatform.Web, bc.getTargetPlatform());
    assertFalse(bc.isPureAs());
    assertEquals(OutputType.Library, bc.getOutputType());
    assertEquals("", bc.getMainClass());
    assertEquals(fbProjectName + ".swc", bc.getOutputFileName());
    assertEquals(myFlashBuilderProjectDir.getPath() + "/bin", bc.getOutputFolder());

    checkContentRoots(myFlashBuilderProjectDir.getUrl());
    checkSourceRoots(myFlashBuilderProjectDir.getUrl() + "/src");

    assertEquals(2, bc.getCompilerOptions().getAllOptions().size());
    assertEquals("", bc.getCompilerOptions().getOption("compiler.locale"));
    final String nsValue = bc.getCompilerOptions().getOption("compiler.namespaces.namespace");
    assertEquals("http://MyNamespace\t" + myFlashBuilderProjectDir.getPath() + "/path/manifest.xml", nsValue);
    assertSameElements(bc.getCompilerOptions().getFilesToIncludeInSWC(), myFlashBuilderProjectDir.getPath() + "/src/foo/a.txt");
  }

  public void testWebASLib() {
    final String flexLibPropertiesFileContent = "<flexLibProperties useMultiPlatformConfig='false'/>";
    final String actionScriptPropertiesFileContent = """
      <actionScriptProperties mainApplicationPath='does not matter'>
        <compiler outputFolderPath='bin' useFlashSDK='true' useApolloConfig='false'/>
      </actionScriptProperties>""";
    final TargetPlatform expectedTargetPlatform = TargetPlatform.Web;

    commonASLibTest(flexLibPropertiesFileContent, actionScriptPropertiesFileContent, expectedTargetPlatform);
  }

  public void testDesktopASLib() {
    final String flexLibPropertiesFileContent = "<flexLibProperties useMultiPlatformConfig='false'/>";
    final String actionScriptPropertiesFileContent = """
      <actionScriptProperties mainApplicationPath='does not matter'>
        <compiler outputFolderPath='bin' useFlashSDK='true' useApolloConfig='true'/>
      </actionScriptProperties>""";
    final TargetPlatform expectedTargetPlatform = TargetPlatform.Desktop;

    commonASLibTest(flexLibPropertiesFileContent, actionScriptPropertiesFileContent, expectedTargetPlatform);
  }

  // not sure that mobile AS libs are supported by FB, but if they are - should be like this
  public void testMobileASLib() {
    final String flexLibPropertiesFileContent = "<flexLibProperties useMultiPlatformConfig='true'/>";
    final String actionScriptPropertiesFileContent = """
      <actionScriptProperties mainApplicationPath='does not matter'>
        <compiler outputFolderPath='bin' useFlashSDK='true' useApolloConfig='true'/>
      </actionScriptProperties>""";
    final TargetPlatform expectedTargetPlatform = TargetPlatform.Mobile;

    commonASLibTest(flexLibPropertiesFileContent, actionScriptPropertiesFileContent, expectedTargetPlatform);
  }

  private void commonASLibTest(final String flexLibPropertiesFileContent,
                               final String actionScriptPropertiesFileContent,
                               final TargetPlatform expectedTargetPlatform) {
    ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        addFileWithContent(FlashBuilderImporter.DOT_FLEX_LIB_PROPERTIES, flexLibPropertiesFileContent, myFlashBuilderProjectDir);
        importProject(actionScriptPropertiesFileContent);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    final String fbProjectName = getTestName(true);
    final FlexBuildConfiguration bc = getBC();
    assertEquals(expectedTargetPlatform, bc.getTargetPlatform());
    assertTrue(bc.isPureAs());
    assertEquals(OutputType.Library, bc.getOutputType());
    assertEquals("", bc.getMainClass());
    assertEquals(fbProjectName + ".swc", bc.getOutputFileName());
    assertEquals(myFlashBuilderProjectDir.getPath() + "/bin", bc.getOutputFolder());

    checkContentRoots(myFlashBuilderProjectDir.getUrl());
    checkSourceRoots();

    assertEquals(1, bc.getCompilerOptions().getAllOptions().size());
    assertEquals("", bc.getCompilerOptions().getOption("compiler.locale"));
  }

  public void testLibrariesCreated() throws Exception {
    final VirtualFile subdir = createChildDirectory(myFlashBuilderProjectDir, "subdir");
    final VirtualFile swc = createEmptySwc(myFlashBuilderProjectDir, "somelib.swc");
    importProject("<actionScriptProperties>\n" +
                  "  <compiler>\n" +
                  "    <libraryPath>\n" +
                  "      <libraryPathEntry kind='4' path='does not matter'/>\n" +
                  "      <libraryPathEntry kind='1' path='" + subdir.getPath() + "'/>\n" +
                  "      <libraryPathEntry kind='1' path='relative/path'/>\n" +
                  "      <libraryPathEntry kind='3' path='relative.swc'/>\n" +
                  "      <libraryPathEntry kind='3' path='" + swc.getPath() + "'/>\n" +
                  "    </libraryPath>" +
                  "  </compiler>\n" +
                  "</actionScriptProperties>");

    final String rootDirPath = myFlashBuilderProjectDir.getPath();
    checkLibraries(
      new LibraryInfo[]{
        new LibraryInfo(null, subdir.getPath(), null),
        new LibraryInfo(null, rootDirPath + "/relative/path", null),
        new LibraryInfo(null, null, rootDirPath + "/relative.swc"),
        new LibraryInfo(null, null, swc.getPath())
      });

    final long maxTime = System.currentTimeMillis() + 10000;
    while (!FileUtil.delete(new File(swc.getPath())) && System.currentTimeMillis() < maxTime) {
      TimeoutUtil.sleep(10);
    }
  }

  private static VirtualFile createEmptySwc(VirtualFile dir, String name) throws IOException {
    final File jarFile = new File(dir.getPath(), name);
    new ZipOutputStream(new FileOutputStream(jarFile)).close();
    return ApplicationManager.getApplication().runWriteAction(
      (ThrowableComputable<VirtualFile, IOException>)() -> FlexUtils.addFileWithContent(name, FileUtil.loadFileBytes(jarFile), dir));
  }

  public void testBcDependencies() throws Exception {
    importProject("""
                    <actionScriptProperties>
                      <compiler>
                        <libraryPath>
                          <libraryPathEntry kind='3' path='/Another Module/bin/some.swc'/>
                        </libraryPath>  </compiler>
                    </actionScriptProperties>""", Arrays.asList("Another Module"), null);

    final DependencyEntry[] entries = getBC().getDependencies().getEntries();
    assertEquals(1, entries.length);
    assertInstanceOf(entries[0], BuildConfigurationEntry.class);
    assertEquals("Another Module", ((BuildConfigurationEntry)entries[0]).getBcName());
    assertEquals("Another Module", ((BuildConfigurationEntry)entries[0]).getModuleName());
    assertEquals(LinkageType.Merged, (entries[0]).getDependencyType().getLinkageType());

    checkLibraries(new LibraryInfo[]{});
  }

  private void prepareSdkTest() throws IOException {
    final VirtualFile settingsDir =
      VfsUtil.createDirectories(
        myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace/.metadata/.plugins/org.eclipse.core.runtime/.settings");
    final VirtualFile customSdkRoot = createChildDirectory(myFlashBuilderProjectDir, "custom_sdk");
    final VirtualFile themesDir = VfsUtil.createDirectories(customSdkRoot.getPath() + "/frameworks/themes");
    final VirtualFile haloDir = createChildDirectory(themesDir, "Halo");
    createEmptySwc(haloDir, "halo.swc");
    final VirtualFile aeonDir = createChildDirectory(themesDir, "AeonGraphical");
    addFileWithContent("AeonGraphical.css", "", aeonDir);
    final VirtualFile wireframeDir = createChildDirectory(themesDir, "Wireframe");
    createEmptySwc(wireframeDir, "wireframe.swc");

    final VirtualFile defaultSdkRoot = createChildDirectory(myFlashBuilderProjectDir, "default_sdk");
    final String settingsContentBase = "com.adobe.flexbuilder.project.flex_sdks=" +
                                       "<sdks>\\r\\n" +
                                       "  <sdk location\\=\"{0}\" name\\=\"custom sdk name\"/>\\r\\n" +
                                       "  <sdk location\\=\"{1}\" defaultSDK\\=\"true\"/>\\r\\n" +
                                       "</sdks>\\r\\n";
    final String settingsContent = MessageFormat.format(settingsContentBase, customSdkRoot.getPath(), defaultSdkRoot.getPath());
    addFileWithContent("com.adobe.flexbuilder.project.prefs", settingsContent, settingsDir);
  }

  public void testDesktopFlexLibWithDefaultSdk() throws Exception {
    prepareSdkTest();
    addFileWithContent(FlashBuilderImporter.DOT_FLEX_LIB_PROPERTIES, "", myFlashBuilderProjectDir);
    importProject("""
                    <actionScriptProperties>
                      <compiler useApolloConfig='true'>
                        <libraryPath>
                          <libraryPathEntry kind='4' path='does not matter'/>
                        </libraryPath>  </compiler>
                    </actionScriptProperties>""",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final FlexBuildConfiguration bc = getBC();
    assertEquals(TargetPlatform.Desktop, bc.getTargetPlatform());
    assertFalse(bc.isPureAs());
    assertEquals(OutputType.Library, bc.getOutputType());

    final Sdk flexSdk = bc.getSdk();
    assertNotNull(flexSdk);
    assertInstanceOf(flexSdk.getSdkType(), FlexSdkType2.class);
    assertEquals(myFlashBuilderProjectDir.getPath() + "/default_sdk", flexSdk.getHomePath());
  }

  public void testMobileFlexLib() throws Exception {
    final String dotFlexLibPropertiesContent = "<flexLibProperties useMultiPlatformConfig=\"true\">" +
                                               "</flexLibProperties>";
    addFileWithContent(FlashBuilderImporter.DOT_FLEX_LIB_PROPERTIES, dotFlexLibPropertiesContent, myFlashBuilderProjectDir);
    importProject("""
                    <actionScriptProperties>
                      <compiler additionalCompilerArguments='-other -locale=en_US,ja_JP' useApolloConfig='true'>
                        <libraryPath>
                          <libraryPathEntry kind='4' path='does not matter'/>
                        </libraryPath>  </compiler>
                    </actionScriptProperties>""",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final FlexBuildConfiguration bc = getBC();
    assertEquals(TargetPlatform.Mobile, bc.getTargetPlatform());
    assertFalse(bc.isPureAs());
    assertEquals(OutputType.Library, bc.getOutputType());

    assertEquals(1, bc.getCompilerOptions().getAllOptions().size());
    assertEquals("en_US\nja_JP", bc.getCompilerOptions().getOption("compiler.locale"));
  }

  public void testMobileASAndroidApp() throws Exception {
    prepareSdkTest();
    importProject("""
                    <actionScriptProperties>
                      <compiler useApolloConfig='true'>
                        <libraryPath>
                          <libraryPathEntry kind='4' path='does not matter'/>
                        </libraryPath>  </compiler>
                      <buildTargets>
                        <buildTarget buildTargetName="com.adobe.flexide.multiplatform.android.platform">
                          <airSettings airCertificatePath="android_cert.p12"/>      <multiPlatformSettings enabled="true"/>    </buildTarget>
                        <buildTarget buildTargetName="default">
                          <airSettings airCertificatePath="not_applicable"/>    </buildTarget>
                        <buildTarget buildTargetName="com.adobe.flexide.multiplatform.ios.platform"                  provisioningFile="ios.mobileprovision">      <airSettings airCertificatePath="ios_cert.p12"/>      <multiPlatformSettings enabled="false"/>    </buildTarget>
                      </buildTargets>
                    </actionScriptProperties>""",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final FlexBuildConfiguration bc = getBC();
    assertEquals(TargetPlatform.Mobile, bc.getTargetPlatform());
    assertTrue(bc.isPureAs());
    assertEquals(OutputType.Application, bc.getOutputType());
    assertTrue(bc.getAndroidPackagingOptions().isEnabled());
    assertFalse(bc.getAndroidPackagingOptions().getSigningOptions().isUseTempCertificate());
    assertEquals("android_cert.p12", bc.getAndroidPackagingOptions().getSigningOptions().getKeystorePath());

    assertFalse(bc.getIosPackagingOptions().isEnabled());
    assertEquals("ios.mobileprovision", bc.getIosPackagingOptions().getSigningOptions().getProvisioningProfilePath());
    assertEquals("ios_cert.p12", bc.getIosPackagingOptions().getSigningOptions().getKeystorePath());

    final Sdk flexSdk = bc.getSdk();
    assertNotNull(flexSdk);
    assertInstanceOf(flexSdk.getSdkType(), FlexSdkType2.class);
    assertEquals(myFlashBuilderProjectDir.getPath() + "/default_sdk", flexSdk.getHomePath());
  }

  public void testMobileFlexIOSApp() throws Exception {
    prepareSdkTest();
    addFileWithContent(FlashBuilderImporter.DOT_FLEX_PROPERTIES, "", myFlashBuilderProjectDir);
    importProject("""
                    <actionScriptProperties>
                      <compiler useApolloConfig='true'>
                        <libraryPath>
                          <libraryPathEntry kind='4' path='does not matter'/>
                        </libraryPath>  </compiler>
                      <buildTargets>
                        <buildTarget buildTargetName="com.adobe.flexide.multiplatform.android.platform">
                          <multiPlatformSettings enabled="false"/>    </buildTarget>
                        <buildTarget buildTargetName="com.adobe.flexide.multiplatform.ios.platform">
                          <multiPlatformSettings enabled="true"/>    </buildTarget>
                      </buildTargets>
                    </actionScriptProperties>""",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final FlexBuildConfiguration bc = getBC();
    assertEquals(TargetPlatform.Mobile, bc.getTargetPlatform());
    assertFalse(bc.isPureAs());
    assertEquals(OutputType.Application, bc.getOutputType());
    assertFalse(bc.getAndroidPackagingOptions().isEnabled());
    assertTrue(bc.getIosPackagingOptions().isEnabled());

    final Sdk flexSdk = bc.getSdk();
    assertNotNull(flexSdk);
    assertInstanceOf(flexSdk.getSdkType(), FlexSdkType2.class);
    assertEquals(myFlashBuilderProjectDir.getPath() + "/default_sdk", flexSdk.getHomePath());
  }

  public void testMobileASAppBoth() throws Exception {
    importProject("""
                    <actionScriptProperties>
                      <compiler/>  <buildTargets>
                        <buildTarget buildTargetName='device' platformId="com.adobe.flexide.multiplatform.android.platform">
                          <multiPlatformSettings enabled="true"/>    </buildTarget>
                        <buildTarget buildTargetName="device">
                          <multiPlatformSettings enabled="true" platformID="com.adobe.flexide.multiplatform.ios.platform"/>    </buildTarget>
                      </buildTargets>
                    </actionScriptProperties>""",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final FlexBuildConfiguration bc = getBC();
    assertEquals(TargetPlatform.Mobile, bc.getTargetPlatform());
    assertTrue(bc.isPureAs());
    assertEquals(OutputType.Application, bc.getOutputType());
    assertTrue(bc.getAndroidPackagingOptions().isEnabled());
    assertTrue(bc.getIosPackagingOptions().isEnabled());
  }

  public void testDesktopASAppWithCustomSdk() throws Exception {
    prepareSdkTest();
    importProject("""
                    <actionScriptProperties>
                      <compiler useApolloConfig='true' flexSDK='custom sdk name'>
                        <libraryPath>
                          <libraryPathEntry kind='4'/>
                        </libraryPath>  </compiler>
                      <buildTargets>
                        <buildTarget buildTargetName="default">
                          <airSettings airCertificatePath="desktop.p12"/>    </buildTarget>
                      </buildTargets>
                    </actionScriptProperties>""",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final FlexBuildConfiguration bc = getBC();
    assertEquals(TargetPlatform.Desktop, bc.getTargetPlatform());
    assertTrue(bc.isPureAs());
    assertEquals(OutputType.Application, bc.getOutputType());
    assertFalse(bc.getAirDesktopPackagingOptions().getSigningOptions().isUseTempCertificate());
    assertEquals("desktop.p12", bc.getAirDesktopPackagingOptions().getSigningOptions().getKeystorePath());

    final Sdk flexSdk = bc.getSdk();
    assertNotNull(flexSdk);
    assertInstanceOf(flexSdk.getSdkType(), FlexSdkType2.class);
    assertEquals(myFlashBuilderProjectDir.getPath() + "/custom_sdk", flexSdk.getHomePath());
  }

  public void testDesktopFlexAppWithoutSdk() throws Exception {
    prepareSdkTest();
    addFileWithContent(FlashBuilderImporter.DOT_FLEX_PROPERTIES, "", myFlashBuilderProjectDir);

    final VirtualFile srcDir = createChildDirectory(myFlashBuilderProjectDir, "src");
    final VirtualFile locale1Dir = createChildDirectory(srcDir, "locale");
    createChildDirectory(locale1Dir, "en_US");
    final VirtualFile locale2 = createChildDirectory(myFlashBuilderProjectDir, "locale");
    createChildDirectory(locale2, "ja_JP");

    importProject("""
                    <actionScriptProperties>
                      <compiler useApolloConfig='true' flexSDK='custom sdk name' sourceFolderPath="src"            additionalCompilerArguments='-locale en_US ja_JP -source-path locale/{locale} -allow-source-path-overlap true' />
                    </actionScriptProperties>""",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_installation");

    final FlexBuildConfiguration bc = getBC();
    assertEquals(TargetPlatform.Desktop, bc.getTargetPlatform());
    assertFalse(bc.isPureAs());
    assertEquals(OutputType.Application, bc.getOutputType());
    assertNull(bc.getSdk());
    assertEquals("-allow-source-path-overlap true", bc.getCompilerOptions().getAdditionalOptions());
    assertEquals(1, bc.getCompilerOptions().getAllOptions().size());
    assertEquals("en_US\nja_JP", bc.getCompilerOptions().getOption("compiler.locale"));
    final String contentRootUrl = myFlashBuilderProjectDir.getUrl();
    checkSourceRoots(contentRootUrl + "/src", contentRootUrl + "/src/locale/en_US", contentRootUrl + "/locale/ja_JP");
  }

  public void testPathVariables() throws Exception {
    final PathMacros pathMacros = PathMacros.getInstance();
    if (pathMacros.getValue("FLASH_BUILDER_PATH_VARIABLE") != null) {
      pathMacros.setMacro("FLASH_BUILDER_PATH_VARIABLE", null);
    }

    final VirtualFile settingsDir = VfsUtil.createDirectories(
      myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace/.metadata/.plugins/org.eclipse.core.runtime/.settings");

    final String someAbsoluteFolderPath = getSomeAbsoluteFolderPath();
    final String content = "#Fri Sep 10 19:11:24 MSD 2010\n" +
                           "eclipse.preferences.version=1\n" +
                           "pathvariable.FLASH_BUILDER_PATH_VARIABLE=" + someAbsoluteFolderPath.replace(":", "\\:") + "\n";
    addFileWithContent("org.eclipse.core.resources.prefs", content, settingsDir);
    addFileWithContent("com.adobe.flexbuilder.project.prefs", content, settingsDir);

    importProject(getStandardDotProjectFileContent(),
                  """
                    <actionScriptProperties>
                      <compiler additionalCompilerArguments='-some -locale  en_US  ja_JP  -other '>
                        <compilerSourcePath>
                          <compilerSourcePathEntry kind="1" path="${FLASH_BUILDER_PATH_VARIABLE}/src5"/>
                          <compilerSourcePathEntry kind="1" path="locales\\{locale}"/>
                        </compilerSourcePath>    <libraryPath defaultLinkType="0">
                          <libraryPathEntry kind="3" linkType="1" path="${FLASH_BUILDER_PATH_VARIABLE}/somelib/somelib.swc" useDefaultLinkType="false"/>
                        </libraryPath>  </compiler>
                    </actionScriptProperties>""",
                  new ArrayList<>(),
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final String contentRootUrl = myFlashBuilderProjectDir.getUrl();
    checkContentRoots(contentRootUrl, VfsUtilCore.pathToUrl(someAbsoluteFolderPath + "/src5"));
    checkSourceRoots(VfsUtilCore.pathToUrl(someAbsoluteFolderPath + "/src5"), contentRootUrl + "/locales/en_US",
                     contentRootUrl + "/locales/ja_JP");
    checkLibraries(new LibraryInfo[]{new LibraryInfo("somelib", null, someAbsoluteFolderPath + "/somelib/somelib.swc")});

    final FlexBuildConfiguration bc = getBC();
    assertEquals("-some -other ", bc.getCompilerOptions().getAdditionalOptions());
    assertEquals(1, bc.getCompilerOptions().getAllOptions().size());
    assertEquals("en_US\nja_JP", bc.getCompilerOptions().getOption("compiler.locale"));

    assertEquals(someAbsoluteFolderPath, pathMacros.getValue("FLASH_BUILDER_PATH_VARIABLE"));
    pathMacros.setMacro("FLASH_BUILDER_PATH_VARIABLE", null);
  }

  private static VirtualFile addFileWithContent(String name, String content, VirtualFile settingsDir) throws IOException {
    return ApplicationManager.getApplication().runWriteAction(
      (ThrowableComputable<VirtualFile, IOException>)() -> FlexUtils.addFileWithContent(name, content, settingsDir));
  }

  public void testWebASAppsAndModules() throws Exception {
    importProject("""
                    <actionScriptProperties mainApplicationPath='pack/App1.mxml'>
                      <compiler outputFolderPath='bin-debug' sourceFolderPath='src'/>
                      <applications>
                        <application path='pack/App1.mxml'/>
                        <application path="FlexUnitCompilerApplication.as"/>
                        <application path="FlexUnitApplication.as"/>
                        <application path='App2.mxml'/>
                      </applications>
                      <modules>
                        <module application='src/pack/App1.mxml' destPath='modules/Module1.swf' optimize='true' sourcePath='src/modules/Module1.mxml'/>
                        <module application='src/App2.mxml' destPath='Module2.swf' optimize='true' sourcePath='src/Module2.mxml'/>
                        <module application='src/pack/App1.mxml' destPath='pack/ModuleNoOpt.swf' optimize='false' sourcePath='src/pack/ModuleNoOpt.as'/>
                      </modules>
                      <buildCSSFiles>
                        <buildCSSFileEntry sourcePath='in_project_root.css'/>
                        <buildCSSFileEntry sourcePath='src/pack/css_in_pack.css'/>
                      </buildCSSFiles>
                    </actionScriptProperties>""");
    final String path = myFlashBuilderProjectDir.getPath();

    final FlexBuildConfiguration bc1 = getBC("App1", 2);
    assertEquals(TargetPlatform.Web, bc1.getTargetPlatform());
    assertTrue(bc1.isPureAs());
    assertEquals(OutputType.Application, bc1.getOutputType());
    assertEquals("pack.App1", bc1.getMainClass());
    assertEquals("App1.swf", bc1.getOutputFileName());
    assertEquals(path + "/bin-debug", bc1.getOutputFolder());
    final List<String> expectedCssPaths = Arrays.asList(path + "/in_project_root.css", path + "/src/pack/css_in_pack.css");
    assertSameElements(bc1.getCssFilesToCompile(), expectedCssPaths);

    final Collection<FlexBuildConfiguration.RLMInfo> rlms1 = bc1.getRLMs();
    assertEquals(2, rlms1.size());
    final Iterator<FlexBuildConfiguration.RLMInfo> iterator1 = rlms1.iterator();
    final FlexBuildConfiguration.RLMInfo rlm11 = iterator1.next();
    assertEquals("modules.Module1", rlm11.MAIN_CLASS);
    assertEquals("modules/Module1.swf", rlm11.OUTPUT_FILE);
    assertTrue(rlm11.OPTIMIZE);
    final FlexBuildConfiguration.RLMInfo rlm12 = iterator1.next();
    assertEquals("pack.ModuleNoOpt", rlm12.MAIN_CLASS);
    assertEquals("pack/ModuleNoOpt.swf", rlm12.OUTPUT_FILE);
    assertFalse(rlm12.OPTIMIZE);

    final DependencyEntry[] entries1 = bc1.getDependencies().getEntries();
    assertEquals(0, entries1.length);

    final FlexBuildConfiguration bc2 = getBC("App2", 2);
    assertEquals(TargetPlatform.Web, bc2.getTargetPlatform());
    assertTrue(bc2.isPureAs());
    assertEquals(OutputType.Application, bc2.getOutputType());
    assertEquals("App2", bc2.getMainClass());
    assertEquals("App2.swf", bc2.getOutputFileName());
    assertEquals(path + "/bin-debug", bc2.getOutputFolder());
    assertSameElements(bc2.getCssFilesToCompile(), expectedCssPaths);

    final Collection<FlexBuildConfiguration.RLMInfo> rlms2 = bc2.getRLMs();
    assertEquals(1, rlms2.size());
    final FlexBuildConfiguration.RLMInfo rlm21 = rlms2.iterator().next();
    assertEquals("Module2", rlm21.MAIN_CLASS);
    assertEquals("Module2.swf", rlm21.OUTPUT_FILE);
    assertTrue(rlm21.OPTIMIZE);

    final DependencyEntry[] entries2 = bc2.getDependencies().getEntries();
    assertEquals(0, entries2.length);
  }

  public void testFilesToPackage() throws Exception {
    createChildDirectory(myFlashBuilderProjectDir, "src");

    final VirtualFile src2 = createChildDirectory(myFlashBuilderProjectDir, "src2");
    addFileWithContent("Main.as", "", src2); // source file - not included in package
    addFileWithContent("Main-app.xml", "", src2); // descriptor - not included
    addFileWithContent("excluded1.xml", "", src2); // excluded in .actionScriptProperties
    addFileWithContent("Main.xml", "", src2); // included

    final VirtualFile sub1 = createChildDirectory(src2, "sub1");
    addFileWithContent("Foo.mxml", "", sub1); // source file - not included
    addFileWithContent("bar.properties", "", sub1); // properties file - not included
    addFileWithContent("excluded2.txt", "", sub1); // excluded in .actionScriptProperties

    final VirtualFile excludedFolder = createChildDirectory(sub1, "excluded_folder"); // excluded in .actionScriptProperties
    addFileWithContent("x.jpeg", "", excludedFolder);

    final VirtualFile sub2 = createChildDirectory(sub1, "sub2");
    createChildDirectory(sub2, ".svn");
    addFileWithContent("a.png", "", sub2); // included

    final VirtualFile sub3 = createChildDirectory(sub2, "sub3"); // included fully
    addFileWithContent("b.js", "", sub3);
    final VirtualFile sub4 = createChildDirectory(sub3, "sub4");
    addFileWithContent("c.html", "", sub4);

    final VirtualFile src3 = createChildDirectory(myFlashBuilderProjectDir, "src3"); // fully included
    final VirtualFile fooDir = createChildDirectory(src3, "foo");
    addFileWithContent("bar.txt", "", fooDir);

    importProject("""
                    <actionScriptProperties mainApplicationPath="Main.as">
                      <compiler sourceFolderPath="src">    <compilerSourcePath>
                          <compilerSourcePathEntry kind="1" linkType="1" path="src2"/>
                          <compilerSourcePathEntry kind="1" linkType="1" path="src3"/>
                        </compilerSourcePath>
                      </compiler>  <buildTargets>
                        <buildTarget buildTargetName='device' platformId="com.adobe.flexide.multiplatform.android.platform">
                          <multiPlatformSettings enabled="true"/>      <airSettings airCertificatePath="" airTimestamp="true" anePathSet="true" version="1">
                            <airExcludes>
                              <pathEntry path="excluded1.xml"/>
                              <pathEntry path="sub1/excluded2.txt"/>
                              <pathEntry path="sub1/excluded_folder"/>
                            </airExcludes>
                          </airSettings>
                        </buildTarget>
                        <buildTarget buildTargetName="device">
                          <multiPlatformSettings enabled="true" platformID="com.adobe.flexide.multiplatform.ios.platform"/>    </buildTarget>
                      </buildTargets>
                    </actionScriptProperties>""",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final FlexBuildConfiguration bc = getBC("Main", 1);

    final List<AirPackagingOptions.FilePathAndPathInPackage> expectedAndroid =
      getExpectedPaths("src",
                       "src2/Main.xml",
                       "src2/sub1/sub2/a.png",
                       "src2/sub1/sub2/sub3",
                       "src3");

    final List<AirPackagingOptions.FilePathAndPathInPackage> expectedIos =
      getExpectedPaths("src",
                       "src2/Main.xml",
                       "src2/excluded1.xml",
                       "src2/sub1/excluded2.txt",
                       "src2/sub1/excluded_folder",
                       "src2/sub1/sub2/a.png",
                       "src2/sub1/sub2/sub3",
                       "src3");

    assertSameElements(bc.getAndroidPackagingOptions().getFilesToPackage(), expectedAndroid);
    assertSameElements(bc.getIosPackagingOptions().getFilesToPackage(), expectedIos);
  }

  private List<AirPackagingOptions.FilePathAndPathInPackage> getExpectedPaths(final String... relPaths) {
    final List<AirPackagingOptions.FilePathAndPathInPackage> expected = new ArrayList<>();
    for (String relPath : relPaths) {
      final int slashIndex = relPath.indexOf("/");
      expected.add(new AirPackagingOptions.FilePathAndPathInPackage(myFlashBuilderProjectDir.getPath() + "/" + relPath,
                                                                    slashIndex == -1 ? "." : relPath.substring(slashIndex + 1)));
    }
    return expected;
  }

  public void testFilesToPackage2() throws Exception {
    final VirtualFile srcDir = createChildDirectory(myFlashBuilderProjectDir, "src");
    addFileWithContent("asset1", "", srcDir); // included
    final VirtualFile packDir = createChildDirectory(srcDir, "pack");
    addFileWithContent("App1.as", "", packDir); // source file - not included in package
    addFileWithContent("App1-app.xml", "", packDir); // descriptor - not included
    addFileWithContent("App2.mxml", "", packDir); // source file - not included
    addFileWithContent("App2-app.xml", "", packDir); // descriptor - not included
    addFileWithContent("asset2", "", packDir); // included

    importProject("""
                    <actionScriptProperties  mainApplicationPath="pack/App1.as">
                      <compiler sourceFolderPath="src" useApolloConfig='true'/>  <applications>
                        <application path='pack/App1.as'/>
                        <application path='pack/App2.mxml'/>
                      </applications>
                      <buildTargets>
                        <buildTarget buildTargetName="default" platformId="default"/>
                      </buildTargets>
                    </actionScriptProperties>""",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final FlexBuildConfiguration bc1 = getBC("App1", 2);
    final FlexBuildConfiguration bc2 = getBC("App2", 2);

    assertSameElements(bc1.getAirDesktopPackagingOptions().getFilesToPackage(), getExpectedPaths("src/asset1", "src/pack/asset2"));
    assertSameElements(bc2.getAirDesktopPackagingOptions().getFilesToPackage(), getExpectedPaths("src/asset1", "src/pack/asset2"));
  }

  public void testHaloTheme() throws Exception {
    commonThemeTest("${SDK_THEMES_DIR}/frameworks/themes/Halo", "${FLEX_SDK}/frameworks/themes/Halo/halo.swc");
  }

  public void testAeonTheme() throws Exception {
    commonThemeTest("${SDK_THEMES_DIR}/frameworks/themes/AeonGraphical",
                    "${FLEX_SDK}/frameworks/themes/Halo/halo.swc\n${FLEX_SDK}/frameworks/themes/AeonGraphical/AeonGraphical.css");
  }

  public void testWireframeTheme() throws Exception {
    commonThemeTest("${SDK_THEMES_DIR}/frameworks/themes/Wireframe",
                    "${FLEX_SDK}/frameworks/themes/Spark/spark.css\n${FLEX_SDK}/frameworks/themes/Wireframe/wireframe.swc");
  }

  private void commonThemeTest(final String themeLocation, final String expectedTheme) throws IOException, ConfigurationException {
    prepareSdkTest();
    addFileWithContent(FlashBuilderImporter.DOT_FLEX_PROPERTIES, "", myFlashBuilderProjectDir);
    importProject("<actionScriptProperties>\n" +
                  "  <compiler flexSDK='custom sdk name' additionalCompilerArguments=\"-locale en_US\">\n" +
                  "    <libraryPath>\n" +
                  "      <libraryPathEntry kind='4'/>\n" +
                  "    </libraryPath>" +
                  "  </compiler>\n" +
                  "  <theme themeIsDefault=\"false\" themeIsSDK=\"true\" themeLocation='" + themeLocation + "'/>\n" +
                  "</actionScriptProperties>",
                  myFlashBuilderProjectDir.getPath() + "/Flash_Builder_workspace");

    final FlexBuildConfiguration bc = getBC();

    assertEquals(2, bc.getCompilerOptions().getAllOptions().size());
    assertEquals("en_US", bc.getCompilerOptions().getOption("compiler.locale"));
    assertEquals(expectedTheme, bc.getCompilerOptions().getOption("compiler.theme"));
  }
}
