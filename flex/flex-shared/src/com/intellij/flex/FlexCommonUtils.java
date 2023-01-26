// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex;

import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.flex.model.JpsFlexCompilerProjectExtension;
import com.intellij.flex.model.bc.*;
import com.intellij.flex.model.sdk.JpsFlexSdkType;
import com.intellij.flex.model.sdk.JpsFlexmojosSdkProperties;
import com.intellij.flex.model.sdk.JpsFlexmojosSdkType;
import com.intellij.flex.model.sdk.RslUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.*;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.Utils;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.java.JdkVersionDetector;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.model.runConfiguration.JpsRunConfigurationType;
import org.jetbrains.jps.model.runConfiguration.JpsTypedRunConfiguration;
import org.jetbrains.jps.model.serialization.JpsModelSerializationDataService;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlexCommonUtils {
  public static final String AIR_NAMESPACE_BASE = "http://ns.adobe.com/air/application/";

  //keep in sync with OutputLogger.ERROR_PATTERN from BuiltInFlexCompiler project !!!
  public static final Pattern ERROR_PATTERN =
    Pattern.compile("(.*?)(\\(\\D.*\\))?(?:\\((-?\\d+)\\))?: ?(?:col: (-?\\d+):?)? (Warning|Error): (.*)");

  public static final String LOCALE_TOKEN = "{locale}";
  public static final Pattern XMX_PATTERN = Pattern.compile("(.* )?-Xmx([0-9]+)[mM]( .*)?");

  public static final String AIR_SDK_VERSION_PREFIX = "AIR SDK ";

  public static final String FLEX_UNIT_LAUNCHER = "____FlexUnitLauncher";
  public static final String SDK_TOOLS_ENCODING = "UTF-8";

  public static final String OUT_OF_MEMORY = "java.lang.OutOfMemoryError";
  public static final String JAVA_HEAP_SPACE = "Java heap space";
  public static final String COULD_NOT_CREATE_JVM = "Could not create the Java virtual machine";

  public static final String HTML_WRAPPER_TEMPLATE_FILE_NAME = "index.template.html";

  public static final String SWF_MACRO = "${swf}";
  public static final String TITLE_MACRO = "${title}";
  public static final String APPLICATION_MACRO = "${application}";
  public static final String BG_COLOR_MACRO = "${bgcolor}";
  public static final String WIDTH_MACRO = "${width}";
  public static final String HEIGHT_MACRO = "${height}";
  public static final String VERSION_MAJOR_MACRO = "${version_major}";
  public static final String VERSION_MINOR_MACRO = "${version_minor}";
  public static final String VERSION_REVISION_MACRO = "${version_revision}";

  public static final String TITLE_ATTR = "pageTitle";
  public static final String BG_COLOR_ATTR = "backgroundColor";
  public static final String WIDTH_ATTR = "width";
  public static final String HEIGHT_ATTR = "height";

  public static final String FLEXUNIT_4_TEST_RUNNER = "com.intellij.flexunit.runner.TestRunner4";
  public static final String FLEXUNIT_1_TEST_RUNNER = "com.intellij.flexunit.runner.TestRunner1";

  private static final String MODULE_PREFIX = "Module: ";
  private static final String BC_PREFIX = "\tBC: ";
  private static final String RUN_CONFIG_TYPE_PREFIX = "Run config type: ";
  private static final String RUN_CONFIG_NAME_PREFIX = "\tName: ";
  private static final String FORCED_DEBUG_STATUS = "\tForced debug status: ";

  private static final Logger LOG = Logger.getInstance(FlexCommonUtils.class.getName());

  public static final boolean KEEP_TEMP_FILES = Boolean.parseBoolean(System.getProperty("idea.keep.flex.temporary.files"));
  public static final Pattern AIR_VERSION_PATTERN = Pattern.compile("[0-9]+\\.[0-9]+(\\.[0-9]+)*");

  private static final Map<Pair<String, Long>, String> ourAdtJarPathAndTimestampToVersion = new HashMap<>();

  public static boolean isSourceFile(final String fileName) {
    final String ext = FileUtilRt.getExtension(fileName);
    return ext.equalsIgnoreCase("as") || ext.equalsIgnoreCase("mxml") || ext.equalsIgnoreCase("fxg");
  }

  public static boolean canHaveResourceFiles(final BuildConfigurationNature nature) {
    return nature.isApp();
  }

  public static boolean isFlexUnitBC(final JpsFlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getMainClass().endsWith(FLEX_UNIT_LAUNCHER);
  }

  public static boolean isRuntimeStyleSheetBC(final JpsFlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && StringUtil.toLowerCase(bc.getMainClass()).endsWith(".css");
  }

  public static boolean isRLMTemporaryBC(final JpsFlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getOutputType() == OutputType.RuntimeLoadedModule;
  }

  public static boolean canHaveRLMsAndRuntimeStylesheets(final JpsFlexBuildConfiguration bc) {
    return canHaveRLMsAndRuntimeStylesheets(bc.getOutputType(), bc.getTargetPlatform());
  }

  public static boolean canHaveRLMsAndRuntimeStylesheets(final OutputType outputType, final TargetPlatform targetPlatform) {
    return outputType == OutputType.Application && targetPlatform != TargetPlatform.Mobile;
  }


  @Nullable
  public static String getBCSpecifier(final JpsFlexBuildConfiguration bc) {
    if (!bc.isTempBCForCompilation()) return null;
    if (isFlexUnitBC(bc)) return "flexunit";
    if (isRLMTemporaryBC(bc)) return "module " + StringUtil.getShortName(bc.getMainClass());
    if (isRuntimeStyleSheetBC(bc)) return PathUtilRt.getFileName(bc.getMainClass());
    return StringUtil.getShortName(bc.getMainClass());
  }

  public static String getTempFlexConfigsDirPath() {
    return getTempFlexConfigsDirPath(FileUtil.getTempDirectory());
  }

  public static String getTempFlexConfigsDirPath(final String tempDirPath) {
    return FileUtil.toSystemIndependentName(tempDirPath) + "/" +
           "IntelliJ_IDEA"; //ApplicationNamesInfo.getInstance().getFullProductName().replace(' ', '_');
  }

  /**
   * @param forcedDebugStatus {@code true} or {@code false} means that this bc is compiled for further packaging and we need swf to have corresponding debug status;
   *                          {@code null} means that bc is compiled as is (i.e. as configured) without any modifications
   */
  public static String getBuildTargetId(final String moduleName, final String bcName, final @Nullable Boolean forcedDebugStatus) {
    return MODULE_PREFIX + moduleName + BC_PREFIX + bcName + FORCED_DEBUG_STATUS + forcedDebugStatus;
  }

  public static String getBuildTargetIdForRunConfig(final String runConfigTypeId, final String runConfigName) {
    return RUN_CONFIG_TYPE_PREFIX + runConfigTypeId + RUN_CONFIG_NAME_PREFIX + runConfigName;
  }

  @Nullable
  public static Pair<String, String> getRunConfigTypeIdAndNameByBuildTargetId(final String buildTargetId) {
    if (buildTargetId.startsWith(RUN_CONFIG_TYPE_PREFIX)) {
      final int index = buildTargetId.indexOf(RUN_CONFIG_NAME_PREFIX);
      assert index > 0 : buildTargetId;
      return Pair.create(buildTargetId.substring(RUN_CONFIG_TYPE_PREFIX.length(), index),
                         buildTargetId.substring(index + RUN_CONFIG_NAME_PREFIX.length()));
    }
    return null;
  }

  /**
   * @return {@code Trinity.first} - module name<br/>
   * {@code Trinity.second} - BC name<br/>
   * {@code Trinity.third} - forced debug status: {@code true} or {@code false} means that this bc is compiled for further packaging and we need swf to have corresponding debug status;
   * {@code null} means that bc is compiled as is (i.e. as configured) without any modifications
   */
  @Nullable
  public static Trinity<String, String, Boolean> getModuleAndBCNameAndForcedDebugStatusByBuildTargetId(final String buildTargetId) {
    if (buildTargetId.startsWith(MODULE_PREFIX)) {
      final int bcIndex = buildTargetId.indexOf(BC_PREFIX);
      final int forceDebugIndex = buildTargetId.indexOf(FORCED_DEBUG_STATUS);
      assert bcIndex > 0 && forceDebugIndex > bcIndex : buildTargetId;

      final String moduleName = buildTargetId.substring(MODULE_PREFIX.length(), bcIndex);
      final String bcName = buildTargetId.substring(bcIndex + BC_PREFIX.length(), forceDebugIndex);

      final String forcedDebugText = buildTargetId.substring(forceDebugIndex + FORCED_DEBUG_STATUS.length());
      final Boolean forcedDebugStatus = forcedDebugText.equalsIgnoreCase("true")
                                        ? Boolean.TRUE
                                        : forcedDebugText.equalsIgnoreCase("false")
                                          ? Boolean.FALSE
                                          : null;
      return Trinity.create(moduleName, bcName, forcedDebugStatus);
    }
    return null;
  }

  @Nullable
  public static <P extends JpsElement> JpsTypedRunConfiguration<P> findRunConfiguration(final @NotNull JpsProject project,
                                                                                        final @NotNull JpsRunConfigurationType<P> runConfigType,
                                                                                        final @NotNull String runConfigName) {
    for (JpsTypedRunConfiguration<P> runConfig : project.getRunConfigurations(runConfigType)) {
      if (runConfigName.equals(runConfig.getName())) {
        return runConfig;
      }
    }

    return null;
  }

  @NotNull
  public static List<String> getOptionValues(final String commandLine, final String... optionAndAliases) {
    if (StringUtil.isEmpty(commandLine)) {
      return Collections.emptyList();
    }

    final List<String> result = new ArrayList<>();

    for (CommandLineTokenizer tokenizer = new CommandLineTokenizer(commandLine); tokenizer.hasMoreTokens(); ) {
      final String token = tokenizer.nextToken();
      for (String option : optionAndAliases) {
        if (token.startsWith("-" + option + "=") || token.startsWith("-" + option + "+=")) {
          result.addAll(StringUtil.split(token.substring(token.indexOf("=") + 1), ","));
        }
        else if (token.equals("-" + option) && tokenizer.countTokens() > 0) {
          if (tokenizer.countTokens() > 0) {
            String nextToken;
            while (tokenizer.hasMoreTokens() && canBeCompilerOptionValue(nextToken = tokenizer.peekNextToken())) {
              tokenizer.nextToken(); // advance tokenizer position
              result.add(nextToken);
            }
          }
        }
      }
    }

    return result;
  }

  public static boolean canBeCompilerOptionValue(final String text) {
    if (text.startsWith("-")) {  // option or negative number
      return text.length() > 1 && Character.isDigit(text.charAt(1));
    }
    return !text.startsWith("+");
  }

  public static String removeOptions(final String commandLine, final String... optionsToRemove) {
    if (commandLine.isEmpty()) return commandLine;

    final StringBuilder buf = new StringBuilder();
    for (StringTokenizer tokenizer = new StringTokenizer(commandLine, " ", true); tokenizer.hasMoreTokens(); ) {
      final String token = tokenizer.nextToken();

      boolean remove = false;
      for (String option : optionsToRemove) {
        if (token.startsWith("-" + option)) {
          remove = true;
          break;
        }
      }

      if (remove) {
        String nextToken = null;

        WHILE:
        while (tokenizer.hasMoreElements()) {
          nextToken = tokenizer.nextToken();
          if (StringUtil.isEmptyOrSpaces(nextToken) || canBeCompilerOptionValue(nextToken)) {
            continue;
          }

          for (String option : optionsToRemove) {
            if (nextToken.startsWith("-" + option)) {
              continue WHILE;
            }
          }

          break;
        }

        if (nextToken != null && !canBeCompilerOptionValue(nextToken)) {
          buf.append(nextToken);
        }
      }
      else {
        buf.append(token);
      }
    }

    return buf.toString();
  }

  public static String getFlexCompilerWorkDirPath(final JpsProject project) {
    final File dir = JpsModelSerializationDataService.getBaseDirectory(project);
    return dir == null ? "" : dir.getPath();
  }

  @Nullable
  public static String findXMLElement(final File file, final String xmlElement) {
    try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
      return findXMLElement(inputStream, xmlElement);
    }
    catch (IOException e) {
      return null;
    }
  }

  @Nullable
  public static String findXMLElement(final InputStream is, final String xmlElement) {
    // xmlElement has format "<root_tag><child_tag>"
    final List<String> elementNames = StringUtil.split(StringUtil.replace(xmlElement, ">", ""), "<");
    if (elementNames.isEmpty()) return null;

    try {
      final Element root = JDOMUtil.load(is);

      if (!root.getName().equals(elementNames.get(0))) {
        return null;
      }

      Element element = root;
      int depth = 0;

      while (elementNames.size() > ++depth) {
        element = element.getChild(elementNames.get(depth), element.getNamespace());

        if (element == null) {
          return null;
        }
      }

      return element.getTextNormalize();
    }
    catch (JDOMException | IOException ignore) {/**/}
    return null;
  }

  public static String getMaximumTargetPlayer(final String sdkHome) {
    return getMaximumVersion(getTargetPlayers(sdkHome));
  }

  @NotNull
  public static String getMaximumVersion(final String[] versions) {
    String version = versions.length > 0 ? versions[0] : "";
    for (int i = 1; i < versions.length; i++) {
      if (StringUtil.compareVersionNumbers(versions[i], version) > 0) {
        version = versions[i];
      }
    }
    return version;
  }

  public static String[] getTargetPlayers(final String sdkHome) {
    final File playerFolder = new File(sdkHome + "/frameworks/libs/player");
    if (playerFolder.isDirectory()) {
      return playerFolder.list((dir, name) -> new File(playerFolder, name + "/playerglobal.swc").isFile());
    }

    return ArrayUtilRt.EMPTY_STRING_ARRAY;
  }

  /**
   * @param processor (namespace, relative path with no leading slash)
   */
  public static void processStandardNamespaces(final JpsFlexBuildConfiguration bc,
                                               final PairConsumer<? super String, ? super String> processor) {
    final JpsSdk<?> sdk = bc.getSdk();
    if (bc.isPureAs() || sdk == null || sdk.getSdkType() != JpsFlexSdkType.INSTANCE) return;

    if (StringUtil.compareVersionNumbers(sdk.getVersionString(), "4") < 0) {
      processor.consume("http://www.adobe.com/2006/mxml", "frameworks/mxml-manifest.xml");
    }
    else {
      processor.consume("http://ns.adobe.com/mxml/2009", "frameworks/mxml-2009-manifest.xml");

      if (bc.getTargetPlatform() == TargetPlatform.Mobile ||
          bc.getDependencies().getComponentSet() == ComponentSet.SparkAndMx ||
          bc.getDependencies().getComponentSet() == ComponentSet.SparkOnly) {
        processor.consume("library://ns.adobe.com/flex/spark", "frameworks/spark-manifest.xml");
      }

      if (bc.getTargetPlatform() != TargetPlatform.Mobile) {
        if (bc.getDependencies().getComponentSet() == ComponentSet.SparkAndMx ||
            bc.getDependencies().getComponentSet() == ComponentSet.MxOnly) {
          processor.consume("library://ns.adobe.com/flex/mx", "frameworks/mx-manifest.xml");
        }

        processor.consume("http://www.adobe.com/2006/mxml", "frameworks/mxml-manifest.xml");
      }
    }
  }

  public static LinkageType getDefaultFrameworkLinkage(final String sdkVersion,
                                                       final BuildConfigurationNature nature) {
    return nature.isLib()
           ? LinkageType.External
           : nature.pureAS || !nature.isWebPlatform()
             ? LinkageType.Merged
             : StringUtil.compareVersionNumbers(sdkVersion, "4") >= 0 && StringUtil.compareVersionNumbers(sdkVersion, "4.8") < 0
               ? LinkageType.RSL
               : LinkageType.Merged; // Flex 3 or Apache Flex
  }

  /**
   * If {@code LinkageType.Default} is returned then use {@link #getDefaultFrameworkLinkage(String, BuildConfigurationNature)} to get real value.
   *
   * @return {@code null} if entry should not be included at all
   */
  @Nullable
  public static LinkageType getSdkEntryLinkageType(final String swcPath, final JpsFlexBuildConfiguration bc) {
    final JpsSdk<?> sdk = bc.getSdk();
    LOG.assertTrue(sdk != null);
    return getSdkEntryLinkageType(sdk.getHomePath(), swcPath, bc.getNature(), bc.getDependencies().getTargetPlayer(),
                                  bc.getDependencies().getComponentSet());
  }

  /**
   * If {@code LinkageType.Default} is returned then use {@link #getDefaultFrameworkLinkage(String, BuildConfigurationNature)} to get real value.
   *
   * @return {@code null} if entry should not be included at all
   */
  @Nullable
  public static LinkageType getSdkEntryLinkageType(final String sdkHome,
                                                   final String swcPath,
                                                   final BuildConfigurationNature bcNature,
                                                   final String targetPlayer,
                                                   final ComponentSet componentSet) {
    LOG.assertTrue(!swcPath.endsWith("!/"), "plain local filesystem path is expected");

    if (swcPath.endsWith("/frameworks/libs/air/airglobal.swc")) {
      return bcNature.isWebPlatform() ? null : LinkageType.External;
    }

    if (swcPath.endsWith("/playerglobal.swc") && swcPath.contains("/frameworks/libs/player/")) {
      if (swcPath.endsWith("/frameworks/libs/player/" + targetPlayer + "/playerglobal.swc")) {
        return bcNature.isWebPlatform() ? LinkageType.External : null;
      }
      return null;
    }

    final boolean swcIncluded;

    final int lastSlashIndex = swcPath.lastIndexOf('/');
    if (lastSlashIndex <= 0 || lastSlashIndex == swcPath.length() - 1) {
      LOG.error("Unexpected Flex SDK root: " + swcPath);
    }
    final String swcName = StringUtil.toLowerCase(swcPath.substring(lastSlashIndex + 1));
    final String folderPath = swcPath.substring(0, lastSlashIndex);

    if (folderPath.endsWith("/frameworks/libs")) {
      swcIncluded = isSwcFromLibsFolderIncluded(bcNature, componentSet, swcName);
    }
    else if (folderPath.endsWith("/frameworks/libs/air")) {
      swcIncluded = isSwcFromAirFolderIncluded(bcNature, componentSet, swcName);
    }
    else if (folderPath.endsWith("/frameworks/libs/mobile")) {
      swcIncluded = isSwcFromMobileFolderIncluded(bcNature, swcName);
    }
    else if (folderPath.endsWith("/frameworks/libs/mx")) {
      swcIncluded = isSwcFromMxFolderIncluded(bcNature, componentSet, swcName);
    }
    else if (folderPath.contains("/frameworks/themes/")) {
      swcIncluded = false;
    }
    else {
      if (Utils.IS_TEST_MODE) {
        LOG.warn("Unknown Flex SDK root: " + swcPath);
      }
      swcIncluded = true;
    }

    if (!swcIncluded) return null;

    // our difference from FB is that in case of library _ALL_ SWCs from SDK are external by default (except *global.swc)
    if (bcNature.isLib()) return LinkageType.Default;

    return RslUtil.canBeRsl(sdkHome, swcPath) ? LinkageType.Default : LinkageType.Merged;
  }

  private static boolean isSwcFromLibsFolderIncluded(final BuildConfigurationNature bcNature,
                                                     final ComponentSet componentSet,
                                                     final String swcName) {
    if (swcName.equals("advancedgrids.swc")) {
      return !(bcNature.isMobilePlatform() || bcNature.pureAS || componentSet == ComponentSet.SparkOnly);
    }

    if (swcName.equals("authoringsupport.swc")) {
      return true;
    }

    if (swcName.equals("charts.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.equals("core.swc")) {
      return bcNature.pureAS;
    }

    if (swcName.equals("datavisualization.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.endsWith("flash-integration.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.equals("flex.swc")) {
      return bcNature.pureAS;
    }

    if (swcName.endsWith("framework.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.endsWith("osmf.swc")) {
      return true;
    }

    if (swcName.endsWith("rpc.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.endsWith("spark.swc")) {
      return !bcNature.pureAS && (bcNature.isMobilePlatform() || componentSet != ComponentSet.MxOnly);
    }

    if (swcName.endsWith("spark_dmv.swc")) {
      return !bcNature.pureAS && !bcNature.isMobilePlatform() && componentSet == ComponentSet.SparkAndMx;
    }

    if (swcName.endsWith("sparkskins.swc")) {
      return !bcNature.pureAS && !bcNature.isMobilePlatform() && componentSet != ComponentSet.MxOnly;
    }

    if (swcName.endsWith("textlayout.swc")) {
      return true;
    }

    if (swcName.endsWith("utilities.swc")) {
      return true;
    }

    if (swcName.endsWith("asc-support.swc")) {
      return true;
    }

    if (swcName.equals("apache.swc") ||
        swcName.equals("experimental.swc")) {
      return !bcNature.pureAS; // Apache Flex SDK 4.9
    }

    if (swcName.equals("experimental_mobile.swc")) {
      return bcNature.isMobilePlatform() && !bcNature.pureAS; // Apache Flex SDK 4.11
    }

    if (swcName.equals("automation.swc") ||
        swcName.equals("automation_agent.swc") ||
        swcName.equals("automation_dmv.swc") ||
        swcName.equals("automation_flashflexkit.swc") ||
        swcName.equals("qtp.swc")) {
      // additionally installed on top of Flex SDK 3.x
      return true;
    }

    if (swcName.equals("starling.swc")) {
      return true; // Feathers SDK
    }

    if (swcName.equals("feathers.swc") ||
        swcName.equals("feathers-mxml.swc")) {
      return !bcNature.pureAS; // Feathers SDK
    }

    LOG.warn("Unknown SWC in '<Flex SDK>/frameworks/libs' folder: " + swcName);
    return true;
  }

  private static boolean isSwcFromAirFolderIncluded(final BuildConfigurationNature bcNature,
                                                    final ComponentSet componentSet,
                                                    final String swcName) {
    if (bcNature.isMobilePlatform()) {
      return swcName.equals("servicemonitor.swc");
    }

    if (bcNature.isDesktopPlatform()) {
      if (swcName.equals("airframework.swc")) {
        return !bcNature.pureAS;
      }

      if (swcName.equals("airspark.swc")) {
        return !bcNature.pureAS && componentSet != ComponentSet.MxOnly;
      }

      return true;
    }

    return false;
  }

  private static boolean isSwcFromMobileFolderIncluded(final BuildConfigurationNature bcNature, final String swcName) {
    return bcNature.isMobilePlatform() && !bcNature.pureAS;
  }

  private static boolean isSwcFromMxFolderIncluded(final BuildConfigurationNature bcNature,
                                                   final ComponentSet componentSet,
                                                   final String swcName) {
    if (!swcName.equals("mx.swc")) {
      LOG.warn("Unknown SWC in '<Flex SDK>/frameworks/libs/mx' folder: " + swcName);
    }
    return !bcNature.isMobilePlatform() && !bcNature.pureAS && componentSet != ComponentSet.SparkOnly;
  }

  public static boolean checkDependencyType(final OutputType bcOutputType,
                                            final OutputType dependencyBCOutputType,
                                            final LinkageType linkageType) {
    switch (dependencyBCOutputType) {
      case Application:
      case RuntimeLoadedModule:
        return bcOutputType != OutputType.Library && linkageType == LinkageType.LoadInRuntime;
      case Library:
        return ArrayUtil.contains(linkageType, LinkageType.getSwcLinkageValues());
      default:
        LOG.error(dependencyBCOutputType);
        return false;
    }
  }

  public static String getPathToBundledJar(String filename) {
    final URL url = FlexCommonUtils.class.getResource("");
    if (url == null || "jar".equals(url.getProtocol())) {
      // running from build
      return FileUtil.toSystemDependentName(PathManager.getPluginsPath() + "/flex/lib/" + filename);
    }
    else {
      final File dir1 = new File("../lib");
      if (dir1.isDirectory()) {
        final String path = FileUtil.toSystemIndependentName(FileUtil.toCanonicalPath(dir1.getAbsolutePath()));
        if (path.endsWith("/flex/lib")) {
          // running tests from 'flex-plugin' project
          return FileUtil.toSystemDependentName(path + "/" + filename);
        }
      }

      final File dir2 = new File(PathManager.getHomePath() + "/plugins/flex/lib");
      if (dir2.isDirectory()) {
        // running IDE from 'flex-plugin' project
        return FileUtil.toSystemIndependentName(dir2.getAbsolutePath() + "/" + filename);
      }

      // running from 'IDEA' project sources
      return FileUtil.toSystemDependentName(PathManager.getHomePath() + "/contrib/flex/lib/" + filename);
    }
  }

  public static Collection<String> getFlexUnitSupportLibNames(final BuildConfigurationNature nature,
                                                              final ComponentSet componentSet,
                                                              final String pathToFlexUnitMainClass) {
    boolean flexUnit4 = true;
    try {
      final String content = FileUtil.loadFile(new File(pathToFlexUnitMainClass), SDK_TOOLS_ENCODING);
      if (content.contains(FLEXUNIT_1_TEST_RUNNER)) {
        flexUnit4 = false;
      }
    }
    catch (IOException ignore) {/*unlucky*/}

    final Collection<String> result = new ArrayList<>(2);

    if (flexUnit4) {
      result.add("unittestingsupport_flexunit_4.swc");
    }

    if (nature.pureAS) {
      result.add("unittestingsupport_as.swc");
    }
    else if (nature.isMobilePlatform() || componentSet == ComponentSet.SparkOnly) {
      result.add("unittestingsupport_spark.swc");
    }
    else {
      result.add("unittestingsupport_mx.swc");
    }

    return result;
  }

  public static String getPathToMainClassFile(final String mainClassFqn, final JpsModule module) {
    if (StringUtil.isEmpty(mainClassFqn)) return "";

    final String s = mainClassFqn.replace('.', '/');
    final String[] classFileRelPaths = {"/" + s + ".mxml", "/" + s + ".as"};

    for (JpsModuleSourceRoot srcRoot : module.getSourceRoots()) {
      final String srcRootPath = JpsPathUtil.urlToPath(srcRoot.getUrl());

      for (final String classFileRelPath : classFileRelPaths) {
        final String pathToMainClassFile = srcRootPath + classFileRelPath;
        if (new File(pathToMainClassFile).isFile()) {
          return pathToMainClassFile;
        }
      }
    }

    return "";
  }

  public static String replacePathMacros(final @NotNull String text, final @NotNull JpsModule module, final @NotNull String sdkRootPath) {
    String preResult = StringUtil.replace(text, CompilerOptionInfo.FLEX_SDK_MACRO, sdkRootPath);

    final File moduleDir = JpsModelSerializationDataService.getBaseDirectory(module);
    if (moduleDir != null) {
      preResult = StringUtil.replace(preResult, "${MODULE_DIR}", moduleDir.getPath());
    }

    final File projectDir = JpsModelSerializationDataService.getBaseDirectory(module.getProject());
    if (projectDir != null) {
      preResult = StringUtil.replace(preResult, "${PROJECT_DIR}", projectDir.getPath());
    }

    preResult = StringUtil.replace(preResult, "${USER_HOME}", SystemProperties.getUserHome());

    final StringBuilder builder = new StringBuilder(preResult);
    int startIndex;
    int endIndex = 0;

    while ((startIndex = builder.indexOf("${", endIndex)) >= 0) {
      endIndex = builder.indexOf("}", startIndex);

      if (endIndex > startIndex) {
        final String macroName = builder.substring(startIndex + 2, endIndex);
        final String macroValue = JpsModelSerializationDataService.getPathVariableValue(module.getProject().getModel().getGlobal(),
                                                                                        macroName);

        if (macroValue != null && !StringUtil.isEmptyOrSpaces(macroValue)) {
          builder.replace(startIndex, endIndex + 1, macroValue);
          endIndex = endIndex + macroValue.length() - (macroName.length() + 3);
        }
      }
      else {
        break;
      }
    }

    return builder.toString();
  }

  public static String getFlexUnitLauncherExtension(final BuildConfigurationNature nature) {
    return nature.pureAS ? ".as" : ".mxml";
  }

  public static boolean is64BitJava6(final String javaHome) {
    JdkVersionDetector.JdkVersionInfo info = JdkVersionDetector.getInstance().detectJdkVersionInfo(javaHome);
    return info != null && info.version.feature == 6 && info.arch.width == 64;
  }

  public static List<String> getCommandLineForSdkTool(final @NotNull JpsProject project,
                                                      final @NotNull JpsSdk<?> sdk,
                                                      final @Nullable String additionalClasspath,
                                                      final @NotNull String mainClass) {
    String javaHome = SystemProperties.getJavaHome();
    boolean customJavaHomeSet = false;
    String additionalJavaArgs = null;
    int heapSizeMbFromJvmConfig = 0;
    String classpath = additionalClasspath;

    final boolean isFlexmojos = sdk.getSdkType() == JpsFlexmojosSdkType.INSTANCE;
    final JpsFlexmojosSdkProperties flexmojosSdkData = isFlexmojos
                                                       ? ((JpsSimpleElement<JpsFlexmojosSdkProperties>)sdk.getSdkProperties()).getData()
                                                       : null;

    if (isFlexmojos) {
      classpath = (StringUtil.isEmpty(classpath) ? "" : (classpath + File.pathSeparator)) +
                  FileUtil.toSystemDependentName(StringUtil.join(flexmojosSdkData.getFlexCompilerClasspath(), File.pathSeparator));
    }
    else {
      FileInputStream inputStream = null;

      try {
        inputStream = new FileInputStream(FileUtil.toSystemDependentName(sdk.getHomePath() + "/bin/jvm.config"));

        final Properties properties = new Properties();
        properties.load(inputStream);

        final String configuredJavaHome = properties.getProperty("java.home");
        if (configuredJavaHome != null && configuredJavaHome.trim().length() > 0) {
          javaHome = configuredJavaHome;
          customJavaHomeSet = true;
        }

        final String javaArgs = properties.getProperty("java.args");
        if (javaArgs != null && javaArgs.trim().length() > 0) {
          additionalJavaArgs = javaArgs;
          final Matcher matcher = XMX_PATTERN.matcher(javaArgs);
          if (matcher.matches()) {
            try {
              heapSizeMbFromJvmConfig = Integer.parseInt(matcher.group(2));
            }
            catch (NumberFormatException e) {/*ignore*/}
          }
        }

        final String classpathFromJvmConfig = properties.getProperty("java.class.path");
        if (classpathFromJvmConfig != null && classpathFromJvmConfig.trim().length() > 0) {
          classpath = (StringUtil.isEmpty(classpath) ? "" : (classpath + File.pathSeparator)) + classpathFromJvmConfig;
        }
        //jvm.config also has properties which are not handled here: 'env' and 'java.library.path'; though not sure that there's any sense in them
      }
      catch (IOException e) {
        // not a big problem, will use default settings
        if (inputStream != null) {
          try {
            inputStream.close();
          }
          catch (IOException e1) {/*ignore*/}
        }
      }
    }
    final String javaExecutable = FileUtil.toSystemDependentName((javaHome + "/bin/java" + (SystemInfo.isWindows ? ".exe" : "")));
    final String applicationHomeParam =
      isFlexmojos ? null : ("-Dapplication.home=" + FileUtil.toSystemDependentName(sdk.getHomePath()));

    final String d32 = getD32IfNeeded(customJavaHomeSet, javaHome);

    final List<String> result = new ArrayList<>();

    result.add(javaExecutable);
    if (StringUtil.isNotEmpty(d32)) result.add(d32);
    if (StringUtil.isNotEmpty(applicationHomeParam)) result.add(applicationHomeParam);
    if (StringUtil.isNotEmpty(additionalJavaArgs)) result.addAll(StringUtil.split(additionalJavaArgs, " "));

    final String vmOptions = JpsFlexCompilerProjectExtension.getInstance(project).VM_OPTIONS;
    if (StringUtil.isNotEmpty(vmOptions)) result.addAll(StringUtil.split(vmOptions, " "));

    if (additionalJavaArgs == null || !additionalJavaArgs.contains("file.encoding")) {
      result.add("-Dfile.encoding=" + SDK_TOOLS_ENCODING);
    }

    result.add("-Djava.awt.headless=true");
    result.add("-Duser.language=en");
    result.add("-Duser.region=en");

    final int heapSizeMb = JpsFlexCompilerProjectExtension.getInstance(project).HEAP_SIZE_MB;
    if (heapSizeMb > heapSizeMbFromJvmConfig) {
      result.add("-Xmx" + heapSizeMb + "m");
    }

    if (StringUtil.isNotEmpty(classpath)) {
      result.add("-classpath");
      result.add(classpath);
    }

    result.add(mainClass);

    return result;
  }

  public static String getD32IfNeeded(boolean customJavaHomeSet, String javaHome) {
    return (!customJavaHomeSet && SystemInfo.isMac && is64BitJava6(javaHome)) ? "-d32" : null;
  }

  @Nullable
  public static String getPathRelativeToSourceRoot(final JpsModule module, final String _path) {
    final String path = FileUtil.toSystemIndependentName(_path);
    for (JpsModuleSourceRoot srcRoot : module.getSourceRoots()) {
      final String rootPath = JpsPathUtil.urlToPath(srcRoot.getUrl());
      if (path.equals(rootPath)) return "";
      if (path.startsWith(rootPath + "/")) return path.substring(rootPath.length() + 1);
    }
    return null;
  }

  @Nullable
  public static String getPathRelativeToContentRoot(final JpsModule module, final String path) {
    for (String rootUrl : module.getContentRootsList().getUrls()) {
      final String rootPath = JpsPathUtil.urlToPath(rootUrl);
      if (path.equals(rootPath)) return "";
      if (path.startsWith(rootPath + "/")) return path.substring(rootPath.length() + 1);
    }
    return null;
  }

  public static String getWrapperFileName(final JpsFlexBuildConfiguration bc) {
    return FileUtilRt.getNameWithoutExtension(PathUtilRt.getFileName(bc.getActualOutputFilePath())) + ".html";
  }


  public static String getGeneratedAirDescriptorName(final JpsFlexBuildConfiguration bc, final JpsAirPackagingOptions packagingOptions) {
    final String suffix = packagingOptions instanceof JpsAirDesktopPackagingOptions
                          ? "-descriptor.xml"
                          : packagingOptions instanceof JpsAndroidPackagingOptions ? "-android-descriptor.xml"
                                                                                   : "-ios-descriptor.xml";
    return FileUtilRt.getNameWithoutExtension(PathUtilRt.getFileName(bc.getActualOutputFilePath())) + suffix;
  }

  public static String replace(final String text, final Map<String, String> replacementMap) {
    final List<String> from = new ArrayList<>(replacementMap.size());
    final List<String> to = new ArrayList<>(replacementMap.size());

    for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
      from.add(entry.getKey());
      to.add(entry.getValue());
    }

    return StringUtil.replace(text, from, to);
  }

  @Nullable
  public static String getVersionOfAirSdkIncludedInFlexSdk(final String flexSdkHomePath) {
    final File adtFile = new File(flexSdkHomePath + "/lib/adt.jar");
    if (!adtFile.isFile()) {
      return null;
    }

    String version = ourAdtJarPathAndTimestampToVersion.get(Pair.create(adtFile.getPath(), adtFile.lastModified()));
    if (version != null) {
      return version;
    }

    try {
      final Ref<String> versionRef = Ref.create();

      final String javaExecutable =
        FileUtil.toSystemDependentName((SystemProperties.getJavaHome() + "/bin/java" + (SystemInfo.isWindows ? ".exe" : "")));

      String[] cmdarray = {javaExecutable, "-jar", adtFile.getPath(), "-version"};
      final Process process = Runtime.getRuntime().exec(cmdarray);
      final BaseOSProcessHandler handler = new BaseOSProcessHandler(process, StringUtil.join(cmdarray, " "), Charset.defaultCharset());

      handler.addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
          if (outputType != ProcessOutputTypes.SYSTEM) {
            parseAirVersionFromAdtOutput(event.getText().trim(), versionRef);
          }
        }
      });

      handler.startNotify();
      handler.waitFor(3000);

      if (!handler.isProcessTerminated()) {
        handler.destroyProcess();
      }

      version = versionRef.get();
      ourAdtJarPathAndTimestampToVersion.put(Pair.create(adtFile.getPath(), adtFile.lastModified()), version);

      return version;
    }
    catch (IOException e) {/*ignore*/}

    return null;
  }

  public static void parseAirVersionFromAdtOutput(String adtOutput, final Ref<? super String> versionRef) {
    // adt version "1.5.0.7220"
    // 13.0.0.36

    final String prefix = "adt version \"";
    final String suffix = "\"";

    if (adtOutput.startsWith(prefix) && adtOutput.endsWith(suffix)) {
      adtOutput = adtOutput.substring(prefix.length(), adtOutput.length() - suffix.length());
    }

    if (AIR_VERSION_PATTERN.matcher(adtOutput).matches()) {
      versionRef.set(adtOutput);
    }
  }

  @Nullable
  public static String getAirVersion(final String sdkHomePath, final String sdkVersion) {
    final String version;

    if (sdkVersion != null && sdkVersion.startsWith(AIR_SDK_VERSION_PREFIX)) {
      version = sdkVersion.substring(AIR_SDK_VERSION_PREFIX.length());
    }
    else if (sdkHomePath != null && sdkHomePath.endsWith(".pom") && new File(sdkHomePath).isFile()) {
      version = guessAirSdkVersionByFlexmojosSdkVersion(sdkVersion);
    }
    else {
      version = getVersionOfAirSdkIncludedInFlexSdk(sdkHomePath);
    }

    if (version != null) {
      if (ArrayUtil.contains(version, "1.5.1", "1.5.2", "1.5.3")) {
        return version;
      }

      final Trinity<String, String, String> majorMinorRevision = getMajorMinorRevisionVersion(version);
      return majorMinorRevision.first + "." + majorMinorRevision.second;
    }

    return null;
  }

  private static String guessAirSdkVersionByFlexmojosSdkVersion(final String sdkVersion) {
    // todo consider separate Flex and AIR SDK management supported by Flexmojos 6.
    if (StringUtil.compareVersionNumbers(sdkVersion, "4") < 0) {
      if (StringUtil.compareVersionNumbers(sdkVersion, "3.1") < 0) return "1.0";
      if (StringUtil.compareVersionNumbers(sdkVersion, "3.2") < 0) return "1.1";
      if (StringUtil.compareVersionNumbers(sdkVersion, "3.3") < 0) return "1.5";
      if (StringUtil.compareVersionNumbers(sdkVersion, "3.4") < 0) return "1.5";
      if (StringUtil.compareVersionNumbers(sdkVersion, "3.5") < 0) return "1.5.2";

      return "1.5.3";
    }

    if (StringUtil.compareVersionNumbers(sdkVersion, "4.1") < 0) return "1.5.3";
    if (StringUtil.compareVersionNumbers(sdkVersion, "4.5") < 0) return "2.0";
    if (StringUtil.compareVersionNumbers(sdkVersion, "4.6") < 0) return "2.6";

    return "3.1";
  }

  public static String fixApplicationId(final String appId) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < appId.length(); i++) {
      final char ch = appId.charAt(i);
      if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '-' || ch == '.') {
        builder.append(ch);
      }
    }
    return builder.toString();
  }

  @Nullable
  public static String parseAirVersionFromDescriptorFile(final String descriptorFilePath) {
    if (StringUtil.isEmpty(descriptorFilePath)) return null;

    try {
      final Element rootElement = JDOMUtil.load(new File(descriptorFilePath));
      final String localName = rootElement.getName();
      final Namespace namespace = rootElement.getNamespace();
      if ("application".equals(localName) && namespace != null && namespace.getURI().startsWith(AIR_NAMESPACE_BASE)) {
        return namespace.getURI().substring(AIR_NAMESPACE_BASE.length());
      }
    }
    catch (JDOMException | IOException e) {/*unlucky*/}

    return null;
  }

  public static String getSwfVersionForTargetPlayer(final String targetPlayer) {
    if (StringUtil.compareVersionNumbers(targetPlayer, "51") >= 0) return "51";
    if (StringUtil.compareVersionNumbers(targetPlayer, "50") >= 0) return "50";
    if (StringUtil.compareVersionNumbers(targetPlayer, "33") >= 0) return "44";
    if (StringUtil.compareVersionNumbers(targetPlayer, "32") >= 0) return "43";
    if (StringUtil.compareVersionNumbers(targetPlayer, "31") >= 0) return "42";
    if (StringUtil.compareVersionNumbers(targetPlayer, "30") >= 0) return "41";
    if (StringUtil.compareVersionNumbers(targetPlayer, "29") >= 0) return "40";
    if (StringUtil.compareVersionNumbers(targetPlayer, "28") >= 0) return "39";
    if (StringUtil.compareVersionNumbers(targetPlayer, "27") >= 0) return "38";
    if (StringUtil.compareVersionNumbers(targetPlayer, "26") >= 0) return "37";
    if (StringUtil.compareVersionNumbers(targetPlayer, "25") >= 0) return "36";
    if (StringUtil.compareVersionNumbers(targetPlayer, "24") >= 0) return "35";
    if (StringUtil.compareVersionNumbers(targetPlayer, "23") >= 0) return "34";
    if (StringUtil.compareVersionNumbers(targetPlayer, "22") >= 0) return "33";
    if (StringUtil.compareVersionNumbers(targetPlayer, "21") >= 0) return "32";
    if (StringUtil.compareVersionNumbers(targetPlayer, "20") >= 0) return "31";
    if (StringUtil.compareVersionNumbers(targetPlayer, "19") >= 0) return "30";
    if (StringUtil.compareVersionNumbers(targetPlayer, "18") >= 0) return "29";
    if (StringUtil.compareVersionNumbers(targetPlayer, "17") >= 0) return "28";
    if (StringUtil.compareVersionNumbers(targetPlayer, "16") >= 0) return "27";
    if (StringUtil.compareVersionNumbers(targetPlayer, "15") >= 0) return "26";
    if (StringUtil.compareVersionNumbers(targetPlayer, "14") >= 0) return "25";
    if (StringUtil.compareVersionNumbers(targetPlayer, "13") >= 0) return "24";
    if (StringUtil.compareVersionNumbers(targetPlayer, "12") >= 0) return "23";
    if (StringUtil.compareVersionNumbers(targetPlayer, "11.9") >= 0) return "22";
    if (StringUtil.compareVersionNumbers(targetPlayer, "11.8") >= 0) return "21";
    if (StringUtil.compareVersionNumbers(targetPlayer, "11.7") >= 0) return "20";
    if (StringUtil.compareVersionNumbers(targetPlayer, "11.6") >= 0) return "19";
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

  public static String getSwfVersionForAirVersion(final String airVersion) {
    if (StringUtil.compareVersionNumbers(airVersion, "51") >= 0) return "51";
    if (StringUtil.compareVersionNumbers(airVersion, "50") >= 0) return "50";
    if (StringUtil.compareVersionNumbers(airVersion, "33") >= 0) return "44";
    if (StringUtil.compareVersionNumbers(airVersion, "32") >= 0) return "43";
    if (StringUtil.compareVersionNumbers(airVersion, "31") >= 0) return "42";
    if (StringUtil.compareVersionNumbers(airVersion, "30") >= 0) return "41";
    if (StringUtil.compareVersionNumbers(airVersion, "29") >= 0) return "40";
    if (StringUtil.compareVersionNumbers(airVersion, "28") >= 0) return "39";
    if (StringUtil.compareVersionNumbers(airVersion, "27") >= 0) return "38";
    if (StringUtil.compareVersionNumbers(airVersion, "26") >= 0) return "37";
    if (StringUtil.compareVersionNumbers(airVersion, "25") >= 0) return "36";
    if (StringUtil.compareVersionNumbers(airVersion, "24") >= 0) return "35";
    if (StringUtil.compareVersionNumbers(airVersion, "23") >= 0) return "34";
    if (StringUtil.compareVersionNumbers(airVersion, "22") >= 0) return "33";
    if (StringUtil.compareVersionNumbers(airVersion, "21") >= 0) return "32";
    if (StringUtil.compareVersionNumbers(airVersion, "20") >= 0) return "31";
    if (StringUtil.compareVersionNumbers(airVersion, "19") >= 0) return "30";
    if (StringUtil.compareVersionNumbers(airVersion, "18") >= 0) return "29";
    if (StringUtil.compareVersionNumbers(airVersion, "17") >= 0) return "28";
    if (StringUtil.compareVersionNumbers(airVersion, "16") >= 0) return "27";
    if (StringUtil.compareVersionNumbers(airVersion, "15") >= 0) return "26";
    if (StringUtil.compareVersionNumbers(airVersion, "14") >= 0) return "25";
    if (StringUtil.compareVersionNumbers(airVersion, "13") >= 0) return "24"; // yes, they are going to release 13 after 4
    if (StringUtil.compareVersionNumbers(airVersion, "4") >= 0) return "23";
    if (StringUtil.compareVersionNumbers(airVersion, "3.9") >= 0) return "22";
    if (StringUtil.compareVersionNumbers(airVersion, "3.8") >= 0) return "21";
    if (StringUtil.compareVersionNumbers(airVersion, "3.7") >= 0) return "20";
    if (StringUtil.compareVersionNumbers(airVersion, "3.6") >= 0) return "19";
    if (StringUtil.compareVersionNumbers(airVersion, "3.5") >= 0) return "18";
    if (StringUtil.compareVersionNumbers(airVersion, "3.4") >= 0) return "17";
    if (StringUtil.compareVersionNumbers(airVersion, "3.3") >= 0) return "16";
    if (StringUtil.compareVersionNumbers(airVersion, "3.2") >= 0) return "15";
    if (StringUtil.compareVersionNumbers(airVersion, "3.1") >= 0) return "14";
    if (StringUtil.compareVersionNumbers(airVersion, "3") >= 0) return "13";
    if (StringUtil.compareVersionNumbers(airVersion, "2.7") >= 0) return "12";
    if (StringUtil.compareVersionNumbers(airVersion, "2.6") >= 0) return "11";
    if (StringUtil.compareVersionNumbers(airVersion, "1.5") >= 0) return "10";
    return "9";
  }

  /**
   * Try not to use this method. Correct swf version can be obtained only by target player or AIR version.
   */
  public static String getSwfVersionForSdk_THE_WORST_WAY(final String sdkVersion) {
    if (sdkVersion.startsWith(AIR_SDK_VERSION_PREFIX)) {
      return getSwfVersionForAirVersion(sdkVersion.substring(AIR_SDK_VERSION_PREFIX.length()));
    }

    if (StringUtil.compareVersionNumbers(sdkVersion, "4.6") >= 0) return "14";
    if (StringUtil.compareVersionNumbers(sdkVersion, "4.5") >= 0) return "11";

    assert false : sdkVersion;
    return null;
  }

  public static boolean containsASC20(final String sdkHome) {
    return new File(sdkHome + "/lib/compiler.jar").isFile();
  }

  public static Pair<String, Integer> getSourcePathAndLineFromASC20Message(final String message) {
    if (message == null) return null;

    final int index = message.lastIndexOf(':');
    if (index <= 0) return null;

    final String filePath = message.substring(0, index);
    final String lineNumber = message.substring(index + 1);

    try {
      final int line = Integer.parseInt(lineNumber);
      if (new File(filePath).isFile()) {
        return Pair.create(FileUtil.toSystemIndependentName(filePath), line);
      }
    }
    catch (NumberFormatException e) {/*unlucky*/}

    return null;
  }

  public static InputStreamReader createInputStreamReader(final InputStream inputStream) {
    try {
      return new InputStreamReader(inputStream, SDK_TOOLS_ENCODING);
    }
    catch (UnsupportedEncodingException e) {
      return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }
  }

  public static String fixSizeReportOption(final String additionalOptions, final String postfix) {
    final List<String> values = getOptionValues(additionalOptions, "size-report");
    final StringBuilder result = new StringBuilder(removeOptions(additionalOptions, "size-report"));
    if (values.size() == 1 && StringUtil.toLowerCase(values.get(0)).endsWith(".xml")) {
      final String path = values.get(0);
      result.append(" -size-report=");
      if (path.contains(" ")) {
        result.append("\"");
      }
      result.append(path, 0, path.length() - ".xml".length());
      result.append("-").append(postfix);
      result.append(path.substring(path.length() - ".xml".length()));
      if (path.contains(" ")) {
        result.append("\"");
      }
    }

    return result.toString();
  }

  public static boolean isAirSdkWithoutFlex(final @Nullable JpsSdk<?> sdk) {
    final String version = sdk == null ? null : sdk.getVersionString();
    return version != null && version.startsWith(AIR_SDK_VERSION_PREFIX);
  }

  public static Trinity<String, String, String> getMajorMinorRevisionVersion(final @NotNull String version) {
    final int firstDotIndex = version.indexOf('.');

    if (firstDotIndex == -1) {
      return Trinity.create(version, "0", "0");
    }

    final String majorVersion = version.substring(0, firstDotIndex);
    final int secondDotIndex = version.indexOf('.', firstDotIndex + 1);

    if (secondDotIndex == -1) {
      return Trinity.create(majorVersion, version.substring(firstDotIndex + 1), "0");
    }

    final String minorVersion = version.substring(firstDotIndex + 1, secondDotIndex);

    final int thirdDotIndex = version.indexOf('.', secondDotIndex + 1);
    final String revision = thirdDotIndex == -1 ? version.substring(secondDotIndex + 1)
                                                : version.substring(secondDotIndex + 1, thirdDotIndex);

    return Trinity.create(majorVersion, minorVersion, revision);
  }

  public static void deleteTempFlexConfigFiles(final String projectName) {
    if (KEEP_TEMP_FILES) return;

    final String hash1 = StringUtil.toUpperCase(Integer.toHexString((SystemProperties.getUserName() + projectName).hashCode()));
    final File dir = new File(getTempFlexConfigsDirPath());

    if (!dir.isDirectory()) return;

    final File[] filesToDelete = dir.listFiles((file, name) -> {
      return name.startsWith("idea-" + hash1) && name.endsWith(".xml"); // PlatformUtils.getPlatformPrefix().toLowerCase()
    });

    if (filesToDelete != null) {
      for (final File file : filesToDelete) {
        FileUtil.delete(file);
      }
    }
  }
}
