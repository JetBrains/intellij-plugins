package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.openapi.util.Pair;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class FlashBuilderProject {

  private String myName = FlexBundle.message("unnamed");
  private Map<String, String> myLinkedResources = new THashMap<String, String>();
  private Set<String> myUsedPathVariables = new THashSet<String>();
  private String myProjectRootPath = "";
  private Collection<String> mySourcePaths = new ArrayList<String>();
  private String myOutputFolderPath = "";
  private boolean mySdkUsed;
  private String mySdkName = "";
  private String myCompilerOutputType = FlexBuildConfiguration.APPLICATION;
  private ProjectType myProjectType;
  private String myMainAppClassName = "";
  private Collection<String> myApplicationClassNames = new ArrayList<String>();
  private String myTargetPlayerVersion;
  private String myAdditionalCompilerOptions = "";
  private Map<String, Collection<String>> myLibraryPathsAndSources = new THashMap<String, Collection<String>>();
  private Collection<Pair<String, String>> myNamespacesAndManifestPaths = new ArrayList<Pair<String, String>>(1);
  private Collection<Pair<String, String>> myModules = new ArrayList<Pair<String, String>>();
  private Collection<String> myCssFilesToCompile = new ArrayList<String>();

  public enum ProjectType {
    ActionScript, Flex, AIR
  }

  FlashBuilderProject() {
  }

  public String getName() {
    return myName;
  }

  public void setName(final String name) {
    myName = name;
  }

  public void addLinkedResource(final String linkName, final String linkLocation) {
    myLinkedResources.put(linkName, linkLocation);
  }

  public Map<String, String> getLinkedResources() {
    return myLinkedResources;
  }

  public String getProjectRootPath() {
    return myProjectRootPath;
  }

  public void setProjectRootPath(final String projectRootPath) {
    myProjectRootPath = projectRootPath;
  }

  public Collection<String> getSourcePaths() {
    return mySourcePaths;
  }

  public void addSourcePath(final String sourcePath) {
    checkIfPathMacroUsed(sourcePath);
    mySourcePaths.add(sourcePath);
  }

  public String getOutputFolderPath() {
    return myOutputFolderPath;
  }

  public void setOutputFolderPath(final String outputFolderPath) {
    checkIfPathMacroUsed(outputFolderPath);
    myOutputFolderPath = outputFolderPath;
  }

  public boolean isSdkUsed() {
    return mySdkUsed;
  }

  public void setSdkUsed(boolean sdkUsed) {
    mySdkUsed = sdkUsed;
  }

  public String getSdkName() {
    return mySdkName;
  }

  public void setSdkName(final String sdkName) {
    mySdkName = sdkName;
  }

  public String getCompilerOutputType() {
    return myCompilerOutputType;
  }

  public void setCompilerOutputType(final String compilerOutputType) {
    assert FlexBuildConfiguration.APPLICATION.equals(compilerOutputType) || FlexBuildConfiguration.LIBRARY.equals(compilerOutputType);
    myCompilerOutputType = compilerOutputType;
  }

  public ProjectType getProjectType() {
    return myProjectType;
  }

  public void setProjectType(final ProjectType projectType) {
    myProjectType = projectType;
  }

  public String getMainAppClassName() {
    return myMainAppClassName;
  }

  public void addApplicationClassName(final String className) {
    if (!className.equals(myMainAppClassName)) {
      myApplicationClassNames.add(className);
    }
  }

  public Collection<String> getApplicationClassNames() {
    return myApplicationClassNames;
  }

  public void setMainAppClassName(final String mainClassName) {
    myMainAppClassName = mainClassName;
  }

  public String getTargetPlayerVersion() {
    return myTargetPlayerVersion;
  }

  public void setTargetPlayerVersion(final String targetPlayerVersion) {
    myTargetPlayerVersion = targetPlayerVersion;
  }

  public String getAdditionalCompilerOptions() {
    return myAdditionalCompilerOptions;
  }

  public void setAdditionalCompilerOptions(final String additionalCompilerOptions) {
    myAdditionalCompilerOptions = additionalCompilerOptions;
  }

  public Collection<String> getLibraryPaths() {
    return myLibraryPathsAndSources.keySet();
  }

  public Collection<String> getLibrarySourcePaths(final String libraryPath) {
    return myLibraryPathsAndSources.get(libraryPath);
  }

  public void addLibraryPathAndSources(final String libraryPath, final Collection<String> sourcePathsForLibrary) {
    checkIfPathMacroUsed(libraryPath);
    for (final String path : sourcePathsForLibrary) {
      checkIfPathMacroUsed(path);
    }
    myLibraryPathsAndSources.put(libraryPath, sourcePathsForLibrary);
  }

  public Set<String> getUsedPathVariables() {
    return myUsedPathVariables;
  }

  public void addNamespaceAndManifestPath(final String namespace, final String manifestPath) {
    myNamespacesAndManifestPaths.add(Pair.create(namespace, manifestPath));
  }

  public Collection<Pair<String, String>> getNamespacesAndManifestPaths() {
    return myNamespacesAndManifestPaths;
  }

  public void addModule(final String sourcePath, final String destPath) {
    checkIfPathMacroUsed(sourcePath);
    myModules.add(Pair.create(sourcePath, destPath));
  }

  public Collection<Pair<String, String>> getModules() {
    return myModules;
  }

  public void addCssFileToCompile(final String cssFilePath) {
    myCssFilesToCompile.add(cssFilePath);
  }

  public Collection<String> getCssFilesToCompile() {
    return myCssFilesToCompile;
  }

  private void checkIfPathMacroUsed(final String path) {
    final int slashIndex = path.indexOf('/');
    final String potentialLink = slashIndex >= 0 ? path.substring(0, slashIndex) : path;

    if (potentialLink.startsWith("${") && potentialLink.endsWith("}")) {
      myUsedPathVariables.add(potentialLink.substring(2, potentialLink.length() - 1));
    }
  }
}