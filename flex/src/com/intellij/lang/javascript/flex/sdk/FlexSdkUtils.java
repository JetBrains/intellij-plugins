package com.intellij.lang.javascript.flex.sdk;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaCommandLineStateUtil;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.facet.FacetManager;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SystemProperties;
import com.intellij.util.io.ZipUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlexSdkUtils {

  static final String EXTERNAL_LIBRARY_PATH_ELEMENT = "<flex-config><compiler><external-library-path><path-element>";
  static final String LIBRARY_PATH_ELEMENT = "<flex-config><compiler><library-path><path-element>";
  static final String LOCALE_ELEMENT = "<flex-config><compiler><locale><locale-element>";
  public static final String TARGET_PLAYER_ELEMENT = "<flex-config><target-player>";

  static final String TARGET_PLAYER_MAJOR_VERSION_TOKEN = "{targetPlayerMajorVersion}";
  static final String TARGET_PLAYER_MINOR_VERSION_TOKEN = "{targetPlayerMinorVersion}";
  static final String LOCALE_TOKEN = "{locale}";
  static final String ADL_RELATIVE_PATH = File.separatorChar + "bin" + File.separatorChar + "adl" + (SystemInfo.isWindows ? ".exe" : "");
  static final String AIR_RUNTIME_RELATIVE_PATH =
    File.separatorChar + "runtimes" + File.separatorChar + "air" + File.separatorChar +
    (SystemInfo.isWindows ? "win" : (SystemInfo.isLinux ? "linux" : "mac"));

  private static final Pattern XMX_PATTERN = Pattern.compile("(.* )?-Xmx([0-9]+)[mM]( .*)?");

  private FlexSdkUtils() {
  }

  static void setupSdkPaths(final Sdk sdk) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }

    final VirtualFile sdkRoot = sdk.getHomeDirectory();
    if (sdkRoot == null || !sdkRoot.isValid()) {
      return;
    }
    sdkRoot.refresh(false, true);

    final SdkModificator sdkModificator = sdk.getSdkModificator();

    sdkModificator.setVersionString(readFlexSdkVersion(sdkRoot));

    final SdkType sdkType = sdk.getSdkType();
    final String configFileRelativePath = getBaseConfigFileRelPath((IFlexSdkType)sdkType);

    final VirtualFile configFile = sdkRoot.findFileByRelativePath(configFileRelativePath);
    if (configFile == null) {
      return;
    }
    try {
      setupSdkLibraries(sdkModificator, configFile);
    }
    catch (IOException e) {
      // ignore
    }

    final VirtualFile projectsDir = VfsUtil.findRelativeFile("frameworks/projects", sdkRoot);
    if (projectsDir != null && projectsDir.isDirectory()) {
      findSourceRoots(projectsDir, sdkModificator);
    }

    sdkModificator.commitChanges();
  }

  private static void findSourceRoots(final VirtualFile dir, final SdkModificator sdkModificator) {
    for (final VirtualFile child : dir.getChildren()) {
      if (child.isDirectory()) {
        if (child.getName().equals("src")) {
          sdkModificator.addRoot(child, OrderRootType.SOURCES);
        }
        else {
          findSourceRoots(child, sdkModificator);
        }
      }
    }
  }

  /**
   * @param configXmlFile <code>&lt;Flex SDK root&gt;/frameworks/flex-config.xml</code> or <code>&lt;Flex SDK root&gt;/frameworks/air-config.xml</code>
   */
  private static void setupSdkLibraries(final @NotNull SdkModificator sdkModificator, final @NotNull VirtualFile configXmlFile)
    throws IOException {
    final VirtualFile baseDir = configXmlFile.getParent();
    assert baseDir != null;
    final List<String> xmlElements =
      Arrays.asList(EXTERNAL_LIBRARY_PATH_ELEMENT, LIBRARY_PATH_ELEMENT, LOCALE_ELEMENT, TARGET_PLAYER_ELEMENT);
    final Map<String, List<String>> infoFromConfigXml = FlexUtils.findXMLElements(configXmlFile.getInputStream(), xmlElements);

    final List<String> libRelativePaths = getLibsRelativePaths(infoFromConfigXml);

    for (String libRelativePath : libRelativePaths) {
      final VirtualFile libPath = baseDir.findFileByRelativePath(libRelativePath);
      if (libPath != null) {
        if (libPath.isDirectory()) {
          for (final VirtualFile libCandidate : libPath.getChildren()) {
            if (!libCandidate.isDirectory() && "swc".equalsIgnoreCase(libCandidate.getExtension())) {
              addSwcRoot(sdkModificator, libCandidate);
            }
          }
        }
        else {
          addSwcRoot(sdkModificator, libPath);
        }
      }
    }
  }

  /**
   * Looks for {locale}, {targetPlayerMajorVersion} and {targetPlayerMinorVersion} information in <code>infoFromConfigXml</code>,
   * substitutes these tokens in &lt;external-library-path&gt; and &lt;library-path&gt; entries
   *
   * @return list of relative paths to Flex SDK libraries (may contain directories and particular swc files)
   */
  private static List<String> getLibsRelativePaths(final Map<String, List<String>> infoFromConfigXml) {
    final List<String> result = new ArrayList<String>();
    final List<String> locales = infoFromConfigXml.get(LOCALE_ELEMENT);
    final List<String> targetPlayers = infoFromConfigXml.get(TARGET_PLAYER_ELEMENT); // contains one element

    String targetPlayerMajorVersion = TARGET_PLAYER_MAJOR_VERSION_TOKEN;
    String targetPlayerMinorVersion = TARGET_PLAYER_MINOR_VERSION_TOKEN;
    if (!targetPlayers.isEmpty()) {
      final String targetPlayer = targetPlayers.iterator().next();
      final Pair<String, String> majorMinor = TargetPlayerUtils.getPlayerMajorMinorVersion(targetPlayer);
      targetPlayerMajorVersion = majorMinor.first;
      targetPlayerMinorVersion = majorMinor.second;
    }

    for (final Map.Entry<String, List<String>> entry : infoFromConfigXml.entrySet()) {
      if (entry.getKey().equals(EXTERNAL_LIBRARY_PATH_ELEMENT) || entry.getKey().equals(LIBRARY_PATH_ELEMENT)) {
        for (String libPath : entry.getValue()) {
          libPath = libPath.replace(TARGET_PLAYER_MAJOR_VERSION_TOKEN, targetPlayerMajorVersion);
          libPath = libPath.replace(TARGET_PLAYER_MINOR_VERSION_TOKEN, targetPlayerMinorVersion);
          if (libPath.contains(LOCALE_TOKEN)) {
            for (final String locale : locales) {
              result.add(libPath.replace(LOCALE_TOKEN, locale));
            }
          }
          else {
            result.add(libPath);
          }
        }
      }
    }

    return result;
  }

  private static void addSwcRoot(final @NotNull SdkModificator sdkModificator, final @NotNull VirtualFile swcFile) {
    final VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(swcFile);
    if (jarRoot != null) {
      sdkModificator.addRoot(jarRoot, OrderRootType.CLASSES);
    }
  }

  public static boolean isValidSdkRoot(final IFlexSdkType sdkType, final VirtualFile sdkRoot) {
    return sdkRoot != null && VfsUtil.findRelativeFile(getBaseConfigFileRelPath(sdkType), sdkRoot) != null;
  }

  public static boolean isFlexOrAirSdkRoot(final VirtualFile sdkRoot) {
    return isValidSdkRoot(FlexSdkType.getInstance(), sdkRoot) || isValidSdkRoot(AirSdkType.getInstance(), sdkRoot);
  }

  @NotNull
  public static String readFlexSdkVersion(final VirtualFile flexSdkRoot) {
    if (flexSdkRoot == null) {
      return FlexBundle.message("flex.sdk.version.unknown");
    }
    final VirtualFile flexSdkDescriptionFile = flexSdkRoot.findChild("flex-sdk-description.xml");
    if (flexSdkDescriptionFile == null) {
      return FlexBundle.message("flex.sdk.version.unknown");
    }
    try {
      final String versionElement = "<flex-sdk-description><version>";
      final String buildElement = "<flex-sdk-description><build>";
      final Map<String, List<String>> versionInfo =
        FlexUtils.findXMLElements(flexSdkDescriptionFile.getInputStream(), Arrays.asList(versionElement, buildElement));
      return (versionInfo.get(versionElement).isEmpty()
              ? FlexBundle.message("flex.sdk.version.unknown")
              : versionInfo.get(versionElement).get(0)) +
             (versionInfo.get(buildElement).isEmpty() ? "" : (" build " + versionInfo.get(buildElement).get(0)));
    }
    catch (IOException e) {
      return FlexBundle.message("flex.sdk.version.unknown");
    }
  }

  public static List<Sdk> getAllFlexSdks() {
    return ProjectJdkTable.getInstance().getSdksOfType(FlexSdkType.getInstance());
  }

  public static List<Sdk> getAllAirSdks() {
    return ProjectJdkTable.getInstance().getSdksOfType(AirSdkType.getInstance());
  }

  public static List<Sdk> getAllFlexOrAirOrMobileSdks() {
    List<Sdk> result = new ArrayList<Sdk>();
    final Sdk[] sdks = ProjectJdkTable.getInstance().getAllJdks();
    for (Sdk sdk : sdks) {
      final SdkType sdkType = sdk.getSdkType();
      if (sdkType instanceof FlexSdkType || sdkType instanceof AirSdkType) {
        result.add(sdk);
      }
    }
    return result;
  }

  public static List<Sdk> getAllFlexRelatedSdks() {
    List<Sdk> result = new ArrayList<Sdk>();
    final Sdk[] sdks = ProjectJdkTable.getInstance().getAllJdks();
    for (Sdk sdk : sdks) {
      if (sdk.getSdkType() instanceof IFlexSdkType) {
        result.add(sdk);
      }
    }
    return result;
  }

  @Nullable
  public static Sdk createOrGetSdk(final SdkType sdkType, final String sdkHomePath) {
    if (sdkHomePath == null || LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkHomePath) == null) {
      return null;
    }
    final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
    for (final Sdk flexSdk : projectJdkTable.getSdksOfType(sdkType)) {
      if (sdkHomePath.replace('\\', '/').equals(flexSdk.getHomePath().replace('\\', '/'))) {
        if (sdkType instanceof FlexmojosSdkType) {
          final SdkAdditionalData data = flexSdk.getSdkAdditionalData();
          if (data == null || ((FlexmojosSdkAdditionalData)data).getFlexCompilerClasspath().isEmpty()) {
            sdkType.setupSdkPaths(flexSdk);
          }
        }
        return flexSdk;
      }
    }
    return createSdk(sdkType, sdkHomePath);
  }

  private static Sdk createSdk(final SdkType sdkType, final @NotNull String sdkHomePath) {
    if (ApplicationManager.getApplication().isDispatchThread() || ApplicationManager.getApplication().isUnitTestMode()) {
      return doCreateSdk(sdkType, sdkHomePath);
    }
    else {
      final Ref<Sdk> sdkRef = new Ref<Sdk>();
      ApplicationManager.getApplication().invokeAndWait(new Runnable() {
          public void run() {
            sdkRef.set(doCreateSdk(sdkType, sdkHomePath));
          }
        }, ModalityState.defaultModalityState());
      return sdkRef.get();
    }
  }

  private static Sdk doCreateSdk(final SdkType sdkType, final @NotNull String sdkHomePath) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<Sdk>() {
      public Sdk compute() {
        final Sdk sdk = PeerFactory.getInstance().createProjectJdk(sdkType.suggestSdkName(null, sdkHomePath), "", sdkHomePath, sdkType);
        sdkType.setupSdkPaths(sdk);
        final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
        projectJdkTable.addJdk(sdk);
        return sdk;
      }
    });
  }

  public static boolean hasDependencyOnAir(final @NotNull Module module) {
    return hasDependencyOn(module, AirSdkType.getInstance(), "Maven: com.adobe.flex.framework:airframework:swc:");
  }

  public static boolean hasDependencyOnAirMobile(final @NotNull Module module) {
    return hasDependencyOn(module, AirMobileSdkType.getInstance(), "Maven: com.adobe.flex.framework:mobilecomponents:swc:");
  }

  private static boolean hasDependencyOn(final @NotNull Module module,
                                         final @NotNull SdkType sdkType,
                                         final @NotNull String mavenLibraryNameStart) {
    final Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
    if (sdk != null) {
      if (sdk.getSdkType() instanceof FlexmojosSdkType) {
        final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        for (OrderEntry orderEntry : rootManager.getOrderEntries()) {
          if (orderEntry instanceof LibraryOrderEntry) {
            final String libName = ((LibraryOrderEntry)orderEntry).getLibraryName();
            if (libName != null && (libName.startsWith(mavenLibraryNameStart))) {
              return true;
            }
          }
        }
      }
      else {
        return sdk.getSdkType() == sdkType;
      }
    }
    return false;
  }

  public static String getAirVersion(final Sdk sdk) {
    if (sdk != null) {
      final String version = sdk.getVersionString();
      if (version != null) {
        if (version.startsWith("3.")) {
          if (version.startsWith("3.0")) {
            return "1.0";
          }
          if (version.startsWith("3.1")) {
            return "1.1";
          }
          if (version.startsWith("3.2")) {
            return "1.5";
          }
          if (version.startsWith("3.3")) {
            return "1.5";
          }
          if (version.startsWith("3.4")) {
            return "1.5.2";
          }

          return "1.5.3";
        }

        if (version.startsWith("4.")) {
          if (version.startsWith("4.0")) {
            return "1.5.3";
          }
          if (version.startsWith("4.1")) {
            return "2.0";
          }
          if (version.startsWith("4.5")) {
            return getFlexSdkRevision(version) >= 20967 ? "2.6" : "2.5";
          }

          return "2.5";
        }
      }
    }

    return "1.5";
  }

  public static int getFlexSdkRevision(final String sdkVersion) {
    // "4.5.0.17689"
    // "4.5.0 build 17689"
    try {
      final int index = Math.max(sdkVersion.lastIndexOf('.'), sdkVersion.lastIndexOf(' '));
      return Integer.parseInt(sdkVersion.substring(index + 1));
    }
    catch (NumberFormatException ignore) {/*ignore*/}

    return 0;
  }

  public static String getAdlPath(final @NotNull Sdk sdk) {
    if (sdk.getSdkType() instanceof FlexmojosSdkType) {
      final SdkAdditionalData data = sdk.getSdkAdditionalData();
      if (data instanceof FlexmojosSdkAdditionalData) {
        return ((FlexmojosSdkAdditionalData)data).getAdlPath();
      }
    }
    return sdk.getHomePath() + ADL_RELATIVE_PATH;
  }

  /**
   * @return either path to a directory or to the zip file. It can't be used to construct command line. Use {@link #getAirRuntimeDirInfoForFlexmojosSdk(com.intellij.openapi.projectRoots.Sdk) instead}
   */
  public static String getAirRuntimePathForFlexmojosSdk(final @NotNull Sdk sdk) {
    assert sdk.getSdkType() instanceof FlexmojosSdkType;
    if (sdk.getSdkType() instanceof FlexmojosSdkType) {
      final SdkAdditionalData data = sdk.getSdkAdditionalData();
      if (data instanceof FlexmojosSdkAdditionalData) {
        return ((FlexmojosSdkAdditionalData)data).getAirRuntimePath();
      }
    }
    return sdk.getHomePath() + AIR_RUNTIME_RELATIVE_PATH;
  }

  @Nullable
  public static String getAirRuntimePath(final @NotNull Sdk sdk) {
    if (sdk.getSdkType() instanceof FlexmojosSdkType) {
      final SdkAdditionalData data = sdk.getSdkAdditionalData();
      if (data instanceof FlexmojosSdkAdditionalData) {
        return ((FlexmojosSdkAdditionalData)data).getAirRuntimePath();
      }
    }
    else {
      return sdk.getHomePath() + AIR_RUNTIME_RELATIVE_PATH;
    }

    return null;
  }

  /**
   * This method unzips AIR Runtime to temporary directory if AIR Runtime is set as a zip file.<br>
   * <b>Caller is responsible to delete temporary files</b> when AIR application terminates.
   *
   * @return the first object is a directory containing AIR Runtime.<br>
   *         The second object is <code>Boolean.TRUE</code> if this directory is temporary (contains zip file content) and must be deleted when AIR application terminates.
   */
  public static Pair<VirtualFile, Boolean> getAirRuntimeDirInfoForFlexmojosSdk(final @NotNull Sdk sdk) throws IOException {
    assert sdk.getSdkType() instanceof FlexmojosSdkType;
    final String airRuntimePath = getAirRuntimePathForFlexmojosSdk(sdk);
    final VirtualFile airRuntime = LocalFileSystem.getInstance().findFileByPath(airRuntimePath);
    if (airRuntime == null) {
      throw new IOException("Can't find AIR Runtime at " + airRuntimePath);
    }

    if (airRuntime.isDirectory()) {
      return new Pair<VirtualFile, Boolean>(airRuntime, Boolean.FALSE);
    }
    else {
      final String systemTempDirPath = FileUtil.getTempDirectory();
      final File systemTempDir = new File(systemTempDirPath);
      if (!systemTempDir.exists() || !systemTempDir.isDirectory()) {
        throw new IOException("Temp directory doesn't exist: " + systemTempDirPath);
      }

      final String tempDirPath = findUniqueTempDirName(systemTempDir);

      try {
        final VirtualFile tempDir = unzip(airRuntime.getPath(), tempDirPath);
        return new Pair<VirtualFile, Boolean>(tempDir, Boolean.TRUE);
      }
      catch (IOException e) {
        throw new IOException(
          MessageFormat.format("Can''t unzip file ''{0}'' to ''{1}'': {2}", FileUtil.toSystemDependentName(airRuntime.getPath()),
                               tempDirPath, e.getMessage()));
      }
    }
  }

  private static String findUniqueTempDirName(final File systemTempDir) {
    String tempDirName;
    final String unzipDirNameBase = "intellij_air_runtime_";
    final String[] children = systemTempDir.list();
    for (int i = 1; ; i++) {
      tempDirName = unzipDirNameBase + i;
      if (!ArrayUtil.contains(tempDirName, children)) break;
    }
    return systemTempDir.getPath() + File.separatorChar + tempDirName;
  }

  @NotNull
  private static VirtualFile unzip(final String zipFilePath, final String outputDirPath) throws IOException {
    final Ref<IOException> ioExceptionRef = new Ref<IOException>();
    final VirtualFile dir = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
      public VirtualFile compute() {
        try {
          ZipUtil.extract(new File(zipFilePath), new File(outputDirPath), null);
          final VirtualFile tempDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(outputDirPath);
          assert tempDir != null;
          return tempDir;
        }
        catch (IOException e) {
          ioExceptionRef.set(e);
        }
        return null;
      }
    });

    if (!ioExceptionRef.isNull()) {
      throw ioExceptionRef.get();
    }
    else {
      assert dir != null;
      return dir;
    }
  }

  public static boolean isFlex2Sdk(final Sdk flexSdk) {
    final String version = flexSdk == null ? null : flexSdk.getVersionString();
    return version != null && (version.startsWith("2.") || version.startsWith("3.0 Moxie"));
  }

  public static boolean isFlex3_0Sdk(final Sdk flexSdk) {
    final String version = flexSdk == null ? null : flexSdk.getVersionString();
    return version != null && version.startsWith("3.0");
  }

  public static boolean isFlex4Sdk(final Sdk flexSdk) {
    final String version = flexSdk == null ? null : flexSdk.getVersionString();
    return version != null && version.startsWith("4.");
  }

  /**
   * @param project
   * @param flexSdk
   * @param additionalClasspath
   * @param mainClass           used in case of Flexmojos SDK, also used for ordinary Flex/AIR SDK if <code>jarName</code> is <code>null</code>
   * @param jarName             if not <code>null</code> - this parameter used in case of Flex/AIR SDK; always ignored in case of Flexmojos SDK
   * @return
   */
  public static List<String> getCommandLineForSdkTool(final @NotNull Project project,
                                                      final @NotNull Sdk flexSdk,
                                                      final @Nullable String additionalClasspath,
                                                      final @NotNull String mainClass,
                                                      final @Nullable String jarName) {
    final boolean isFlexmojos = flexSdk.getSdkType() instanceof FlexmojosSdkType;
    String javaHome = SystemProperties.getJavaHome();
    boolean customJavaHomeSet = false;
    String additionalJavaArgs = null;
    int heapSizeMbFromJvmConfig = 0;
    String classpath = additionalClasspath;

    if (isFlexmojos) {
      final FlexmojosSdkAdditionalData data = (FlexmojosSdkAdditionalData)flexSdk.getSdkAdditionalData();
      classpath = (StringUtil.isEmpty(classpath) ? "" : (classpath + File.pathSeparator)) +
                  FileUtil.toSystemDependentName(StringUtil.join(data.getFlexCompilerClasspath(), File.pathSeparator));
    }
    else {
      final VirtualFile jvmConfigFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(flexSdk.getHomePath() + "/bin/jvm.config");
      if (jvmConfigFile != null) {
        final Properties properties = new Properties();
        try {
          properties.load(jvmConfigFile.getInputStream());
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
        }
      }
    }

    final String javaExecutable = FileUtil.toSystemDependentName((javaHome + "/bin/java" + (SystemInfo.isWindows ? ".exe" : "")));
    final String applicationHomeParam =
      isFlexmojos ? null : ("-Dapplication.home=" + FileUtil.toSystemDependentName(flexSdk.getHomePath()));

    final String d32 = (!customJavaHomeSet && SystemInfo.isMac && is64BitJava(javaExecutable)) ? "-d32" : null;

    final List<String> result = new ArrayList<String>();

    result.add(javaExecutable);
    if (StringUtil.isNotEmpty(d32)) result.add(d32);
    if (StringUtil.isNotEmpty(applicationHomeParam)) result.add(applicationHomeParam);
    if (StringUtil.isNotEmpty(additionalJavaArgs)) result.addAll(StringUtil.split(additionalJavaArgs, " "));

    final String vmOptions = FlexCompilerProjectConfiguration.getInstance(project).VM_OPTIONS;
    if (StringUtil.isNotEmpty(vmOptions)) result.addAll(StringUtil.split(vmOptions, " "));

    result.add("-Djava.awt.headless=true");
    result.add("-Duser.language=en");
    result.add("-Duser.region=en");

    final int heapSizeMb = FlexCompilerProjectConfiguration.getInstance(project).HEAP_SIZE_MB;
    if (heapSizeMb > heapSizeMbFromJvmConfig) {
      result.add("-Xmx" + heapSizeMb + "m");
    }

    if (StringUtil.isNotEmpty(classpath)) {
      result.add("-classpath");
      result.add(classpath);
    }

    if (isFlexmojos || jarName == null) {
      result.add(mainClass);
    }
    else {
      result.add("-jar");
      result.add(FileUtil.toSystemDependentName(flexSdk.getHomePath() + "/lib/" + jarName));
    }

    return result;
  }

  private static boolean is64BitJava(final String javaExecutable) {
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(javaExecutable);
    commandLine.addParameter("-version");
    try {
      final Ref<Boolean> is64Bit = new Ref<Boolean>(false);
      final OSProcessHandler handler = JavaCommandLineStateUtil.startProcess(commandLine);
      handler.addProcessListener(new ProcessAdapter() {
        public void onTextAvailable(ProcessEvent event, Key outputType) {
          final String text = event.getText();
          if (!text.contains(javaExecutable) &&
              text.contains("64-Bit")) { // first condition is to make sure that "64-Bit" is not in java[.exe] path which is also printed
            is64Bit.set(true);
          }
        }
      });
      handler.startNotify();
      handler.waitFor(3000);
      if (!handler.isProcessTerminated()) {
        handler.destroyProcess();
      }
      return is64Bit.get();
    }
    catch (ExecutionException e) {/*ignore*/}

    return false;
  }

  public static String getBaseConfigFileRelPath(final IFlexSdkType sdkType) {
    return "frameworks/" + getBaseConfigFileName(sdkType);
  }

  public static String getBaseConfigFileName(final IFlexSdkType sdkType) {
    switch (sdkType.getSubtype()) {
      case Flex:
        return "flex-config.xml";
      case AIR:
        return "air-config.xml";
      case AIRMobile:
        return "airmobile-config.xml";
      case Flexmojos:
      default:
        return null;
    }
  }

  public static void openModuleOrFacetConfigurable(final Module module) {
    final ProjectStructureConfigurable projectStructureConfigurable = ProjectStructureConfigurable.getInstance(module.getProject());
    ShowSettingsUtil.getInstance().editConfigurable(module.getProject(), projectStructureConfigurable, new Runnable() {
      public void run() {
        if (module.getModuleType() instanceof FlexModuleType) {
          projectStructureConfigurable.select(module.getName(), ProjectBundle.message("modules.classpath.title"), true);
        }
        else {
          final FlexFacet facet = FacetManager.getInstance(module).getFacetByType(FlexFacet.ID);
          if (facet != null) {
            projectStructureConfigurable.select(facet, true);
          }
        }
      }
    });
  }
}
