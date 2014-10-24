package org.jetbrains.osgi.jps.build;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsDependencyElement;
import org.jetbrains.jps.model.module.JpsLibraryDependency;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;
import org.jetbrains.osgi.jps.model.OsmorcJarContentEntry;
import org.jetbrains.osgi.jps.util.OsgiBuildUtil;
import org.jetbrains.jps.util.JpsPathUtil;
import org.osgi.framework.Constants;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class OsgiBuildSession implements Reporter {
  private static final Logger LOG = Logger.getInstance(OsgiBuildSession.class);

  /**
   * Condition which matches order entries that are not representing a framework library.
   */
  public static final Condition<JpsDependencyElement> NOT_FRAMEWORK_LIBRARY_CONDITION = new Condition<JpsDependencyElement>() {
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
  private BndWrapper myBndWrapper;
  private String mySourceToReport = null;

  public void build(@NotNull OsmorcBuildTarget target, @NotNull CompileContext context) {
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
    }
  }

  private void prepare() throws OsgiBuildException {
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

    String moduleOutputUrl = JpsJavaExtensionService.getInstance().getOutputUrl(myModule, false);
    if (moduleOutputUrl == null) {
      throw new OsgiBuildException("Unable to determine the compiler output path for the module.");
    }
    myModuleOutputDir = JpsPathUtil.urlToFile(moduleOutputUrl);

    myOutputDir = BndWrapper.getOutputDir(myModuleOutputDir);

    myBndWrapper = new BndWrapper(this);
  }

  private void doBuild() throws OsgiBuildException {
    progress("Running Bnd to build the bundle");

    File bndFile = getBndFile();

    if (!myExtension.isUseBundlorFile()) {
      mySourceToReport = getSourceFileToReport(bndFile);
      myBndWrapper.build(bndFile, myModuleOutputDir, myOutputJarFile);
      mySourceToReport = null;
    }
    else {
      File tempFile = new File(myOutputJarFile.getAbsolutePath() + ".tmp.jar");
      mySourceToReport = getSourceFileToReport(bndFile);
      myBndWrapper.build(bndFile, myModuleOutputDir, tempFile);
      mySourceToReport = null;

      progress("Running Bundlor to calculate the manifest");

      String bundlorPath = myExtension.getBundlorFileLocation();
      File bundlorFile = OsgiBuildUtil.findFileInModuleContentRoots(myModule, bundlorPath);
      if (bundlorFile == null) {
        throw new OsgiBuildException("Bundlor file missing '" + bundlorPath + "' - please check OSGi facet settings.");
      }

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

    if (!myExtension.isUseBndFile() && !myExtension.isUseBundlorFile()) {
      progress("Bundling non-OSGi libraries");
      bundlifyLibraries();
    }
  }

  @NotNull
  private File getBndFile() throws OsgiBuildException {
    if (myExtension.isUseBndFile()) {
      String bndPath = myExtension.getBndFileLocation();
      File bndFile = OsgiBuildUtil.findFileInModuleContentRoots(myModule, bndPath);
      if (bndFile != null && bndFile.canRead()) {
        return bndFile;
      }
      throw new OsgiBuildException("Bnd file missing '" + bndPath + "' - please check OSGi facet settings.");
    }

    // use a linked hash map to keep the order of properties.
    Map<String, String> buildProperties = new LinkedHashMap<String, String>();
    if (myExtension.isManifestManuallyEdited() || myExtension.isOsmorcControlsManifest()) {
      if (myExtension.isOsmorcControlsManifest()) {
        // fully osmorc controlled, no bnd file, read in all  properties
        buildProperties.putAll(myExtension.getAdditionalProperties());
        buildProperties.put(Constants.BUNDLE_SYMBOLICNAME, myExtension.getBundleSymbolicName());
        buildProperties.put(Constants.BUNDLE_VERSION, myExtension.getBundleVersion());
        String activator = myExtension.getBundleActivator();
        if (!StringUtil.isEmptyOrSpaces(activator)) buildProperties.put(Constants.BUNDLE_ACTIVATOR, activator);
      }
      else { // manually edited manifest
        File manifestFile = myExtension.getManifestFile();
        if (manifestFile == null) {
          throw new OsgiBuildException("Manifest file '" + myExtension.getManifestLocation() + "' missing - please check OSGi facet settings.");
        }
        buildProperties.put("-manifest", manifestFile.getAbsolutePath());
      }

      StringBuilder pathBuilder = new StringBuilder();

      // add all the class paths to include resources, so stuff from the project gets copied over.
      // XXX: one could argue if this should be done for a non-osmorc build
      pathBuilder.append(myModuleOutputDir.getPath());

      // now include the paths from the configuration
      for (OsmorcJarContentEntry contentEntry : myExtension.getAdditionalJarContents()) {
        if (pathBuilder.length() > 0) pathBuilder.append(",");
        pathBuilder.append(contentEntry.myDestination).append(" = ").append(contentEntry.mySource);
      }

      // and tell bnd what resources to include
      StringBuilder includedResources = new StringBuilder();
      if (!myExtension.isManifestManuallyEdited()) {
        String resources = myExtension.getAdditionalProperties().get("Include-Resource");
        if (resources != null) includedResources.append(resources).append(',');
        includedResources.append(pathBuilder);
      }
      else {
        includedResources.append(pathBuilder);
      }
      buildProperties.put("Include-Resource", includedResources.toString());

      // add the ignore pattern for the resources
      String pattern = myExtension.getIgnoreFilePattern();
      if (!StringUtil.isEmptyOrSpaces(pattern)) {
        try {
          //noinspection ResultOfMethodCallIgnored
          Pattern.compile(pattern);
        }
        catch (PatternSyntaxException e) {
          throw new OsgiBuildException("The file ignore pattern is invalid - please check OSGi facet settings.");
        }
        buildProperties.put("-donotcopy", pattern);
      }

      if (myExtension.isOsmorcControlsManifest()) {
        // support the {local-packages} instruction
        progress("Calculating local packages");
        LocalPackageCollector.addLocalPackages(myModuleOutputDir, buildProperties);
      }
    }
    else if (!myExtension.isUseBundlorFile()) {
      throw new OsgiBuildException("Bundle creation method not specified - please check OSGi facet settings.");
    }

    String comment = "Generated by IDEA for module '" + myModule.getName() + "' in project '" + myModule.getProject().getName() + "'";
    return myBndWrapper.makeBndFile(buildProperties, comment, myOutputDir);
  }

  private String getSourceFileToReport(File bndFile) {
    if (myExtension.isManifestManuallyEdited()) {
      // link warnings/errors to the user-provided manifest file
      File manifestFile = myExtension.getManifestFile();
      if (manifestFile != null) {
        return manifestFile.getPath();
      }
    }
    else {
      File mavenProjectFile = OsgiBuildUtil.getMavenProjectPath(myContext, myModule);
      if (mavenProjectFile != null) {
        // ok it's imported from Maven, link warnings/errors back to pom.xml
        return mavenProjectFile.getPath();
      }
    }
    return bndFile.getPath();
  }

  /**
   * Bundlifies all libraries that belong to the given module and that are not bundles.
   * The bundles are cached, so if the source library does not change, it will not be bundlified again.
   * Returns a string array containing paths of the bundlified libraries.
   */
  @NotNull
  private List<String> bundlifyLibraries() {
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
          File bundledDependency = myBndWrapper.wrapLibrary(dependency, myOutputDir);
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
