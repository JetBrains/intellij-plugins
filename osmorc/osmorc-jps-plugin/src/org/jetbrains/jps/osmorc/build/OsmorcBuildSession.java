package org.jetbrains.jps.osmorc.build;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsDependencyElement;
import org.jetbrains.jps.model.module.JpsLibraryDependency;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.JpsCachingBundleInfoProvider;
import org.jetbrains.jps.osmorc.model.JpsOsmorcModuleExtension;
import org.jetbrains.jps.osmorc.model.impl.OsmorcJarContentEntry;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.*;

public class OsmorcBuildSession {

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

  private String myModuleNameMessagePrefix;
  private File myOutputJarFile;
  private File myModuleOutputDir;
  private BndWrapper myBndWrapper;

  public void build(@NotNull OsmorcBuildTarget target, @NotNull CompileContext context) {
    myContext = context;
    myExtension = target.getExtension();
    myModule = myExtension.getModule();

    myModuleNameMessagePrefix = "[" + myModule.getName() + "] ";

    progressMessage("Building bundle for the module");

    try {
      prepare();

      doBuild();
    }
    catch (OsmorcBuildException e) {
      processException(e);
    }
  }

  private void prepare() throws OsmorcBuildException {
    String jarFileLocation = myExtension.getJarFileLocation();
    if (jarFileLocation.isEmpty()) {
      throw new OsmorcBuildException("Output jar path is empty");
    }

    // create the jar file
    myOutputJarFile = new File(jarFileLocation);
    if (!FileUtil.delete(myOutputJarFile)) {
      throw new OsmorcBuildException("Cannot delete bundle jar", myOutputJarFile);
    }
    if (!FileUtil.createParentDirs(myOutputJarFile)) {
      throw new OsmorcBuildException("Cannot create path to bundle jar", myOutputJarFile);
    }

    String moduleOutputUrl = JpsJavaExtensionService.getInstance().getOutputUrl(myModule, false);
    if (moduleOutputUrl == null) {
      throw new OsmorcBuildException("Unable to determine the compiler output path for the module").setWarning();
    }
    myModuleOutputDir = JpsPathUtil.urlToFile(moduleOutputUrl);


    myBndWrapper = new BndWrapper(this);
  }


  private void doBuild() throws OsmorcBuildException {
    // build a bnd file or use a provided one.
    File bndFile = getBndFile();

    File tempJarFile = new File(myOutputJarFile.getAbsolutePath() + ".tmp.jar");

    boolean useBundlorFile = myExtension.isUseBundlorFile();

    progressMessage("Running bnd to build the bundle");

    myBndWrapper.build(bndFile, useBundlorFile ? tempJarFile : myOutputJarFile);

    // if we use bundlor, let bundlor work on the generated file.
    if (useBundlorFile) {
      progressMessage("Running bundlor to calculate the manifest");

      String bundlorFileLocation = myExtension.getBundlorFileLocation();
      File bundlorFile = myExtension.findFileInModuleContentRoots(bundlorFileLocation);
      if (bundlorFile == null) {
        throw new OsmorcBuildException("The Bundlor file for the module does not exist", bundlorFileLocation);
      }

      BundlorWrapper bw = new BundlorWrapper();
      try {
        bw.wrapModule(this, tempJarFile, bundlorFile);
      }
      finally {
        // delete the tmp jar
        if (FileUtil.delete(tempJarFile)) {
          warn("Could not delete the temporary file", tempJarFile);
        }
      }
    }

    if (!myExtension.isUseBndFile() && !myExtension.isUseBundlorFile()) {
      // finally bundlify all the libs for this one
      bundlifyLibraries();
    }
  }

  @NotNull
  private File getBndFile() throws OsmorcBuildException {
    if (myExtension.isUseBndFile()) {
      String fileLocation = myExtension.getBndFileLocation();
      File bndFile = myExtension.findFileInModuleContentRoots(fileLocation);
      if (bndFile != null && bndFile.canRead()) {
        return bndFile;
      }
      else {
        throw new OsmorcBuildException("The bnd file for the module does not exist", fileLocation);
      }
    }

    // use a linked hash map to keep the order of properties.
    Map<String, String> buildProperties = new LinkedHashMap<String, String>();
    if (myExtension.isManifestManuallyEdited() || myExtension.isOsmorcControlsManifest()) {
      if (myExtension.isOsmorcControlsManifest()) {
        // fully osmorc controlled, no bnd file, read in all  properties
        buildProperties.putAll(myExtension.getBndFileProperties());
      }
      else if (myExtension.isManifestManuallyEdited()) { // manually edited manifest
        File manifestFile = myExtension.getManifestFile();
        if (manifestFile == null) {
          throw new OsmorcBuildException(
            "Manifest file for the module does not exist or cannot be found. Check that file exists and is not excluded from the module",
            myExtension.getManifestLocation());
        }
        buildProperties.put("-manifest", manifestFile.getAbsolutePath());
      }

      StringBuilder pathBuilder = new StringBuilder();

      // add all the class paths to include resources, so stuff from the project gets copied over.
      // XXX: one could argue if this should be done for a non-osmorc build
      pathBuilder.append(myModuleOutputDir.getPath());

      // now include the paths from the configuration
      for (OsmorcJarContentEntry contentEntry : myExtension.getAdditionalJARContents()) {
        if (pathBuilder.length() > 0) pathBuilder.append(",");
        pathBuilder.append(contentEntry.myDestination).append(" = ").append(contentEntry.mySource);
      }

      // and tell bnd what resources to include
      StringBuilder includedResources = new StringBuilder();
      if (!myExtension.isManifestManuallyEdited()) {
        String resources = myExtension.getAdditionalPropertiesAsMap().get("Include-Resource");
        if (resources != null) {
          includedResources.append(resources).append(",").append(pathBuilder);
        }
        else {
          includedResources.append(pathBuilder);
        }
      }
      else {
        includedResources.append(pathBuilder);
      }
      buildProperties.put("Include-Resource", includedResources.toString());

      // add the ignore pattern for the resources
      if (!myExtension.getIgnoreFilePattern().isEmpty()) {
        if (!myExtension.isIgnorePatternValid()) {
          throw new OsmorcBuildException("The file ignore pattern in the facet configuration is invalid");
        }
        buildProperties.put("-donotcopy", myExtension.getIgnoreFilePattern());
      }

      if (myExtension.isOsmorcControlsManifest()) {
        // support the {local-packages} instruction
        progressMessage("Calculating local packages");
        LocalPackageCollector.addLocalPackages(myModuleOutputDir, buildProperties);
      }
    }
    else if (!myExtension.isUseBundlorFile()) {
      throw new OsmorcBuildException("OSGi facet configuration for the module seems to be invalid. " +
                                     "No supported manifest handling method is set up. Please check configuration and try again.");
    }

    return myBndWrapper.makeBndFile(buildProperties);
  }

  /**
   * Bundlifies all libraries that belong to the given module and that are not bundles and that are not modules.
   * The bundles are cached, so if * the source library does not change, it will not be bundlified again.
   * Returns a string array containing the urls of the bundlified libraries.
   */
  @NotNull
  private List<File> bundlifyLibraries() throws OsmorcBuildException {
    Collection<File> dependencyFiles = JpsJavaExtensionService.getInstance().enumerateDependencies(Collections.singletonList(myModule))
      .withoutSdk()
      .withoutModuleSourceEntries()
      .withoutDepModules()
      .productionOnly()
      .runtimeOnly()
      .recursively()
      .exportedOnly()
      .satisfying(NOT_FRAMEWORK_LIBRARY_CONDITION)
      .classes().getRoots();
    List<File> result = new ArrayList<File>();

    for (File dependencyFile : dependencyFiles) {
      if (JpsCachingBundleInfoProvider.canBeBundlified(dependencyFile)) { // Fixes IDEA-56666
        progressMessage("Bundling non-OSGi libraries for the module, dependency: " + dependencyFile.getAbsolutePath());
        // ok it is not a bundle, so we need to bundlify
        File bundledDependencyFile = myBndWrapper.wrapLibrary(dependencyFile);
        // if no bundle could (or should) be created, we exempt this library
        if (bundledDependencyFile != null) {
          result.add(bundledDependencyFile);
        }
      }
      else if (JpsCachingBundleInfoProvider.isBundle(dependencyFile)) { // Exclude non-bundles (IDEA-56666)
        result.add(dependencyFile);
      }
    }
    return result;
  }

  public void processException(OsmorcBuildException e) {
    StringBuilder text = new StringBuilder();
    text.append(myModuleNameMessagePrefix).append(e.getMessage());
    Throwable cause = e.getCause();
    if (cause != null) {
      text.append(" : ").append(CompilerMessage.getTextFromThrowable(cause));
    }
    BuildMessage.Kind kind = e.isWarningNotError() ? BuildMessage.Kind.WARNING : BuildMessage.Kind.ERROR;
    myContext.processMessage(new CompilerMessage(OsmorcBuilder.NAME, kind, text.toString(), e.getSourcePath()));
  }

  public void progressMessage(String message) {
    myContext.processMessage(new ProgressMessage(myModuleNameMessagePrefix + message));
  }

  public void warn(String message, String sourcePath) {
    processException(new OsmorcBuildException(message, sourcePath).setWarning());
  }

  public void warn(String message, File sourceFile) {
    warn(message, sourceFile.getAbsolutePath());
  }

  public CompileContext getContext() {
    return myContext;
  }

  public File getModuleOutputDir() {
    return myModuleOutputDir;
  }

  public JpsModule getModule() {
    return myModule;
  }

  public JpsOsmorcModuleExtension getExtension() {
    return myExtension;
  }

  public File getOutputJarFile() {
    return myOutputJarFile;
  }
}
