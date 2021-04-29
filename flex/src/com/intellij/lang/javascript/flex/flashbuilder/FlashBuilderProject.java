// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.util.Pair;

import java.util.*;

public class FlashBuilderProject {
  private String myName = FlexBundle.message("unnamed");
  private final Map<String, String> myLinkedResources = new HashMap<>();
  private final Set<String> myUsedPathVariables = new HashSet<>();
  private String myProjectRootPath = "";
  private final Collection<String> mySourcePaths = new ArrayList<>();
  private String myOutputFolderPath = "";
  private boolean mySdkUsed;
  private String mySdkName = FlashBuilderSdkFinder.DEFAULT_SDK_NAME;
  private TargetPlatform myTargetPlatform = TargetPlatform.Web;
  private boolean myPureActionScript = false;
  private boolean myAirSdk = false;
  private OutputType myOutputType = OutputType.Application;
  private boolean myAndroidSupported = false;
  private boolean myIosSupported = false;
  private String myDesktopCertPath;
  private String myAndroidCertPath;
  private String myIOSCertPath;
  private String myIOSProvisioningPath;
  private String myMainAppClassName = "";
  private final Collection<String> myApplicationClassNames = new ArrayList<>();
  private String myTargetPlayerVersion;
  private String myAdditionalCompilerOptions = "";
  private boolean myUseHtmlWrapper = false;
  private final Map<String, Collection<String>> myLibraryPathsAndSources = new LinkedHashMap<>();
  private final Collection<Pair<String, String>> myNamespacesAndManifestPaths = new ArrayList<>(1);
  private final Collection<String> myFilesIncludedInSwc = new ArrayList<>();
  private final Collection<FBRLMInfo> myModules = new ArrayList<>();
  private final Collection<String> myCssFilesToCompile = new ArrayList<>();
  private final Collection<String> myPathsExcludedFromDesktopPackaging = new ArrayList<>();
  private final Collection<String> myPathsExcludedFromAndroidPackaging = new ArrayList<>();
  private final Collection<String> myPathsExcludedFromIOSPackaging = new ArrayList<>();
  private String myThemeDirPathRaw;

  public static class FBRLMInfo {
    public final String MAIN_CLASS_PATH;
    public final String OUTPUT_PATH;
    public final boolean OPTIMIZE;
    public final String OPTIMIZE_FOR;

    public FBRLMInfo(final String mainClassPath, final String outputPath, final boolean optimize, final String optimizeFor) {
      this.MAIN_CLASS_PATH = mainClassPath;
      this.OUTPUT_PATH = outputPath;
      this.OPTIMIZE = optimize;
      this.OPTIMIZE_FOR = optimizeFor;
    }
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

  public TargetPlatform getTargetPlatform() {
    return myTargetPlatform;
  }

  public void setTargetPlatform(final TargetPlatform targetPlatform) {
    myTargetPlatform = targetPlatform;
  }

  public boolean isPureActionScript() {
    return myPureActionScript;
  }

  public void setPureActionScript(final boolean pureActionScript) {
    myPureActionScript = pureActionScript;
  }

  public boolean isAirSdk() {
    return myAirSdk;
  }

  public void setAirSdk(final boolean airSdk) {
    myAirSdk = airSdk;
  }

  public OutputType getOutputType() {
    return myOutputType;
  }

  public void setOutputType(final OutputType outputType) {
    myOutputType = outputType;
  }

  public boolean isAndroidSupported() {
    return myAndroidSupported;
  }

  public void setAndroidSupported(final boolean androidSupported) {
    myAndroidSupported = androidSupported;
  }

  public boolean isIosSupported() {
    return myIosSupported;
  }

  public void setIosSupported(final boolean iosSupported) {
    myIosSupported = iosSupported;
  }

  public String getDesktopCertPath() {
    return myDesktopCertPath;
  }

  public void setDesktopCertPath(final String desktopCertPath) {
    myDesktopCertPath = desktopCertPath;
  }

  public String getAndroidCertPath() {
    return myAndroidCertPath;
  }

  public void setAndroidCertPath(final String androidCertPath) {
    myAndroidCertPath = androidCertPath;
  }

  public String getIOSCertPath() {
    return myIOSCertPath;
  }

  public void setIOSCertPath(final String iOSCertPath) {
    myIOSCertPath = iOSCertPath;
  }

  public String getIOSProvisioningPath() {
    return myIOSProvisioningPath;
  }

  public void setIOSProvisioningPath(final String IOSProvisioningPath) {
    myIOSProvisioningPath = IOSProvisioningPath;
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

  public boolean isUseHtmlWrapper() {
    return myUseHtmlWrapper;
  }

  public void setUseHtmlWrapper(final boolean useHtmlWrapper) {
    myUseHtmlWrapper = useHtmlWrapper;
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
    checkIfPathMacroUsed(manifestPath);
    myNamespacesAndManifestPaths.add(Pair.create(namespace, manifestPath));
  }

  public Collection<Pair<String, String>> getNamespacesAndManifestPaths() {
    return myNamespacesAndManifestPaths;
  }

  public void addFileIncludedInSwc(final String path) {
    myFilesIncludedInSwc.add(path);
  }

  public Collection<String> getFilesIncludedInSwc() {
    return myFilesIncludedInSwc;
  }

  public void addModule(final FBRLMInfo rlmInfo) {
    checkIfPathMacroUsed(rlmInfo.MAIN_CLASS_PATH);
    checkIfPathMacroUsed(rlmInfo.OUTPUT_PATH);
    checkIfPathMacroUsed(rlmInfo.OPTIMIZE_FOR);
    myModules.add(rlmInfo);
  }

  public Collection<FBRLMInfo> getModules() {
    return myModules;
  }

  public void addCssFileToCompile(final String cssFilePath) {
    checkIfPathMacroUsed(cssFilePath);
    myCssFilesToCompile.add(cssFilePath);
  }

  public Collection<String> getCssFilesToCompile() {
    return myCssFilesToCompile;
  }

  public void addPathExcludedFromDesktopPackaging(final String path) {
    myPathsExcludedFromDesktopPackaging.add(path);
  }

  public Collection<String> getPathsExcludedFromDesktopPackaging() {
    return myPathsExcludedFromDesktopPackaging;
  }

  public void addPathExcludedFromAndroidPackaging(final String path) {
    myPathsExcludedFromAndroidPackaging.add(path);
  }

  public Collection<String> getPathsExcludedFromAndroidPackaging() {
    return myPathsExcludedFromAndroidPackaging;
  }

  public void addPathExcludedFromIOSPackaging(final String path) {
    myPathsExcludedFromIOSPackaging.add(path);
  }

  public Collection<String> getPathsExcludedFromIOSPackaging() {
    return myPathsExcludedFromIOSPackaging;
  }

  public void setThemeDirPathRaw(final String themeDirPathRaw) {
    // do not check path macro usage - there's a special macros always
    myThemeDirPathRaw = themeDirPathRaw;
  }

  public String getThemeDirPathRaw() {
    return myThemeDirPathRaw;
  }

  private void checkIfPathMacroUsed(final String path) {
    final int slashIndex = path.indexOf('/');
    final String potentialLink = slashIndex >= 0 ? path.substring(0, slashIndex) : path;

    if (potentialLink.startsWith("${") && potentialLink.endsWith("}")) {
      myUsedPathVariables.add(potentialLink.substring(2, potentialLink.length() - 1));
    }
  }
}