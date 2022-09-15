// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.FlexCompilerConfigFileUtilBase;
import com.intellij.flex.model.bc.*;
import com.intellij.flex.model.sdk.RslUtil;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitPrecompileTask;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.stubs.JSQualifiedElementIndex;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.PlatformUtils;
import com.intellij.util.SystemProperties;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class CompilerConfigGenerator {

  private static final String[] LIB_ORDER =
    {"framework", "textLayout", "osmf", "spark", "sparkskins", "rpc", "charts", "spark_dmv", "mx", "advancedgrids"};

  private final Module myModule;
  private final FlexBuildConfiguration myBC;
  private final boolean myFlexUnit;
  private final boolean myCSS;
  private final Sdk mySdk;
  private final boolean myFlexmojos;
  private final CompilerOptions myModuleLevelCompilerOptions;
  private final CompilerOptions myProjectLevelCompilerOptions;

  private CompilerConfigGenerator(final @NotNull Module module,
                                  final @NotNull FlexBuildConfiguration bc,
                                  final @NotNull CompilerOptions moduleLevelCompilerOptions,
                                  final @NotNull CompilerOptions projectLevelCompilerOptions) throws IOException {
    myModule = module;
    myBC = bc;
    myFlexUnit = BCUtils.isFlexUnitBC(myBC);
    myCSS = BCUtils.isRuntimeStyleSheetBC(bc);
    mySdk = bc.getSdk();
    if (mySdk == null) {
      throw new IOException(FlexCommonBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), module.getName()));
    }
    myFlexmojos = mySdk.getSdkType() == FlexmojosSdkType.getInstance();
    myModuleLevelCompilerOptions = moduleLevelCompilerOptions;
    myProjectLevelCompilerOptions = projectLevelCompilerOptions;
  }

  public static VirtualFile getOrCreateConfigFile(final Module module, final FlexBuildConfiguration bc) throws IOException {
    final CompilerConfigGenerator generator =
      new CompilerConfigGenerator(module, bc,
                                  FlexBuildConfigurationManager.getInstance(module).getModuleLevelCompilerOptions(),
                                  FlexProjectLevelCompilerOptionsHolder.getInstance(module.getProject()).getProjectLevelCompilerOptions());
    String text = generator.generateConfigFileText();

    if (bc.isTempBCForCompilation()) {
      final FlexBuildConfiguration originalBC = FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(bc.getName());
      final boolean makeExternalLibsMerged =
        BCUtils.isFlexUnitBC(bc) || (originalBC != null && originalBC.getOutputType() == OutputType.Library);
      final boolean makeIncludedLibsMerged = BCUtils.isRuntimeStyleSheetBC(bc);
      text = FlexCompilerConfigFileUtilBase.mergeWithCustomConfigFile(text, bc.getCompilerOptions().getAdditionalConfigFilePath(),
                                                                      makeExternalLibsMerged, makeIncludedLibsMerged);
    }

    final String name =
      getConfigFileName(module, bc.getName(), StringUtil.toLowerCase(PlatformUtils.getPlatformPrefix()), BCUtils.getBCSpecifier(bc));
    return getOrCreateConfigFile(name, text);
  }

  private String generateConfigFileText() throws IOException {
    final Element rootElement =
      new Element(FlexCompilerConfigFileUtilBase.FLEX_CONFIG, FlexApplicationComponent.HTTP_WWW_ADOBE_COM_2006_FLEX_CONFIG);

    addMandatoryOptions(rootElement);
    addSourcePaths(rootElement);
    if (!myFlexmojos) {
      handleOptionsWithSpecialValues(rootElement);
      addNamespaces(rootElement);
      addRootsFromSdk(rootElement);
    }
    addLibs(rootElement);
    addOtherOptions(rootElement);
    addInputOutputPaths(rootElement);

    return JDOMUtil.writeElement(rootElement);
  }

  private void addMandatoryOptions(final Element rootElement) {
    if (!BCUtils.isRLMTemporaryBC(myBC) && !BCUtils.isRuntimeStyleSheetBC(myBC) &&
        BCUtils.canHaveRLMsAndRuntimeStylesheets(myBC) && myBC.getRLMs().size() > 0) {
      addOption(rootElement, CompilerOptionInfo.LINK_REPORT_INFO, getLinkReportFilePath(myModule, myBC.getName()));
    }

    if (BCUtils.isRLMTemporaryBC(myBC) && !myBC.getOptimizeFor().isEmpty()) {
      final String customLinkReportPath = getCustomLinkReportPath(myModule, myBC);
      final String linkReportPath = StringUtil.notNullize(customLinkReportPath, getLinkReportFilePath(myModule, myBC.getName()));
      addOption(rootElement, CompilerOptionInfo.LOAD_EXTERNS_INFO, linkReportPath);
    }

    addOption(rootElement, CompilerOptionInfo.WARN_NO_CONSTRUCTOR_INFO, "false");
    if (myFlexmojos) return;

    final BuildConfigurationNature nature = myBC.getNature();
    final String targetPlayer = nature.isWebPlatform()
                                ? myBC.getDependencies().getTargetPlayer()
                                : FlexCommonUtils.getMaximumTargetPlayer(mySdk.getHomePath());
    addOption(rootElement, CompilerOptionInfo.TARGET_PLAYER_INFO, targetPlayer);

    if (FlexSdkUtils.isAirSdkWithoutFlex(mySdk) || StringUtil.compareVersionNumbers(mySdk.getVersionString(), "4.5") >= 0) {
      final String swfVersion;
      if (nature.isWebPlatform()) {
        swfVersion = FlexCommonUtils.getSwfVersionForTargetPlayer(targetPlayer);
      }
      else {
        String airVersion = getAirVersionIfCustomDescriptor(myBC);
        if (airVersion == null) {
          airVersion = FlexCommonUtils.getAirVersion(mySdk.getHomePath(), mySdk.getVersionString());
        }
        swfVersion = airVersion != null
                     ? FlexCommonUtils.getSwfVersionForAirVersion(airVersion)
                     : FlexCommonUtils.getSwfVersionForSdk_THE_WORST_WAY(mySdk.getVersionString());
      }

      addOption(rootElement, CompilerOptionInfo.SWF_VERSION_INFO, swfVersion);
    }

    if (nature.isMobilePlatform()) {
      addOption(rootElement, CompilerOptionInfo.MOBILE_INFO, "true");
      addOption(rootElement, CompilerOptionInfo.PRELOADER_INFO, "spark.preloaders.SplashScreen");
    }

    if (!FlexSdkUtils.isAirSdkWithoutFlex(mySdk)) {
      final String accessible = nature.isMobilePlatform() ? "false"
                                                          : StringUtil.compareVersionNumbers(mySdk.getVersionString(), "4") >= 0 ? "true"
                                                                                                                                 : "false";
      addOption(rootElement, CompilerOptionInfo.ACCESSIBLE_INFO, accessible);

      final String fontManagers = StringUtil.compareVersionNumbers(mySdk.getVersionString(), "4") >= 0
                                  ? "flash.fonts.JREFontManager" + CompilerOptionInfo.LIST_ENTRIES_SEPARATOR +
                                    "flash.fonts.BatikFontManager" + CompilerOptionInfo.LIST_ENTRIES_SEPARATOR +
                                    "flash.fonts.AFEFontManager" + CompilerOptionInfo.LIST_ENTRIES_SEPARATOR +
                                    "flash.fonts.CFFFontManager"

                                  : "flash.fonts.JREFontManager" + CompilerOptionInfo.LIST_ENTRIES_SEPARATOR +
                                    "flash.fonts.AFEFontManager" + CompilerOptionInfo.LIST_ENTRIES_SEPARATOR +
                                    "flash.fonts.BatikFontManager";
      addOption(rootElement, CompilerOptionInfo.FONT_MANAGERS_INFO, fontManagers);

      addOption(rootElement, CompilerOptionInfo.STATIC_RSLS_INFO, "false");
    }
  }

  @Nullable
  private static String getAirVersionIfCustomDescriptor(final FlexBuildConfiguration bc) {
    if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
      final AirDesktopPackagingOptions packagingOptions = bc.getAirDesktopPackagingOptions();
      if (!packagingOptions.isUseGeneratedDescriptor()) {
        return FlexCommonUtils.parseAirVersionFromDescriptorFile(packagingOptions.getCustomDescriptorPath());
      }
    }
    else if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      final AndroidPackagingOptions androidOptions = bc.getAndroidPackagingOptions();
      final IosPackagingOptions iosPackagingOptions = bc.getIosPackagingOptions();

      // if at least one of descriptors is generated - return null
      if (androidOptions.isEnabled() && androidOptions.isUseGeneratedDescriptor() ||
          iosPackagingOptions.isEnabled() && iosPackagingOptions.isUseGeneratedDescriptor()) {
        return null;
      }

      String androidAirVersion = null;
      String iosAirVersion = null;

      if (androidOptions.isEnabled() && !androidOptions.isUseGeneratedDescriptor()) {
        androidAirVersion = FlexCommonUtils.parseAirVersionFromDescriptorFile(androidOptions.getCustomDescriptorPath());
      }

      if (iosPackagingOptions.isEnabled() && !iosPackagingOptions.isUseGeneratedDescriptor()) {
        iosAirVersion = FlexCommonUtils.parseAirVersionFromDescriptorFile(iosPackagingOptions.getCustomDescriptorPath());
      }

      if (androidAirVersion == null) return iosAirVersion;
      if (iosAirVersion == null) return androidAirVersion;

      // return minimal
      return StringUtil.compareVersionNumbers(androidAirVersion, iosAirVersion) > 0 ? iosAirVersion : androidAirVersion;
    }
    return null;
  }

  @Nullable
  private static String getCustomLinkReportPath(final Module module, final FlexBuildConfiguration rlmBC) {
    final FlexBuildConfiguration appBC = FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(rlmBC.getName());
    if (appBC != null) {
      final List<String> linkReports = FlexCommonUtils.getOptionValues(appBC.getCompilerOptions().getAdditionalOptions(), "link-report");
      if (!linkReports.isEmpty()) {
        final String path = linkReports.get(0);
        if (new File(path).isFile()) return path;
        final String absPath = FlexUtils.getFlexCompilerWorkDirPath(module.getProject(), null) + "/" + path;
        if (new File(absPath).isFile()) return absPath;
      }
      else {
        final String configFilePath = appBC.getCompilerOptions().getAdditionalConfigFilePath();
        if (!configFilePath.isEmpty()) {
          final VirtualFile configFile = LocalFileSystem.getInstance().findFileByPath(configFilePath);
          if (configFile != null) {
            try {
              String path = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><link-report>");
              if (path != null) {
                path = path.trim();
                if (new File(path).isFile()) return path;
                // I have no idea why Flex compiler treats path relative to source root for "link-report" option
                for (VirtualFile srcRoot : ModuleRootManager.getInstance(module).getSourceRoots()) {
                  final String absPath = srcRoot.getPath() + "/" + path;
                  if (new File(absPath).isFile()) return absPath;
                }
              }
            }
            catch (IOException ignore) {/*ignore*/}
          }
        }
      }
    }

    return null;
  }

  /**
   * Adds options that get incorrect default values inside compiler code if not set explicitly.
   */
  private void handleOptionsWithSpecialValues(final Element rootElement) {
    for (final CompilerOptionInfo info : CompilerOptionInfo.getOptionsWithSpecialValues()) {
      final Pair<String, ValueSource> valueAndSource = getValueAndSource(info);
      final boolean themeForPureAS = myBC.isPureAs() && "compiler.theme".equals(info.ID);
      if (valueAndSource.second == ValueSource.GlobalDefault && (!valueAndSource.first.isEmpty() || themeForPureAS)) {
        // do not add empty preloader to Web/Desktop, let compiler take default itself (mx.preloaders.SparkDownloadProgressBar when -compatibility-version >= 4.0 and mx.preloaders.DownloadProgressBar when -compatibility-version < 4.0)
        addOption(rootElement, info, valueAndSource.first);
      }
    }
  }

  private void addNamespaces(final Element rootElement) {
    final StringBuilder namespaceBuilder = new StringBuilder();
    FlexSdkUtils.processStandardNamespaces(myBC, (namespace, relativePath) -> {
      if (namespaceBuilder.length() > 0) {
        namespaceBuilder.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
      }
      namespaceBuilder.append(namespace).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
        .append(CompilerOptionInfo.FLEX_SDK_MACRO + "/").append(relativePath);
    });

    if (namespaceBuilder.length() == 0) return;
    final CompilerOptionInfo info = CompilerOptionInfo.getOptionInfo("compiler.namespaces.namespace");
    addOption(rootElement, info, namespaceBuilder.toString());
  }

  private void addRootsFromSdk(final Element rootElement) {
    final CompilerOptionInfo localeInfo = CompilerOptionInfo.getOptionInfo("compiler.locale");
    if (!getValueAndSource(localeInfo).first.isEmpty()) {
      addOption(rootElement, CompilerOptionInfo.LIBRARY_PATH_INFO, mySdk.getHomePath() + "/frameworks/locale/{locale}");
    }

    final Map<String, String> libNameToRslInfo = new THashMap<>();

    for (final String swcUrl : mySdk.getRootProvider().getUrls(OrderRootType.CLASSES)) {
      final String swcPath = VirtualFileManager.extractPath(StringUtil.trimEnd(swcUrl, JarFileSystem.JAR_SEPARATOR));
      if (!StringUtil.toLowerCase(swcPath).endsWith(".swc")) {
        Logger.getInstance(CompilerConfigGenerator.class.getName()).warn("Unexpected URL in Flex SDK classes: " + swcUrl);
        continue;
      }

      LinkageType linkageType = BCUtils.getSdkEntryLinkageType(swcPath, myBC);

      // check applicability
      if (linkageType == null) continue;
      // resolve default
      if (linkageType == LinkageType.Default) linkageType = myBC.getDependencies().getFrameworkLinkage();
      if (linkageType == LinkageType.Default) {
        linkageType = FlexCommonUtils.getDefaultFrameworkLinkage(mySdk.getVersionString(), myBC.getNature());
      }
      if (myCSS && linkageType == LinkageType.Include) linkageType = LinkageType.Merged;

      final CompilerOptionInfo info = linkageType == LinkageType.Merged ? CompilerOptionInfo.LIBRARY_PATH_INFO :
                                      linkageType == LinkageType.RSL ? CompilerOptionInfo.LIBRARY_PATH_INFO :
                                      linkageType == LinkageType.External ? CompilerOptionInfo.EXTERNAL_LIBRARY_INFO :
                                      linkageType == LinkageType.Include ? CompilerOptionInfo.INCLUDE_LIBRARY_INFO :
                                      null;

      assert info != null : swcPath + ": " + linkageType.getShortText();

      addOption(rootElement, info, swcPath);

      if (linkageType == LinkageType.RSL) {
        final List<String> rslUrls = RslUtil.getRslUrls(mySdk.getHomePath(), swcPath);
        if (rslUrls.isEmpty()) continue;

        final StringBuilder rslBuilder = new StringBuilder();
        final String firstUrl = rslUrls.get(0);
        rslBuilder
          .append(swcPath)
          .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
          .append(firstUrl)
          .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        if (firstUrl.startsWith("http://")) {
          rslBuilder.append("http://fpdownload.adobe.com/pub/swz/crossdomain.xml");
        }

        if (rslUrls.size() > 1) {
          final String secondUrl = rslUrls.get(1);
          rslBuilder
            .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
            .append(secondUrl)
            .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
          if (secondUrl.startsWith("http://")) {
            rslBuilder.append("http://fpdownload.adobe.com/pub/swz/crossdomain.xml");
          }
        }

        final String swcName = PathUtil.getFileName(swcPath);
        final String libName = swcName.substring(0, swcName.length() - ".swc".length());
        libNameToRslInfo.put(libName, rslBuilder.toString());
      }
    }

    if (myBC.getNature().isLib()) {
      final String theme = getValueAndSource(CompilerOptionInfo.getOptionInfo("compiler.theme")).first;
      if (theme != null && StringUtil.toLowerCase(theme).endsWith(".swc")) {
        addOption(rootElement, CompilerOptionInfo.LIBRARY_PATH_INFO, theme);
      }
    }

    addRslInfo(rootElement, libNameToRslInfo);
  }

  private void addRslInfo(final Element rootElement, final Map<String, String> libNameToRslInfo) {
    if (libNameToRslInfo.isEmpty()) return;

    // RSL order is important!
    for (final String libName : LIB_ORDER) {
      final String rslInfo = libNameToRslInfo.remove(libName);
      if (rslInfo != null) {
        final CompilerOptionInfo option = StringUtil.split(rslInfo, CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, true, false).size() == 3
                                          ? CompilerOptionInfo.RSL_ONE_URL_PATH_INFO
                                          : CompilerOptionInfo.RSL_TWO_URLS_PATH_INFO;
        addOption(rootElement, option, rslInfo);
      }
    }

    // now add other in random order, though up to Flex SDK 4.5.1 the map should be empty at this stage
    for (final String rslInfo : libNameToRslInfo.values()) {
      final CompilerOptionInfo option = StringUtil.split(rslInfo, CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, true, false).size() == 3
                                        ? CompilerOptionInfo.RSL_ONE_URL_PATH_INFO
                                        : CompilerOptionInfo.RSL_TWO_URLS_PATH_INFO;
      addOption(rootElement, option, rslInfo);
    }
  }

  private void addLibs(final Element rootElement) {
    for (final DependencyEntry entry : myBC.getDependencies().getEntries()) {
      LinkageType linkageType = entry.getDependencyType().getLinkageType();
      if (linkageType == LinkageType.Test) {
        if (myFlexUnit) {
          linkageType = LinkageType.Merged;
        }
        else {
          continue;
        }
      }
      if (myCSS && linkageType == LinkageType.Include) linkageType = LinkageType.Merged;

      if (entry instanceof BuildConfigurationEntry) {
        if (linkageType == LinkageType.LoadInRuntime) continue;

        final FlexBuildConfiguration dependencyBC = ((BuildConfigurationEntry)entry).findBuildConfiguration();
        if (dependencyBC != null && FlexCommonUtils.checkDependencyType(myBC.getOutputType(), dependencyBC.getOutputType(), linkageType)) {
          addLib(rootElement, dependencyBC.getActualOutputFilePath(), linkageType);
        }
      }
      else if (entry instanceof ModuleLibraryEntry) {
        final LibraryOrderEntry orderEntry =
          FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, ModuleRootManager.getInstance(myModule));
        if (orderEntry != null) {
          addLibraryRoots(rootElement, orderEntry.getRootFiles(OrderRootType.CLASSES), linkageType);
        }
      }
      else if (entry instanceof SharedLibraryEntry) {
        final Library library = FlexProjectRootsUtil.findOrderEntry(myModule.getProject(), (SharedLibraryEntry)entry);
        if (library != null) {
          addLibraryRoots(rootElement, library.getFiles((OrderRootType.CLASSES)), linkageType);
        }
      }
    }

    if (myFlexUnit) {
      final Collection<String> flexUnitLibNames = FlexCommonUtils
        .getFlexUnitSupportLibNames(myBC.getNature(), myBC.getDependencies().getComponentSet(),
                                    getPathToFlexUnitMainClass(myModule.getProject(), myBC.getNature(), myBC.getMainClass()));
      for (String libName : flexUnitLibNames) {
        final String libPath = FlexCommonUtils.getPathToBundledJar(libName);
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(libPath);
        assert file != null;
        addLibraryRoots(rootElement, new VirtualFile[]{file}, LinkageType.Merged);
      }
    }
  }

  private void addLibraryRoots(final Element rootElement, final VirtualFile[] libClassRoots, final LinkageType linkageType) {
    for (VirtualFile libFile : libClassRoots) {
      libFile = FlexCompilationUtils.getRealFile(libFile);
      if (libFile == null) continue;

      if (libFile.isDirectory()) {
        addOption(rootElement, CompilerOptionInfo.SOURCE_PATH_INFO, libFile.getPath());
      }
      else {
        if ("ane".equalsIgnoreCase(libFile.getExtension())) {
          addLib(rootElement, libFile.getPath(), LinkageType.External);
        }
        else if ("swc".equalsIgnoreCase(libFile.getExtension())) {
          // "airglobal.swc" and "playerglobal.swc" file names are hardcoded in Flex compiler
          // including libraries like "playerglobal-3.5.0.12683-9.swc" may lead to error at runtime like "VerifyError Error #1079: Native methods are not allowed in loaded code."
          // so here we just skip including such libraries in config file.
          // Compilation should be ok because base flexmojos config file contains correct reference to its copy in target/classes/libraries/playerglobal.swc
          final String libFileName = StringUtil.toLowerCase(libFile.getName());
          if (libFileName.startsWith("airglobal") && !libFileName.equals("airglobal.swc") ||
              libFileName.startsWith("playerglobal") && !libFileName.equals("playerglobal.swc")) {
            continue;
          }

          addLib(rootElement, libFile.getPath(), linkageType);
        }
      }
    }
  }

  private void addLib(final Element rootElement, final String swcPath, final LinkageType linkageType) {
    final CompilerOptionInfo info = linkageType == LinkageType.Merged || linkageType == LinkageType.RSL
                                    ? CompilerOptionInfo.LIBRARY_PATH_INFO
                                    : linkageType == LinkageType.External
                                      ? CompilerOptionInfo.EXTERNAL_LIBRARY_INFO
                                      : linkageType == LinkageType.Include
                                        ? CompilerOptionInfo.INCLUDE_LIBRARY_INFO
                                        : null;
    assert info != null : swcPath + ": " + linkageType;

    addOption(rootElement, info, swcPath);

    if (linkageType == LinkageType.RSL) {
      // todo add RSL URLs
    }
  }

  private void addSourcePaths(final Element rootElement) {
    final String localeValue = getValueAndSource(CompilerOptionInfo.getOptionInfo("compiler.locale")).first;
    final List<String> locales = StringUtil.split(localeValue, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
    // when adding source paths we respect locales set both in UI and in Additional compiler options
    locales.addAll(FlexCommonUtils.getOptionValues(myProjectLevelCompilerOptions.getAdditionalOptions(), "locale", "compiler.locale"));
    locales.addAll(FlexCommonUtils.getOptionValues(myModuleLevelCompilerOptions.getAdditionalOptions(), "locale", "compiler.locale"));
    locales.addAll(FlexCommonUtils.getOptionValues(myBC.getCompilerOptions().getAdditionalOptions(), "locale", "compiler.locale"));

    final Set<String> sourcePathsWithLocaleToken = new THashSet<>(); // Set - to avoid duplication of paths like "locale/{locale}"
    final List<String> sourcePathsWithoutLocaleToken = new LinkedList<>();

    for (final VirtualFile sourceRoot : ModuleRootManager.getInstance(myModule).getSourceRoots(includeTestRoots())) {
      if (locales.contains(sourceRoot.getName())) {
        sourcePathsWithLocaleToken.add(sourceRoot.getParent().getPath() + "/" + FlexCommonUtils.LOCALE_TOKEN);
      }
      else {
        sourcePathsWithoutLocaleToken.add(sourceRoot.getPath());
      }
    }

    final StringBuilder sourcePathBuilder = new StringBuilder();

    if (myCSS) {
      final String cssFolderPath = PathUtil.getParentPath(myBC.getMainClass());
      if (!sourcePathsWithoutLocaleToken.contains(cssFolderPath)) {
        sourcePathBuilder.append(cssFolderPath);
      }
    }

    for (final String sourcePath : sourcePathsWithLocaleToken) {
      if (sourcePathBuilder.length() > 0) {
        sourcePathBuilder.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
      }
      sourcePathBuilder.append(sourcePath);
    }

    for (final String sourcePath : sourcePathsWithoutLocaleToken) {
      if (sourcePathBuilder.length() > 0) {
        sourcePathBuilder.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
      }
      sourcePathBuilder.append(sourcePath);
    }

    addOption(rootElement, CompilerOptionInfo.SOURCE_PATH_INFO, sourcePathBuilder.toString());
  }

  private boolean includeTestRoots() {
    if (myFlexUnit) return true;
    if (myCSS) return false;
    if (myBC.getOutputType() != OutputType.Application) return false;

    final String path = FlexUtils.getPathToMainClassFile(myBC.getMainClass(), myModule);
    final VirtualFile file = path.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(path);
    return file != null && ModuleRootManager.getInstance(myModule).getFileIndex().isInTestSourceContent(file);
  }

  private void addOtherOptions(final Element rootElement) {
    final Map<String, String> options = new THashMap<>(myProjectLevelCompilerOptions.getAllOptions());
    options.putAll(myModuleLevelCompilerOptions.getAllOptions());
    options.putAll(myBC.getCompilerOptions().getAllOptions());

    final String addOptions = myProjectLevelCompilerOptions.getAdditionalOptions() + " " +
                              myModuleLevelCompilerOptions.getAdditionalOptions() + " " +
                              myBC.getCompilerOptions().getAdditionalOptions();
    final List<String> contextRootInAddOptions = FlexCommonUtils.getOptionValues(addOptions, "context-root", "compiler.context-root");

    if (options.get("compiler.context-root") == null && contextRootInAddOptions.isEmpty()) {
      final List<String> servicesInAddOptions = FlexCommonUtils.getOptionValues(addOptions, "services", "compiler.services");
      if (options.get("compiler.services") != null || !servicesInAddOptions.isEmpty()) {
        options.put("compiler.context-root", "");
      }
    }

    for (final Map.Entry<String, String> entry : options.entrySet()) {
      addOption(rootElement, CompilerOptionInfo.getOptionInfo(entry.getKey()), entry.getValue());
    }

    final String namespacesRaw = options.get("compiler.namespaces.namespace");
    if (namespacesRaw != null && myBC.getOutputType() == OutputType.Library) {
      final String namespaces = FlexUtils.replacePathMacros(namespacesRaw, myModule,
                                                            myFlexmojos ? "" : mySdk.getHomePath());
      final StringBuilder buf = new StringBuilder();
      for (final String listEntry : StringUtil.split(namespaces, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)) {
        final int tabIndex = listEntry.indexOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        assert tabIndex != -1 : namespaces;
        final String namespace = listEntry.substring(0, tabIndex);
        if (buf.length() > 0) buf.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
        buf.append(namespace);
      }

      if (buf.length() > 0) {
        addOption(rootElement, CompilerOptionInfo.INCLUDE_NAMESPACES_INFO, buf.toString());
      }
    }
  }

  private void addInputOutputPaths(final Element rootElement) throws IOException {
    if (myBC.getOutputType() == OutputType.Library) {
      addFilesIncludedInSwc(rootElement);

      if (!myFlexmojos) {
        addLibClasses(rootElement);
      }
    }
    else {
      final InfoFromConfigFile info =
        FlexCompilerConfigFileUtil.getInfoFromConfigFile(myBC.getCompilerOptions().getAdditionalConfigFilePath());

      final String pathToMainClassFile = myCSS
                                         ? myBC.getMainClass()
                                         : myFlexUnit
                                           ? getPathToFlexUnitMainClass(myModule.getProject(), myBC.getNature(), myBC.getMainClass())
                                           : FlexUtils.getPathToMainClassFile(myBC.getMainClass(), myModule);

      if (pathToMainClassFile.isEmpty() && info.getMainClass(myModule) == null && !ApplicationManager.getApplication().isUnitTestMode()) {
        throw new IOException(FlexCommonBundle.message("bc.incorrect.main.class", myBC.getMainClass(), myBC.getName(), myModule.getName()));
      }

      if (!pathToMainClassFile.isEmpty()) {
        addOption(rootElement, CompilerOptionInfo.MAIN_CLASS_INFO, FileUtil.toSystemIndependentName(pathToMainClassFile));
      }
    }

    addOption(rootElement, CompilerOptionInfo.OUTPUT_PATH_INFO, myBC.getActualOutputFilePath());
  }

  private void addFilesIncludedInSwc(final Element rootElement) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myModule.getProject()).getFileIndex();
    final CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(myModule.getProject());

    final Map<String, String> filePathToPathInSwc = new THashMap<>();

    for (String path : myBC.getCompilerOptions().getFilesToIncludeInSWC()) {
      final VirtualFile fileOrDir = LocalFileSystem.getInstance().findFileByPath(path);
      if (fileOrDir == null ||
          compilerConfiguration.isExcludedFromCompilation(fileOrDir) ||
          FileTypeManager.getInstance().isFileIgnored(fileOrDir)) {
        continue;
      }

      if (fileOrDir.isDirectory()) {
        final VirtualFile srcRoot = fileIndex.getModuleForFile(fileOrDir) == myModule ? fileIndex.getSourceRootForFile(fileOrDir) : null;
        final String baseRelativePath = srcRoot == null ? fileOrDir.getName() : VfsUtilCore.getRelativePath(fileOrDir, srcRoot, '/');
        assert baseRelativePath != null;

        VfsUtilCore.visitChildrenRecursively(fileOrDir, new VirtualFileVisitor<Void>() {
          @Override
          public boolean visitFile(@NotNull final VirtualFile file) {
            if (FileTypeManager.getInstance().isFileIgnored(file)) return false;

            if (!file.isDirectory() &&
                !FlexCommonUtils.isSourceFile(file.getName()) &&
                !compilerConfiguration.isExcludedFromCompilation(file)) {
              final String relativePath = VfsUtilCore.getRelativePath(file, fileOrDir, '/');
              final String pathInSwc = baseRelativePath.isEmpty() ? relativePath : baseRelativePath + "/" + relativePath;
              filePathToPathInSwc.put(file.getPath(), pathInSwc);
            }
            return true;
          }
        });
      }
      else {
        final VirtualFile srcRoot = fileIndex.getSourceRootForFile(fileOrDir);
        final String relativePath = srcRoot == null ? null : VfsUtilCore.getRelativePath(fileOrDir, srcRoot, '/');
        final String pathInSwc = StringUtil.notNullize(relativePath, fileOrDir.getName());
        filePathToPathInSwc.put(fileOrDir.getPath(), pathInSwc);
      }
    }

    for (Map.Entry<String, String> entry : filePathToPathInSwc.entrySet()) {
      final String value = entry.getValue() + CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR + entry.getKey();
      addOption(rootElement, CompilerOptionInfo.INCLUDE_FILE_INFO, value);
    }
  }

  private void addLibClasses(final Element rootElement) throws IOException {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myModule.getProject()).getFileIndex();
    final CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(myModule.getProject());
    final Ref<Boolean> noClasses = new Ref<>(true);

    for (final VirtualFile sourceRoot : ModuleRootManager.getInstance(myModule).getSourceRoots(false)) {
      fileIndex.iterateContentUnderDirectory(sourceRoot, file -> {
        if (file.isDirectory()) return true;
        if (!FlexCommonUtils.isSourceFile(file.getName())) return true;
        if (compilerConfiguration.isExcludedFromCompilation(file)) return true;

        final String packageText = VfsUtilCore.getRelativePath(file.getParent(), sourceRoot, '.');
        assert packageText != null : sourceRoot.getPath() + ": " + file.getPath();
        final String qName = (packageText.length() > 0 ? packageText + "." : "") + file.getNameWithoutExtension();

        if (isSourceFileWithPublicDeclaration(myModule, file, qName)) {
          addOption(rootElement, CompilerOptionInfo.INCLUDE_CLASSES_INFO, qName);
          noClasses.set(false);
        }

        return true;
      });
    }

    if (noClasses.get() &&
        myBC.getCompilerOptions().getFilesToIncludeInSWC().isEmpty() &&
        !ApplicationManager.getApplication().isUnitTestMode()) {
      throw new IOException(FlexCommonBundle.message("nothing.to.compile.in.library", myModule.getName(), myBC.getName()));
    }
  }

  private void addOption(final Element rootElement, final CompilerOptionInfo info, final String rawValue) {
    if (!info.isApplicable(mySdk.getVersionString(), myBC.getNature())) {
      return;
    }

    final String value = FlexUtils.replacePathMacros(rawValue, myModule, myFlexmojos ? "" : mySdk.getHomePath());

    final String pathInFlexConfig = info.ID.startsWith("compiler.debug") ? "compiler.debug" : info.ID;
    final List<String> elementNames = StringUtil.split(pathInFlexConfig, ".");
    Element parentElement = rootElement;

    for (int i1 = 0; i1 < elementNames.size() - 1; i1++) {
      parentElement = getOrCreateElement(parentElement, elementNames.get(i1));
    }

    final String elementName = elementNames.get(elementNames.size() - 1);

    switch (info.TYPE) {
      case Boolean, String, Int, File -> {
        final Element simpleElement = new Element(elementName, parentElement.getNamespace());
        simpleElement.setText(value);
        parentElement.addContent(simpleElement);
      }
      case List -> {
        if (info.LIST_ELEMENTS.length == 1) {
          final Element listHolderElement = getOrCreateElement(parentElement, elementName);
          for (final String listElementValue : StringUtil.split(value, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)) {
            final Element child = new Element(info.LIST_ELEMENTS[0].NAME, listHolderElement.getNamespace());
            child.setText(listElementValue);
            listHolderElement.addContent(child);
          }
        }
        else {
          for (final String listEntry : StringUtil.split(value, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)) {
            final Element repeatableListHolderElement = new Element(elementName, parentElement.getNamespace());

            final List<String> values = StringUtil.split(listEntry, CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, true, false);
            assert info.LIST_ELEMENTS.length == values.size() : info.ID + "=" + value;

            for (int i = 0; i < info.LIST_ELEMENTS.length; i++) {
              final Element child = new Element(info.LIST_ELEMENTS[i].NAME, repeatableListHolderElement.getNamespace());
              child.setText(values.get(i));
              repeatableListHolderElement.addContent(child);
            }

            parentElement.addContent(repeatableListHolderElement);
          }
        }
      }
      default -> {
        assert false : info.DISPLAY_NAME;
      }
    }
  }

  private static Element getOrCreateElement(final Element parentElement, final String elementName) {
    Element child = parentElement.getChild(elementName, parentElement.getNamespace());
    if (child == null) {
      child = new Element(elementName, parentElement.getNamespace());
      parentElement.addContent(child);
    }
    return child;
  }

  private Pair<String, ValueSource> getValueAndSource(final CompilerOptionInfo info) {
    assert !info.isGroup() : info.DISPLAY_NAME;

    final String bcLevelValue = myBC.getCompilerOptions().getOption(info.ID);
    if (bcLevelValue != null) return Pair.create(bcLevelValue, ValueSource.BC);

    final String moduleLevelValue = myModuleLevelCompilerOptions.getOption(info.ID);
    if (moduleLevelValue != null) return Pair.create(moduleLevelValue, ValueSource.ModuleDefault);

    final String projectLevelValue = myProjectLevelCompilerOptions.getOption(info.ID);
    if (projectLevelValue != null) return Pair.create(projectLevelValue, ValueSource.ProjectDefault);

    return Pair.create(info.getDefaultValue(mySdk.getVersionString(), myBC.getNature(), myBC.getDependencies().getComponentSet()),
                       ValueSource.GlobalDefault);
  }

  private static VirtualFile getOrCreateConfigFile(final String fileName, final String text) throws IOException {

    final VirtualFile existingConfigFile = FlexCompilationUtils.refreshAndFindFileInWriteAction(
      FlexCommonUtils.getTempFlexConfigsDirPath() + "/" + fileName);

    if (existingConfigFile != null && existingConfigFile.isValid() &&
        Arrays.equals(text.getBytes(StandardCharsets.UTF_8), existingConfigFile.contentsToByteArray())) {
      return existingConfigFile;
    }

    final Ref<VirtualFile> fileRef = new Ref<>();
    final Ref<IOException> error = new Ref<>();
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        fileRef.set(ApplicationManager.getApplication().runWriteAction(new NullableComputable<>() {
          @Override
          public VirtualFile compute() {
            try {
              final String baseDirPath = FlexCommonUtils.getTempFlexConfigsDirPath();
              final VirtualFile baseDir = VfsUtil.createDirectories(baseDirPath);

              VirtualFile configFile = baseDir.findChild(fileName);
              if (configFile == null) {
                configFile = baseDir.createChildData(this, fileName);
              }
              VfsUtil.saveText(configFile, text);
              return configFile;
            }
            catch (IOException ex) {
              error.set(ex);
            }
            return null;
          }
        }));
      }
    };

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      runnable.run();
    }
    else {
      ApplicationManager.getApplication().invokeAndWait(runnable);
    }

    if (!error.isNull()) {
      throw error.get();
    }
    return fileRef.get();
  }

  private static String getConfigFileName(final Module module, final @Nullable String bcName,
                                          final String prefix, final @Nullable String postfix) {
    final String hash1 = StringUtil.toUpperCase(Integer.toHexString((SystemProperties.getUserName() + module.getProject().getName()).hashCode()));
    final String hash2 = StringUtil.toUpperCase(Integer.toHexString((module.getName() + StringUtil.notNullize(bcName)).hashCode()));
    return prefix + "-" + hash1 + "-" + hash2 + (postfix == null ? ".xml" : ("-" + postfix.replace(' ', '-') + ".xml"));
  }

  private static String getLinkReportFilePath(final Module module, final String bcName) {
    final String fileName = getConfigFileName(module, bcName, StringUtil.toLowerCase(PlatformUtils.getPlatformPrefix()), "link-report");
    return FlexCommonUtils.getTempFlexConfigsDirPath() + "/" + fileName;
  }

  private static boolean isSourceFileWithPublicDeclaration(final Module module, final VirtualFile file, final String qName) {
    return JavaScriptSupportLoader.isMxmlOrFxgFile(file) ||
           ReadAction.compute(() -> {
             // we include file in compilation if it has (or intended to have) some public declaration (class, namespace, function) which is equivalent to having JSPackageStatement declaration.
             // But first we try to find it in JSQualifiedElementIndex because it is faster.
             final Collection<JSQualifiedNamedElement> elements = StubIndex
               .getElements(JSQualifiedElementIndex.KEY, qName, module.getProject(), GlobalSearchScope.moduleScope(module),
                            JSQualifiedNamedElement.class);
             if (elements.isEmpty()) {
               // If SomeClass.as contains IncorrectClass definition - we want to include this class into compilation so that compilation fails.
               final PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(file);
               return psiFile != null && PsiTreeUtil.getChildOfType(psiFile, JSPackageStatement.class) != null;
             }
             else {
               return true;
             }
           });
  }

  private static String getPathToFlexUnitMainClass(final Project project,
                                                   final BuildConfigurationNature nature,
                                                   final String mainClass) {
    return FlexUnitPrecompileTask.getPathToFlexUnitTempDirectory(project) +
           "/" + mainClass + FlexCommonUtils.getFlexUnitLauncherExtension(nature);
  }
}
