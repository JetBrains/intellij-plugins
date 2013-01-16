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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PairConsumer;
import com.intellij.util.PathUtilRt;
import com.intellij.util.SystemProperties;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.Utils;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.model.runConfiguration.JpsRunConfigurationType;
import org.jetbrains.jps.model.runConfiguration.JpsTypedRunConfiguration;
import org.jetbrains.jps.model.serialization.JpsGlobalLoader;
import org.jetbrains.jps.model.serialization.JpsModelSerializationDataService;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlexCommonUtils {

  public static final String AIR_NAMESPACE_BASE = "http://ns.adobe.com/air/application/";

  //keep in sync with OutputLogger.ERROR_PATTERN from BuiltInFlexCompiler project !!!
  public static final Pattern ERROR_PATTERN =
    Pattern.compile("(.*?)(\\(\\D.*\\))?(?:\\((-?\\d+)\\))?: ?(?:col: (-?\\d+):?)? (Warning|Error): (.*)");

  public static final String LOCALE_TOKEN = "{locale}";
  public static final Pattern XMX_PATTERN = Pattern.compile("(.* )?-Xmx([0-9]+)[mM]( .*)?");

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

  private static final String MODULE_PREFIX = "Module: ";
  private static final String BC_PREFIX = "\tBC: ";
  private static final String RUN_CONFIG_TYPE_PREFIX = "Run config type: ";
  private static final String RUN_CONFIG_NAME_PREFIX = "\tName: ";
  private static final String FORCED_DEBUG_STATUS = "\tForced debug status: ";

  private static final Logger LOG = Logger.getInstance(FlexCommonUtils.class.getName());

  public static boolean isSourceFile(final String fileName) {
    final String ext = FileUtil.getExtension(fileName);
    return ext.equalsIgnoreCase("as") || ext.equalsIgnoreCase("mxml") || ext.equalsIgnoreCase("fxg");
  }

  public static boolean canHaveResourceFiles(final BuildConfigurationNature nature) {
    return nature.isApp();
  }

  public static boolean isFlexUnitBC(final JpsFlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getMainClass().endsWith(FLEX_UNIT_LAUNCHER);
  }

  public static boolean isRuntimeStyleSheetBC(final JpsFlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getMainClass().toLowerCase().endsWith(".css");
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
    return FileUtil.toSystemIndependentName(FileUtil.getTempDirectory()) + "/" +
           "IntelliJ_IDEA"; //ApplicationNamesInfo.getInstance().getFullProductName().replace(' ', '_');
  }

  public static String getPathToFlexUnitTempDirectory(final String projectName) {
    return getTempFlexConfigsDirPath() + "/flexunit-" +
           Integer.toHexString((SystemProperties.getUserName() + projectName).hashCode()).toUpperCase();
  }

  /**
   * @param forcedDebugStatus <code>true</code> or <code>false</code> means that this bc is compiled for further packaging and we need swf to have corresponding debug status;
   *                          <code>null</code> means that bc is compiled as is (i.e. as configured) without any modifications
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
   * @return <code>Trinity.first</code> - module name<br/>
   *         <code>Trinity.second</code> - BC name<br/>
   *         <code>Trinity.third</code> - forced debug status: <code>true</code> or <code>false</code> means that this bc is compiled for further packaging and we need swf to have corresponding debug status;
   *         <code>null</code> means that bc is compiled as is (i.e. as configured) without any modifications
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

  public static List<String> getOptionValues(final String commandLine, final String... optionAndAliases) {
    if (StringUtil.isEmpty(commandLine)) {
      return Collections.emptyList();
    }

    final List<String> result = new LinkedList<String>();

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
    try {
      final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
      try {
        return findXMLElement(inputStream, xmlElement);
      }
      finally {
        inputStream.close();
      }
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
      final Document document = JDOMUtil.loadDocument(is);
      final Element root = document.getRootElement();

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
    catch (JDOMException ignore) {/**/}
    catch (IOException ignore) {/**/}
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
      return playerFolder.list(new FilenameFilter() {
        public boolean accept(final File dir, final String name) {
          return new File(playerFolder, name + "/playerglobal.swc").isFile();
        }
      });
    }

    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  /**
   * @param processor (namespace, relative path with no leading slash)
   */
  public static void processStandardNamespaces(final JpsFlexBuildConfiguration bc, final PairConsumer<String, String> processor) {
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
   * If <code>LinkageType.Default</code> is returned then use {@link #getDefaultFrameworkLinkage(String, BuildConfigurationNature)} to get real value.
   *
   * @return <code>null</code> if entry should not be included at all
   */
  @Nullable
  public static LinkageType getSdkEntryLinkageType(final String swcPath, final JpsFlexBuildConfiguration bc) {
    final JpsSdk<?> sdk = bc.getSdk();
    LOG.assertTrue(sdk != null);
    return getSdkEntryLinkageType(sdk.getHomePath(), swcPath, bc.getNature(), bc.getDependencies().getTargetPlayer(),
                                  bc.getDependencies().getComponentSet());
  }

  /**
   * If <code>LinkageType.Default</code> is returned then use {@link #getDefaultFrameworkLinkage(String, BuildConfigurationNature)} to get real value.
   *
   * @return <code>null</code> if entry should not be included at all
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
    final String swcName = swcPath.substring(lastSlashIndex + 1);
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

    if (swcName.endsWith("textLayout.swc")) {
      return true;
    }

    if (swcName.endsWith("utilities.swc")) {
      return true;
    }

    if (swcName.endsWith("asc-support.swc")) {
      return false; // bundled with AIR SDK 3.4 with ASC 2.0, no idea what for is it
    }

    if (swcName.equals("automation.swc") ||
        swcName.equals("automation_agent.swc") ||
        swcName.equals("automation_dmv.swc") ||
        swcName.equals("automation_flashflexkit.swc") ||
        swcName.equals("qtp.swc")) {
      // additionally installed on top of Flex SDK 3.x
      return true;
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
    if (!swcName.equals("mobilecomponents.swc")) {
      LOG.warn("Unknown SWC in '<Flex SDK>/frameworks/libs/mobile' folder: " + swcName);
    }
    return !bcNature.pureAS && bcNature.isMobilePlatform();
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
    final boolean ok;

    switch (dependencyBCOutputType) {
      case Application:
        ok = false;
        break;
      case RuntimeLoadedModule:
        ok = bcOutputType == OutputType.Application && linkageType == LinkageType.LoadInRuntime;
        break;
      case Library:
        ok = ArrayUtil.contains(linkageType, LinkageType.getSwcLinkageValues());
        break;
      default:
        assert false;
        ok = false;
    }

    return ok;
  }

  public static String getPathToBundledJar(String filename) {
    final URL url = FlexCommonUtils.class.getResource("");
    String folder;
    if ("jar".equals(url.getProtocol())) {
      // running from build
      folder = "/plugins/flex/lib/";
    }
    else {
      // running from sources
      folder = "/flex/lib/";
    }
    return FileUtil.toSystemDependentName(PathManager.getHomePath() + folder + filename);
  }

  public static String getFlexUnitSupportLibName(final BuildConfigurationNature nature, final ComponentSet componentSet) {
    if (nature.pureAS) {
      return "unittestingsupport_as.swc";
    }
    else if (nature.isMobilePlatform() || componentSet == ComponentSet.SparkOnly) {
      return "unittestingsupport_spark.swc";
    }
    else {
      return "unittestingsupport_mx.swc";
    }
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
        final String macroValue = JpsGlobalLoader.getPathVariable(module.getProject().getModel().getGlobal(), macroName);

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

  public static boolean is64BitJava6(final String javaExecutablePath) {
    try {
      final Ref<Boolean> is64Bit = Ref.create(false);
      final Ref<Boolean> isJava6 = Ref.create(false);

      final Process process = Runtime.getRuntime().exec(new String[]{javaExecutablePath, "-version"});
      final BaseOSProcessHandler handler = new BaseOSProcessHandler(process, "doesn't matter", Charset.defaultCharset());

      handler.addProcessListener(new ProcessAdapter() {
        public void onTextAvailable(ProcessEvent event, Key outputType) {
          if (outputType != ProcessOutputTypes.SYSTEM) {
            if (event.getText().contains("64-Bit")) {
              is64Bit.set(true);
            }
            if (event.getText().contains("version \"1.6.")) {
              isJava6.set(true);
            }
          }
        }
      });

      handler.startNotify();
      handler.waitFor(3000);

      if (!handler.isProcessTerminated()) {
        handler.destroyProcess();
      }

      return is64Bit.get() && isJava6.get();
    }
    catch (IOException e) {/*ignore*/}

    return false;
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

    final String d32 = getD32IfNeed(customJavaHomeSet, javaExecutable);

    final List<String> result = new ArrayList<String>();

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

  public static String getD32IfNeed(boolean customJavaHomeSet, String javaExecutable) {
    return (!customJavaHomeSet && SystemInfo.isMac && is64BitJava6(javaExecutable)) ? "-d32" : null;
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
    return FileUtil.getNameWithoutExtension(PathUtilRt.getFileName(bc.getActualOutputFilePath())) + ".html";
  }


  public static String getGeneratedAirDescriptorName(final JpsFlexBuildConfiguration bc, final JpsAirPackagingOptions packagingOptions) {
    final String suffix = packagingOptions instanceof JpsAirDesktopPackagingOptions
                          ? "-descriptor.xml"
                          : packagingOptions instanceof JpsAndroidPackagingOptions ? "-android-descriptor.xml"
                                                                                   : "-ios-descriptor.xml";
    return FileUtil.getNameWithoutExtension(PathUtilRt.getFileName(bc.getActualOutputFilePath())) + suffix;
  }

  public static String replace(final String text, final Map<String, String> replacementMap) {
    final String[] from = new String[replacementMap.size()];
    final String[] to = new String[replacementMap.size()];

    int i = 0;
    for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
      from[i] = entry.getKey();
      to[i] = entry.getValue();
      i++;
    }

    return StringUtil.replace(text, from, to);
  }

  public static String getAirVersion(final String flexVersion) {
    // todo store adt -version

    if (flexVersion.startsWith("4.")) {
      if (flexVersion.startsWith("4.0")) {
        return "1.5.3";
      }
      if (flexVersion.startsWith("4.1")) {
        return "2.0";
      }
      if (flexVersion.startsWith("4.5")) {
        return "2.6";
      }

      return "3.1";
    }

    if (flexVersion.startsWith("3.")) {
      if (flexVersion.startsWith("3.0")) {
        return "1.0";
      }
      if (flexVersion.startsWith("3.1")) {
        return "1.1";
      }
      if (flexVersion.startsWith("3.2")) {
        return "1.5";
      }
      if (flexVersion.startsWith("3.3")) {
        return "1.5";
      }
      if (flexVersion.startsWith("3.4")) {
        return "1.5.2";
      }

      return "1.5.3";
    }

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
  public static String parseAirVersion(final String descriptorFilePath) {
    if (StringUtil.isEmpty(descriptorFilePath)) return null;

    try {
      final Document document = JDOMUtil.loadDocument(new File(descriptorFilePath));
      final Element rootElement = document.getRootElement();
      if (rootElement != null) {
        final String localName = rootElement.getName();
        final Namespace namespace = rootElement.getNamespace();
        if ("application".equals(localName) && namespace != null && namespace.getURI().startsWith(AIR_NAMESPACE_BASE)) {
          return namespace.getURI().substring(AIR_NAMESPACE_BASE.length());
        }
      }
    }
    catch (JDOMException e) {/*unlucky*/}
    catch (IOException e) {/*unlucky*/}

    return null;
  }

  public static String getSwfVersionForTargetPlayer(final String targetPlayer) {
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

  public static String getSwfVersionForSdk(final String sdkVersion) {
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
}
