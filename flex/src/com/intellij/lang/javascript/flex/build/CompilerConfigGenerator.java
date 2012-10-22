package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.TargetPlayerUtils;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitPrecompileTask;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.ValueSource;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.flex.sdk.RslUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.stubs.JSQualifiedElementIndex;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PairConsumer;
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
import java.util.*;

public class CompilerConfigGenerator {

  private static final String[] LIB_ORDER =
    {"framework", "textLayout", "osmf", "spark", "sparkskins", "rpc", "charts", "spark_dmv", "mx", "advancedgrids"};

  private final Module myModule;
  private final FlexIdeBuildConfiguration myBC;
  private final boolean myFlexUnit;
  private final boolean myCSS;
  private final Sdk mySdk;
  private final boolean myFlexmojos;
  private final CompilerOptions myModuleLevelCompilerOptions;
  private final CompilerOptions myProjectLevelCompilerOptions;

  private CompilerConfigGenerator(final @NotNull Module module,
                                  final @NotNull FlexIdeBuildConfiguration bc,
                                  final @NotNull CompilerOptions moduleLevelCompilerOptions,
                                  final @NotNull CompilerOptions projectLevelCompilerOptions) throws IOException {
    myModule = module;
    myBC = bc;
    myFlexUnit = BCUtils.isFlexUnitBC(myBC);
    myCSS = BCUtils.isRuntimeStyleSheetBC(bc);
    mySdk = bc.getSdk();
    if (mySdk == null) {
      throw new IOException(FlexBundle.message("sdk.not.set.for.bc.0.of.module.1", bc.getName(), module.getName()));
    }
    myFlexmojos = mySdk.getSdkType() == FlexmojosSdkType.getInstance();
    myModuleLevelCompilerOptions = moduleLevelCompilerOptions;
    myProjectLevelCompilerOptions = projectLevelCompilerOptions;
  }

  public static VirtualFile getOrCreateConfigFile(final Module module, final FlexIdeBuildConfiguration bc) throws IOException {
    final CompilerConfigGenerator generator =
      new CompilerConfigGenerator(module, bc,
                                  FlexBuildConfigurationManager.getInstance(module).getModuleLevelCompilerOptions(),
                                  FlexProjectLevelCompilerOptionsHolder.getInstance(module.getProject()).getProjectLevelCompilerOptions());
    String text = generator.generateConfigFileText();

    if (bc.isTempBCForCompilation()) {
      final FlexIdeBuildConfiguration originalBC = FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(bc.getName());
      final boolean makeExternalLibsMerged =
        BCUtils.isFlexUnitBC(bc) || (originalBC != null && originalBC.getOutputType() == OutputType.Library);
      final boolean makeIncludedLibsMerged = BCUtils.isRuntimeStyleSheetBC(bc);
      text = FlexCompilerConfigFileUtil.mergeWithCustomConfigFile(text, bc.getCompilerOptions().getAdditionalConfigFilePath(),
                                                                  makeExternalLibsMerged, makeIncludedLibsMerged);
    }

    final String name =
      getConfigFileName(module, bc.getName(), PlatformUtils.getPlatformPrefix().toLowerCase(), BCUtils.getBCSpecifier(bc));
    return getOrCreateConfigFile(name, text);
  }

  private String generateConfigFileText() throws IOException {
    final Element rootElement =
      new Element(FlexCompilerConfigFileUtil.FLEX_CONFIG, FlexApplicationComponent.HTTP_WWW_ADOBE_COM_2006_FLEX_CONFIG);

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

    return JDOMUtil.writeElement(rootElement, "\n");
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
                                : TargetPlayerUtils.getMaximumTargetPlayer(mySdk.getHomePath());
    addOption(rootElement, CompilerOptionInfo.TARGET_PLAYER_INFO, targetPlayer);

    if (StringUtil.compareVersionNumbers(mySdk.getVersionString(), "4.5") >= 0) {
      final String swfVersion = nature.isWebPlatform() ? getSwfVersionForTargetPlayer(targetPlayer)
                                                       : getSwfVersionForSdk(mySdk.getVersionString());
      addOption(rootElement, CompilerOptionInfo.SWF_VERSION_INFO, swfVersion);
    }

    if (nature.isMobilePlatform()) {
      addOption(rootElement, CompilerOptionInfo.MOBILE_INFO, "true");
      addOption(rootElement, CompilerOptionInfo.PRELOADER_INFO, "spark.preloaders.SplashScreen");
    }

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

  @Nullable
  private static String getCustomLinkReportPath(final Module module, final FlexIdeBuildConfiguration rlmBC) {
    final FlexIdeBuildConfiguration appBC = FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(rlmBC.getName());
    if (appBC != null) {
      final List<String> linkReports = FlexUtils.getOptionValues(appBC.getCompilerOptions().getAdditionalOptions(), "link-report");
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

  public static String getSwfVersionForTargetPlayer(final String targetPlayer) {
    if (StringUtil.compareVersionNumbers(targetPlayer, "11.5") >= 0) return "18";
    if (StringUtil.compareVersionNumbers(targetPlayer, "11.4") >= 0) return "17";
    if (StringUtil.compareVersionNumbers(targetPlayer, "11.3") >= 0) return "16";
    if (StringUtil.compareVersionNumbers(targetPlayer, "11.2") >= 0) return "15";
    if (StringUtil.compareVersionNumbers(targetPlayer, "11.1") >= 0) return "14";
    if (StringUtil.compareVersionNumbers(targetPlayer, "11") >= 0) return "13";
    if (StringUtil.compareVersionNumbers(targetPlayer, "10.3") >= 0) return "12";
    if (StringUtil.compareVersionNumbers(targetPlayer, "10.2") >= 0) return "11";
    if (StringUtil.compareVersionNumbers(targetPlayer, "10.1") >= 0) return "10";
    return "9";
  }

  public static String getSwfVersionForSdk(final String sdkVersion) {
    if (StringUtil.compareVersionNumbers(sdkVersion, "4.6") >= 0) return "14";
    if (StringUtil.compareVersionNumbers(sdkVersion, "4.5") >= 0) return "11";
    assert false : sdkVersion;
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
    FlexSdkUtils.processStandardNamespaces(myBC, new PairConsumer<String, String>() {
      @Override
      public void consume(final String namespace, final String relativePath) {
        if (namespaceBuilder.length() > 0) {
          namespaceBuilder.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
        }
        namespaceBuilder.append(namespace).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
          .append(CompilerOptionInfo.FLEX_SDK_MACRO + "/").append(relativePath);
      }
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

    final Map<String, String> libNameToRslInfo = new THashMap<String, String>();

    for (final String swcUrl : mySdk.getRootProvider().getUrls(OrderRootType.CLASSES)) {
      final String swcPath = VirtualFileManager.extractPath(StringUtil.trimEnd(swcUrl, JarFileSystem.JAR_SEPARATOR));
      if (!swcPath.toLowerCase().endsWith(".swc")) {
        Logger.getInstance(CompilerConfigGenerator.class.getName()).warn("Unexpected URL in Flex SDK classes: " + swcUrl);
        continue;
      }

      LinkageType linkageType = BCUtils.getSdkEntryLinkageType(swcPath, myBC);

      // check applicability
      if (linkageType == null) continue;
      // resolve default
      if (linkageType == LinkageType.Default) linkageType = myBC.getDependencies().getFrameworkLinkage();
      if (linkageType == LinkageType.Default) linkageType = BCUtils.getDefaultFrameworkLinkage(mySdk.getVersionString(), myBC.getNature());
      if (myCSS && linkageType == LinkageType.Include) linkageType = LinkageType.Merged;

      final CompilerOptionInfo info = linkageType == LinkageType.Merged ? CompilerOptionInfo.LIBRARY_PATH_INFO :
                                      linkageType == LinkageType.RSL ? CompilerOptionInfo.LIBRARY_PATH_INFO :
                                      linkageType == LinkageType.External ? CompilerOptionInfo.EXTERNAL_LIBRARY_INFO :
                                      linkageType == LinkageType.Include ? CompilerOptionInfo.INCLUDE_LIBRARY_INFO :
                                      null;

      assert info != null : swcPath + ": " + linkageType.getShortText();

      addOption(rootElement, info, swcPath);

      if (linkageType == LinkageType.RSL) {
        final List<String> rslUrls = RslUtil.getRslUrls(mySdk, swcPath);
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

        final FlexIdeBuildConfiguration dependencyBC = ((BuildConfigurationEntry)entry).findBuildConfiguration();
        if (dependencyBC != null && FlexCompiler.checkDependencyType(myBC, dependencyBC, linkageType)) {
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
      String unitTestingSupportSwc = FlexUtils.getPathToBundledJar(FlexUnitPrecompileTask.getSupportLibraryName(myBC));
      VirtualFile file = LocalFileSystem.getInstance().findFileByPath(unitTestingSupportSwc);
      assert file != null;
      addLibraryRoots(rootElement, new VirtualFile[]{file}, LinkageType.Merged);
    }
  }

  private void addLibraryRoots(final Element rootElement, final VirtualFile[] libClassRoots, final LinkageType linkageType) {
    for (VirtualFile libFile : libClassRoots) {
      libFile = FlexCompilerHandler.getRealFile(libFile);
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
          final String libFileName = libFile.getName().toLowerCase();
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
    locales.addAll(FlexUtils.getOptionValues(myProjectLevelCompilerOptions.getAdditionalOptions(), "locale", "compiler.locale"));
    locales.addAll(FlexUtils.getOptionValues(myModuleLevelCompilerOptions.getAdditionalOptions(), "locale", "compiler.locale"));
    locales.addAll(FlexUtils.getOptionValues(myBC.getCompilerOptions().getAdditionalOptions(), "locale", "compiler.locale"));

    final Set<String> sourcePathsWithLocaleToken = new THashSet<String>(); // Set - to avoid duplication of paths like "locale/{locale}"
    final List<String> sourcePathsWithoutLocaleToken = new LinkedList<String>();

    for (final VirtualFile sourceRoot : ModuleRootManager.getInstance(myModule).getSourceRoots(includeTestRoots())) {
      if (locales.contains(sourceRoot.getName())) {
        sourcePathsWithLocaleToken.add(sourceRoot.getParent().getPath() + "/" + FlexCompilerHandler.LOCALE_TOKEN);
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
    final Map<String, String> options = new THashMap<String, String>(myProjectLevelCompilerOptions.getAllOptions());
    options.putAll(myModuleLevelCompilerOptions.getAllOptions());
    options.putAll(myBC.getCompilerOptions().getAllOptions());

    final String addOptions = myProjectLevelCompilerOptions.getAdditionalOptions() + " " +
                              myModuleLevelCompilerOptions.getAdditionalOptions() + " " +
                              myBC.getCompilerOptions().getAdditionalOptions();
    final List<String> contextRootInAddOptions = FlexUtils.getOptionValues(addOptions, "context-root", "compiler.context-root");

    if (options.get("compiler.context-root") == null && contextRootInAddOptions.isEmpty()) {
      final List<String> servicesInAddOptions = FlexUtils.getOptionValues(addOptions, "services", "compiler.services");
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

      final String pathToMainClassFile = myCSS ? myBC.getMainClass()
                                               : myFlexUnit ? FlexUtils.getPathToFlexUnitTempDirectory(myModule.getProject().getName())
                                                              + "/" + myBC.getMainClass()
                                                              + FlexUnitPrecompileTask.getFlexUnitLauncherExtension(myBC)
                                                            : FlexUtils.getPathToMainClassFile(myBC.getMainClass(), myModule);

      if (pathToMainClassFile.isEmpty() && info.getMainClass(myModule) == null && !ApplicationManager.getApplication().isUnitTestMode()) {
        throw new IOException(FlexBundle.message("bc.incorrect.main.class", myBC.getMainClass(), myBC.getName(), myModule.getName()));
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

    final Map<String, String> filePathToPathInSwc = new THashMap<String, String>();

    for (String path : myBC.getCompilerOptions().getFilesToIncludeInSWC()) {
      final VirtualFile fileOrDir = LocalFileSystem.getInstance().findFileByPath(path);
      if (fileOrDir == null || compilerConfiguration.isExcludedFromCompilation(fileOrDir)) continue;

      if (fileOrDir.isDirectory()) {
        final VirtualFile srcRoot = fileIndex.getSourceRootForFile(fileOrDir);
        final String baseRelativePath = srcRoot == null ? fileOrDir.getName() : VfsUtilCore.getRelativePath(fileOrDir, srcRoot, '/');
        assert baseRelativePath != null;

        VfsUtilCore.visitChildrenRecursively(fileOrDir, new VirtualFileVisitor() {
          @Override
          public boolean visitFile(@NotNull final VirtualFile file) {
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
    final Ref<Boolean> noClasses = new Ref<Boolean>(true);

    for (final VirtualFile sourceRoot : ModuleRootManager.getInstance(myModule).getSourceRoots(false)) {
      fileIndex.iterateContentUnderDirectory(sourceRoot, new ContentIterator() {
        @Override
        public boolean processFile(final VirtualFile file) {
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
        }
      });
    }

    if (noClasses.get() && !ApplicationManager.getApplication().isUnitTestMode()) {
      throw new IOException(FlexBundle.message("nothing.to.compile.in.library", myModule.getName(), myBC.getName()));
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
      case Boolean:
      case String:
      case Int:
      case File:
        final Element simpleElement = new Element(elementName, parentElement.getNamespace());
        simpleElement.setText(value);
        parentElement.addContent(simpleElement);
        break;
      case List:
        if (info.LIST_ELEMENTS.length == 1) {
          final Element listHolderElement = getOrCreateElement(parentElement, elementName);
          for (final String listElementValue : StringUtil.split(value, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)) {
            final Element child = new Element(info.LIST_ELEMENTS[0].NAME, listHolderElement.getNamespace());
            child.setText(listElementValue);
            listHolderElement.addContent(child);
          }
        }
        else {
          for (final String listEntry : StringUtil.split(value, String.valueOf(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR))) {
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
        break;
      default:
        assert false : info.DISPLAY_NAME;
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

    final VirtualFile existingConfigFile = FlexCompilationManager.refreshAndFindFileInWriteAction(
      FlexUtils.getTempFlexConfigsDirPath() + "/" + fileName);

    if (existingConfigFile != null && existingConfigFile.isValid() &&
        Arrays.equals(text.getBytes(), existingConfigFile.contentsToByteArray())) {
      return existingConfigFile;
    }

    final Ref<VirtualFile> fileRef = new Ref<VirtualFile>();
    final Ref<IOException> error = new Ref<IOException>();
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        fileRef.set(ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
          @Override
          public VirtualFile compute() {
            try {
              final String baseDirPath = FlexUtils.getTempFlexConfigsDirPath();
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
      ApplicationManager.getApplication()
        .invokeAndWait(runnable, ProgressManager.getInstance().getProgressIndicator().getModalityState());
    }

    if (!error.isNull()) {
      throw error.get();
    }
    return fileRef.get();
  }

  private static String getConfigFileName(final Module module, final @Nullable String bcName,
                                          final String prefix, final @Nullable String postfix) {
    final String hash1 = Integer.toHexString((SystemProperties.getUserName() + module.getProject().getName()).hashCode()).toUpperCase();
    final String hash2 = Integer.toHexString((module.getName() + StringUtil.notNullize(bcName)).hashCode()).toUpperCase();
    return prefix + "-" + hash1 + "-" + hash2 + (postfix == null ? ".xml" : ("-" + StringUtil.replaceChar(postfix, ' ', '-') + ".xml"));
  }

  private static String getLinkReportFilePath(final Module module, final String bcName) {
    final String fileName = getConfigFileName(module, bcName, PlatformUtils.getPlatformPrefix().toLowerCase(), "link-report");
    return FlexUtils.getTempFlexConfigsDirPath() + "/" + fileName;
  }

  private static boolean isSourceFileWithPublicDeclaration(final Module module, final VirtualFile file, final String qName) {
    return JavaScriptSupportLoader.isMxmlOrFxgFile(file) ||
           ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
             @Override
             public Boolean compute() {
               // we include file in compilation if it has (or intended to have) some public declaration (class, namespace, function) which is equivalent to having JSPackageStatement declaration.
               // But first we try to find it in JSQualifiedElementIndex because it is faster.
               final Collection<JSQualifiedNamedElement> elements = StubIndex.getInstance()
                 .get(JSQualifiedElementIndex.KEY, qName.hashCode(), module.getProject(), GlobalSearchScope.moduleScope(module));
               if (elements.isEmpty()) {
                 // If SomeClass.as contains IncorrectClass definition - we want to include this class into compilation so that compilation fails.
                 final PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(file);
                 return psiFile != null && PsiTreeUtil.getChildOfType(psiFile, JSPackageStatement.class) != null;
               }
               else {
                 return true;
               }
             }
           });
  }
}
