/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.jps.build;

import aQute.bnd.osgi.Constants;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.logging.ProjectBuilderLogger;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.DoneSomethingNotification;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsDependencyElement;
import org.jetbrains.jps.model.module.JpsLibraryDependency;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.osgi.jps.model.*;
import org.jetbrains.osgi.jps.model.impl.JpsOsmorcModuleExtensionImpl;
import org.jetbrains.osgi.jps.util.OsgiBuildUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class OsgiBuildSession implements Reporter {
  private static final Logger LOG = Logger.getInstance(OsgiBuildSession.class);

  /**
   * Condition which matches order entries that are not representing a framework library.
   */
  private static final Condition<JpsDependencyElement> NOT_FRAMEWORK_LIBRARY_CONDITION = new Condition<JpsDependencyElement>() {
    @Override
    public boolean value(JpsDependencyElement entry) {
      if (entry instanceof JpsLibraryDependency) {
        JpsLibraryDependency libEntry = (JpsLibraryDependency)entry;
        String libraryName = libEntry.getLibraryReference().getLibraryName();
        if (libraryName.startsWith("Osmorc:")) {
          return false;
        }
      }
      return true;
    }
  };

  private CompileContext myContext;
  private JpsOsmorcModuleExtension myExtension;
  private JpsModule myModule;
  private String myMessagePrefix;
  private File myOutputJarFile;
  private File myModuleOutputDir;
  private File myOutputDir;
  private File[] mySources;
  private BndWrapper myBndWrapper;
  private String mySourceToReport = null;

  public void build(@NotNull OsmorcBuildTarget target, @NotNull CompileContext context) throws IOException {
    myContext = context;
    myExtension = target.getExtension();
    myModule = target.getModule();
    myMessagePrefix = "[" + myModule.getName() + "] ";

    progress("Building OSGi bundle");

    try {
      prepare();
      doBuild();
    }
    catch (OsgiBuildException e) {
      error(e.getMessage(), e.getCause(), e.getSourcePath());
      return;
    }

    if (!myOutputJarFile.exists()) {
      error("Bundle was not built", null, null);
      return;
    }

    ProjectBuilderLogger logger = context.getLoggingManager().getProjectBuilderLogger();
    if (logger.isEnabled()) {
      logger.logCompiledFiles(Collections.singleton(myOutputJarFile), OsmorcBuilder.ID, "Built OSGi bundles:");
    }

    context.processMessage(DoneSomethingNotification.INSTANCE);
  }

  private void prepare() throws OsgiBuildException {
    myModuleOutputDir = JpsJavaExtensionService.getInstance().getOutputDirectory(myModule, false);
    if (myModuleOutputDir == null) {
      throw new OsgiBuildException("Unable to determine the compiler output path for the module.");
    }

    String jarFileLocation = myExtension.getJarFileLocation();
    if (jarFileLocation.isEmpty()) {
      throw new OsgiBuildException("Bundle path is empty - please check OSGi facet settings.");
    }

    myOutputJarFile = new File(jarFileLocation);
    if (!FileUtil.delete(myOutputJarFile)) {
      throw new OsgiBuildException("Can't delete bundle file '" + myOutputJarFile + "'.");
    }
    if (!FileUtil.createParentDirs(myOutputJarFile)) {
      throw new OsgiBuildException("Cannot create directory for bundle file '" + myOutputJarFile + "'.");
    }

    mySources = ContainerUtil.map2Array(myModule.getSourceRoots(), File.class, new Function<JpsModuleSourceRoot, File>() {
      @Override
      public File fun(JpsModuleSourceRoot root) {
        return root.getFile();
      }
    });
    myOutputDir = BndWrapper.getOutputDir(myModuleOutputDir);
    myBndWrapper = new BndWrapper(this);
  }

  private void doBuild() throws OsgiBuildException {
    progress("Running Bnd to build the bundle");

    if (myExtension.isUseBndFile()) {
      String bndPath = myExtension.getBndFileLocation();
      File bndFile = OsgiBuildUtil.findFileInModuleContentRoots(myModule, bndPath);
      if (bndFile == null || !bndFile.canRead()) {
        throw new OsgiBuildException("Bnd file missing '" + bndPath + "' - please check OSGi facet settings.");
      }

      mySourceToReport = bndFile.getAbsolutePath();
      try {
        myBndWrapper.build(bndFile, myModuleOutputDir, mySources, myOutputJarFile);
      }
      catch (Exception e) {
        throw new OsgiBuildException("Unexpected build error", e, null);
      }
      mySourceToReport = null;
    }
    else if (myExtension.isUseBundlorFile()) {
      String bundlorPath = myExtension.getBundlorFileLocation();
      File bundlorFile = OsgiBuildUtil.findFileInModuleContentRoots(myModule, bundlorPath);
      if (bundlorFile == null) {
        throw new OsgiBuildException("Bundlor file missing '" + bundlorPath + "' - please check OSGi facet settings.");
      }

      File tempFile = new File(myOutputJarFile.getAbsolutePath() + ".tmp.jar");

      try {
        Map<String, String> properties = Collections.singletonMap(Constants.CREATED_BY, "IntelliJ IDEA / OSGi Plugin");
        myBndWrapper.build(properties, myModuleOutputDir, mySources, tempFile);
      }
      catch (Exception e) {
        throw new OsgiBuildException("Unexpected build error", e, null);
      }

      progress("Running Bundlor to calculate the manifest");
      try {
        Properties properties = OsgiBuildUtil.getMavenProjectProperties(myContext, myModule);
        List<String> warnings = new BundlorWrapper().wrapModule(properties, tempFile, myOutputJarFile, bundlorFile);
        for (String warning : warnings) {
          warning(warning, null, bundlorFile.getPath());
        }
      }
      finally {
        if (!FileUtil.delete(tempFile)) {
          warning("Can't delete temporary file '" + tempFile + "'", null, null);
        }
      }
    }
    else if (myExtension.isManifestManuallyEdited() || myExtension.isOsmorcControlsManifest()) {
      Map<String, String> buildProperties = getBuildProperties();
      if (LOG.isDebugEnabled()) {
        LOG.debug("build properties: " + buildProperties);
      }

      mySourceToReport = getSourceFileToReport();
      try {
        myBndWrapper.build(buildProperties, myModuleOutputDir, mySources, myOutputJarFile);
      }
      catch (Exception e) {
        throw new OsgiBuildException("Unexpected build error", e, null);
      }
      mySourceToReport = null;

      progress("Bundling non-OSGi libraries");
      bundlifyLibraries();
    }
    else {
      ManifestGenerationMode mode = ((JpsOsmorcModuleExtensionImpl)myExtension).getProperties().myManifestGenerationMode;
      throw new OsgiBuildException("Internal error: unknown build method: " + mode);
    }
  }

  @NotNull
  private Map<String, String> getBuildProperties() throws OsgiBuildException {
    Map<String, String> properties = ContainerUtil.newHashMap();

    // defaults (similar to Maven)

    properties.put(Constants.IMPORT_PACKAGE, "*");
    properties.put(Constants.REMOVEHEADERS, Constants.INCLUDE_RESOURCE + ',' + Constants.PRIVATE_PACKAGE);
    properties.put(Constants.CREATED_BY, "IntelliJ IDEA / OSGi Plugin");

    // user settings

    if (myExtension.isOsmorcControlsManifest()) {
      properties.putAll(myExtension.getAdditionalProperties());

      properties.put(Constants.BUNDLE_SYMBOLICNAME, myExtension.getBundleSymbolicName());
      properties.put(Constants.BUNDLE_VERSION, myExtension.getBundleVersion());

      String activator = myExtension.getBundleActivator();
      if (!StringUtil.isEmptyOrSpaces(activator)) {
        properties.put(Constants.BUNDLE_ACTIVATOR, activator);
      }
    }
    else {
      File manifestFile = myExtension.getManifestFile();
      if (manifestFile == null) {
        throw new OsgiBuildException("Manifest file '" + myExtension.getManifestLocation() + "' missing - please check OSGi facet settings.");
      }
      properties.put(Constants.MANIFEST, manifestFile.getAbsolutePath());
    }

    // resources

    StringBuilder pathBuilder = new StringBuilder(myModuleOutputDir.getPath());
    for (OsmorcJarContentEntry contentEntry : myExtension.getAdditionalJarContents()) {
      pathBuilder.append(',').append(contentEntry.myDestination).append('=').append(contentEntry.mySource);
    }

    StringBuilder includedResources;
    if (myExtension.isOsmorcControlsManifest()) {
      includedResources = new StringBuilder();
      String resources = properties.get(Constants.INCLUDE_RESOURCE);
      if (resources != null) includedResources.append(resources).append(',');
      includedResources.append(pathBuilder);
    }
    else {
      includedResources = pathBuilder;
    }
    properties.put(Constants.INCLUDE_RESOURCE, includedResources.toString());

    String pattern = myExtension.getIgnoreFilePattern();
    if (!StringUtil.isEmptyOrSpaces(pattern)) {
      try {
        //noinspection ResultOfMethodCallIgnored
        Pattern.compile(pattern);
      }
      catch (PatternSyntaxException e) {
        throw new OsgiBuildException("The file ignore pattern is invalid - please check OSGi facet settings.");
      }
      properties.put(Constants.DONOTCOPY, pattern);
    }

    if (myExtension.isOsmorcControlsManifest()) {
      // support the {local-packages} instruction
      progress("Calculating local packages");
      LocalPackageCollector.addLocalPackages(myModuleOutputDir, properties);
    }

    return properties;
  }

  private String getSourceFileToReport() {
    if (myExtension.isManifestManuallyEdited()) {
      File manifestFile = myExtension.getManifestFile();
      if (manifestFile != null) {
        return manifestFile.getPath();
      }
    }
    else {
      File mavenProjectFile = OsgiBuildUtil.getMavenProjectPath(myContext, myModule);
      if (mavenProjectFile != null) {
        return mavenProjectFile.getPath();
      }
    }
    return null;
  }

  /**
   * Bundlifies all libraries that belong to the given module and that are not bundles.
   * The bundles are cached, so if the source library does not change, it will not be bundlified again.
   * Returns a string array containing paths of the bundlified libraries.
   */
  @NotNull
  private List<String> bundlifyLibraries() {
    List<LibraryBundlificationRule> libRules = JpsOsmorcExtensionService.getInstance().getLibraryBundlificationRules();

    Collection<File> dependencies = JpsJavaExtensionService.getInstance().enumerateDependencies(Collections.singletonList(myModule))
      .withoutSdk()
      .withoutModuleSourceEntries()
      .withoutDepModules()
      .productionOnly()
      .runtimeOnly()
      .recursively()
      .exportedOnly()
      .satisfying(NOT_FRAMEWORK_LIBRARY_CONDITION)
      .classes()
      .getRoots();

    List<String> result = ContainerUtil.newArrayList();
    for (File dependency : dependencies) {
      String path = dependency.getPath();
      if (CachingBundleInfoProvider.canBeBundlified(path)) {
        try {
          File bundledDependency = myBndWrapper.wrapLibrary(dependency, myOutputDir, libRules);
          if (bundledDependency != null) {
            result.add(bundledDependency.getPath());
          }
        }
        catch (OsgiBuildException e) {
          warning(e.getMessage(), e.getCause(), e.getSourcePath());
        }
      }
      else if (CachingBundleInfoProvider.isBundle(path)) {
        result.add(path);
      }
    }
    return result;
  }

  @Override
  public void progress(@NotNull String message) {
    myContext.processMessage(new ProgressMessage(myMessagePrefix + message));
  }

  @Override
  public void warning(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath) {
    LOG.warn(message, t);
    if (sourcePath == null) sourcePath = mySourceToReport;
    myContext.processMessage(new CompilerMessage(OsmorcBuilder.ID, BuildMessage.Kind.WARNING, myMessagePrefix + message, sourcePath));
  }

  @Override
  public void error(@NotNull String message, @Nullable Throwable t, @Nullable String sourcePath) {
    LOG.warn(message, t);
    if (sourcePath == null) sourcePath = mySourceToReport;
    myContext.processMessage(new CompilerMessage(OsmorcBuilder.ID, BuildMessage.Kind.ERROR, myMessagePrefix + message, sourcePath));
  }
}
